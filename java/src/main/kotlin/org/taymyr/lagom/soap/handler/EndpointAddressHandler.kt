package org.taymyr.lagom.soap.handler

import org.taymyr.lagom.soap.InvokeHandler
import javax.xml.ws.BindingProvider

/**
 * [InvokeHandler] for setting `javax.xml.ws.service.endpoint.address`.
 */
class EndpointAddressHandler<T>(private val address: String) : InvokeHandler<T> {

    override fun afterInit(service: T) {
        // Do Nothing
    }

    override fun beforeInvoke(service: T) {
        (service as BindingProvider).requestContext[BindingProvider.ENDPOINT_ADDRESS_PROPERTY] = address
    }

    override fun afterInvoke(service: T) {
        // Do Nothing
    }

    /**
     * Utilities for work with [EndpointAddressHandler].
     */
    companion object {
        /**
         * Instantiating [EndpointAddressHandler].
         *
         * Code example:
         *
         * ```
         * serviceProvider.get(endpointAddress(address)).method(params)
         * ```
         *
         * @return Instance of [EndpointAddressHandler]
         */
        @JvmStatic
        fun <T> endpointAddress(address: String) = EndpointAddressHandler<T>(address)
    }
}
