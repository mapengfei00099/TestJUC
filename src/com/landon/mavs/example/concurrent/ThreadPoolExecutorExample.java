package com.landon.mavs.example.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link java.util.concurrent.ThreadPoolExecutor}ʾ��
 * 
 * 
 * 1.public class ThreadPoolExecutor extends AbstractExecutorService
 * 
 * 2.AbstractExecutorService�ڲ����ύ���񷽷�ϵ�����վ�������execute����ִ������.{@link java.util.concurrent.RunnableFuture}
 * 
 * 3.Executors:
 * // 1.corePoolSize��maximumPoolSizeһ��.
 * // 2.keepAliveTime����0L,���ڶ�����Ԫ��ʱ��ֱ�Ӳ��ȴ�ֱ�ӷ���null(���̳߳ش�С���ᳬ��corePoolSize_�޽����������ҡ�1������).
 * ��ò�������getTask������(poolSize > corePoolSize || allowCoreThreadTimeOut)ʱ����,����һ�������Ѿ���false��,�������пյ�ʱ��
 * ��һֱ������(take).����ֻ�����̳߳�������allowCoreThreadTimeOut����ʱ�Ż���е���.����allowCoreThreadTimeOut(boolean value)����
 * ��ʵ����,���(value && keepAliveTime <= 0)���׳��쳣.��allowCoreThreadTimeOut(true)��keepAliveTime<=0��������������ͬʱ����.
 * ������FixedThreadPoolʵ����keepAliveTime������Ч(����Զ�������Worker�߳�).
 * // 3.workQueueΪLinkedBlockingQueue(δָ��capacity),���޽���������.���̳߳ش�С>=corePoolSizeʱ������������.
 * // 4.�ܽ�:FixedThreadPool����������һ�����̳߳��е��߳���Ŀ��Fixed,�̶���,Worker�̲߳��ᱻ�����Ҷ���������ʱ��һֱ����.
 * public static ExecutorService newFixedThreadPool(int nThreads) {
 * return new ThreadPoolExecutor(nThreads, nThreads,
 * 0L, TimeUnit.MILLISECONDS,
 * new LinkedBlockingQueue());
 * }
 * 
 * // ���߳�+�޽�������Ϣ���еľ���ģ��.���̰߳�ȫ����.
 * // ע���䷵�ص��Ƿ�װ��FinalizableDelegatedExecutorService��ʵ����finalize����,��finalize������������̳߳ص�shutdown����.
 * // ͬʱҪ��Ҫǿת������.��ʵ�����Ͳ���ThreadPoolExecutor
 * // ��FinalizableDelegatedExecutorService�̳���DelegatedExecutorService(ί�� /����),��ֻ�ǰ�װ��,����¶��ExecutorService��ʵ�ַ���.
 * // ������Ϊ��Ϊ������ǵ��̵߳�,������ȫû�б�Ҫ��¶ThreadPoolExecutor�����з��ʷ�������¶�˷���������Ϊ����Ҫ���鷳.
 * // ����֮�����ٷ�װһ��finalize�Ͳ�֪Ϊ����.(GC����֮ǰ�ĵ���?��ɶ��Ҫ��?�������û�ж�������shutdown()����ô����ȷ�����ڱ�����ʱ����shutdown()����ֹ�߳�)
 * // ����ֻ���ð�ȫ����������������
 * // landon������������.1.����Ҫԭ��ֻ��Ҫ��¶ExecutorService�ķ���,��Ҫ��¶ThreadPoolExecutor�����з��ʷ���
 * 2.����finalize��ԭ������ThreadPoolExecutor������finalize����,��ʵ��Ϊshutdown.��DelegatedExecutorService������û�е�.
 * ���Զ��������FinalizableDelegatedExecutorService������finalize.��ThreadPoolExecutor��finalize����һ��.
 * public static ExecutorService newSingleThreadExecutor() {
 * return new FinalizableDelegatedExecutorService
 * (new ThreadPoolExecutor(1, 1,
 * 0L, TimeUnit.MILLISECONDS,
 * new LinkedBlockingQueue()));
 * }
 * 
 * // 1.corePoolSizeΪ0,maximumPoolSizeΪInteger.MAX_VALUE.��ִ�������ʱ���ֱ����workQueue.offer����.
 * // 2.workQueueΪSynchronousQueue,��ͬ����������(�ǹ�ƽ),��offer����ǡ�����߳�poll�ſ��Գɹ�.��һ��ִ�������ʱ��,offer�϶�fail.����
 * ->addIfUnderMaximumPoolSize->�����һ��worker�߳�.(��������»�һֱUnderMaximum.��ΪInteger.MAX_VALUE)
 * // 3.keepAliveTimeΪ60��.��poolSizeһ������corePoolSize(Ϊ0)->workQueue.poll(keepAliveTime)->���ӹ�������poll.����˵
 * �����60����������offer��worker�߳�getTask�ɹ���ִ�����񣻷��򷵻�null,����Ϊ��ͬ�����������������ж�workerCanExit��ʱ��(isEmpty��ԶΪtrue),
 * ����worker�̻߳��˳�������.
 * // �ܽ�:1.����̳߳ط�æ�������,ÿ���̶߳���ִ�������ʱ��,��������½��µ�worker�߳�ȥִ������.
 * 2.�������ύ�������ʱ��ǡ�����߳����ڿ���getTask(60s��ʱ��)���ί�п����߳�ȥ��.
 * 3.����̳߳ز���æ,ż����һ������.���һ������ᴴ��һ��workder�߳�,��ʱִ��������1�����ڻ�û����������̻߳ᱻ�Զ�����.�����̳߳���С���߳���Ŀ��ʵ��0.
 * public static ExecutorService newCachedThreadPool() {
 * return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
 * 60L, TimeUnit.SECONDS,
 * new SynchronousQueue());
 * }
 * 
 * 4.RejectPolicy
 * 1.ThreadPoolExecutor�ڲ�Ԥ������4�оܾ��Ĵ���������
 * 2.Reject��ִ��ʱ��:
 * 1.���ύһ������t��ʱ��,�̳߳�����Ŀ������coreSize->�ύ��workQueue.��ʱ����̳߳�״̬������running���̳߳���ͻȻû�����߳�(�п����Ƕ����̵߳������̳߳�
 * ��shutdown/shutdownNow)->ensureQueuedTaskHandled->��������ִ�ʱ״̬����running�ҿ��Դ�workQueue��t�Ƴ�,��ִ�оܾ�����.��shutdown��ʱ��
 * �µ�����ᱻ�ܾ�.->������������ǵ�����shutdown(�̳߳�״̬��SHUTDOWN)->��workQueue��Ϊ����poolSize���worker�߳�->����֤
 * ��������ִ�����.{@link #shutdown}�������жϿ��е��߳�(����coreSize���߳�) {@link getTask} 2.���̳߳��߳���Ŀ����maximumPoolSize��ʱ����ִ�оܾ�����.
 * 3.
 * 1.AbortPolicy:��ֹ����
 * // �����̳߳ص�Ĭ�Ͼܾ�����defaultHandler.->execute�����ĵ����߳���ֱ���׳��쳣
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * throw new RejectedExecutionException();
 * }
 * 
 * 2.CallerRunsPolicy:���������в���
 * // ֱ����execute�����ĵ����߳����б��ܾ�������.����̳߳��ѹر�����������.
 * // ��Ϊ����execute�ĵ����߳������е�.���Կɼ򵥵ļ�����������ύ�ٶ�.���õȵ�ִ���걻�̳߳ؾܾ������������ύ����.
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * if (!e.isShutdown()) {
 * r.run();
 * }
 * }
 * 
 * 3.DiscardOldestPolicy:������ɵ��������
 * // ������ɵ�δ���������(����ͷԪ��),�����ύִ�б��ܾ�������r.����̳߳��ѹر�����������.
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * if (!e.isShutdown()) {
 * e.getQueue().poll();
 * e.execute(r);
 * }
 * }
 * 
 * 4.DiscardPolicy:��������
 * // ��ʵ��,��ֱ�Ӷ������ܾ�������
 * public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
 * }
 * 
 * 5.���ӷ���:
 * 1.protected void beforeExecute(Thread t, Runnable r),�������าд.��ִ�е��߳���������֮ǰ���õķ���.�˷�����t����.
 * ->(��������ʱ,����ͨ��Ӧ�õ���super.beforeExecute->Ƕ�׶����д����)
 * 2.protected void afterExecute(Runnable r, Throwable t) ,�������าд,��ɸ�������������õķ���.�˷�����ִ�������worker�߳�
 * ����.tΪִ�и�����ʱ������ֹʱ���쳣->���쳣�ᱻ�׵��ϲ�runȻ��try.(û��catch).->���tΪnull,���ʾ����ִ��˳��.
 * ע:���ύ������������submit�����ύ��(��FutureTaskʱ)->{@link FutureTask$Sync#innerRun}�����ڲ�������쳣.�����䲻�ᵼ��worker
 * �߳�ͻȻ��ֹ.���쳣Ҳ���ᴫ�ݸ��÷�����
 * ->(������ʼʱ,����ͨ��Ӧ�õ���super.afterExecute->Ƕ�׶����д����)
 * ע:workerDone��������worker�߳̽���ʱ���õķ���->����������
 * 3.protected void terminated,��Ϊ�̳߳���ֹʱ���õķ���{@link #tryTerminate}.����ɸ�д.
 * 
 * 6.public BlockingQueue getQueue(),�÷����������ʹ�������.->���ڼ�غ͵���Ŀ��.->ǿ�ҷ��Գ�������Ŀ�Ķ�ʹ�ô˷���.
 * 
 * 7.public boolean remove(Runnable task),���̳߳ص��ڲ������������Ƴ�������.�������δ��ʼ,���䲻������.ע:����ͨ��submit�����runnable�޷��Ƴ�.
 * ��Ϊ���Ѿ���ת����������ʽ��FutureTask.
 * 
 * 8. public void purge(),���Դӹ��������Ƴ���ȡ����Future����.->ȡ�������񲻻��ٴ�ִ��.�������ǿ����ڹ����������ۻ�.ֱ��worker�߳̽��������Ƴ�(�ӹ�������poll).
 * �÷�������ͼ�Ƴ�����.������������̵߳ĸ�Ԥ->���׳�ConcurrentModificationException.��ʧ��.
 * 
 * 9. ��ʹ�û����ǵ�����shutdown�ر��̳߳�:Ҳϣ��ȷ���ɻ����߳�->����keepAliveTime/allowCoreThreadTimeOut/corePoolSizeΪ0. {@link #getTask()}
 * 
 * 10.public boolean prestartCoreThread() ���������߳�,ʹ�䴦��getTask�Ŀ���״̬. ������������߳�,�򷵻�true
 * ��Դ����:���ڲ�ֱ�ӵ�����addIfUnderCorePoolSize(null).�����coreSize��Ϊ0,�������һ��worker�̲߳�����getTask�ĵȴ�״̬
 * 
 * 11.public int prestartAllCoreThreads() �������к����߳�,ʹ�䴦�ڵȴ�����Ŀ���״̬
 * ��Դ����: while (addIfUnderCorePoolSize(null))->������coreSize������ѭ��. �������������߳���
 * 
 * 12.Worker#isActive
 * // runLock����runTask�����ڵ���.������run����.������߳���getTask�ȴ����е�ʱ����active.ֻ��������ִ�������ʱ����active.
 * boolean isActive() {
 * return runLock.isLocked();
 * }
 * 
 * 13.public void setCorePoolSize(int corePoolSize) ���ú����߳���
 * ��Դ����:1.����coreSizeΪ�������ֵ
 * 2.�����ֵ���ھ�ֵ,�����Ӷ����߳�,�����������߳���Ŀһ�����ᳬ����ǰ�������еĴ�С.
 * 3.�����ֵС�ھ�ֵ,��������ǰ���е�worker�߳�,��������߳̽���interruptIfIdle().���л���һ��������workQueue.remainingCapacity() == 0.
 * Ҳ����˵Ҫ���ʱ�������еĿɸ���Ԫ������Ϊ0,��ǰ������������.
 * (������Ϊ����������������Ϊ�����ʱ������������,���ٴ��ύ�����ʱ�����maximum֮�¼�������̵߳�.Ҳ����˵����ʱ���ж�һ��core�߳���û�������.)
 * ->��������������ʱ�������������߳̽�����һ�ο���ʱ��ֹ(��ΪpoolSize > coreSize).{@link #getTask}
 * 
 * 14.public void setMaximumPoolSize(int maximumPoolSize) �������������߳���
 * ��Դ����:1.����maximumPoolSize����>0 �� >=corePoolSize,�����׳�IllegalArgumentException.
 * 2.������ֵ.
 * 3.�����ֵС�ڵ�ǰֵ�ҵ�ǰpoolSize > maximumPoolSize->�����������߳�,��������߳�interruptIfIdle.
 * 
 * 15.�̳߳��ڵ�����shutdown������㲻�����ύ������,��Ϊ��ʱ���̳߳�״̬�Ѿ�����running��.��������߳���Ϊִ��������쳣��ֹ�Ļ�,ȴ��Ȼ�����ύ����.
 * ��Ϊ��ʱ״̬����running.
 * 
 * 
 * 
 * @author landon
 * 
 */
public class ThreadPoolExecutorExample {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolExecutorExample.class);

	public static void main(String[] args) throws Exception {
		// �̶�2���̵߳��̳߳�
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor1 = new MavsFixedThreadPoolExecutor(2,
				new MavsThreadFactory("Example", "FixedThreadPool-1"), new MavsRejectedExecutionPolicy());
		// ���̳߳ص�״̬����������:��ʱpoolSize=1/workQueueSize=0,��������һ���߳�,��������û������
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());
		// ���̳߳ص�״̬����������:��ʱpoolSize=2/workQueueSize=0,����������һ���߳�
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());

		// ���ύ��3������,���������:poolSizeһֱΪ2.��workQueueSize���Ϊ3->���������ִ��,workQueueSize��Ϊ0
		// ����MavsFixedThreadPoolExecutor����̳߳ػᱣ�̶ֹ��߳�����
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());

		// ִ��shutdown
		// ����������:�����̳߳���ֹ��ʱ�������terminate����
		fixedThreadPoolExecutor1.shutdown();

		Thread.sleep(1 * 1000);

		// ����shutdown��,������ִ��������?
		// �𰸵�Ȼ��NO.��Ϊ�½�worker�̵߳���������������ж���������RUNNING״̬��.
		// ��ִ����shutdown�������������״̬ΪSHUTDOWN
		fixedThreadPoolExecutor1.execute(new ThreadPoolTask());

		Thread.sleep(1 * 1000);
		LOGGER.debug("----------------------------------------------------------");

		// cached�̳߳�
		MavsCachedThreadPoolExecutor cachedThreadPoolExecutor1 = new MavsCachedThreadPoolExecutor(
				new MavsThreadFactory("Example", "CachedThreadPool-1"), new MavsRejectedExecutionPolicy());

		// ��������Կ���,�̳߳����������5���߳�,workQueueSizeһֱΪ0
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());
		cachedThreadPoolExecutor1.execute(new ThreadPoolTask());

		// ��ͣ2����,ʹ��Ĭ�Ͽ���1���ӵ�worker�߳��˳�
		Thread.sleep(2 * 60 * 1000);

		LOGGER.debug("----------------------------------------------------------");

		// ��������Կ���:poolSize=0,�����е�worker�̱߳�������.
		// �������е�worker�̱߳�������,�̳߳ؾͽ�����.
		// ��Ϊ:ThreadPoolExecutor#void workerDone(Worker w)->
		// if (--poolSize ==0)tryTerminate()
		// ���������߳���Ȼ�����Ļ�,��û�е��ø�д��terminate����.��ΪtryTerminate��ʵ�������жϵ�ǰ�̳߳�״̬��STOP/SHUTDOWN��ʱ���ִ��terminated������
		LOGGER.debug("cachedThreadPoolExecutor1.state:{}",
				MavsThreadPoolStateMonitor.monitor(cachedThreadPoolExecutor1));

		// ���߳��̳߳�,ע�������{@link
		// Executors#newSingleThreadExecutor������},���߽��Ƿ��صı�¶��ExecutorService�ӿ�
		MavsFixedThreadPoolExecutor singleExecutor = new MavsFixedThreadPoolExecutor(1, new MavsThreadFactory(
				"Example", "SingleThreadPool-1"), new MavsRejectedExecutionPolicy());
		// �ύһ�����׳��쳣������
		// ���������
		// 1:ִ����afterExecute���������е�Throwable tΪ��null.��ִ�������ʱ���׳����쳣.
		// 2.�߳���Ϊ�쳣��ֹ,��ָ�����߳�Ĭ�ϵ�UncaughtExceptionHandler,����ִ����uncaughtException����.
		singleExecutor.execute(new ThreadPoolExceptionTask());
		Thread.sleep(1 * 1000);
		// ��������Կ���:poolSize=0��Ϊ��0.���߳���ֹ��.
		// ��ΪWorker�̵߳�run����ֻ��try/finally,����û�в����쳣.��runTask�����׳��쳣��run,ֱ�ӵ�finally.->workerDone->poolSize--
		// ->tryTerminate
		LOGGER.debug("singleExecutor.state:{}", MavsThreadPoolStateMonitor.monitor(singleExecutor));

		Thread.sleep(1 * 1000);
		// �����̳߳��쳣��ֹ��,������ִ��������?
		// ����YES.��Ϊ��ʱ���̳߳�״̬��Ȼ��RUNNING.
		singleExecutor.execute(new ThreadPoolTask());
		Thread.sleep(1 * 1000);
		// ���������:poolSize=1,��������һ��worker�߳�.������̵߳�����Mavs-Example-SingleThreadPool-1-2Ҳ�ɿ��ó�.
		LOGGER.debug("singleExecutor.state:{}", MavsThreadPoolStateMonitor.monitor(singleExecutor));

		// �������ύ��һ������,�ڲ��ᱻ��װ��->RunnableFuture->FutureTask
		// �����ڲ�run->Sync#innerRun->���ڲ��ᱻtry/catch��->���������Ͻ��Ӧ���߳�Ӧ�ò����쳣��ֹ.
		// �������:1.afterExecute�����е��쳣����Ϊnull.
		// 2.û�õ���Ĭ�ϵ�UncaughtExceptionHandler.Ҳ����˵�߳���������.
		singleExecutor.submit(new ThreadPoolExceptionTask());

		singleExecutor.shutdown();

		// ����setCoreSize�Լ�setMaximumSize

		// 3���̶��߳���Ŀ���̳߳�
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor2 = new MavsFixedThreadPoolExecutor(3,
				new MavsThreadFactory("Example", "FixedThreadPool-2"), new MavsRejectedExecutionPolicy());

		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());

		// ���ú����̴߳�СΪ6.
		fixedThreadPoolExecutor2.setCorePoolSize(6);

		Thread.sleep(1 * 1000);
		// �������:poolSize=6
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		// ���ú����̴߳�СΪ2
		fixedThreadPoolExecutor2.setCorePoolSize(2);
		Thread.sleep(1 * 1000);
		// �������.poolSize=6
		// ��Ϊ workQueue.remainingCapacity()��ʱ��Ϊ0,�������ж϶���Ŀ����߳�.
		// �����ʱ���е�worker�߳����ڴ��ڵȴ�״̬.
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		Thread.sleep(1 * 1000);
		// ��������Կ���:��ʱpoolSize=5.��Ϊĳ���ȴ��̻߳��ִ�л�����ٴ�getTask��->��ִ��pool(keepAliveTime),��ֱ�ӻ����˳�.
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		// ����ִ��3������
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor2.execute(new ThreadPoolTask());
		Thread.sleep(1 * 1000);
		// ��������Է���:��ʱpoolSize=2,��Ϊ������߳���ִ���������´�getTask�жϵ�ʱ��ֱ�Ӿͱ�������.
		// ����:��ʱmaximumSize��3.coreSizeΪ2.Ҳ����˵��ʱ���̳߳��Ѿ������ǹ̶������̵߳��̳߳���.
		LOGGER.debug("fixedThreadPoolExecutor2.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor2));

		fixedThreadPoolExecutor2.shutdown();

		// ����setMaximumPoolSize
		// 2���̶��߳���Ŀ���̳߳�
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor3 = new MavsFixedThreadPoolExecutor(2,
				new MavsThreadFactory("Example", "FixedThreadPool-3"), new MavsRejectedExecutionPolicy());

		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());

		Thread.sleep(1 * 1000);
		// ��������̳߳ش�СΪ4
		fixedThreadPoolExecutor3.setMaximumPoolSize(4);

		// �ύһϵ������
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());
		fixedThreadPoolExecutor3.execute(new ThreadPoolTask());

		Thread.sleep(1 * 1000);
		// �������:maximumPoolSize=4/poolSize=2
		// ��ֻ���޸���maximumPoolSize��ֵ/poolSize��ȻΪ2.��Ϊ�õ���������������,���Զ�������񶼱��ŵ��˶���.
		LOGGER.debug("fixedThreadPoolExecutor3.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor3));
		try {
			// �����׳���һ���쳣,��Ϊ1��coreSize 2��ҪС
			fixedThreadPoolExecutor3.setMaximumPoolSize(1);
		} catch (Exception e) {
			LOGGER.warn("fixedThreadPoolExecutor3.setMaximumPoolSize.err.", e);
		}

		fixedThreadPoolExecutor3.shutdown();

		// �Զ����̳߳�1
		// ��������Ϊ����3����������
		// �ȴ�����ʱ��Ϊ60s
		ThreadPoolExecutor userDefinedExecutor1 = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(3), new MavsThreadFactory("Example", "User-Define-Executor-1"),
				new MavsRejectedExecutionPolicy());

		// ֱ���ύ�ܶ�����
		// ������Ե�Ŀ�����ڲ��Ծܾ�����.��������Կ���:
		// poolSize=4/workQueueSize=3���ʱ�򣬼��Ѿ��ﵽ������߳���Ŀ�Ͷ�������,��ִ���˾ܾ�����.
		for (int i = 0; i < 20; i++) {
			userDefinedExecutor1.execute(new ThreadPoolTask());
		}

		Thread.sleep(5 * 1000);
		LOGGER.debug("userDefinedExecutor1.state:{}", MavsThreadPoolStateMonitor.monitor(userDefinedExecutor1));
		// ���̳߳�������Ŀ����Ϊ3.��ʱ��poolSizeΪ4.
		userDefinedExecutor1.setMaximumPoolSize(3);
		Thread.sleep(1 * 1000);
		// �������:poolSize����Ϊ4.��Ϊ��ʱ���е�worker�̶߳���poll(timeout)->Ȼ��setMaximumPoolSize->���ж�һ�������߳�->����getTask����
		// ��try/catch��.
		// ����������߳��ڿ��е�ʱ�򶼻ᱻ����.
		LOGGER.debug("userDefinedExecutor1.state:{}", MavsThreadPoolStateMonitor.monitor(userDefinedExecutor1));
		Thread.sleep(5 * 1000);
		LOGGER.debug("userDefinedExecutor1.state:{}", MavsThreadPoolStateMonitor.monitor(userDefinedExecutor1));

		userDefinedExecutor1.shutdown();

		// ����prestartCoreThread()/prestartAllCoreThreads
		MavsFixedThreadPoolExecutor fixedThreadPoolExecutor4 = new MavsFixedThreadPoolExecutor(3,
				new MavsThreadFactory("Example", "FixedThreadPool-4"), new MavsRejectedExecutionPolicy());

		LOGGER.debug("fixedThreadPoolExecutor4.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor4));
		// ����һ�������߳�
		fixedThreadPoolExecutor4.prestartCoreThread();
		// ��������Կ���:poolSizeΪ1����������һ��worker.
		LOGGER.debug("fixedThreadPoolExecutor4.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor4));
		// �������к����߳�
		// ��������Կ���:poolSizeΪ3,���������������еĺ����߳�
		fixedThreadPoolExecutor4.prestartAllCoreThreads();
		LOGGER.debug("fixedThreadPoolExecutor4.state:{}", MavsThreadPoolStateMonitor.monitor(fixedThreadPoolExecutor4));
	}

	/**
	 * 
	 * ���ڲ��Ե��̳߳�����
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
	 * ���ڲ��Ե��̳߳��쳣����
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
