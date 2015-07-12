package com.landon.mavs.example.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 封装的固定线程数量的Mavs线程池 {@link java.util.concurrent.Executors#newFixedThreadPool(int)}
 * 
 * @author landon
 * 
 */
public class MavsFixedThreadPoolExecutor extends MavsThreadPoolExecutor {

	public MavsFixedThreadPoolExecutor(int nThreads) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public MavsFixedThreadPoolExecutor(int nThreads, ThreadFactory threadFactory) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
	}

	public MavsFixedThreadPoolExecutor(int nThreads, ThreadFactory threadFactory, RejectedExecutionHandler rejectHandler) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory,
				rejectHandler);
	}
}