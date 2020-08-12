package com.aaja.demo;

/**
 * @Description
 * @Author Jun Sj
 * @Date 2020/8/12 14:24
 **/
public class DemoClass {
    public static void main(String[] args) {
        Base base= new Child();//向上转型后无法访问子类特有的属性
        base.methodB();
    }
}

class Base {
    public void method(){
        System.out.print ("Base method");
    }
}
class Child extends Base{
    public void methodB(){
        System.out.print ("Child methodB");
    }
}

