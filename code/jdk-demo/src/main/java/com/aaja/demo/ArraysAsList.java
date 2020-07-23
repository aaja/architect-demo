package com.aaja.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Title: ArraysAsList</p>
 * <p>Description: ${}</p>
 * Arrays.ArrayList 是工具类 Arrays 的一个内部静态类，它没有完全实现List的方法，而 ArrayList直接
 *                  实现了List 接口，实现了List所有方法。
 *  1、长度不同 和 实现的方法不同
 *      Arrays.ArrayList是一个定长集合，因为它没有重写add,remove方法，所以一旦初始化元素后，集合的
 *      size就是不可变的。
 *  2、参数赋值方式不同
 *      Arrays.ArrayList将外部数组的引用直接通过“=”赋予内部的泛型数组，所以本质指向同一个数组。
 *      ArrayList是将其他集合转为数组后copy到自己内部的数组的。底层使用的数组的克隆或者System.arrayCopy()
 * @author aaja
 * @date 2020/7/23 16:24
 */
public class ArraysAsList {

    public static void main(String[] args) {
        Error1();
//        Error2();
//        Error3();
//        Right1();
//        RightForJava8();
//        TestFourMethodForTransfer();
    }

    /**
     * 错误示范1
     * 由于Arrays.ArrayList参数为可变长泛型，而基本类型是无法泛型化的，所以它把int[] arr数组
     * 当成了一个泛型对象，所以集合中最终只有一个元素arr。
     */
    public static void Error1(){
        int[] arr = {1, 2, 3};
        List ints = Arrays.asList(arr);
        System.out.println(ints.size());
        System.out.println(Arrays.toString(arr)); //jdk1.5出来的方法
    }

    /**
     * 由于asList产生的集合元素是直接引用作为参数的数组，所以当外部数组或集合改变时，数组和集合
     * 会同步变化，这在平时我们编码时可能产生莫名的问题。
     */
    public static void Error2(){
        String[] arr = {"张三", "李四", "test"};
        List ints = Arrays.asList(arr);
        arr[1] = "爱上";
        ints.set(2, "我");
        System.out.println(Arrays.toString(arr));
        System.out.println(ints.toString());
    }

    /**
     * 由于asList产生的集合并没有重写add,remove等方法，所以它会调用父类AbstractList的方法，而父
     * 类的方法中抛出的却是异常信息。
     * Exception in thread "main" java.lang.UnsupportedOperationException
     */
    public static void Error3(){
        String[] arr = {"张三", "李四", "test"};
        List ints = Arrays.asList(arr);
        ints.add("小明");
        ints.remove("张三");
        System.out.println(ints.toString());
    }

    /**
     * 支持基础类型的方式 - String
     */
    public static void Right1(){
        int[] arr = {1, 2, 3};
//        List list = CollectionUtils.arrayToList(arr); //Spring框架的工具类

    }

    /**
     * 支持基础类型的方式 - java8
     */
    public static void RightForJava8(){
        int[] arr = {1, 2, 3};
        Arrays.stream(arr)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * @Description 数组转ArrayList
     * 1、遍历转换
     * 2、使用工具类
     * 3、使用java8的lamada表达式
     * 4、两个集合类结合
     */
    public static void TestFourMethodForTransfer(){
        int[] arr = {1, 2, 3};
        // 方法1
        ArrayList<Integer> list1 = new ArrayList();
        for(int rr : arr){
            list1.add(rr);
        }
        // 方法2
        String[] strr = {"张三", "李四", "小明"};
        ArrayList<String> list2 = new ArrayList();
        Collections.addAll(list2, strr);
        // 方法3
        List<Integer> collect = Arrays.stream(arr)
                .boxed()
                .collect(Collectors.toList());
        System.out.println(collect.toString());
        // 方法4
        ArrayList<String> arrList = new ArrayList<String>(Arrays.asList(strr));
        System.out.println(arrList.toString());

    }
}
