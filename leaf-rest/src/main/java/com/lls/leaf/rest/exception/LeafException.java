package com.lls.leaf.rest.exception;

/************************************
 * LeafException
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
public class LeafException extends Exception {

    public LeafException(String message) {
        super(message);
    }

    public LeafException(String message, Throwable cause) {
        super(message, cause);
    }

    public LeafException(Throwable cause) {
        super(cause);
    }

    public LeafException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
