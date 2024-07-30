package com.demo.study.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述自定义线程池信息
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/11 13:02
 */
public class ThreadPoolConfig {
    /**
     * 平台环境信息
     */
    public static final Runtime RUNTIME = Runtime.getRuntime();
    /**
     * 线程ID生成器
     */
    public static final AtomicInteger THREAD_ID_GENERATOR = new AtomicInteger(0);
    /**
     * 自定义线程池
     */
    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2,
            2,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            (runnable) -> new Thread(runnable, String.format("%s-%s", "接力线程", THREAD_ID_GENERATOR.incrementAndGet())),
            new ThreadPoolExecutor.AbortPolicy());
}
