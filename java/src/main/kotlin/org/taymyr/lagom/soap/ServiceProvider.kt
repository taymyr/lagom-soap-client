package org.taymyr.lagom.soap

import javax.inject.Provider
import javax.xml.ws.handler.Handler
import javax.xml.ws.handler.soap.SOAPMessageContext

/**
 * Guice provider for SOAP service with the port of type `T`.
 *
 * @param T Type of SOAP service port
 */
interface ServiceProvider<T> : Provider<T> {

    /**
     * Provides instances of `T` with SOAP message handlers.
     * @param handlers SOAP message handlers
     * @return SOAP service port
     */
    fun get(vararg handlers: Handler<SOAPMessageContext>): T

    /**
     * Provides instances of `T` with SOAP message handlers and invocation handlers.
     * @param invokeHandlers Invocation handlers
     * @param soapHandlers SOAP message handlers
     * @return SOAP service port
     */
    fun get(invokeHandlers: List<InvokeHandler<T>>, vararg soapHandlers: Handler<SOAPMessageContext>): T
}