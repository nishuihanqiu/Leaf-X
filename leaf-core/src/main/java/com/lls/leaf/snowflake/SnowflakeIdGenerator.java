package com.lls.leaf.snowflake;

import com.google.common.base.Preconditions;
import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import com.lls.leaf.core.StatusEnum;
import com.lls.leaf.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/************************************
 * SnowflakeIdGenerator
 * @author liliangshan
 * @date 2019-03-12
 ************************************/
public class SnowflakeIdGenerator implements IdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);
    private final long twepoch = 1288834974657L;
    private final long workerIdBits = 10L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);//最大能够分配的workerid =1023
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    public boolean initFlag = false;
    private static final Random RANDOM = new Random();
    private int port;

    public SnowflakeIdGenerator(String zkAddress, int port) {
        this.port = port;
        ZookeeperHolder holder = new ZookeeperHolder(NetworkUtils.getIP(), String.valueOf(port), zkAddress);
        initFlag = holder.init();
        if (initFlag) {
            workerId = holder.getWorkerId();
            logger.info("START SUCCESS USE ZK WORKERID-{}", workerId);
        } else {
            Preconditions.checkArgument(initFlag, "Snowflake Id Gen is not init ok");
        }
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public synchronized Result get(String key) {
        long timestamp = this.generateTime();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = this.generateTime();
                    if (timestamp < lastTimestamp) {
                        return new Result(-1, StatusEnum.FAILED.getCode(), "timestamp lt lastTimestamp");
                    }
                } catch (InterruptedException e) {
                    logger.error("wait interrupted");
                    return new Result(-2, StatusEnum.FAILED.getCode(), "wait interrupted");
                }
            } else {
                return new Result(-3, StatusEnum.FAILED.getCode(), "offset gt 5");
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                timestamp = this.tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return new Result(id, StatusEnum.SUCCESS.getCode(), "success id:" + id);
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = this.generateTime();
        while (timestamp <= lastTimestamp) {
            timestamp = this.generateTime();
        }
        return timestamp;
    }

    private long generateTime() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return workerId;
    }
}
