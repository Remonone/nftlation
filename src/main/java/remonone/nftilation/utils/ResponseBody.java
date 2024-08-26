package remonone.nftilation.utils;

import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class ResponseBody<T> {
    private final T data;
    private int responseCode;

    @Nullable
    private String errorMessage;
    private ResponseBody(T data, int responseCode, @Nullable String errorMessage) {
        this.data = data;
    }

    public static <T> ResponseBody<T> createOKResponse(T data) {
        return new ResponseBody<>(data, 200, null);
    }

    public static <T> ResponseBody<T> createBadRequestResponse(String message) {
        return new ResponseBody<>(null, 400, message);
    }

}
