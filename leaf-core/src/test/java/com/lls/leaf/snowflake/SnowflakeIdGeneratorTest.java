package com.lls.leaf.snowflake;

import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import com.lls.leaf.util.PropertiesUtils;
import org.junit.Test;

import java.util.Properties;

/************************************
 * SnowflakeIdGeneratorTest
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
public class SnowflakeIdGeneratorTest {

    @Test
    public void testGenerateId() {
        Properties properties = PropertiesUtils.getProperties();
        IdGenerator idGenerator = new SnowflakeIdGenerator(properties.getProperty("leaf_x.snowflake.zk.address"), 32775);
        for (int i = 1; i < 1000; ++i) {
            Result r = idGenerator.get("a");
            System.out.println(r);
        }
    }

}
