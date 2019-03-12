package com.lls.leaf.exception;

/************************************
 * ClockGoBackException
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class ClockGoBackException extends RuntimeException {

    public ClockGoBackException(String message) {
        super(message);
    }

    public ClockGoBackException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClockGoBackException(Throwable cause) {
        super(cause);
    }

    public ClockGoBackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
