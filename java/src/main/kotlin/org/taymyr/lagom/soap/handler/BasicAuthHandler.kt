package org.taymyr.lagom.soap.handler

import com.google.common.net.HttpHeaders.AUTHORIZATION
import org.pac4j.core.context.HttpConstants.BASIC_HEADER_PREFIX
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS
import javax.xml.ws.handler.soap.SOAPHandler
import javax.xml.ws.handler.soap.SOAPMessageContext

/**
 * [SOAPHandler] for setting HTTP authorization header for SOAP request.
 */
class BasicAuthHandler(private val username: String, private val password: String) : AbstractSoapMessageHandler() {

    override fun handleOutboundMessage(context: SOAPMessageContext): Boolean {
        val headers = context.getOrPut(HTTP_REQUEST_HEADERS) { mutableMapOf<String, List<String>>() }
        @Suppress("unchecked_cast")
        (headers as MutableMap<String, List<String>>)[AUTHORIZATION] = listOf(
            BASIC_HEADER_PREFIX + Base64.getEncoder().encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
        )
        return true
    }

    /**
     * Utilities for work with [BasicAuthHandler].
     */
    companion object {
        /**
         * Instantiating [BasicAuthHandler].
         *
         * Code example:
         *
         * ```
         * serviceProvider.get(basicAuth("Username-Value", "Password-Value")).method(params)
         * ```
         *
         * @param username username value of header Authorization. Should not be `null`
         * @param password password value of header Authorization. Should not be `null`
         * @return Instance of [BasicAuthHandler]
         */
        @JvmStatic
        fun basicAuth(username: String, password: String): BasicAuthHandler = BasicAuthHandler(username, password)
    }
}
