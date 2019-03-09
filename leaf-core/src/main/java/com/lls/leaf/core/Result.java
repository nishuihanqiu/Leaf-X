package com.lls.leaf.core;

/************************************
 * Result
 * @author liliangshan
 * @date 2019-03-08
 ************************************/
public class Result {

    private long id;
    private int status;
    private String message;

    public Result() {
    }

    public Result(long id, int status) {
        this(id, status, "ok");
    }

    public Result(long id, int status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
