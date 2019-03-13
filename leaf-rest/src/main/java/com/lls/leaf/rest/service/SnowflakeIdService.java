package com.lls.leaf.rest.service;

import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import com.lls.leaf.core.ZeroIdGenerator;
import com.lls.leaf.rest.consts.Constants;
import com.lls.leaf.rest.exception.LeafException;
import com.lls.leaf.snowflake.SnowflakeIdGenerator;
import com.lls.leaf.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Properties;

/************************************
 * SnowflakeIdService
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
@Service("snowflakeIdService")
public class SnowflakeIdService {

    private static final Logger logger = LoggerFactory.getLogger(SegmentIdService.class);

    private IdGenerator idGenerator;

    public SnowflakeIdService() throws LeafException {
        this.initialize();
    }

    private void initialize() throws LeafException {
        Properties properties = PropertiesUtils.getProperties();
        boolean enable = Boolean.parseBoolean(properties.getProperty(Constants.LEAF_SNOWFLAKE_ENABLE, "true"));
        if (!enable) {
            idGenerator = new ZeroIdGenerator();
            logger.info("Zero id Generator Service init Successfully");
            return;
        }

        String zkAddress = properties.getProperty(Constants.LEAF_SNOWFLAKE_ZK_ADDRESS);
        int port = Integer.parseInt(properties.getProperty(Constants.LEAF_SNOWFLAKE_PORT));
        idGenerator = new SnowflakeIdGenerator(zkAddress, port);
        if (idGenerator.initialize()) {
            logger.info("Snowflake Service Init Successfully");
        } else {
            throw new LeafException("Snowflake Service Init Fail");
        }

    }

    public Result getId(String key) {
        return idGenerator.get(key);
    }
}
