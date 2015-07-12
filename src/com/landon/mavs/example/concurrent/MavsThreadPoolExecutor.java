package com.landon.mavs.example.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Mavs线程池,提供了钩子方法的默认实现
 * 
 * @author landon
 * 
 */
public class MavsThreadPoolExecutor extends ThreadPoolExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MavsThreadPoolExecutor.class);

	public MavsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public MavsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public MavsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public MavsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		LOGGER.info("Thread[" + t.getName() + "]#beforeExecute:{}", MavsThreadPoolStateMonitor.monitor(this));
		super.beforeExecute(t, r);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		LOGGER.info("Thread[" + Thread.currentThread().getName() + "]EfterExecute:{}",
				MavsThreadPoolStateMonitor.monitor(this));
		if (t != null) {
			LOGGER.warn("Worker.runs.task.err", t);
		}
	}

	@Override
	protected void terminated() {
		super.terminated();
		LOGGER.info("terminated:{}", MavsThreadPoolStateMonitor.monitor(this));
	}
}