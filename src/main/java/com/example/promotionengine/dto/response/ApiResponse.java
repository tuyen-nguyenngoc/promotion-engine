package com.example.promotionengine.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private T data;
    private ErrorDetail error;

    @Data
    @NoArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setData(data);
        r.setError(null);
        return r;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setData(null);
        r.setError(new ErrorDetail(code, message));
        return r;
    }
}
