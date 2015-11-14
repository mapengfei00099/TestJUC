package com.mapengfei.test.concurency;

/**
 * Created by mapengfei on 15/11/14.
 */
public class TestMainJoin {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() +" start");
        MyThread thread = new MyThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() +" end");
    }

    static class MyThread extends Thread{


        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() +" start");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() +" end");
        }
    }
}

