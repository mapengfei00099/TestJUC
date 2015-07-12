package com.landon.mavs.example.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Mavs�߳�Ĭ�ϵ��쳣��ֹ������ {@link java.lang.ThreadGroup#uncaughtException(Thread, Throwable)} �ж���ThreadDeath�Ĵ���
 * 
 * @author landon
 * 
 */
public class MavsThreadDefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(MavsThreadDefaultUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.warn("Exception in thread \"" + t.getName() + "\" ", e);
	}

}