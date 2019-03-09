package com.lls.leaf.model;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/************************************
 * SegmentBuffer
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class SegmentBuffer {

    private String key;
    private Segment[] segments; //双buffer
    private volatile int currentIndex; //当前的使用的segment的index
    private volatile boolean nextReady; //下一个segment是否处于可切换状态
    private volatile boolean initOk; //是否初始化完成
    private final AtomicBoolean threadRunning;  //线程是否在运行中
    private ReadWriteLock lock;

    private volatile int step;
    private volatile int minStep;
    private volatile long updatedTime;

    public SegmentBuffer() {
        segments = new Segment[]{new Segment(this), new Segment(this)};
        currentIndex = 0;
        nextReady = false;
        initOk = false;
        threadRunning = new AtomicBoolean(false);
        lock = new ReentrantReadWriteLock();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Segment[] getSegments() {
        return segments;
    }

    public Segment getCurrent() {
        return segments[currentIndex];
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int nextIndex() {
        return (currentIndex + 1) % 2;
    }

    public void switchIndex() {
        currentIndex = this.nextIndex();
    }

    public boolean isInitOk() {
        return initOk;
    }

    public void setInitOk(boolean initOk) {
        this.initOk = initOk;
    }

    public boolean isNextReady() {
        return nextReady;
    }

    public void setNextReady(boolean nextReady) {
        this.nextReady = nextReady;
    }

    public AtomicBoolean getThreadRunning() {
        return threadRunning;
    }

    public Lock readLock() {
        return lock.readLock();
    }

    public Lock writeLock() {
        return lock.writeLock();
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "SegmentBuffer{" +
                "key='" + key + '\'' +
                ", segments=" + Arrays.toString(segments) +
                ", currentIndex=" + currentIndex +
                ", nextReady=" + nextReady +
                ", initOk=" + initOk +
                ", threadRunning=" + threadRunning +
                ", step=" + step +
                ", minStep=" + minStep +
                ", updatedTime=" + updatedTime +
                '}';
    }
}

