package org.taymyr.lagom.soap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.taymyr.lagom.soap.WebFaultException.processWebFault;

class WebFaultExceptionTest {

    private class BusinessException extends Exception {
    }

    private Function<Throwable, String> processException = e -> (e instanceof BusinessException) ? "business" : "unknown";

    @Test
    @DisplayName("Processing WebFaultException should be to process cause exception")
    void processWebFaultExceptionShouldProcessCause() {
        String result = processWebFault(new WebFaultException(new BusinessException()), processException);
        assertThat(result).isEqualTo("business");
    }

    @Test
    @DisplayName("Processing CompletionException should be to process cause exception")
    void processCompletionExceptionShouldProcessCause() {
        String result = processWebFault(new CompletionException(new BusinessException()), processException);
        assertThat(result).isEqualTo("business");
    }

    @Test
    @DisplayName("Processing any exception should be to process this exception")
    void processAnyExceptionShouldProcessThisException() {
        String result = processWebFault(new RuntimeException(""), processException);
        assertThat(result).isEqualTo("unknown");
    }

    @Test
    @DisplayName("Processing null should be throw IllegalArgumentException")
    void processNullShouldThrowIllegalArgument() {
        assertThatThrownBy(() -> processWebFault(null, processException)).isInstanceOf(IllegalArgumentException.class);
    }

}
