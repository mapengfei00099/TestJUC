package com.landon.mavs.example.concurrent;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Mavs Cacheœﬂ≥Ã≥ÿ {@link java.util.concurrent.Executors#newCachedThreadPool()}
 * 
 * @author landon
 * 
 */
public class MavsCachedThreadPoolExecutor extends MavsThreadPoolExecutor {

	public MavsCachedThreadPoolExecutor() {
		super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public MavsCachedThreadPoolExecutor(ThreadFactory threadFactory) {
		super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
	}

	public MavsCachedThreadPoolExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectHandler) {
		super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory,
				rejectHandler);
	}
}
