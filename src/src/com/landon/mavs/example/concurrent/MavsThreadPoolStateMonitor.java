package com.landon.mavs.example.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Mavs线程池状态Monitor
 * 
 * @author landon
 * 
 */
public class MavsThreadPoolStateMonitor {
	/**
	 * 
	 * 线程池状态监视
	 * 
	 * @param executor
	 * @return
	 */
	public static String monitor(ThreadPoolExecutor executor) {
		if (executor == null) {
			throw new NullPointerException();
		}

		// 核心线程数
		int corePoolSize = executor.getCorePoolSize();
		// 最大线程数
		int maximumPoolSize = executor.getMaximumPoolSize();
		// 线程保持活动时间
		long keepAliveTime = executor.getKeepAliveTime(TimeUnit.MILLISECONDS);

		// 当前线程数
		int poolSize = executor.getPoolSize();
		// 返回活跃(正在执行任务)的近似线程数
		int activeThreadCount = executor.getActiveCount();
		// 返回曾经同时位于池中的最大线程数(包括已被回收的worker线程计数)
		int largestPoolSize = executor.getLargestPoolSize();

		// 已完成执行的近似任务总数
		long completedTaskCount = executor.getCompletedTaskCount();
		// 曾计划完成的近似任务总数(completedTaskCount + 工作队列大小 + 正在执行任务的worker线程数目)
		long taskCount = executor.getTaskCount();
		// 工作队列大小
		int workQueueSize = executor.getQueue().size();

		// 是否在非RUNNING状态下
		boolean isShutdown = executor.isShutdown();
		// 是否是TERMINATED状态
		boolean isTerminated = executor.isTerminated();
		// 是否是SHUTDOWN或者STOP状态
		boolean isTerminating = executor.isTerminating();

		String executorName = "Default-ThreadPoolExecutor";
		ThreadFactory factory = executor.getThreadFactory();
		if ((factory != null) && (factory instanceof MavsThreadFactory)) {
			executorName = ((MavsThreadFactory) factory).getNamePrefix();
		}

		return executorName + " [corePoolSize=" + corePoolSize + ", maximumPoolSize=" + maximumPoolSize
				+ ", keepAliveTime=" + keepAliveTime + ", poolSize=" + poolSize + ", activeThreadCount="
				+ activeThreadCount + ", largestPoolSize=" + largestPoolSize + ", completedTaskCount="
				+ completedTaskCount + ", taskCount=" + taskCount + ", workQueueSize=" + workQueueSize
				+ ", isShutdown=" + isShutdown + ", isTerminated=" + isTerminated + ", isTerminating=" + isTerminating
				+ "]";
	}
}