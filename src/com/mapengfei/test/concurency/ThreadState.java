package com.mapengfei.test.concurency;

import java.util.concurrent.TimeUnit;

/**线程状态示例
 * Created by mapengfei on 15/11/14.
 */
public class ThreadState {
    public static void main(String[] args){
        new Thread(new TimeWaiting(),"TimeWaiting ").start();
        new Thread(new Waiting(),"Waiting").start();
        new Thread(new Blocked(),"Block-1").start();
        new Thread(new Blocked(),"Block-2").start();
    }

    static class TimeWaiting implements Runnable{

        @Override
        public void run() {
            while(true){
                SleepUtils.second(100);
            }
        }
    }

    static class Waiting implements Runnable{

        @Override
        public void run() {
            while (true){
                synchronized (Waiting.class){
                    try {
                        Waiting.class.wait();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class Blocked implements Runnable{

        @Override
        public void run() {
            synchronized (Blocked.class){
                while (true){
                    SleepUtils.second(100);
                }
            }
        }
    }

    static class SleepUtils{
        public static final void second(long seconds){
            try {
                TimeUnit.SECONDS.sleep(seconds);
            }catch (InterruptedException e){

            }
        }
    }
}