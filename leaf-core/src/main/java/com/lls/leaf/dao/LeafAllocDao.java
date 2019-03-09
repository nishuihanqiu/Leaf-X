package com.lls.leaf.dao;

import com.lls.leaf.model.LeafAlloc;

import java.util.List;

/************************************
 * LeafAllocDao
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public interface LeafAllocDao {

    List<LeafAlloc> getAllLeafAllocs();

    LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);

    LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);

    List<String> getAllTags();

}
