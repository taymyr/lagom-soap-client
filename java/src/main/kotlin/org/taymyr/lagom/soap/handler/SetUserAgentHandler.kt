package org.taymyr.lagom.soap.handler

import com.google.common.net.HttpHeaders.USER_AGENT
import javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS
import javax.xml.ws.handler.soap.SOAPHandler
import javax.xml.ws.handler.soap.SOAPMessageContext

/**
 * [SOAPHandler] for setting HTTP header `User-Agent` for SOAP request.
 */
class SetUserAgentHandler(private val userAgent: String) : AbstractSoapMessageHandler() {

    override fun handleOutboundMessage(context: SOAPMessageContext): Boolean {
        val headers = context.getOrPut(HTTP_REQUEST_HEADERS) { mutableMapOf<String, List<String>>() }
        @Suppress("unchecked_cast")
        (headers as MutableMap<String, List<String>>)[USER_AGENT] = listOf(userAgent)
        return true
    }

    /**
     * Utilities for work with [SetUserAgentHandler].
     */
    companion object {
        /**
         * Instantiating [SetUserAgentHandler].
         *
         * Code example:
         *
         * ```
         * serviceProvider.get(userAgent("User-Agent-Value")).method(params)
         * ```
         *
         * @param userAgent Value of header `User-Agent`. Should not be `null`
         * @return Instance of [SetUserAgentHandler]
         */
        @JvmStatic
        fun userAgent(userAgent: String): SetUserAgentHandler = SetUserAgentHandler(userAgent)
    }
}
