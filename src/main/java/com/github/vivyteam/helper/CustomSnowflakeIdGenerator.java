package com.github.vivyteam.helper;

import java.util.concurrent.atomic.AtomicLong;

public class CustomSnowflakeIdGenerator {

    private static final int TIMESTAMP_BIT_SHIFT = 22;
    private static final int WORKER_ID_BIT_SHIFT = 12;
    private static final int SEQUENCE_BIT_MASK = 0xFFF;

    private final long workerId;
    private final AtomicLong sequence = new AtomicLong(0);
    private final long epoch;

    public CustomSnowflakeIdGenerator(long workerId, long epoch) {
        this.workerId = workerId;
        this.epoch = epoch;
    }

    public synchronized long generateId() {
        long currentTimeMillis = System.currentTimeMillis();
        long sequenceValue = sequence.getAndIncrement() & SEQUENCE_BIT_MASK;
        if (sequenceValue == 0) {
            // Sequence exhausted, wait for the next millisecond
            while (currentTimeMillis == System.currentTimeMillis()) {
                currentTimeMillis = System.currentTimeMillis();
            }
        }

        long id = ((currentTimeMillis - epoch) << TIMESTAMP_BIT_SHIFT) |
                (workerId << WORKER_ID_BIT_SHIFT) |
                sequenceValue;

        return id;
    }
}
