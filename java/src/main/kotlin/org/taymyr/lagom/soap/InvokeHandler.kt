package org.taymyr.lagom.soap

/**
 * Interceptor of invocation methods of SOAP service.
 * Service can be cast to [play.soap.PlayJaxWsClientProxy]:
 * ```
 * ((PlayJaxWsClientProxy)Proxy.getInvocationHandler(service))
 * ```
 *
 * @param T Type of SOAP service
 */
interface InvokeHandler<T> {

    /**
     * After init of the port object of SOAP service.
     * @param service SOAP service
     */
    fun afterInit(service: T)

    /**
     * Before invoking a method of SOAP service.
     * @param service SOAP service
     */
    fun beforeInvoke(service: T)

    /**
     * After invoking a method of SOAP service.
     * @param service SOAP service
     */
    fun afterInvoke(service: T)
}
