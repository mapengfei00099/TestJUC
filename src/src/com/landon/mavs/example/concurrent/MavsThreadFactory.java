package com.landon.mavs.example.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Mavs线程工厂 {@link java.util.concurrent.Executors#defaultThreadFactory()} 参考
 * {@link java.util.concurrent.Executors$DefaultThreadFactory}实现
 * 
 * @author landon
 * 
 */
public class MavsThreadFactory implements ThreadFactory {
	private static final String MAVS_NAME_PREFIX = "Mavs-";

	/** 线程号 */
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	/** 线程组 */
	private final ThreadGroup threadGroup;
	/** 线程名字前缀 */
	private final String namePrefix;

	/**
	 * 
	 * 构造MavsThreadFactory
	 * 
	 * @param processPrefix
	 *            进程前缀
	 * @param threadName
	 *            线程名
	 */
	public MavsThreadFactory(String processPrefix, String threadName) {
		SecurityManager sm = System.getSecurityManager();
		threadGroup = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();

		namePrefix = MAVS_NAME_PREFIX + processPrefix + "-" + threadName + "-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(threadGroup, r, namePrefix + threadNumber.getAndIncrement(), 0);

		// 做这两个设置的原因在于线程的daemon/priority属性默认是由Thread.currentThread决定

		if (t.isDaemon()) {
			t.setDaemon(false);
		}

		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}

		// 设置Mavs线程默认的异常终止处理器
		if (Thread.getDefaultUncaughtExceptionHandler() == null) {
			Thread.setDefaultUncaughtExceptionHandler(new MavsThreadDefaultUncaughtExceptionHandler());
		}

		return t;
	}

	public String getNamePrefix() {
		return namePrefix;
	}
}