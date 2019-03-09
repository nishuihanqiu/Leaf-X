package com.lls.leaf.model;

/************************************
 * LeafAlloc
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class LeafAlloc {

    private String key;
    private long maxId;
    private int step;
    private String updatedTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

}
