package org.taymyr.lagom.soap

import com.google.inject.Provider
import com.lightbend.lagom.javadsl.client.CircuitBreakersPanel
import com.typesafe.config.Config
import io.github.config4k.extract
import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import mu.KotlinLogging
import org.apache.cxf.transport.http.HTTPConduit
import play.soap.PlayJaxWsClientProxy
import play.soap.PlaySoapClient
import java.lang.String.format
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.xml.ws.WebFault
import javax.xml.ws.handler.Handler
import javax.xml.ws.handler.soap.SOAPMessageContext

private val log = KotlinLogging.logger {}

/**
 * Implementation of provider SOAP services and his ports.
 * @param S Type of SOAP service
 * @param P Type of SOAP service port
 */
internal class ServiceProviderImpl<S : PlaySoapClient, P>
@Throws(NoSuchMethodException::class)
constructor(
    serviceClass: Class<S>,
    private val portClass: Class<P>,
    private val serviceProvider: Provider<S>,
    private val breakersProvider: Provider<CircuitBreakersPanel>,
    private val configProvider: Provider<Config>,
    private vararg val handlers: Handler<SOAPMessageContext>
) : ServiceProvider<P> {

    private val name: String = serviceClass.name
    private val getPortMethod: Method
    private val factory: ProxyFactory

    init {
        this.getPortMethod = findGetPortMethod(serviceClass, portClass)
        this.factory = ProxyFactory()
        this.factory.interfaces = arrayOf(portClass)
    }

    @Throws(NoSuchMethodException::class)
    private fun findGetPortMethod(serviceClass: Class<S>, portClass: Class<P>): Method =
        serviceClass.methods.find { method ->
            method.returnType == portClass &&
            method.parameterCount == 1 &&
            method.parameters[0].type.isArray &&
            method.parameters[0].type.componentType == Handler::class.java
        } ?: throw NoSuchMethodException()

    override fun get(): P = get(emptyList())

    override fun get(vararg handlers: Handler<SOAPMessageContext>): P = get(emptyList(), *handlers)

    override fun get(invokeHandlers: List<InvokeHandler<P>>, vararg soapHandlers: Handler<SOAPMessageContext>): P {
        try {
            @Suppress("unchecked_cast")
            return factory.create(emptyArray(), emptyArray(), PortProxyInvokeHandler(invokeHandlers, *soapHandlers)) as P
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    /**
     * Method handler for proxy object of SOAP service port.
     */
    private inner class PortProxyInvokeHandler
    internal constructor(private val invokeHandlers: List<InvokeHandler<P>>, vararg soapHandlers: Handler<SOAPMessageContext>) :
        MethodHandler {

        private val config: Config
        private val breakerConfig: Config
        private val service: S = serviceProvider.get()
        private val soapHandlers: Array<Handler<SOAPMessageContext>>

        init {
            val globalConfig = configProvider.get()
            val cbConfig = globalConfig.getConfig("lagom.circuit-breaker")
            val defaultBreakerConfig = cbConfig.getConfig("default")
            breakerConfig = cbConfig.getConfig(name).withFallback(defaultBreakerConfig)
            val pssConfig = globalConfig.getConfig("play.soap.services")
            val configPortPath = "$name.ports.${portClass.name}"
            this.config = if (pssConfig.hasPath(configPortPath)) pssConfig.getConfig(configPortPath) else pssConfig.getConfig(name)
            this.soapHandlers = arrayOf(*soapHandlers).plus(handlers)
        }

        override fun invoke(self: Any?, thisMethod: Method?, proceed: Method?, args: Array<out Any>?): Any =
            breakersProvider.get().withCircuitBreaker(name) {
                try {
                    invokeService(thisMethod!!, args ?: emptyArray())
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException(e)
                }
            }

        private fun before(port: P, method: Method) =
            invokeHandlers.forEach {
                try {
                    it.beforeInvoke(port)
                } catch (e: Exception) {
                    log.error(e) { "Handle before invoke '$method' failed" }
                }
            }

        private fun after(port: P, method: Method) =
            invokeHandlers.forEach {
                try {
                    it.afterInvoke(port)
                } catch (e: Exception) {
                    log.error(e) { "Handle after invoke '$method' failed" }
                }
            }

        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        private fun invokeService(thisMethod: Method, args: Array<out Any>): CompletionStage<Any> {
            // http://cxf.apache.org/faq.html#FAQ-AreJAX-WSclientproxiesthreadsafe%3F
            @Suppress("unchecked_cast")
            val port = getPortMethod.invoke(service, soapHandlers as Any) as P
            val proxy = Proxy.getInvocationHandler(port) as PlayJaxWsClientProxy
            val httpClientPolicy = (proxy.client.conduit as HTTPConduit).client
            val timeout = breakerConfig.getDuration("call-timeout", MILLISECONDS)
            httpClientPolicy.receiveTimeout = timeout
            httpClientPolicy.connectionTimeout = timeout
            httpClientPolicy.connectionRequestTimeout = timeout
            httpClientPolicy.browserType = config.extract("browser-type") ?: "lagom"
            before(port, thisMethod)
            return (thisMethod.invoke(port, *args) as CompletionStage<*>).handle { result, throwable ->
                after(port, thisMethod)
                throwable?.let {
                    if (throwable.javaClass.isAnnotationPresent(WebFault::class.java)) throw WebFaultException(throwable)
                    throw RuntimeException(format("Failed invoke '%s'", thisMethod), throwable)
                }
                result
            }
        }
    }
}