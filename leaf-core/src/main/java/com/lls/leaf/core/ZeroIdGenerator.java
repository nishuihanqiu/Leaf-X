package com.lls.leaf.core;

/************************************
 * ZeroIdGenerator
 * @author liliangshan
 * @date 2019-03-08
 ************************************/
public class ZeroIdGenerator implements IdGenerator {

    public Result get(String key) {
        return new Result(0, StatusEnum.SUCCESS.getCode(), StatusEnum.SUCCESS.getMessage());
    }

    public boolean initialize() {
        return false;
    }

}
