package com.lls.leaf.model;

import java.util.concurrent.atomic.AtomicLong;

/************************************
 * Segment
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class Segment {

    private AtomicLong value = new AtomicLong(0);
    private volatile long max;
    private volatile int step;
    private SegmentBuffer buffer;

    public Segment(SegmentBuffer buffer) {
        this.buffer = buffer;
    }

    public AtomicLong getValue() {
        return value;
    }

    public void setValue(AtomicLong value) {
        this.value = value;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public SegmentBuffer getBuffer() {
        return buffer;
    }

    public long getIdle() {
        return max - value.get();
    }

    @Override
    public String toString() {
        return "Segment{" +
                "value=" + value +
                ", max=" + max +
                ", step=" + step +
                ", buffer=" + buffer +
                '}';
    }
}
