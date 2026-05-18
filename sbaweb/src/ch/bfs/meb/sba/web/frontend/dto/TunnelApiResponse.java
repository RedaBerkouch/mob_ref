package ch.bfs.meb.sba.web.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TunnelApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private boolean success;

    public static <T> TunnelApiResponse<T> ok(T data) {
        return new TunnelApiResponse<>(200, "OK", data, true);
    }

    public static <T> TunnelApiResponse<T> error(int status, String message) {
        return new TunnelApiResponse<>(status, message, null, false);
    }
}