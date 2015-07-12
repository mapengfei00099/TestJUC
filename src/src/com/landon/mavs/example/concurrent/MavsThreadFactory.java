package com.landon.mavs.example.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Mavs�̹߳��� {@link java.util.concurrent.Executors#defaultThreadFactory()} �ο�
 * {@link java.util.concurrent.Executors$DefaultThreadFactory}ʵ��
 * 
 * @author landon
 * 
 */
public class MavsThreadFactory implements ThreadFactory {
	private static final String MAVS_NAME_PREFIX = "Mavs-";

	/** �̺߳� */
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	/** �߳��� */
	private final ThreadGroup threadGroup;
	/** �߳�����ǰ׺ */
	private final String namePrefix;

	/**
	 * 
	 * ����MavsThreadFactory
	 * 
	 * @param processPrefix
	 *            ����ǰ׺
	 * @param threadName
	 *            �߳���
	 */
	public MavsThreadFactory(String processPrefix, String threadName) {
		SecurityManager sm = System.getSecurityManager();
		threadGroup = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();

		namePrefix = MAVS_NAME_PREFIX + processPrefix + "-" + threadName + "-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(threadGroup, r, namePrefix + threadNumber.getAndIncrement(), 0);

		// �����������õ�ԭ�������̵߳�daemon/priority����Ĭ������Thread.currentThread����

		if (t.isDaemon()) {
			t.setDaemon(false);
		}

		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}

		// ����Mavs�߳�Ĭ�ϵ��쳣��ֹ������
		if (Thread.getDefaultUncaughtExceptionHandler() == null) {
			Thread.setDefaultUncaughtExceptionHandler(new MavsThreadDefaultUncaughtExceptionHandler());
		}

		return t;
	}

	public String getNamePrefix() {
		return namePrefix;
	}
}