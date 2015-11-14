package com.mapengfei.test.concurency;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**java本事就是多线程
 * Created by mapengfei on 15/11/14.
 */
public class MultThread {
    public static void main(String[] args){
        //获取Java线程管理MXBean

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false,false);
        for(ThreadInfo threadInfo:threadInfos){
            System.out.println("[" + threadInfo.getThreadId() + " ] " + threadInfo.getLockName());
        }
    }
}