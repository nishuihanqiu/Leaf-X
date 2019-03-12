package com.lls.leaf.segment;

import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/************************************
 * SpringIdGeneratorServiceTest
 * @author liliangshan
 * @date 2019-03-12
 ************************************/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application_context.xml"}) //加载配置文件
public class SpringIdGeneratorServiceTest {

    @Autowired
    private IdGenerator idGenerator;

    @Test
    public void testGenerateId() {
        for (int i = 0; i < 100; i++) {
            Result r = idGenerator.get("leaf-segment-test");
            System.out.println(r);
        }
    }

}
