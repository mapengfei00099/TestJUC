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
		// execute(Runnable command) 执行一个Runnable
		exeSrv.execute(new OneRunnable(1));

		// Future submit(Runnable task) 提交一个Runable
		Future oneRunFuture = exeSrv.submit(new OneRunnable(2));
		// Future#isDone 返回任务是否结束
		LOGGER.debug("oneRun is complete:" + oneRunFuture.isDone());
		try {
			// 等待计算完成,返回计算结果
			// 当前成功完成的时候 #get 返回null
			LOGGER.debug("oneRun result:" + oneRunFuture.get());
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneRun#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneRun#get compuation throws a exception");
		}

		// Future submit(Callable task) 提交一个Callable
		Future<String> oneCallFuture = exeSrv.submit(new OneCallable(1));

		try {
			// V get() throws InterruptedException, ExecutionException
			// 等待计算完成,返回计算结果
			LOGGER.debug("oneCall result:" + oneCallFuture.get());
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneCall#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneCall#get compuation throws a exception");
		}

		Future<String> oneCallFuture2 = exeSrv.submit(new OneCallable(2));

		try {
			// V get(long timeout, TimeUnit unit) 指定等待超时时间
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
		// 尝试取消任务的执行.如果任务已完成或者已经被取消或者因为其他一些原因被能取消则尝试会失败
		// 如果尝试成功且任务还未开始则该任务再也不会运行.如果任务已经启动,mayInterruptIfRunning参数决定任务执行线程是否中断尝试结束任务
		boolean isFuture3CancelSuccess = oneCallFuture3.cancel(false);
		LOGGER.debug("oneCallFuture3#cancel(false) result:" + isFuture3CancelSuccess);
		LOGGER.debug("oneCallFuture3#isDone:" + oneCallFuture3.isDone());
		LOGGER.debug("oneCallFuture3#isCancelled:" + oneCallFuture3.isCancelled());

		Future<String> oneCallFuture4 = exeSrv.submit(new OneCallable(4));
		// 主线程暂停2秒后执行cancel
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {

		}
		// 此处cancel传true则表明如果任务已启动则中断执行任务线程尝试结束任务
		// 从输出可以看到,即输出了任务开始,但是却没有输出任务结束->且返回true表明任务被中断取消
		boolean isFuture4CancelSuccess = oneCallFuture4.cancel(true);
		LOGGER.debug("oneCallFuture4#cancel(true) result:" + isFuture4CancelSuccess);
		LOGGER.debug("oneCallFuture4#isDone:" + oneCallFuture4.isDone());
		LOGGER.debug("oneCallFuture4#isCancelled:" + oneCallFuture4.isCancelled());

		Future<String> oneCallFuture5 = exeSrv.submit(new OneCallable(5));
		// 主线程暂停8秒后执行cancel,此时任务有可能已经执行完毕
		try {
			TimeUnit.SECONDS.sleep(8);
		} catch (InterruptedException e) {

		}
		// 从输入可以看到,任务5输出了end.即cancel时任务已经完成.所以isFuture5CancelSuccess为false.isDone为true.isCancelled为false
		boolean isFuture5CancelSuccess = oneCallFuture5.cancel(true);
		LOGGER.debug("oneCallFuture5#cancel(true) result:" + isFuture5CancelSuccess);
		LOGGER.debug("oneCallFuture5#isDone:" + oneCallFuture5.isDone());
		LOGGER.debug("oneCallFuture5#isCancelled:" + oneCallFuture5.isCancelled());

		// Future submit(Runnable task, T result) 当任务完成时get方法会返回指定的result
		Future<String> oneRun3Future = exeSrv.submit(new OneRunnable(3), "isOk");
		try {
			// 从输入可以看到get方法的返回是传入的"isOk"
			LOGGER.debug("oneRun3 result:" + oneRun3Future.get());
		} catch (InterruptedException e) {
			LOGGER.warn("exception_oneRun3l#get is interrupted while waiting result.");
		} catch (ExecutionException e) {
			LOGGER.warn("exception_oneRun3#get compuation throws a exception");
		}

		// 任务集合
		List<OneCallable> oneCallList = Arrays.asList(new OneCallable(10), new OneCallable(11), new OneCallable(12));
		try {
			// List> invokeAll(Collection>
			// tasks) throws InterruptedException;
			// 相当于批量执行任务.从方法的异常列表可以看出.此方法会等待(即阻塞)直到所有任务完成
			List<Future<String>> oneCallListFutures = exeSrv.invokeAll(oneCallList);

			// 处理完成结果 从输出可以看到->invokeAll确实是在等待所有任务执行完毕.
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

		// 任务集合2
		List<OneCallable> oneCallList2 = Arrays.asList(new OneCallable(20), new OneCallable(21), new OneCallable(22));
		try {
			// T invokeAny(Collection> tasks) throws
			// InterruptedException, ExecutionException;
			// 批量执行任务->等待直到某个任务已成功完成(注意只要某个任务成功返回则返回结果) 另外注意返回结果是T,而非Future
			String oneCallList2Result = exeSrv.invokeAny(oneCallList2);
			// 从输出结果可以看到:
			// [oneCallList2Result:OneCallable [taskNum=20]OK],即20号任务执行完成即返回了
			LOGGER.debug("oneCallList2Result:" + oneCallList2Result);
		} catch (InterruptedException e) {
			LOGGER.warn("exeSrv#invokeAny(oneCallList2) exception_waiting one task complete was interrupted.");
		} catch (ExecutionException e) {
			LOGGER.warn("exeSrv#invokeAll(oneCallList2) exception_one any one task was completed.");
		}

		// 任务集合3
		List<OneCallable> oneCallList3 = Arrays.asList(new OneCallable(30), new OneCallable(31), new OneCallable(32));
		try {
			// List> invokeAll(Collection>
			// tasks, long timeout, TimeUnit unit) throws InterruptedException;
			// 批量执行任务->指定等待超时时间->注意该方法并不会抛出超时异常,即如果没有被打断的情况下，超时后(直接返回),则某些任务只是未完成而已(注返回后会取消尚未完成的任务)
			List<Future<String>> oneCallList3Futures = exeSrv.invokeAll(oneCallList3, 2, TimeUnit.SECONDS);

			List<Boolean> oneCallList3Results = new ArrayList<Boolean>();
			for (Future<String> future : oneCallList3Futures) {
				if (future.isDone()) {
					oneCallList3Results.add(true);
				} else {
					oneCallList3Results.add(false);
				}
			}

			// 从输出看,这个方法很特殊.即返回的Future列表的isDone方法都返回true.且所有的任务没有输出结束end.
			// 从API看,即当所有任务完成或者超时(无论哪个首先发生)则返回的Future列表的isDone方法返回true
			// 一旦返回后,即取消尚未完成的任务
			LOGGER.debug("oneCallList3Results:" + oneCallList3Results);
		} catch (InterruptedException e) {
			LOGGER.warn("exeSrv#invokeAll(oneCallList3, 2, TimeUnit.SECONDS) exception_waiting all task compelte was interrupted.");
		}

		// 任务集合4
		List<OneCallable> oneCallList4 = Arrays.asList(new OneCallable(40), new OneCallable(41), new OneCallable(42));
		try {
			// T invokeAny(Collection> tasks,long
			// timeout, TimeUnit unit) throws InterruptedException,
			// ExecutionException, TimeoutException;
			// 批量执行任务->指定等待超时时间->注意这个方法抛出了TimeoutException->即在等待超时后会抛出异常
			// 从输出结果可以看到,在等待超时后->尚未完成的任务都被取消了,因为输出只有begin没有end
			exeSrv.invokeAny(oneCallList4, 1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.debug("exeSrv#invokeAny(oneCallList4, 1, TimeUnit.SECONDS) exception_waiting any one task complete was interrupted.");
		} catch (ExecutionException e) {
			LOGGER.debug("exeSrv#invokeAny(oneCallList4, 1, TimeUnit.SECONDS) exception_no any one task was completed");
		} catch (TimeoutException e) {
			LOGGER.debug("exeSrv#invokeAny(oneCallList4, 1, TimeUnit.SECONDS) exception_waiting timeout");
		}

		// void shutdown() 启动一次顺序关闭,执行以前提交的任务,但是不接受新任务.如果已经关闭，则调用没有其他作用
		exeSrv.shutdown();
		// boolean isShutdown()
		// ThreadPoolExecutor#isShutdown{return runState != RUNNING}
		LOGGER.debug("exeSrv#shutdown.isShutdown:" + exeSrv.isShutdown());
		// isTerminated
		// ThreadPoolExecutor#isTerminated{return runState == TERMINATED}
		// 如果关闭后所有任务都完成,则返回true.注:必须要先调用shutdown/shutdownNow
		// 该方法可结合awaitTermination使用awaitTermination,即if(!isTerminated){awaitTermination}
		LOGGER.debug("exeSrv#shutdown.isTerminated:" + exeSrv.isTerminated());

		ExecutorService exeSrv2 = Executors.newFixedThreadPool(2);

		exeSrv2.submit(new OneRunnable(50));
		exeSrv2.submit(new OneCallable(60));

		// List shutdownNow()
		// 试图终止所有正在执行的活动任务.暂停处理正在等待的任务,并返回等待执行的任务列表
		// 无法保证能够停止正在处理的活动执行任务,但是会尽力尝试.如通过Thread.interrupt这种典型的实现来取消->所以任何任务无法响应中断都可能永远无法停止
		// 从输出可以看到50号任务被interrupt了(异常被捕获了).而60号的任务其实也被interrupt了,但是异常被抛出到了上层.
		exeSrv2.shutdownNow();
		LOGGER.debug("exeSrv2#shutdown.isShutdown:" + exeSrv2.isShutdown());
		LOGGER.debug("exeSrv2#shutdown.isTerminated:" + exeSrv2.isTerminated());

		ExecutorService exeSrv3 = Executors.newFixedThreadPool(2);
		exeSrv3.submit(new OneRunnable(70));
		exeSrv3.submit(new OneRunnable(80));

		exeSrv3.shutdown();
		// boolean awaitTermination(long timeout, TimeUnit unit) throws
		// InterruptedException
		// 1.阻塞直到shutdown请求后，所有任务完成 2.阻塞直到超时 3.阻塞直到当前线程被中断
		try {
			if (!exeSrv3.isTerminated()) {
				exeSrv3.awaitTermination(10, TimeUnit.SECONDS);
				// 从输出看.任务花费了5秒即执行完毕(多线程并行).所以线程池所有任务任务完成后,awaitTermination也不在阻塞.
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

			// 用sleep模拟业务逻辑耗时
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

			// 用sleep模拟业务逻辑耗时
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
