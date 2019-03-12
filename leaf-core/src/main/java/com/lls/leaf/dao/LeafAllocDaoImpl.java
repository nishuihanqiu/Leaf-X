package com.lls.leaf.dao;

import com.lls.leaf.model.LeafAlloc;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

/************************************
 * LeafAllocDaoImpl
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class LeafAllocDaoImpl implements LeafAllocDao {

    private SqlSessionFactory sqlSessionFactory;

    public LeafAllocDaoImpl(DataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(LeafAllocMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    }

    public List<LeafAlloc> getAllLeafAllocs() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.lls.leaf.dao.LeafAllocMapper.getAllLeafAllocs");
        } finally {
            sqlSession.close();
        }
    }

    public LeafAlloc updateMaxIdAndGetLeafAlloc(String tag) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.lls.leaf.dao.LeafAllocMapper.updateMaxIdByTag", tag);
            LeafAlloc leafAlloc = sqlSession.selectOne("com.lls.leaf.dao.LeafAllocMapper.getLeafAlloc", tag);
            sqlSession.commit();
            return leafAlloc;
        } finally {
            sqlSession.close();
        }
    }

    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.lls.leaf.dao.LeafAllocMapper.updateMaxIdByCustomStep", leafAlloc);
            LeafAlloc result = sqlSession.selectOne("com.lls.leaf.dao.LeafAllocMapper.getLeafAlloc", leafAlloc.getKey());
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    public List<String> getAllTags() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            return sqlSession.selectList("com.lls.leaf.dao.LeafAllocMapper.getAllTags");
        } finally {
            sqlSession.close();
        }
    }

}
