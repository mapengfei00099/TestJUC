package com.mapengfei.test.concurency;

import java.sql.Connection;
import java.util.LinkedList;

/**
 * 基于等待模式的线程池
 */
public class ConnectionPool {
    private LinkedList<Connection> pool = new LinkedList<Connection>();

    public ConnectionPool(int initialSize){
        if(initialSize > 0){
            for(int i=1;i<initialSize;i++){
                pool.addLast(ConnectionDrive.createConnection());
            }
        }
    }

    public void releaseConnection(Connection connection){
        if(connection != null){
            synchronized (pool){
                pool.addLast(connection);
                pool.notifyAll();
            }
        }
    }

    public Connection fetchConnection(long mills) throws InterruptedException {
        synchronized (pool){
            if(mills <= 0){
                while (pool.isEmpty()){
                    pool.wait();
                }
                return pool.removeFirst();
            }else{
                long future = System.currentTimeMillis() + mills;
                long remainning = mills;
                while(pool.isEmpty() && remainning > 0){
                    pool.wait();
                    remainning = future - System.currentTimeMillis();
                }
                Connection result = null;
                if(! pool.isEmpty()){
                    result = pool.removeFirst();
                }
                return result;
            }
        }
    }
}

