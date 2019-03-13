package com.lls.leaf.rest.exception;

/************************************
 * LeafRestException
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
public class LeafRestException extends RuntimeException {

    public LeafRestException(String message) {
        super(message);
    }

    public LeafRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public LeafRestException(Throwable cause) {
        super(cause);
    }

    public LeafRestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
