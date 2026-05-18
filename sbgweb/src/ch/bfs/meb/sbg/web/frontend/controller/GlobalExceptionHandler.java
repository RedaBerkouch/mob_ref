package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.sbg.web.frontend.dto.TunnelApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Error handling with 200 ok is bad practice. But here it is a workaround.
     * On BIT environment, eIAM intercepts 500 internal server error and returns its own html.
     * That is why server application returns errors with 200 ok to avoid the interception by eIAM.
     * see tunnelRestApi.png
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<TunnelApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        TunnelApiResponse<Void> response = TunnelApiResponse.error(
                ex.getStatus().value(),
                ex.getReason()
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TunnelApiResponse<Void>> handleGenericException(Exception ex) {
        TunnelApiResponse<Void> response = TunnelApiResponse.error(500, ex.getMessage());
        return ResponseEntity.ok(response);
    }
}
