package com.mapengfei.test.concurency;

/**
 * Created by mapengfei on 15/11/14.
 */
public interface ThreadPool<Job extends Runnable> {
    void execute(Job job);
    void shutdown();
    void addWorkers(int num);
    void removeWorker(int num);
    int getJobSize();
}

