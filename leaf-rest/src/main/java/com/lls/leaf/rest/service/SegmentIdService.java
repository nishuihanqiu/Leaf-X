package com.lls.leaf.rest.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import com.lls.leaf.core.ZeroIdGenerator;
import com.lls.leaf.dao.LeafAllocDao;
import com.lls.leaf.dao.LeafAllocDaoImpl;
import com.lls.leaf.rest.consts.Constants;
import com.lls.leaf.rest.exception.LeafException;
import com.lls.leaf.segment.SegmentIdGenerator;
import com.lls.leaf.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Properties;

/************************************
 * SegmentIdService
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
@Service("segmentIdService")
public class SegmentIdService {

    private static final Logger logger = LoggerFactory.getLogger(SegmentIdService.class);

    private IdGenerator idGenerator;
    private DruidDataSource dataSource;

    public SegmentIdService() throws SQLException, LeafException {
        this.initialize();
    }

    private void initialize() throws SQLException, LeafException {
        Properties properties = PropertiesUtils.getProperties();
        String leafEnable = properties.getProperty(Constants.LEAF_SEGMENT_ENABLE, "true");
        boolean flag = Boolean.parseBoolean(leafEnable);
        if (!flag) {
            idGenerator = new ZeroIdGenerator();
            logger.info("Zero id Generator Service init Successfully");
            return;
        }

        dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getProperty(Constants.LEAF_JDBC_URL));
        dataSource.setUsername(properties.getProperty(Constants.LEAF_JDBC_USERNAME));
        dataSource.setPassword(properties.getProperty(Constants.LEAF_JDBC_PASSWORD));
        dataSource.init();

        LeafAllocDao dao = new LeafAllocDaoImpl(dataSource);
        idGenerator = new SegmentIdGenerator(dao);
        if (idGenerator.initialize()) {
            logger.info("Segment Service Init Successfully");
        } else {
            throw new LeafException("Segment Service Init Fail");
        }
    }

    public Result getId(String key) {
        return idGenerator.get(key);
    }

    public SegmentIdGenerator getSegmentIdGenerator() {
        if (idGenerator instanceof SegmentIdGenerator) {
            return (SegmentIdGenerator) idGenerator;
        }
        return null;
    }
}
