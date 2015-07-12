package com.landon.mavs.example.concurrent;


import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Mavs线程池拒绝执行策略
 * 
 * @author landon
 * 
 */
public class MavsRejectedExecutionPolicy implements RejectedExecutionHandler {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MavsRejectedExecutionPolicy.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOGGER.debug("task rejectedExecution.ThreadPool.state:{}",
                MavsThreadPoolStateMonitor.monitor(executor));
    }
}