package ru.atconsulting.tele2.lkb2c.handler

import org.taymyr.lagom.soap.InvokeHandler
import javax.xml.ws.BindingProvider

/**
 * [InvokeHandler] for setting `javax.xml.ws.security.auth.username` and `javax.xml.ws.security.auth.password`.
 */
class BasicAuthHandler<T>(private val username: String, private val password: String) : InvokeHandler<T> {

    override fun afterInit(service: T) {
        // Do Nothing
    }

    override fun beforeInvoke(service: T) {
        (service as BindingProvider).requestContext[BindingProvider.USERNAME_PROPERTY] = username
        (service as BindingProvider).requestContext[BindingProvider.PASSWORD_PROPERTY] = password
    }

    override fun afterInvoke(service: T) {
        // Do Nothing
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
         * serviceProvider.get(listof(basicAuth(username, password))).method(params)
         * ```
         *
         * @return Instance of [BasicAuthHandler]
         */
        @JvmStatic
        fun <T> basicAuth(username: String, password: String) = BasicAuthHandler<T>(username, password)
    }
}
