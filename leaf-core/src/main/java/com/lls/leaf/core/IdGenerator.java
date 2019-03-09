package com.lls.leaf.core;

/************************************
 * IdGenerator
 * @author liliangshan
 * @date 2019-03-08
 ************************************/
public interface IdGenerator {

    boolean initialize();

    Result get(String key);

}
