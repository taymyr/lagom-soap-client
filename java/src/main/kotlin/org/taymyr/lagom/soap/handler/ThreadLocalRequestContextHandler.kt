package org.taymyr.lagom.soap.handler

import org.taymyr.lagom.soap.InvokeHandler
import javax.xml.ws.BindingProvider

/**
 * [InvokeHandler] for setting `thread.local.request.context`.
 */
class ThreadLocalRequestContextHandler<T> : InvokeHandler<T> {

    override fun beforeInvoke(service: T) {
        // Do Nothing
    }

    override fun afterInvoke(service: T) {
        // Do Nothing
    }

    /**
     * Utilities for work with [ThreadLocalRequestContextHandler].
     */
    companion object {
        /**
         * Instantiating [ThreadLocalRequestContextHandler].
         *
         * Code example:
         *
         * ```
         * serviceProvider.get(threadLocalRequestContext()).method(params)
         * ```
         *
         * @return Instance of [ThreadLocalRequestContextHandler]
         */
        @JvmStatic
        fun <T> threadLocalRequestContext(): ThreadLocalRequestContextHandler<T> = ThreadLocalRequestContextHandler<T>()
    }

    override fun afterInit(service: T) {
        (service as BindingProvider).requestContext["thread.local.request.context"] = "true"
    }
}