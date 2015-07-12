package com.landon.mavs.example.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Mavs�̳߳�״̬Monitor
 * 
 * @author landon
 * 
 */
public class MavsThreadPoolStateMonitor {
	/**
	 * 
	 * �̳߳�״̬����
	 * 
	 * @param executor
	 * @return
	 */
	public static String monitor(ThreadPoolExecutor executor) {
		if (executor == null) {
			throw new NullPointerException();
		}

		// �����߳���
		int corePoolSize = executor.getCorePoolSize();
		// ����߳���
		int maximumPoolSize = executor.getMaximumPoolSize();
		// �̱߳��ֻʱ��
		long keepAliveTime = executor.getKeepAliveTime(TimeUnit.MILLISECONDS);

		// ��ǰ�߳���
		int poolSize = executor.getPoolSize();
		// ���ػ�Ծ(����ִ������)�Ľ����߳���
		int activeThreadCount = executor.getActiveCount();
		// ��������ͬʱλ�ڳ��е�����߳���(�����ѱ����յ�worker�̼߳���)
		int largestPoolSize = executor.getLargestPoolSize();

		// �����ִ�еĽ�����������
		long completedTaskCount = executor.getCompletedTaskCount();
		// ���ƻ���ɵĽ�����������(completedTaskCount + �������д�С + ����ִ�������worker�߳���Ŀ)
		long taskCount = executor.getTaskCount();
		// �������д�С
		int workQueueSize = executor.getQueue().size();

		// �Ƿ��ڷ�RUNNING״̬��
		boolean isShutdown = executor.isShutdown();
		// �Ƿ���TERMINATED״̬
		boolean isTerminated = executor.isTerminated();
		// �Ƿ���SHUTDOWN����STOP״̬
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