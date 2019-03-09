package com.lls.leaf.segment;

import com.lls.leaf.core.IdGenerator;
import com.lls.leaf.core.Result;
import com.lls.leaf.core.StatusEnum;
import com.lls.leaf.dao.LeafAllocDao;
import com.lls.leaf.model.LeafAlloc;
import com.lls.leaf.model.Segment;
import com.lls.leaf.model.SegmentBuffer;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/************************************
 * SegmentIdGenerator
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class SegmentIdGenerator implements IdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SegmentIdGenerator.class);

    // IDCache未初始化成功时的异常码
    private static final long EXCEPTION_ID_CACHE_INIT_FAILED = -1;
    // key不存在时的异常码
    private static final long EXCEPTION_ID_KEY_NOT_EXISTS = -2;
    // SegmentBuffer中的两个Segment均未从DB中装载时的异常码
    private static final long EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = -3;
    // 最大步长不超过100,0000
    private static final int MAX_STEP = 1000000;
    // 一个Segment维持时间为15分钟
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;
    private ExecutorService executor = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new UpdateThreadFactory());
    private volatile boolean initOK = false;
    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<String, SegmentBuffer>();
    private LeafAllocDao leafAllocDao;

    public static class UpdateThreadFactory implements ThreadFactory {

        private static final AtomicInteger threadNumber = new AtomicInteger(0);
        private static final String threadNamePrefix = "segmentId-generator-thread-";

        private static int nextThreadNumber() {
            return threadNumber.getAndIncrement();
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, threadNamePrefix + nextThreadNumber());
        }

    }

    public SegmentIdGenerator(LeafAllocDao leafAllocDao) {
        this.leafAllocDao = leafAllocDao;
    }

    public boolean initialize() {
        logger.info("init start ...");
        // 确保加载到kv后才初始化成功
        this.syncCacheFromDb();
        this.initOK = true;
        this.executeCacheFromDbAtEveryMinute();
        return this.initOK;
    }

    public Result get(String key) {
        if (!initOK) {
            return new Result(EXCEPTION_ID_CACHE_INIT_FAILED, StatusEnum.FAILED.getCode(), "not init.");
        }
        if (cache.containsKey(key)) {
            SegmentBuffer segmentBuffer = cache.get(key);
            if (!segmentBuffer.isInitOk()) {
                synchronized (segmentBuffer) {
                    if (!segmentBuffer.isInitOk()) {
                        try {
                            updateSegmentFromDb(key, segmentBuffer.getCurrent());
                            logger.info("Init buffer. Update leafkey {} {} from db", key, segmentBuffer.getCurrent());
                            segmentBuffer.setInitOk(true);
                        } catch (Exception e) {
                            logger.warn("Init buffer {} exception", segmentBuffer.getCurrent(), e);
                        }
                    }
                }
            }
            return getIdFromSegmentBuffer(segmentBuffer);
        }
        return new Result(EXCEPTION_ID_KEY_NOT_EXISTS, StatusEnum.FAILED.getCode(), "id key not exists.");
    }

    private void executeCacheFromDbAtEveryMinute() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("update-cache-from-db");
                thread.setDaemon(true);
                return thread;
            }
        });

        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                syncCacheFromDb();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void syncCacheFromDb() {
        logger.info("update cache from db ...");
        StopWatch stopWatch = new Slf4JStopWatch();

        try {
            List<String> dbTags = leafAllocDao.getAllTags();
            if (dbTags == null || dbTags.isEmpty()) {
                return;
            }
            List<String> cachedTags = new ArrayList<>(cache.keySet());
            List<String> insertedTags = new ArrayList<>(dbTags);
            List<String> removedTags = new ArrayList<>(cachedTags);
            //db中新加的tags灌进cache
            insertedTags.removeAll(cachedTags);
            for (String tag : insertedTags) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
                logger.info("add tag {} from db to cache, SegmentBuffer {}", tag, buffer);
            }
            //cache中已失效的tags从cache删除
            removedTags.removeAll(dbTags);
            for (String tag : removedTags) {
                cache.remove(tag);
                logger.info("remove tag {} from cache", tag);
            }
        } catch (Exception e) {
            logger.warn("update cache from db exception", e);
        } finally {
            stopWatch.stop("syncCacheFromDb");
        }
    }

    private void updateSegmentFromDb(String key, Segment segment) {
        StopWatch stopWatch = new Slf4JStopWatch();
        SegmentBuffer buffer = segment.getBuffer();
        LeafAlloc leafAlloc;
        if (!buffer.isInitOk()) {
            leafAlloc = leafAllocDao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setStep(leafAlloc.getStep());
            buffer.setMinStep(leafAlloc.getStep());
        } else if (buffer.getUpdatedTime() == 0) {
            leafAlloc = leafAllocDao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setUpdatedTime(System.currentTimeMillis());
            buffer.setMinStep(leafAlloc.getStep());
        } else {
            long duration = System.currentTimeMillis() - buffer.getUpdatedTime();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION) {
                if (nextStep * 2 > MAX_STEP) {

                } else {
                    nextStep = nextStep * 2;
                }
            } else if (duration < SEGMENT_DURATION * 2) {

            } else {
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
            logger.info("leafKey[{}], step[{}], duration[{}mins], nextStep[{}]", key, buffer.getStep(), String.format("%.2f", ((double) duration / (1000 * 60))), nextStep);
            LeafAlloc temp = new LeafAlloc();
            temp.setKey(key);
            temp.setStep(nextStep);
            leafAlloc = leafAllocDao.updateMaxIdByCustomStepAndGetLeafAlloc(temp);
            buffer.setUpdatedTime(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(leafAlloc.getStep());//leafAlloc的step为DB中的step
        }

        // must set value before set max
        long value = leafAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value);
        segment.setMax(leafAlloc.getMaxId());
        segment.setStep(buffer.getStep());
        stopWatch.stop("updateSegmentFromDb", key + " " + segment);
    }

    public Result getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {

            try {
                buffer.readLock().lock();
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep())
                        && buffer.getThreadRunning().compareAndSet(false, true)) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Segment nextSegment = buffer.getSegments()[buffer.nextIndex()];
                            boolean updateOK = false;
                            try {
                                updateSegmentFromDb(buffer.getKey(), nextSegment);
                                updateOK = true;
                                logger.info("update segment {} from db {}", buffer.getKey(), nextSegment);
                            } catch (Exception e) {
                                logger.warn(buffer.getKey() + " updateSegmentFromDb exception", e);
                            } finally {
                                if (updateOK) {
                                    buffer.writeLock().lock();
                                    buffer.setNextReady(true);
                                    buffer.getThreadRunning().set(false);
                                    buffer.writeLock().unlock();
                                } else {
                                    buffer.getThreadRunning().set(false);
                                }
                            }
                        }
                    });
                }

                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, StatusEnum.SUCCESS.getCode());
                }
            } finally {
                buffer.readLock().unlock();
            }


            try {
                buffer.writeLock().lock();
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, StatusEnum.SUCCESS.getCode());
                }
                if (buffer.isNextReady()) {
                    buffer.switchIndex();
                    buffer.setNextReady(false);
                } else {
                    logger.error("Both two segments in {} are not ready!", buffer);
                    return new Result(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL, StatusEnum.FAILED.getCode());
                }
            } finally {
                buffer.writeLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    Thread.currentThread().sleep(10);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    public List<LeafAlloc> getAllLeafAllocs() {
        return leafAllocDao.getAllLeafAllocs();
    }

    public Map<String, SegmentBuffer> getCache() {
        return cache;
    }


    public LeafAllocDao getLeafAllocDao() {
        return leafAllocDao;
    }

}
