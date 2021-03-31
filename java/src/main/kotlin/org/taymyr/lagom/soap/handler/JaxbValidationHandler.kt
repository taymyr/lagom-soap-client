package org.taymyr.lagom.soap.handler

import javax.xml.ws.handler.soap.SOAPHandler
import javax.xml.ws.handler.soap.SOAPMessageContext

/**
 * [SOAPHandler] for settings JAXB validation.
 */
abstract class JaxbValidationHandler internal constructor(private val validation: Boolean) : AbstractSoapMessageHandler() {

    override fun handleMessage(context: SOAPMessageContext): Boolean {
        context["set-jaxb-validation-event-handler"] = validation
        return true
    }
}

/**
 * [SOAPHandler] for *disable* JAXB validation.
 */
class DisableJaxbValidationHandler : JaxbValidationHandler(false)

/**
 * [SOAPHandler] for *enable* JAXB validation.
 */
class EnableJaxbValidationHandler : JaxbValidationHandler(true)
