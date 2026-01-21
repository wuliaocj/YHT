package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 * 生成64位的长整型ID，包含：时间戳+机器ID+序列号
 */
@Slf4j
@Component
public class SnowflakeIdGenerator {

    /**
     * 起始时间戳（2023-01-01 00:00:00）
     */
    private static final long EPOCH = 1672531200000L;

    /**
     * 各部分位数
     */
    private static final long SEQUENCE_BITS = 12L;       // 序列号占12位
    private static final long MACHINE_BITS = 10L;       // 机器ID占10位

    /**
     * 各部分最大值
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);  // 4095
    private static final long MAX_MACHINE = ~(-1L << MACHINE_BITS);     // 1023

    /**
     * 各部分偏移量
     */
    private static final long SEQUENCE_SHIFT = 0L;
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS;

    /**
     * 机器ID（可通过配置文件设置，默认为1）
     */
    private final long machineId;

    /**
     * 序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     */
    public SnowflakeIdGenerator() {
        this(1L); // 默认机器ID为1
    }

    /**
     * 构造函数
     * @param machineId 机器ID（0-1023）
     */
    public SnowflakeIdGenerator(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE) {
            throw new IllegalArgumentException("机器ID必须在0-1023之间");
        }
        this.machineId = machineId;
        log.info("雪花算法ID生成器初始化完成，机器ID：{}", machineId);
    }

    /**
     * 生成下一个ID
     * @return 雪花算法ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨检查
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            if (offset <= 5) {
                // 小幅度回拨，等待时钟追上
                try {
                    Thread.sleep(offset << 1);
                    currentTimestamp = System.currentTimeMillis();
                    if (currentTimestamp < lastTimestamp) {
                        throw new RuntimeException("时钟回拨超过阈值，无法生成ID");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("时钟回拨等待被中断", e);
                }
            } else {
                throw new RuntimeException("时钟回拨超过阈值，无法生成ID");
            }
        }

        // 同一毫秒内，序列号递增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 序列号溢出，等待下一毫秒
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒，序列号重置为0
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 组装ID
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | (sequence << SEQUENCE_SHIFT);
    }

    /**
     * 生成订单号（带前缀）
     * @param prefix 前缀，如"ORD"
     * @return 订单号
     */
    public String generateOrderNo(String prefix) {
        return prefix + nextId();
    }

    /**
     * 生成支付单号（带前缀）
     * @param prefix 前缀，如"PAY"
     * @return 支付单号
     */
    public String generatePaymentNo(String prefix) {
        return prefix + nextId();
    }

    /**
     * 解析ID（用于调试）
     * @param id 雪花算法ID
     * @return 解析结果
     */
    public String parseId(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        long machine = (id >> MACHINE_SHIFT) & MAX_MACHINE;
        long sequence = id & MAX_SEQUENCE;
        
        return String.format("ID=%d, 时间戳=%d, 机器ID=%d, 序列号=%d, 时间=%s", 
                id, timestamp, machine, sequence, new java.util.Date(timestamp));
    }

    /**
     * 等待下一毫秒
     * @param lastTimestamp 上次时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取机器ID
     * @return 机器ID
     */
    public long getMachineId() {
        return machineId;
    }

    /**
     * 获取当前序列号
     * @return 序列号
     */
    public long getSequence() {
        return sequence;
    }
}