package com.lls.leaf.segment;

import com.alibaba.druid.pool.DruidDataSource;
import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import com.lls.leaf.dao.LeafAllocDao;
import com.lls.leaf.dao.LeafAllocDaoImpl;
import com.lls.leaf.util.PropertiesUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/************************************
 * IdGeneratorServiceTest
 * @author liliangshan
 * @date 2019-03-12
 ************************************/
public class IdGeneratorServiceTest {

    private IdGenerator idGenerator;
    private DruidDataSource dataSource;

    @Before
    public void before() throws IOException, SQLException {
        // LOAD db config
        Properties properties = PropertiesUtils.getProperties();

        // config dataSource
        dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getProperty("jdbc.url"));
        dataSource.setUsername(properties.getProperty("jdbc.username"));
        dataSource.setPassword(properties.getProperty("jdbc.password"));
        dataSource.init();

        // config dao
        LeafAllocDao dao = new LeafAllocDaoImpl(dataSource);

        // Config ID Generator
        idGenerator = new SegmentIdGenerator(dao);
        idGenerator.initialize();
    }

    @Test
    public void testGetId() {
        for (int i = 0; i < 100; ++i) {
            Result r = idGenerator.get("leaf-segment-test");
            System.out.println(r);
        }
    }

    @After
    public void after() {
        dataSource.close();
    }

}
