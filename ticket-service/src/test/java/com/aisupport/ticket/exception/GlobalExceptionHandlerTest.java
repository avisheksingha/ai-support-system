package com.aisupport.ticket.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.aisupport.common.exception.ErrorResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadRequest_shouldReturn400ForInvalidTicketInput() {
        WebRequest request = new ServletWebRequest(
                new MockHttpServletRequest("GET", "/api/v1/tickets")
        );

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
                new InvalidTicketInputException("Invalid status 'X'"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Invalid status");
    }
}
