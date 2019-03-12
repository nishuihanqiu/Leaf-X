package com.lls.leaf.exception;

/************************************
 * CheckOtherNodeException
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class CheckOtherNodeException extends RuntimeException {

    public CheckOtherNodeException(String message) {
        super(message);
    }

    public CheckOtherNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckOtherNodeException(Throwable cause) {
        super(cause);
    }

    public CheckOtherNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
