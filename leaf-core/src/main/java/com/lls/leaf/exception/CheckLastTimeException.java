package com.lls.leaf.exception;

/************************************
 * CheckLastTimeException
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class CheckLastTimeException extends RuntimeException {

    public CheckLastTimeException(String message) {
        super(message);
    }

    public CheckLastTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckLastTimeException(Throwable cause) {
        super(cause);
    }

    public CheckLastTimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
