package org.taymyr.lagom.soap

import java.util.concurrent.CompletionException
import java.util.function.Function

/**
 * Wrapper for all checked SOAP exceptions.
 * Need for using on Circuit Breaker exceptions whitelist.
 *
 * Example configuration:
 * ```
 * lagom.circuit-breaker {
 *
 *   default.exception-whitelist = [
 *     org.taymyr.lagom.soap.WebFaultException
 *   ]
 *
 * }
 * ```
 */
class WebFaultException(cause: Throwable) : RuntimeException(cause) {

    /**
     * Utilities for work with [WebFaultException].
     */
    companion object {
        /**
         *
         * Processing [WebFaultException].
         *
         * Code example:
         * ```
         * service.foo()
         *   .thenApplyAsync(...)
         *   .exceptionally(throwable -> processWebFault(throwable, e -> {
         *     if (e instanceof CheckedException1) {
         *       ...
         *     } else if (e instanceof CheckedException2) {
         *       ...
         *     } else {
         *       ...
         *     }
         *   }));
         * ```
         *
         * @param e Exception, thrown from calling of SOAP service port
         * @param fn Function of processing of checked SOAP exception
         * @param T Return type of method of SOAP service port
         * @return Result of handling of checked SOAP exception
         */
        @JvmStatic
        fun <T> processWebFault(e: Throwable, fn: Function<Throwable?, out T>): T {
            var throwable: Throwable? = e
            if (e is CompletionException) {
                throwable = throwable?.cause
            }
            if (throwable is WebFaultException) {
                throwable = throwable.cause
            }
            return fn.apply(throwable)
        }
    }
}
