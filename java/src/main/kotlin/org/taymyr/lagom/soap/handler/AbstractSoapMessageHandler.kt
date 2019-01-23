package org.taymyr.lagom.soap.handler

import javax.xml.namespace.QName
import javax.xml.ws.handler.MessageContext
import javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY
import javax.xml.ws.handler.soap.SOAPHandler
import javax.xml.ws.handler.soap.SOAPMessageContext

/**
 * Abstract SOAP message handler with default implementation for all method [SOAPHandler].
 */
abstract class AbstractSoapMessageHandler : SOAPHandler<SOAPMessageContext> {

    override fun getHeaders(): Set<QName>? = null

    override fun handleMessage(context: SOAPMessageContext): Boolean =
        if (context[MESSAGE_OUTBOUND_PROPERTY] as Boolean) handleOutboundMessage(context) else handleInboundMessage(context)

    /**
     * Handle inbound SOAP message.
     * @param context Context of SOAP message
     * @return An indication of whether handler processing should continue for the current message
     * * Return `true` to continue processing
     * * Return `false` to block processing
     */
    protected open fun handleInboundMessage(context: SOAPMessageContext): Boolean = true

    /**
     * Handle outbound SOAP message.
     * @param context Context of SOAP message.
     * @return An indication of whether handler processing should continue for the current message
     * * Return `true` to continue processing.
     * * Return `false` to block processing.
     */
    protected open fun handleOutboundMessage(context: SOAPMessageContext): Boolean = true

    override fun handleFault(context: SOAPMessageContext): Boolean = true

    override fun close(context: MessageContext) {}
}