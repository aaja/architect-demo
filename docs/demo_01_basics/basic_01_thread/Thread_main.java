/*
 * Copyright (C) <2020>  <aalx crystal>
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.aalx.mt_01_concept.c01_thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * <p>Title: Thread_main</p>
 * <p>Description: </p>
 * 总结：
 *      什么是线程？
 *          线程是程序里面不同的执行路径
 *      线程的三种基本实现？
 *          extends Thread/ implements Runnable/Callable
 *      常用方法
 *          join() yield() sleep() interrupted() isAlive()
 *      线程的状态
 *          新建 - new完进入新建状态
 *          就绪 - 调用start()之后
 *          运行 - 获得cpu以后执行run()
 *          阻塞 - 很多阻塞方法(sleep())，让出cpu
 *          死亡 - 两种(1.run结束正常退出；2.未捕获的异常中断了run())
 *      实现Runnable接口相比继承Thread类有如下优势：
 *          1、可以避免由于Java的单继承特性而带来的局限；
 *          2、增强程序的健壮性，代码能够被多个线程共享，代码与数据是独立的；
 *          3、适合多个相同程序代码的线程区处理同一资源的情况。
 *      实现Runnable接口和实现Callable接口的区别:
 *          1、Runnable是自从java1.1就有了，而Callable是1.5之后才加上去的
 *          2、Callable规定的方法是call(),Runnable规定的方法是run()
 *          3、Callable的任务执行后可返回值，而Runnable的任务是不能返回值(是void)
 *          4、call方法可以抛出异常，run方法不可以
 *          5、运行Callable任务可以拿到一个Future对象，表示异步计算的结果。它提供了检查计算是否完成的方法，以等待计算的完成，并检索计算的结果。通过Future对象可以了解任务执行情况，可取消任务的执行，还可获取执行结果。
 *          6、加入线程池运行，Runnable使用ExecutorService的execute方法，Callable使用submit方法。
 *
 * @author aalx
 * @date 2020/4/15 1:01
 */
public class Thread_main {

    public static void main(String[] args) {

        // 继承thread类
        Thread_Thread tt = new Thread_Thread();
        tt.setName("Thread extends from Thread is called A"); //set thread name
        tt.setDaemon(false); //守护线程的优先级比较低，用于为系统中的其它对象和线程提供服务。垃圾回收是经典的实现
        tt.setPriority(10); //线程优先级有10个等级，分别用整数1-10表示。其中1位最低优先级，10为最高优先级，5为默认值。
//        tt.setContextClassLoader();
//        tt.setUncaughtExceptionHandler();

//        try {
//            tt.join(); //线程合并
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        Thread.interrupted(); //结束线程，不是最优的结束方式

//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        tt.isAlive(); //判断线程的状态 阻塞和运行状态返回true，其他状态返回false
        tt.start();

        // 实现Runnable接口
        Thread_Runnable tr = new Thread_Runnable();
        Thread tr_ = new Thread(tr);
        tr_.setName("Thread interface from Runnable.class is called B");
        tr_.start();

        // 实现Callable接口
        Callable<Integer> tc = new Thread_Callable(); //创建callable对象
        FutureTask<Integer> ft = new FutureTask<>(tc); //使用FutureTask包装FutureTask对象
        Thread tc_ = new Thread(ft); //FutureTask对象作为Thread对象的target创建新的线程
        tr_.setName("This thread interface from Callable.class is called C");
        tc_.start();
        try {
            //取得新创建的新线程中的call()方法返回的结果
            System.out.printf("futureTask包装的线程执行结果是：%d\n", ft.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        new Thread(()->{
            // do some thing ...
        }).start();
    }
}
