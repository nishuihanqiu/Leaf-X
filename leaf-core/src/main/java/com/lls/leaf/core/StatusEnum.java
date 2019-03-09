package com.lls.leaf.core;

/************************************
 * StatusEnum
 * @author liliangshan
 * @date 2019-03-08
 ************************************/
public enum StatusEnum {

    SUCCESS(200, "success"),
    FAILED(400, "failed");

    private int code;
    private String message;

    StatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
