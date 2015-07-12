package com.landon.mavs.example.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link java.util.concurrent.ThreadPoolExecutor}示例
 * 
 * 
 * 1.public class ThreadPoolExecutor extends AbstractExecutorService
 * 
 * 2.AbstractExecutorService内部的提交任务方法系列最终均调用了execute方法执行任务.{@link java.util.concurrent.RunnableFuture}
 * 
 * 3.Executors:
 * // 1.corePoolSize和maximumPoolSize一致.
 * // 2.keepAliveTime传入0L,即在队列无元素时则直接不等待直接返回null(因线程池大小不会超过corePoolSize_无界阻塞队列且【1描述】).
 * 因该参数是在getTask方法当(poolSize > corePoolSize || allowCoreThreadTimeOut)时调用,而第一个条件已经是false了,即当队列空的时候
 * 则一直会阻塞(take).所以只有在线程池设置了allowCoreThreadTimeOut参数时才会进行调用.而在allowCoreThreadTimeOut(boolean value)方法
 * 的实现中,如果(value && keepAliveTime <= 0)则抛出异常.即allowCoreThreadTimeOut(true)和keepAliveTime<=0这两个参数不能同时存在.
 * 所以在FixedThreadPool实现中keepAliveTime参数无效(即永远不会回收Worker线程).
 * // 3.workQueue为LinkedBlockingQueue(未指定capacity),即无界阻塞队列.则线程池大小>=corePoolSize时则将任务插入队列.
 * // 4.总结:FixedThreadPool正如其名字一样，线程池中的线程数目是Fixed,固定的,Worker线程不会被回收且队列无任务时则一直阻塞.
 * public static ExecutorService newFixedThreadPool(int nThreads) {
 * return new ThreadPoolExecutor(nThreads, nThreads,
 * 0L, TimeUnit.MILLISECONDS,
 * new LinkedBlockingQueue());
 * }
 * 
 * // 单线程+无界阻塞消息队列的经典模型.无线程安全问题.
 * // 注意其返回的是封装的FinalizableDelegatedExecutorService并实现了finalize方法,而finalize方法则调用了线程池的shutdown方法.
 * // 同时要主要强转的问题.其实际类型不是ThreadPoolExecutor
 * // 而FinalizableDelegatedExecutorService继承了DelegatedExecutorService(委托 /代理),其只是包装了,仅暴露了ExecutorService的实现方法.
 * // 个人认为因为其就是是单线程的,所以完全没有必要暴露ThreadPoolExecutor的所有访问方法，暴露了反而可能因为不必要的麻烦.
 * // 但是之所以再封装一层finalize就不知为何了.(GC回收之前的调用?有啥必要呢?如果我们没有对它调用shutdown()，那么可以确保它在被回收时调用shutdown()来终止线程)
 * // 所以只能用安全网来解释这个设计了
 * // landon：终于明白了.1.其主要原因只是要暴露ExecutorService的方法,不要暴露ThreadPoolExecutor的所有访问方法
 * 2.加上finalize的原因在于ThreadPoolExecutor本身有finalize方法,且实现为shutdown.而DelegatedExecutorService本身是没有的.
 * 所以额外加了在FinalizableDelegatedExecutorService加上了finalize.与ThreadPoolExecutor的finalize保持一致.
 * public static ExecutorService newSingleThreadExecutor() {
 * return new FinalizableDelegatedExecutorService
 * (new ThreadPoolExecutor(1, 1,
 * 0L, TimeUnit.MILLISECONDS,
 * new LinkedBlockingQueue()));
 * }
 * 
 * // 1.corePoolSize为0,maximumPoolSize为Integer.MAX_VALUE.则执行任务的时候会直接向workQueue.offer任务.
 * // 2.workQueue为SynchronousQueue,即同步阻塞队列(非公平),即offer的是恰好有线程poll才可以成功.第一次执行任务的时候,offer肯定fail.所以
 * ->addIfUnderMaximumPoolSize->即添加一个worker线程.(常规情况下会一直UnderMaximum.因为Integer.MAX_VALUE)
 * // 3.keepAliveTime为60秒.而poolSize一定大于corePoolSize(为0)->workQueue.poll(keepAliveTime)->即从工作队列poll.所以说
 * 如果在60秒内有任务offer则worker线程getTask成功则执行任务；否则返回null,又因为是同步阻塞队列所有在判断workerCanExit的时候(isEmpty永远为true),
 * 所以worker线程会退出被回收.
 * // 总结:1.如果线程池繁忙的情况下,每个线程都在执行任务的时候,新任务会新建新的worker线程去执行任务.
 * 2.假如在提交新任务的时候恰好有线程正在空闲getTask(60s超时内)则会委托空闲线程去做.
 * 3.如果线程池不繁忙,偶尔来一个任务.则第一个任务会创建一个workder线程,此时执行完毕如果1分钟内还没有任务则该线程会被自动回收.即该线程池最小的线程数目其实是0.
 * public static ExecutorService newCachedThreadPool() {
 * return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
 * 60L, TimeUnit.SECONDS,
 * new SynchronousQueue());
 * }
 * 
 * 4.RejectPolicy
 * 1.ThreadPoolExecutor内部预定义了4中拒绝的处理程序策略
 * 2.Reject的执行时机:
 * 1.当提交一个任务t的时候,线程池中数目超出了coreSize->提交至workQueue.此时检查线程池状态不在是running或线程池中突然没有了线程(有可能是额外线程调用了线程池
 * 的shutdown/shutdownNow)->ensureQueuedTaskHandled->即如果发现此时状态不是running且可以从workQueue将t移除,则执行拒绝策略.即shutdown的时候
 * 新的任务会被拒绝.->后续如果发现是调用了shutdown(线程池状态是SHUTDOWN)->且workQueue不为空且poolSize添加worker线程->即保证
 * 队列任务执行完毕.{@link #shutdown}方法会中断空闲的线程(超出coreSize的线程) {@link getTask} 2.当线程池线程数目超出maximumPoolSize的时候则执行拒绝策略.
 * 3.
 * 1.AbortPolicy:终止策略
 * // 其是线程池的默认拒绝策略defaultHandler.->execute方法的调用线程则直接抛出异常
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * throw new RejectedExecutionException();
 * }
 * 
 * 2.CallerRunsPolicy:调用者运行策略
 * // 直接在execute方法的调用线程运行被拒绝的任务.如果线程池已关闭则丢弃该任务.
 * // 因为是在execute的调用线程中运行的.所以可简单的减缓新任务的提交速度.即得等到执行完被线程池拒绝的任务后才能提交任务.
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * if (!e.isShutdown()) {
 * r.run();
 * }
 * }
 * 
 * 3.DiscardOldestPolicy:丢弃最旧的任务策略
 * // 放弃最旧的未处理的任务(即队头元素),重新提交执行被拒绝的任务r.如果线程池已关闭则丢弃该任务.
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * if (!e.isShutdown()) {
 * e.getQueue().poll();
 * e.execute(r);
 * }
 * }
 * 
 * 4.DiscardPolicy:丢弃策略
 * // 空实现,即直接丢弃被拒绝的任务
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * }
 * 
 * 5.钩子方法:
 * 1.protected void beforeExecute(Thread t, Runnable r),可在子类覆写.在执行的线程运行任务之前调用的方法.此方法由t调用.
 * ->(方法结束时,子类通常应该调用super.beforeExecute->嵌套多个重写操作)
 * 2.protected void afterExecute(Runnable r, Throwable t) ,可在子类覆写,完成给定任务后所调用的方法.此方法由执行任务的worker线程
 * 调用.t为执行该任务时导致终止时的异常->该异常会被抛到上层run然后被try.(没有catch).->如果t为null,则表示任务执行顺利.
 * 注:当提交的任务类似于submit方法提交的(如FutureTask时)->{@link FutureTask$Sync#innerRun}会在内部捕获该异常.所以其不会导致worker
 * 线程突然终止.而异常也不会传递给该方法。
 * ->(方法开始时,子类通常应该调用super.afterExecute->嵌套多个重写操作)
 * 注:workerDone方法是在worker线程结束时调用的方法->完成任务计数
 * 3.protected void terminated,此为线程池终止时调用的方法{@link #tryTerminate}.子类可覆写.
 * 
 * 6.public BlockingQueue getQueue(),该方法用来访问工作队列.->用于监控和调试目的.->强烈反对出于其他目的而使用此方法.
 * 
 * 7.public boolean remove(Runnable task),从线程池的内部工作队列中移除此任务.如果其尚未开始,则其不再运行.注:对于通过submit输入的runnable无法移除.
 * 因为其已经被转换了其他形式如FutureTask.
 * 
 * 8. public void purge(),尝试从工作队列移除已取消的Future任务.->取消的任务不会再次执行.但是他们可能在工作队列中累积.直到worker线程将其主动移除(从工作队列poll).
 * 该方法则试图移除他们.如果出现其他线程的干预->则抛出ConcurrentModificationException.则失败.
 * 
 * 9. 即使用户忘记调用了shutdown关闭线程池:也希望确保可回收线程->设置keepAliveTime/allowCoreThreadTimeOut/corePoolSize为0. {@link #getTask()}
 * 
 * 10.public boolean prestartCoreThread() 启动核心线程,使其处于getTask的空闲状态. 如果已启动了线程,则返回true
 * 从源码上:其内部直接调用了addIfUnderCorePoolSize(null).既如果coreSize不为0,则会启动一个worker线程并处于getTask的等待状态
 * 
 * 11.public int prestartAllCoreThreads() 启动所有核心线程,使其处于等待任务的空闲状态
 * 从源码上: while (addIfUnderCorePoolSize(null))->即超出coreSize则跳出循环. 返回已启动的线程数
 * 
 * 12.Worker#isActive
 * // runLock是在runTask方法内的锁.而不是run的锁.即如果线程在getTask等待空闲的时候不是active.只有在真正执行任务的时候是active.
 * boolean isActive() {
 * return runLock.isLocked();
 * }
 * 
 * 13.public void setCorePoolSize(int corePoolSize) 设置核心线程数
 * 从源码上:1.设置coreSize为传入的新值
 * 2.如果新值大于旧值,则会添加额外线程,但是启动的线程数目一定不会超过当前工作队列的大小.
 * 3.如果新值小于旧值,则会遍历当前所有的worker线程,将多出的线程进行interruptIfIdle().其中还有一个条件是workQueue.remainingCapacity() == 0.
 * 也就是说要求此时工作队列的可附加元素数量为0,则当前工作队列已满.
 * (个人认为这个条件的添加是因为如果此时工作队列已满,则再次提交任务的时候会在maximum之下继续添加线程的.也就是说在这时候中断一个core线程是没有问题的.)
 * ->如果条件不满足的时候则多余的现有线程将在下一次空闲时终止(因为poolSize > coreSize).{@link #getTask}
 * 
 * 14.public void setMaximumPoolSize(int maximumPoolSize) 设置运行最大的线程数
 * 从源码上:1.参数maximumPoolSize必须>0 且 >=corePoolSize,否则抛出IllegalArgumentException.
 * 2.设置新值.
 * 3.如果新值小于当前值且当前poolSize > maximumPoolSize->则会遍历工作线程,将多余的线程interruptIfIdle.
 * 
 * 15.线程池在调用了shutdown方法后便不能在提交任务了,因为此时的线程池状态已经不是running了.但是如果线程因为执行任务而异常终止的话,却依然可以提交任务.
 * 因为此时状态还是running.
 * 
 * 
 * 
 * @author landon
 * 
 */
public class ThreadPoolExecutorExample {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolExecutorExample.class);

	public static void main(String[] args) throws Exception {
		// 固定2个线程的线程池
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor1 = new MavsFixedThreadPoolExecutor(2,
				new MavsThreadFactory("Example", "FixedThreadPool-1"), new MavsRejectedExecutionPolicy());
		// 从线程池的状态监视器来看:此时poolSize=1/workQueueSize=0,即启动了一个线程,工作队列没有任务
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());
		// 从线程池的状态监视器来看:此时poolSize=2/workQueueSize=0,即又启动了一个线程
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());

		// 由提交了3个任务,从输出来看:poolSize一直为2.而workQueueSize最多为3->随着任务的执行,workQueueSize变为0
		// 所以MavsFixedThreadPoolExecutor这个线程池会保持固定线程数量
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());

		// 执行shutdown
		// 另外从输出看:发现线程池终止的时候调用了terminate方法
		fixedThreadPoolExecutor1.shutdown();

		Thread.sleep(1 * 1000);

		// 测试shutdown后,还可以执行任务吗?
		// 答案当然是NO.因为新建worker线程的条件包括插入队列都必须是在RUNNING状态下.
		// 而执行了shutdown后则更改了运行状态为SHUTDOWN
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());

		Thread.sleep(1 * 1000);
		LOGGER.debug("----------------------------------------------------------");

		// cached线程池
		MavsCachedThreadPoolExecutor cachedThreadPoolExecutor1 = new MavsCachedThreadPoolExecutor(
				new MavsThreadFactory("Example", "CachedThreadPool-1"), new MavsRejectedExecutionPolicy());

		// 从输出可以看到,线程池最多启动了5个线程,workQueueSize一直为0
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());

		// 暂停2分钟,使得默认空闲1分钟的worker线程退出
		Thread.sleep(2 * 60 * 1000);

		LOGGER.debug("----------------------------------------------------------");

		// 从输出可以看到:poolSize=0,即空闲的worker线程被回收了.
		// 另外所有的worker线程被回收了,线程池就结束了.
		// 因为:ThreadPoolExecutor#void workerDone(Worker w)->
		// if (--poolSize ==0)tryTerminate()
		// 但是这种线程自然结束的话,并没有调用覆写的terminate方法.因为tryTerminate的实现中是判断当前线程池状态是STOP/SHUTDOWN的时候才执行terminated方法的
		LOGGER.debug("cachedThreadPoolExecutor1.state:{}",
				MavsThreadPoolStateMonitor.monitor(cachedThreadPoolExecutor1));

		// 单线程线程池,注意这个和{@link
		// Executors#newSingleThreadExecutor的区别},后者仅是返回的暴露的ExecutorService接口
		MavsFixedThreadPoolExecutor singleExecutor = new MavsFixedThreadPoolExecutor(1, new MavsThreadFactory(
				"Example", "SingleThreadPool-1"), new MavsRejectedExecutionPolicy());
		// 提交一个可抛出异常的任务
		// 从输出看出
		// 1:执行了afterExecute方法且其中的Throwable t为不null.此执行任务的时候抛出了异常.
		// 2.线程因为异常终止,因指定了线程默认的UncaughtExceptionHandler,所以执行了uncaughtException方法.
		singleExecutor.execute(new ThreadPoolExceptionTask());
		Thread.sleep(1 * 1000);
		// 从输出可以看到:poolSize=0变为了0.即线程终止了.
		// 因为Worker线程的run方法只是try/finally,即并没有捕获异常.而runTask向上抛出异常至run,直接到finally.->workerDone->poolSize--
		// ->tryTerminate
		LOGGER.debug("singleExecutor.state:{}", MavsThreadPoolStateMonitor.monitor(singleExecutor));

		Thread.sleep(1 * 1000);
		// 测试线程池异常终止后,还可以执行任务吗?
		// 答案是YES.因为此时的线程池状态依然是RUNNING.
		singleExecutor.execute(new ThreadPoolTask());
		Thread.sleep(1 * 1000);
		// 从输出发现:poolSize=1,即新增了一个worker线程.另外从线程的名字Mavs-Example-SingleThreadPool-1-2也可看得出.
		LOGGER.debug("singleExecutor.state:{}", MavsThreadPoolStateMonitor.monitor(singleExecutor));

		// 这里是提交了一个任务,内部会被封装成->RunnableFuture->FutureTask
		// 而其内部run->Sync#innerRun->其内部会被try/catch的->所以理论上结果应该线程应该不会异常终止.
		// 从输出看:1.afterExecute方法中的异常参数为null.
		// 2.没用调用默认的UncaughtExceptionHandler.也就是说线程正常运行.
		singleExecutor.submit(new ThreadPoolExceptionTask());

		singleExecutor.shutdown();

		// 测试setCoreSize以及setMaximumSize

		// 3个固定线程数目的线程池
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor2 = new MavsFixedThreadPoolExecutor(3,
				new MavsThreadFactory("Example", "FixedThreadPool-2"), new MavsRejectedExecutionPolicy());

		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());

		// 设置核心线程大小为6.
		fixedThreadPoolExecutor2.setCorePoolSize(6);

		Thread.sleep(1 * 1000);
		// 从输出看:poolSize=6
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		// 设置核心线程大小为2
		fixedThreadPoolExecutor2.setCorePoolSize(2);
		Thread.sleep(1 * 1000);
		// 从输出看.poolSize=6
		// 因为 workQueue.remainingCapacity()此时不为0,即不会中断多余的空闲线程.
		// 另外此时所有的worker线程正在处在等待状态.
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		Thread.sleep(1 * 1000);
		// 从输出可以看到:此时poolSize=5.因为某个等待线程获得执行机会后再次getTask后->会执行pool(keepAliveTime),则直接回收退出.
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		// 继续执行3个任务
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		Thread.sleep(1 * 1000);
		// 从输出可以发现:此时poolSize=2,因为多余的线程在执行完任务下次getTask判断的时候直接就被回收了.
		// 另外:此时maximumSize是3.coreSize为2.也就是说此时的线程池已经不再是固定数量线程的线程池了.
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		fixedThreadPoolExecutor2.shutdown();

		// 测试setMaximumPoolSize
		// 2个固定线程数目的线程池
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor3 = new MavsFixedThreadPoolExecutor(2,
				new MavsThreadFactory("Example", "FixedThreadPool-3"), new MavsRejectedExecutionPolicy());

		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());

		Thread.sleep(1 * 1000);
		// 设置最大线程池大小为4
		fixedThreadPoolExecutor3.setMaximumPoolSize(4);

		// 提交一系列任务
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());

		Thread.sleep(1 * 1000);
		// 从输出看:maximumPoolSize=4/poolSize=2
		// 即只是修改了maximumPoolSize的值/poolSize仍然为2.因为用的是无限阻塞队列,所以多余的任务都被放到了队列.
		LOGGER.debug("fixedThreadPoolExecutor3.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor3));
		try {
			// 这里抛出了一个异常,因为1比coreSize 2还要小
			fixedThreadPoolExecutor3.setMaximumPoolSize(1);
		} catch (Exception e) {
			LOGGER.warn("fixedThreadPoolExecutor3.setMaximumPoolSize.err.", e);
		}

		fixedThreadPoolExecutor3.shutdown();

		// 自定义线程池1
		// 工作队列为容量3的阻塞队列
		// 等待空闲时间为60s
		ThreadPoolExecutor userDefinedExecutor1 = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(3), new MavsThreadFactory("Example", "User-Define-Executor-1"),
				new MavsRejectedExecutionPolicy());

		// 直接提交很多任务
		// 这个测试的目的在于测试拒绝策略.从输出可以看到:
		// poolSize=4/workQueueSize=3这个时候，即已经达到了最大线程数目和队列上限,则执行了拒绝策略.
		for (int i = 0; i < 20; i++) {
			userDefinedExecutor1.execute(new ThreadPoolTask());
		}

		Thread.sleep(5 * 1000);
		LOGGER.debug("userDefinedExecutor1.state:{}", MavsThreadPoolStateMonitor.monitor(userDefinedExecutor1));
		// 将线程池最大池数目调整为3.此时的poolSize为4.
		userDefinedExecutor1.setMaximumPoolSize(3);
		Thread.sleep(1 * 1000);
		// 从输出看:poolSize还是为4.因为此时所有的worker线程都在poll(timeout)->然后setMaximumPoolSize->会中断一个空闲线程->但是getTask这里
		// 被try/catch了.
		// 不过多余的线程在空闲的时候都会被回收.
		LOGGER.debug("userDefinedExecutor1.state:{}", MavsThreadPoolStateMonitor.monitor(userDefinedExecutor1));
		Thread.sleep(5 * 1000);
		LOGGER.debug("userDefinedExecutor1.state:{}", MavsThreadPoolStateMonitor.monitor(userDefinedExecutor1));

		userDefinedExecutor1.shutdown();

		// 测试prestartCoreThread()/prestartAllCoreThreads
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor4 = new MavsFixedThreadPoolExecutor(3,
				new MavsThreadFactory("Example", "FixedThreadPool-4"), new MavsRejectedExecutionPolicy());

		LOGGER.debug("fixedThreadPoolExecutor4.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor4));
		// 启动一个核心线程
		fixedThreadPoolExecutor4.prestartCoreThread();
		// 从输出可以看出:poolSize为1，即启动了一个worker.
		LOGGER.debug("fixedThreadPoolExecutor4.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor4));
		// 启动所有核心线程
		// 从输出可以看出:poolSize为3,即现在启动了所有的核心线程
		fixedThreadPoolExecutor4.prestartAllCoreThreads();
		LOGGER.debug("fixedThreadPoolExecutor4.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor4));
	}

	/**
	 * 
	 * 用于测试的线程池任务
	 * 
	 * @author landon
	 * 
	 */
	private static class ThreadPoolTask implements Runnable {
		private static final AtomicInteger COUNTER = new AtomicInteger(1);

		private int id;

		public ThreadPoolTask() {
			id = COUNTER.getAndIncrement();
		}

		@Override
		public void run() {
			LOGGER.debug(this + " begin");

			try {
				TimeUnit.MICROSECONDS.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.warn(this + " was interrupted", e);
			}

			LOGGER.debug(this + " end");
		}

		@Override
		public String toString() {
			return "ThreadPoolTask [id=" + id + "]" + "[" + Thread.currentThread().getName() + "]";
		}
	}

	/**
	 * 
	 * 用于测试的线程池异常任务
	 * 
	 * @author landon
	 * 
	 */
	private static class ThreadPoolExceptionTask implements Runnable {
		private static final AtomicInteger COUNTER = new AtomicInteger(1);

		private int id;

		public ThreadPoolExceptionTask() {
			id = COUNTER.getAndIncrement();
		}

		@Override
		public void run() {
			LOGGER.debug(this + " begin");

			throw new RuntimeException("ThreadPoolExceptionTask.Exception.");
		}

		@Override
		public String toString() {
			return "ThreadPoolExceptionTask [id=" + id + "]" + "[" + Thread.currentThread().getName() + "]";
		}
	}
}
