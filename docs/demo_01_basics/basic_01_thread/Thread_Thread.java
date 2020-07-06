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

/**
 * <p>Title: Thread_Thread</p>
 * <p>Description: </p>
 *
 * @author aalx
 * @date 2020/4/14 12:40
 */
public class Thread_Thread extends Thread{

    Thread_Thread(){
        super();
    }

    //重写父类run()
    public void run(){
//        yield();  //让别的线程先执行
        System.out.println(Thread.currentThread().getName());
        super.run();
    }

}
