package org.taymyr.lagom.soap;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.jaxws.handler.soap.SOAPMessageContextImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taymyr.lagom.soap.handler.AbstractSoapMessageHandler;
import org.taymyr.lagom.soap.handler.DisableJaxbValidationHandler;
import org.taymyr.lagom.soap.handler.EnableJaxbValidationHandler;

import java.util.List;
import java.util.Map;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static com.google.common.net.HttpHeaders.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.taymyr.lagom.soap.handler.SetUserAgentHandler.userAgent;

import static java.util.Collections.singletonList;
import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

class HandlersTest {

    private SOAPMessageContext createInboundMessage() {
        SOAPMessageContext context = new SOAPMessageContextImpl(new SoapMessage(Soap12.getInstance()));
        context.put(MESSAGE_OUTBOUND_PROPERTY, false);
        return context;
    }

    private SOAPMessageContext createOutboundMessage() {
        SOAPMessageContext context = new SOAPMessageContextImpl(new SoapMessage(Soap12.getInstance()));
        context.put(MESSAGE_OUTBOUND_PROPERTY, true);
        return context;
    }

    @Test
    @DisplayName("AbstractSoapMessageHandler should be correct")
    void anyHandlerShouldReturnDefaultValue() {
        class AnyHandler extends AbstractSoapMessageHandler {}
        AnyHandler handler = new AnyHandler();
        handler.close(createOutboundMessage());
        assertThat(handler.getHeaders()).isNull();
        assertThat(handler.handleFault(createInboundMessage())).isTrue();
        assertThat(handler.handleMessage(createInboundMessage())).isTrue();
        assertThat(handler.handleMessage(createOutboundMessage())).isTrue();
    }

    @Test
    @DisplayName("SetUserAgentHandler should successfully set `User-Agent` header")
    @SuppressWarnings("unchecked")
    void userAgentHandlerShouldSetHeaderForOutboundMessage() {
        String userAgentValue = "Custom";
        SOAPMessageContext context = createOutboundMessage();
        userAgent(userAgentValue).handleMessage(context);
        Map<String, List<String>> headers = (Map<String, List<String>>) context.get(HTTP_REQUEST_HEADERS);
        assertThat(headers).containsEntry(USER_AGENT, singletonList(userAgentValue));

        userAgentValue = "Custom2";
        userAgent(userAgentValue).handleMessage(context);
        headers = (Map<String, List<String>>) context.get(HTTP_REQUEST_HEADERS);
        assertThat(headers).containsEntry(USER_AGENT, singletonList(userAgentValue));
    }

    @Test
    @DisplayName("EnableJaxbValidationHandler should successfully enable JAXB message validation")
    void enableJaxbValidationHandlerShouldBeCorrect() {
        EnableJaxbValidationHandler handler = new EnableJaxbValidationHandler();

        SOAPMessageContext outboundMessage = createOutboundMessage();
        handler.handleMessage(outboundMessage);
        assertThat(outboundMessage).containsEntry("set-jaxb-validation-event-handler", true);

        SOAPMessageContext inboundMessage = createInboundMessage();
        handler.handleMessage(inboundMessage);
        assertThat(inboundMessage).containsEntry("set-jaxb-validation-event-handler", true);
    }

    @Test
    @DisplayName("DisableJaxbValidationHandler should successfully disable JAXB message validation")
    void disableJaxbValidationHandlerShouldBeCorrect() {
        DisableJaxbValidationHandler handler = new DisableJaxbValidationHandler();

        SOAPMessageContext outboundMessage = createOutboundMessage();
        handler.handleMessage(outboundMessage);
        assertThat(outboundMessage).containsEntry("set-jaxb-validation-event-handler", false);

        SOAPMessageContext inboundMessage = createInboundMessage();
        handler.handleMessage(inboundMessage);
        assertThat(inboundMessage).containsEntry("set-jaxb-validation-event-handler", false);
    }

}
