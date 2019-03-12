package com.lls.leaf.dao;

import com.lls.leaf.model.LeafAlloc;
import org.apache.ibatis.annotations.*;

import java.util.List;

/************************************
 * LeafAllocMapper
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public interface LeafAllocMapper {

    String map = "baseMap";

    @Results(id = map, value = {
            @Result(column = "biz_tag", property = "key"),
            @Result(column = "max_id", property = "maxId"),
            @Result(column = "step", property = "step"),
            @Result(column = "updated_time", property = "updatedTime")
    })
    @Select("SELECT biz_tag, max_id, step, updated_time FROM leaf_alloc")
    List<LeafAlloc> getAllLeafAllocs();

    @ResultMap(value = map)
    @Select("SELECT biz_tag, max_id, step, updated_time FROM leaf_alloc WHERE biz_tag = #{tag}")
    LeafAlloc getLeafAlloc(@Param("tag") String tag);

    @Update("UPDATE leaf_alloc SET max_id = max_id + step WHERE biz_tag = #{tag}")
    void updateMaxIdByTag(@Param("tag") String tag);

    @Update("UPDATE leaf_alloc SET max_id = max_id + #{step} WHERE biz_tag = #{key}")
    void updateMaxIdByCustomStep(@Param("leafAlloc") LeafAlloc leafAlloc);

    @Select("SELECT biz_tag FROM leaf_alloc")
    List<String> getAllTags();

}
