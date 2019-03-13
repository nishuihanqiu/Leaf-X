package com.lls.leaf.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/************************************
 * NotFoundKeyException
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
@ResponseStatus(code= HttpStatus.INTERNAL_SERVER_ERROR,reason="Key is none")
public class NotFoundKeyException extends RuntimeException {

    public NotFoundKeyException(String message) {
        super(message);
    }

    public NotFoundKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundKeyException(Throwable cause) {
        super(cause);
    }

    public NotFoundKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
