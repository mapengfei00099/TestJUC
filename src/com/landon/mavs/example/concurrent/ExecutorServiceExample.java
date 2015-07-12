package com.landon.mavs.example.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ExecutorServiceExample
 * 
 * @author landon
 * 
 */
public class ExecutorServiceExample {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceExample.class);

	public static void main(String[] args) {
		ExecutorService exeSrv = Executors.newFixedThreadPool(4);
		// execute(Runnable command) ִ��һ��Runnable
		exeSrv.execute(new OneRunnable(1));

		// Future submit(Runnable task) �ύһ��Runable
		Future oneRunFuture = exeSrv.submit(new OneRunnable(2));
		// Future#isDone ���������Ƿ����
		LOGGER.debug("oneRun is complete:" + oneRunFuture.isDone());
		try {
			// �ȴ��������,���ؼ�����
			// ��ǰ�ɹ���ɵ�ʱ�� #get ����null
			LOGGER.debug("oneRun result:" + oneRunFuture.get());
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneRun#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneRun#get compuation throws a exception");
		}

		// Future submit(Callable task) �ύһ��Callable
		Future<String> oneCallFuture = exeSrv.submit(new OneCallable(1));

		try {
			// V get() throws InterruptedException, ExecutionException
			// �ȴ��������,���ؼ�����
			LOGGER.debug("oneCall result:" + oneCallFuture.get());
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneCall#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneCall#get compuation throws a exception");
		}

		Future<String> oneCallFuture2 = exeSrv.submit(new OneCallable(2));

		try {
			// V get(long timeout, TimeUnit unit) ָ���ȴ���ʱʱ��
			LOGGER.debug("oneCall2 result:" + oneCallFuture2.get(1, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneCall2#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneCall2#get compuation throws a exception");
		} catch (TimeoutException e) {
			LOGGER.warn("exception_oneCall2#get timeout");
		}

		Future<String> oneCallFuture3 = exeSrv.submit(new OneCallable(3));
		// boolean cancel(boolean mayInterruptIfRunning)
		// ����ȡ�������ִ��.�����������ɻ����Ѿ���ȡ��������Ϊ����һЩԭ����ȡ�����Ի�ʧ��
		// ������Գɹ�������δ��ʼ���������Ҳ��������.��������Ѿ�����,mayInterruptIfRunning������������ִ���߳��Ƿ��жϳ��Խ�������
		boolean isFuture3CancelSuccess = oneCallFuture3.cancel(false);
		LOGGER.debug("oneCallFuture3#cancel(false) result:" + isFuture3CancelSuccess);
		LOGGER.debug("oneCallFuture3#isDone:" + oneCallFuture3.isDone());
		LOGGER.debug("oneCallFuture3#isCancelled:" + oneCallFuture3.isCancelled());

		Future<String> oneCallFuture4 = exeSrv.submit(new OneCallable(4));
		// ���߳���ͣ2���ִ��cancel
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {

		}
		// �˴�cancel��true���������������������ж�ִ�������̳߳��Խ�������
		// ��������Կ���,�����������ʼ,����ȴû������������->�ҷ���true���������ж�ȡ��
		boolean isFuture4CancelSuccess = oneCallFuture4.cancel(true);
		LOGGER.debug("oneCallFuture4#cancel(true) result:" + isFuture4CancelSuccess);
		LOGGER.debug("oneCallFuture4#isDone:" + oneCallFuture4.isDone());
		LOGGER.debug("oneCallFuture4#isCancelled:" + oneCallFuture4.isCancelled());

		Future<String> oneCallFuture5 = exeSrv.submit(new OneCallable(5));
		// ���߳���ͣ8���ִ��cancel,��ʱ�����п����Ѿ�ִ�����
		try {
			TimeUnit.SECONDS.sleep(8);
		} catch (InterruptedException e) {

		}
		// ��������Կ���,����5�����end.��cancelʱ�����Ѿ����.����isFuture5CancelSuccessΪfalse.isDoneΪtrue.isCancelledΪfalse
		boolean isFuture5CancelSuccess = oneCallFuture5.cancel(true);
		LOGGER.debug("oneCallFuture5#cancel(true) result:" + isFuture5CancelSuccess);
		LOGGER.debug("oneCallFuture5#isDone:" + oneCallFuture5.isDone());
		LOGGER.debug("oneCallFuture5#isCancelled:" + oneCallFuture5.isCancelled());

		// Future submit(Runnable task, T result) ���������ʱget�����᷵��ָ����result
		Future<String> oneRun3Future = exeSrv.submit(new OneRunnable(3), "isOk");
		try {
			// ��������Կ���get�����ķ����Ǵ����"isOk"
			LOGGER.debug("oneRun3 result:" + oneRun3Future.get());
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneRun3l#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneRun3#get compuation throws a exception");
		}

		// ���񼯺�
		List<OneCallable> oneCallList = Arrays.asList(new OneCallable(10), new OneCallable(11), new OneCallable(12));
		try {
			// List> invokeAll(Collection>
			// tasks) throws InterruptedException;
			// �൱������ִ������.�ӷ������쳣�б���Կ���.�˷�����ȴ�(������)ֱ�������������
			List<Future<String>> oneCallListFutures = exeSrv.invokeAll(oneCallList);

			// ������ɽ�� ��������Կ���->invokeAllȷʵ���ڵȴ���������ִ�����.
			List<Boolean> resultList = new ArrayList<Boolean>();
			for (Future<String> future : oneCallListFutures) {
				if (future.isDone()) {
					resultList.add(true);
				}
			}

			LOGGER.debug("oneCallListFutures result: " + resultList);
		} catch (InterruptedException e) {
			LOGGER.warn("exeSrv#invokeAll(oneCallList) exception_waiting all task complete was interrupted.");
		}

		// ���񼯺�2
		List<OneCallable> oneCallList2 = Arrays.asList(new OneCallable(20), new OneCallable(21), new OneCallable(22));
		try {
			// T invokeAny(Collection> tasks) throws
			// InterruptedException, ExecutionException;
			// ����ִ������->�ȴ�ֱ��ĳ�������ѳɹ����(ע��ֻҪĳ������ɹ������򷵻ؽ��) ����ע�ⷵ�ؽ����T,����Future
			String oneCallList2Result = exeSrv.invokeAny(oneCallList2);
			// �����������Կ���:
			// [oneCallList2Result:OneCallable [taskNum=20]OK],��20������ִ����ɼ�������
			LOGGER.debug("oneCallList2Result:" + oneCallList2Result);
		} catch (InterruptedException e) {
			LOGGER.warn("exeSrv#invokeAny(oneCallList2) exception_waiting one task complete was interrupted.");
		} catch (ExecutionException e) {
			LOGGER.warn("exeSrv#invokeAll(oneCallList2) exception_one any one task was completed.");
		}

		// ���񼯺�3
		List<OneCallable> oneCallList3 = Arrays.asList(new OneCallable(30), new OneCallable(31), new OneCallable(32));
		try {
			// List> invokeAll(Collection>
			// tasks, long timeout, TimeUnit unit) throws InterruptedException;
			// ����ִ������->ָ���ȴ���ʱʱ��->ע��÷����������׳���ʱ�쳣,�����û�б���ϵ�����£���ʱ��(ֱ�ӷ���),��ĳЩ����ֻ��δ��ɶ���(ע���غ��ȡ����δ��ɵ�����)
			List<Future<String>> oneCallList3Futures = exeSrv.invokeAll(oneCallList3, 2, TimeUnit.SECONDS);

			List<Boolean> oneCallList3Results = new ArrayList<Boolean>();
			for (Future<String> future : oneCallList3Futures) {
				if (future.isDone()) {
					oneCallList3Results.add(true);
				} else {
					oneCallList3Results.add(false);
				}
			}

			// �������,�������������.�����ص�Future�б��isDone����������true.�����е�����û���������end.
			// ��API��,��������������ɻ��߳�ʱ(�����ĸ����ȷ���)�򷵻ص�Future�б��isDone��������true
			// һ�����غ�,��ȡ����δ��ɵ�����
			LOGGER.debug("oneCallList3Results:" + oneCallList3Results);
		} catch (InterruptedException e) {
			LOGGER.warn("exeSrv#invokeAll(oneCallList3, 2, TimeUnit.SECONDS) exception_waiting all task compelte was interrupted.");
		}

		// ���񼯺�4
		List<OneCallable> oneCallList4 = Arrays.asList(new OneCallable(40), new OneCallable(41), new OneCallable(42));
		try {
			// T invokeAny(Collection> tasks,long
			// timeout, TimeUnit unit) throws InterruptedException,
			// ExecutionException, TimeoutException;
			// ����ִ������->ָ���ȴ���ʱʱ��->ע����������׳���TimeoutException->���ڵȴ���ʱ����׳��쳣
			// �����������Կ���,�ڵȴ���ʱ��->��δ��ɵ����񶼱�ȡ����,��Ϊ���ֻ��beginû��end
			exeSrv.invokeAny(oneCallList4, 1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.debug("exeSrv#invokeAny(oneCallList4, 1, TimeUnit.SECONDS) exception_waiting any one task complete was interrupted.");
		} catch (ExecutionException e) {
			LOGGER.debug("exeSrv#invokeAny(oneCallList4, 1, TimeUnit.SECONDS) exception_no any one task was completed");
		} catch (TimeoutException e) {
			LOGGER.debug("exeSrv#invokeAny(oneCallList4, 1, TimeUnit.SECONDS) exception_waiting timeout");
		}

		// void shutdown() ����һ��˳��ر�,ִ����ǰ�ύ������,���ǲ�����������.����Ѿ��رգ������û����������
		exeSrv.shutdown();
		// boolean isShutdown()
		// ThreadPoolExecutor#isShutdown{return runState != RUNNING}
		LOGGER.debug("exeSrv#shutdown.isShutdown:" + exeSrv.isShutdown());
		// isTerminated
		// ThreadPoolExecutor#isTerminated{return runState == TERMINATED}
		// ����رպ������������,�򷵻�true.ע:����Ҫ�ȵ���shutdown/shutdownNow
		// �÷����ɽ��awaitTerminationʹ��awaitTermination,��if(!isTerminated){awaitTermination}
		LOGGER.debug("exeSrv#shutdown.isTerminated:" + exeSrv.isTerminated());

		ExecutorService exeSrv2 = Executors.newFixedThreadPool(2);

		exeSrv2.submit(new OneRunnable(50));
		exeSrv2.submit(new OneCallable(60));

		// List shutdownNow()
		// ��ͼ��ֹ��������ִ�еĻ����.��ͣ�������ڵȴ�������,�����صȴ�ִ�е������б�
		// �޷���֤�ܹ�ֹͣ���ڴ���Ļִ������,���ǻᾡ������.��ͨ��Thread.interrupt���ֵ��͵�ʵ����ȡ��->�����κ������޷���Ӧ�ж϶�������Զ�޷�ֹͣ
		// ��������Կ���50������interrupt��(�쳣��������).��60�ŵ�������ʵҲ��interrupt��,�����쳣���׳������ϲ�.
		exeSrv2.shutdownNow();
		LOGGER.debug("exeSrv2#shutdown.isShutdown:" + exeSrv2.isShutdown());
		LOGGER.debug("exeSrv2#shutdown.isTerminated:" + exeSrv2.isTerminated());

		ExecutorService exeSrv3 = Executors.newFixedThreadPool(2);
		exeSrv3.submit(new OneRunnable(70));
		exeSrv3.submit(new OneRunnable(80));

		exeSrv3.shutdown();
		// boolean awaitTermination(long timeout, TimeUnit unit) throws
		// InterruptedException
		// 1.����ֱ��shutdown���������������� 2.����ֱ����ʱ 3.����ֱ����ǰ�̱߳��ж�
		try {
			if (!exeSrv3.isTerminated()) {
				exeSrv3.awaitTermination(10, TimeUnit.SECONDS);
				// �������.���񻨷���5�뼴ִ�����(���̲߳���).�����̳߳���������������ɺ�,awaitTerminationҲ��������.
				LOGGER.debug("exeSrv3EwaitTermination(10, TimeUnit.SECONDS) end.");
			}

		} catch (InterruptedException e) {
			LOGGER.debug("exeSrv3.awaitTermination(10, TimeUnit.SECONDS) was interrupted.");
		}
	}

	private static class OneRunnable implements Runnable {
		private int taskNum;

		public OneRunnable(int taskNum) {
			this.taskNum = taskNum;
		}

		@Override
		public void run() {
			LOGGER.debug(this + " begin");

			// ��sleepģ��ҵ���߼���ʱ
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				LOGGER.warn("execute" + this + " was interrupt");
			}

			LOGGER.debug(this + " end");
		}

		@Override
		public String toString() {
			return "OneRunnable [taskNum=" + taskNum + "]";
		}
	}

	private static class OneCallable implements Callable<String> {
		private int taskNum;

		public OneCallable(int taskNum) {
			this.taskNum = taskNum;
		}

		@Override
		public String call() throws Exception {
			LOGGER.debug(this + " begin");

			// ��sleepģ��ҵ���߼���ʱ
			Thread.sleep(3 * 1000);

			LOGGER.debug(this + " end");

			return this + " OK";
		}

		@Override
		public String toString() {
			return "OneCallable [taskNum=" + taskNum + "]";
		}
	}
}
