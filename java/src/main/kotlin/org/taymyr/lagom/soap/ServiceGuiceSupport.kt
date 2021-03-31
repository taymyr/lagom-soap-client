package org.taymyr.lagom.soap

import com.google.inject.TypeLiteral
import com.google.inject.spi.Message
import com.lightbend.lagom.internal.javadsl.BinderAccessor.binder
import com.lightbend.lagom.javadsl.client.CircuitBreakersPanel
import com.typesafe.config.Config
import org.apache.commons.lang3.reflect.TypeUtils
import play.soap.PlaySoapClient
import java.lang.String.format
import javax.inject.Singleton
import javax.xml.ws.handler.Handler
import javax.xml.ws.handler.soap.SOAPMessageContext

/**
 * Helper injection SOAP service client.
 * Use [com.lightbend.lagom.javadsl.server.ServiceGuiceSupport] instead.
 */
interface ServiceGuiceSupport : com.lightbend.lagom.javadsl.server.ServiceGuiceSupport {

    /**
     * Binding SOAP service of type `S`, his port of type `P` and SOAP message handlers.
     *
     * @param serviceClass Class of type SOAP service
     * @param portClass Class of type SOAP service port
     * @param invokeHandlers Invocation method handlers
     * @param handlers SOAP message handlers
     * @param S Type of SOAP service
     * @param P Type of SOAP service port
     */
    @JvmDefault
    fun <S : PlaySoapClient, P> bindSoapClient(serviceClass: Class<S>, portClass: Class<P>, invokeHandlers: List<InvokeHandler<P>>, vararg handlers: Handler<SOAPMessageContext>) {
        val binder = binder(this)
        val serviceProvider = binder.getProvider(serviceClass)
        val breakersProvider = binder.getProvider(CircuitBreakersPanel::class.java)
        val configProvider = binder.getProvider(Config::class.java)
        try {
            val provider = ServiceProviderImpl(
                serviceClass, portClass,
                serviceProvider, breakersProvider,
                configProvider, invokeHandlers, *handlers
            )
            binder.bind(portClass).toProvider(provider).`in`(Singleton::class.java)
            val providerType = TypeUtils.parameterize(ServiceProvider::class.java, portClass)
            @Suppress("unchecked_cast")
            binder.bind(TypeLiteral.get(providerType) as TypeLiteral<ServiceProvider<P>>).toInstance(provider)
        } catch (e: NoSuchMethodException) {
            val message = "Incorrect classes params for binding soap client.\nMethod with return type %s not found in %s."
            binder.addError(Message(this, format(message, portClass.name, serviceClass.name)))
        }
    }

    /**
     * Binding SOAP service of type `S`, his port of type `P` and SOAP message handlers.
     *
     * @param serviceClass Class of type SOAP service
     * @param portClass Class of type SOAP service port
     * @param handlers SOAP message handlers
     * @param S Type of SOAP service
     * @param P Type of SOAP service port
     */
    @JvmDefault
    fun <S : PlaySoapClient, P> bindSoapClient(serviceClass: Class<S>, portClass: Class<P>, vararg handlers: Handler<SOAPMessageContext>) {
        bindSoapClient(serviceClass, portClass, emptyList<InvokeHandler<P>>(), *handlers)
    }
}
