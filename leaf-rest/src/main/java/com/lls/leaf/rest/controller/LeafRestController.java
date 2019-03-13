package com.lls.leaf.rest.controller;

import com.lls.leaf.core.Result;
import com.lls.leaf.core.StatusEnum;
import com.lls.leaf.rest.exception.LeafRestException;
import com.lls.leaf.rest.exception.NotFoundKeyException;
import com.lls.leaf.rest.service.SegmentIdService;
import com.lls.leaf.rest.service.SnowflakeIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/************************************
 * LeafRestController
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
@RestController
public class LeafRestController {

    private static final Logger logger = LoggerFactory.getLogger(LeafRestController.class);

    @Autowired
    private SegmentIdService segmentIdService;
    @Autowired
    private SnowflakeIdService snowflakeIdService;

    @RequestMapping(value = "/api/segment/get/{key}", method = RequestMethod.GET)
    public Result getSegmentID(@PathVariable("key") String key) {
        return get(key, segmentIdService.getId(key));
    }

    @RequestMapping(value = "/api/snowflake/get/{key}", method = RequestMethod.GET)
    public Result getSnowflakeID(@PathVariable("key") String key) {
        return get(key, snowflakeIdService.getId(key));

    }

    private Result get(String key, Result id) {
        Result result;
        if (key == null || key.isEmpty()) {
            throw new NotFoundKeyException("not found key");
        }

        result = id;
        if (result.getStatus() == StatusEnum.FAILED.getCode()) {
            throw new LeafRestException(result.toString());
        }
        return result;
    }

}
