package com.mapengfei.test.concurency;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mapengfei on 15/11/14.
 */
public class ConnectionTest {
    static ConnectionPool pool = new ConnectionPool(10);
    static CountDownLatch start = new CountDownLatch(1);

    static CountDownLatch end;

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 30;
        end = new CountDownLatch(threadCount);
        int count = 20;
        AtomicInteger got = new AtomicInteger();
        AtomicInteger notGot = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new ConnectionRunner(count, got, notGot), "Thread");
            thread.start();
        }
        start.countDown();
        end.await();
        System.out.println("total invoke:" + (threadCount * count));
        System.out.println("got " + got);
        System.out.println("notGot " + notGot);
    }

    static class ConnectionRunner implements Runnable {
        int count;
        AtomicInteger got;
        AtomicInteger notGot;

        public ConnectionRunner(int count, AtomicInteger got, AtomicInteger notGot) {
            this.count = count;
            this.got = got;
            this.notGot = notGot;
        }

        @Override
        public void run() {
            try {
                start.await();
            } catch (Exception e) {

            }
            while (count > 0) {
                try {
                    Connection connection = pool.fetchConnection(1000);
                    if (connection != null) {
                        try {
                            connection.createStatement();
                            connection.commit();
                        } catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            pool.releaseConnection(connection);
                            got.incrementAndGet();
                        }
                    } else {
                        notGot.incrementAndGet();
                    }
                } catch (Exception ex) {

                } finally {
                    count--;
                }
            }
            end.countDown();
        }
    }
}