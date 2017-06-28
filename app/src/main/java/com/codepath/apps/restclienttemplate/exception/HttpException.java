package com.codepath.apps.restclienttemplate.exception;

/**
 * @author Joseph Gardi
 */
public class HttpException extends RuntimeException {
    private Object extraInfo;

    public HttpException(Object extraInfo, String msg, Throwable cause) {
        super(msg, cause);
        this.extraInfo = extraInfo;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }
}
