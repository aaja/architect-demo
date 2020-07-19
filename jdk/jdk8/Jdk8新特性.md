# Java8新特性总览

# 1.介绍
毫无疑问，Java 8发行版是自Java 5（发行于2004，已经过了相当一段时间了）以来最具革命性的版本。Java 8 为Java语言、编译器、类库、开发工具与JVM（Java虚拟机）带来了大量新特性。在这篇教程中，我们将一一探索这些变化，并用真实的例子说明它们适用的场景。
这篇教程由以下几部分组成，它们分别涉及到Java平台某一特定方面的内容：
Java语言
编译器
类库
工具
Java运行时（JVM）
# 2.Java语言新特性
## 2.1 Lambda表达式
Lambda表达式（也称为闭包）是整个Java 8发行版中最受期待的在Java语言层面上的改变，Lambda允许把函数作为一个方法的参数（函数作为参数传递进方法中），或者把代码看成数据：函数式程序员对这一概念非常熟悉。在JVM平台上的很多语言（Groovy，Scala，……）从一开始就有Lambda，但是Java程序员不得不使用毫无新意的匿名类来代替lambda。
关于Lambda设计的讨论占用了大量的时间与社区的努力。可喜的是，最终找到了一个平衡点，使得可以使用一种即简洁又紧凑的新方式来构造Lambdas。在最简单的形式中，一个lambda可以由用逗号分隔的参数列表、–>符号与函数体三部分表示。例如：
```java
Arrays.asList( "a", "b", "d" ).forEach( e -> System.out.println( e ) );
```
请注意参数e的类型是由编译器推测出来的。同时，你也可以通过把参数类型与参数包括在括号中的形式直接给出参数的类型：
```java
Arrays.asList( "a", "b", "d" ).forEach( ( String e ) -> System.out.println( e ) );
```
在某些情况下lambda的函数体会更加复杂，这时可以把函数体放到在一对花括号中，就像在Java中定义普通函数一样。例如：
```java
Arrays.asList( "a", "b", "d" ).forEach( e -> {
    System.out.print( e );
    System.out.print( e );
} );
```
Lambda可以引用类的成员变量与局部变量（如果这些变量不是final的话，它们会被隐含的转为final，这样效率更高）。例如，下面两个代码片段是等价的：
```java
String separator = ",";
Arrays.asList( "a", "b", "d" ).forEach( 
   ( String e ) -> System.out.print( e + separator ) );
```
和：
```java
final String separator = ",";
Arrays.asList( "a", "b", "d" ).forEach( 
    ( String e ) -> System.out.print( e + separator ) );
```
Lambda可能会返回一个值。返回值的类型也是由编译器推测出来的。如果lambda的函数体只有一行的话，那么没有必要显式使用return语句。下面两个代码片段是等价的：
```java
Arrays.asList( "a", "b", "d" ).sort( ( e1, e2 ) -> e1.compareTo( e2 ) );
```
和：
```java
Arrays.asList( "a", "b", "d" ).sort( ( e1, e2 ) -> {
    int result = e1.compareTo( e2 );
    return result;
} );
```
语言设计者投入了大量精力来思考如何使现有的函数友好地支持lambda。最终采取的方法是：增加函数式接口的概念。函数式接口就是一个具有一个方法的普通接口。像这样的接口，可以被隐式转换为lambda表达式。java.lang.Runnable与java.util.concurrent.Callable是函数式接口最典型的两个例子。在实际使用过程中，函数式接口是容易出错的：如有某个人在接口定义中增加了另一个方法，这时，这个接口就不再是函数式的了，并且编译过程也会失败。为了克服函数式接口的这种脆弱性并且能够明确声明接口作为函数式接口的意图，Java 8增加了一种特殊的注解@FunctionalInterface（Java 8中所有类库的已有接口都添加了@FunctionalInterface注解）。让我们看一下这种函数式接口的定义：
```java
@FunctionalInterface
public interface Functional {
    void method();
}
```
需要记住的一件事是：默认方法与静态方法并不影响函数式接口的契约，可以任意使用：
```java
@FunctionalInterface
public interface FunctionalDefaultMethods {
    void method();
         
    default void defaultMethod() {            
    }        
}
```
Lambda是Java 8最大的卖点。它具有吸引越来越多程序员到Java平台上的潜力，并且能够在纯Java语言环境中提供一种优雅的方式来支持函数式编程。更多详情可以参考[官方文档](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)。
一下是一个字符串排序的例子，test1是没用lamada表达式的写法，test2是用lamada表达式的写法
```java
package lamada;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LamadaTest {

    @Test
    public void test1() {
        List names = Arrays.asList("peter", "anna", "mike", "xenia");

        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
            	return b.compareTo(a);
            }
        });
        System.out.println(Arrays.toString(names.toArray()));
    }

    @Test
    public void test2() {
        List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");
        Collections.sort(names, (String a, String b) -> {
        	return b.compareTo(a);
        });

        Collections.sort(names, (String a, String b) -> b.compareTo(a));

        Collections.sort(names, (a, b) -> b.compareTo(a));
        System.out.println(Arrays.toString(names.toArray()));
    }

	static void add(double a,String b) {
        System.out.println(a + b);
    }
    
    @Test
    public void test5() {
    	D d = (a,b) -> add(a,b);
    //        interface D {
    //            void get(int i,String j);
    //        }
    //        // 这里要求，add的两个参数和get的两个参数吻合并且返回类型也要相等，否则报错
    //        static void add(double a,String b) {
    //            System.out.println(a + b);
    //        }
    }

    @FunctionalInterface
    interface D {
    	void get(int i,String j);
    }

}
```
## 2.2 接口的默认方法与静态方法 
Java 8用默认方法与静态方法这两个新概念来扩展接口的声明。默认方法使接口有点像Traits（Scala中特征(trait)类似于Java中的Interface，但它可以包含实现代码，也就是目前Java8新增的功能），但与传统的接口又有些不一样，它允许在已有的接口中添加新方法，而同时又保持了与旧版本代码的兼容性。
默认方法与抽象方法不同之处在于抽象方法必须要求实现，但是默认方法则没有这个要求。相反，每个接口都必须提供一个所谓的默认实现，这样所有的接口实现者将会默认继承它（如果有必要的话，可以覆盖这个默认实现）。让我们看看下面的例子：
```java
private interface Defaulable {
    // Interfaces now allow default methods, the implementer may or 
    // may not implement (override) them.
    default String notRequired() { 
        return "Default implementation"; 
    }        
}
         
private static class DefaultableImpl implements Defaulable {
}
     
private static class OverridableImpl implements Defaulable {
    @Override
    public String notRequired() {
        return "Overridden implementation";
    }
}
```
Defaulable接口用关键字default声明了一个默认方法notRequired()，Defaulable接口的实现者之一DefaultableImpl实现了这个接口，并且让默认方法保持原样。Defaulable接口的另一个实现者OverridableImpl用自己的方法覆盖了默认方法。
Java 8带来的另一个有趣的特性是接口可以声明（并且可以提供实现）静态方法。例如：
```java
private interface DefaulableFactory {
    // Interfaces now allow static methods
    static Defaulable create( Supplier< Defaulable > supplier ) {
        return supplier.get();
    }
}
```
下面的一小段代码片段把上面的默认方法与静态方法黏合到一起。
```java
public static void main( String[] args ) {
    Defaulable defaulable = DefaulableFactory.create( DefaultableImpl::new );
    System.out.println( defaulable.notRequired() );
         
    defaulable = DefaulableFactory.create( OverridableImpl::new );
    System.out.println( defaulable.notRequired() );
}
```
这个程序的控制台输出如下：
```java
Default implementation
Overridden implementation
```
在JVM中，默认方法的实现是非常高效的，并且通过字节码指令为方法调用提供了支持。默认方法允许继续使用现有的Java接口，而同时能够保障正常的编译过程。这方面好的例子是大量的方法被添加到java.util.Collection接口中去：stream()，parallelStream()，forEach()，removeIf()，……
尽管默认方法非常强大，但是在使用默认方法时我们需要小心注意一个地方：在声明一个默认方法前，请仔细思考是不是真的有必要使用默认方法，因为默认方法会带给程序歧义，并且在复杂的继承体系中容易产生编译错误。更多详情请参考[官方文档](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)。
## 2.3 方法引用
方法引用提供了非常有用的语法，可以直接引用已有Java类或对象（实例）的方法或构造器。与lambda联合使用，方法引用可以使语言的构造更紧凑简洁，减少冗余代码。
下面，我们以定义了4个方法的Car这个类作为例子，区分Java中支持的4种不同的方法引用。
```java
public static class Car {
    public static Car create( final Supplier< Car > supplier ) {
        return supplier.get();
    }              
         
    public static void collide( final Car car ) {
        System.out.println( "Collided " + car.toString() );
    }
         
    public void follow( final Car another ) {
        System.out.println( "Following the " + another.toString() );
    }
         
    public void repair() {   
        System.out.println( "Repaired " + this.toString() );
    }
}
```
第一种方法引用是构造器引用，它的语法是Class::new，或者更一般的Class< T >::new。请注意构造器没有参数。
```java
final Car car = Car.create( Car::new );
final List< Car > cars = Arrays.asList( car );
```
第二种方法引用是静态方法引用，它的语法是Class::static_method。请注意这个方法接受一个Car类型的参数。
```java
cars.forEach( Car::collide );
```
第三种方法引用是特定类的任意对象的方法引用，它的语法是Class::method。请注意，这个方法没有参数。
```java
cars.forEach( Car::repair );
```
最后，第四种方法引用是特定对象的方法引用，它的语法是instance::method。请注意，这个方法接受一个Car类型的参数
```java
1. final Car police = Car.create( Car::new );
2. cars.forEach( police::follow );
```
运行上面的Java程序在控制台上会有下面的输出（Car的实例可能不一样）：
```java
1. Collided com.javacodegeeks.java8.method.references.MethodReferences$Car@7a81197d
2. Repaired com.javacodegeeks.java8.method.references.MethodReferences$Car@7a81197d
3. Following the com.javacodegeeks.java8.method.references.MethodReferences$Car@7a81197d
```
关于方法引用的更多详情请参考[官方文档](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html)。
## 2.4 重复注解
自从Java 5引入了注解机制，这一特性就变得非常流行并且广为使用。然而，使用注解的一个限制是相同的注解在同一位置只能声明一次，不能声明多次。Java 8打破了这条规则，引入了重复注解机制，这样相同的注解可以在同一地方声明多次。
重复注解机制本身必须用@Repeatable注解。事实上，这并不是语言层面上的改变，更多的是编译器的技巧，底层的原理保持不变。让我们看一个快速入门的例子：
```java
package com.javacodegeeks.java8.repeatable.annotations;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
public class RepeatingAnnotations {
    @Target( ElementType.TYPE )
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Filters {
        Filter[] value();
    }
     
    @Target( ElementType.TYPE )
    @Retention( RetentionPolicy.RUNTIME )
    @Repeatable( Filters.class )
    public @interface Filter {
        String value();
    };
     
    @Filter( "filter1" )
    @Filter( "filter2" )
    public interface Filterable {        
    }
     
    public static void main(String[] args) {
        for( Filter filter: Filterable.class.getAnnotationsByType( Filter.class ) ) {
            System.out.println( filter.value() );
        }
    }
}
```
正如我们看到的，这里有个使用@Repeatable( Filters.class )注解的注解类Filter，Filters仅仅是Filter注解的数组，但Java编译器并不想让程序员意识到Filters的存在。这样，接口Filterable就拥有了两次Filter（并没有提到Filter）注解。
同时，反射相关的API提供了新的函数getAnnotationsByType()来返回重复注解的类型（请注意Filterable.class.getAnnotation( Filters.class )经编译器处理后将会返回Filters的实例）。程序输出结果如下：
```java
filter1
filter2
```
更多详情请参考官方文档
## 2.5 更好的类型推测机制
Java 8在类型推测方面有了很大的提高。在很多情况下，编译器可以推测出确定的参数类型，这样就能使代码更整洁。让我们看一个例子：
```java
package com.javacodegeeks.java8.type.inference;
 
public class Value< T > {
    public static< T > T defaultValue() { 
        return null; 
    }
     
    public T getOrDefault( T value, T defaultValue ) {
        return ( value != null ) ? value : defaultValue;
    }
}
```
这里是Value< String >类型的用法。
```java
package com.javacodegeeks.java8.type.inference;
 
public class TypeInference {
    public static void main(String[] args) {
        final Value< String > value = new Value<>();
        value.getOrDefault( "22", Value.defaultValue() );
    }
}
```
Value.defaultValue()的参数类型可以被推测出，所以就不必明确给出。在Java 7中，相同的例子将不会通过编译，正确的书写方式是 Value.< String >defaultValue()。
## 2.6 扩展注解的支持
Java 8扩展了注解的上下文。现在几乎可以为任何东西添加注解：局部变量、泛型类、父类与接口的实现，就连方法的异常也能添加注解。下面演示几个例子：
```java
package com.javacodegeeks.java8.annotations;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
 
public class Annotations {
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.TYPE_USE, ElementType.TYPE_PARAMETER } )
    public @interface NonEmpty {        
    }
         
    public static class Holder< @NonEmpty T > extends @NonEmpty Object {
        public void method() throws @NonEmpty Exception {           
        }
    }
         
    @SuppressWarnings( "unused" )
    public static void main(String[] args) {
        final Holder< String > holder = new @NonEmpty Holder< String >();       
        @NonEmpty Collection< @NonEmpty String > strings = new ArrayList<>();       
    }
}
```
ElementType.TYPE_USE和ElementType.TYPE_PARAMETER是两个新添加的用于描述适当的注解上下文的元素类型。在Java语言中，注解处理API也有小的改动来识别新增的类型注解。
# 3. Java编译器的新特性
## 3.1 参数名字
很长一段时间里，Java程序员一直在发明不同的方式使得方法参数的名字能保留在Java字节码中，并且能够在运行时获取它们（比如，Paranamer类库）。最终，在Java 8中把这个强烈要求的功能添加到语言层面（通过反射API与Parameter.getName()方法）与字节码文件（通过新版的javac的–parameters选项）中。
```java
package com.javacodegeeks.java8.parameter.names;
 
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
 
public class ParameterNames {
    public static void main(String[] args) throws Exception {
        Method method = ParameterNames.class.getMethod( "main", String[].class );
        for( final Parameter parameter: method.getParameters() ) {
            System.out.println( "Parameter: " + parameter.getName() );
        }
    }
}
```
如果不使用–parameters参数来编译这个类，然后运行这个类，会得到下面的输出：
```java
Parameter: arg0
```
如果使用–parameters参数来编译这个类，程序的结构会有所不同（参数的真实名字将会显示出来）：
```
Parameter: args
```
对于有经验的Maven用户，通过maven-compiler-plugin的配置可以将-parameters参数添加到编译器中去。
```java
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.1</version>
    <configuration>
        <compilerArgument>-parameters</compilerArgument>
        <source>1.8</source>
        <target>1.8</target>
    </configuration>
</plugin>
```
针对Java 8最新发布的Eclipse Kepler SR2（请检查这里的下载说明）提供了非常实用的配置选项，可以通过下图的配置方式来控制编译器行为图1. 配置Eclipse工程使之支持Java 8编译器的新特性——parameters参数
此外，Parameter类有一个很方便的方法isNamePresent()来验证是否可以获取参数的名字。
# 4. Java 类库的新特性
Java 8 通过增加大量新类，扩展已有类的功能的方式来改善对并发编程、函数式编程、日期/时间相关操作以及其他更多方面的支持。
## 4.1 Optional
到目前为止，臭名昭著的空指针异常是导致Java应用程序失败的最常见原因。以前，为了解决空指针异常，Google公司著名的Guava项目引入了Optional类，Guava通过使用检查空值的方式来防止代码污染，它鼓励程序员写更干净的代码。受到Google Guava的启发，Optional类已经成为Java 8类库的一部分。
Optional实际上是个容器：它可以保存类型T的值，或者仅仅保存null。Optional提供很多有用的方法，这样我们就不用显式进行空值检测。更多详情请参考官方文档。
我们下面用两个小例子来演示如何使用Optional类：一个允许为空值，一个不允许为空值。
```java
Optional< String > fullName = Optional.ofNullable( null );
System.out.println( "Full Name is set? " + fullName.isPresent() );        
System.out.println( "Full Name: " + fullName.orElseGet( () -> "[none]" ) ); 
System.out.println( fullName.map( s -> "Hey " + s + "!" ).orElse( "Hey Stranger!" ) );
```
如果Optional类的实例为非空值的话，isPresent()返回true，否从返回false。为了防止Optional为空值，orElseGet()方法通过回调函数来产生一个默认值。map()函数对当前Optional的值进行转化，然后返回一个新的Optional实例。orElse()方法和orElseGet()方法类似，但是orElse接受一个默认值而不是一个回调函数。下面是这个程序的输出：
```java
Full Name is set? false
Full Name: [none]
Hey Stranger!
```
让我们来看看另一个例子：
```java
Optional< String > firstName = Optional.of( "Tom" );
System.out.println( "First Name is set? " + firstName.isPresent() );        
System.out.println( "First Name: " + firstName.orElseGet( () -> "[none]" ) ); 
System.out.println( firstName.map( s -> "Hey " + s + "!" ).orElse( "Hey Stranger!" ) );
System.out.println();
```
下面是程序的输出：
```java
First Name is set? true
First Name: Tom
Hey Tom!
```
更多详情请参考官方文档
## 4.2 Stream
最新添加的Stream API（java.util.stream） 把真正的函数式编程风格引入到Java中。这是目前为止对Java类库最好的补充，因为Stream API可以极大提供Java程序员的生产力，让程序员写出高效率、干净、简洁的代码。
Stream API极大简化了集合框架的处理（但它的处理的范围不仅仅限于集合框架的处理，这点后面我们会看到）。让我们以一个简单的Task类为例进行介绍：
```java
public class Streams  {
    private enum Status {
        OPEN, CLOSED
    };
     
    private static final class Task {
        private final Status status;
        private final Integer points;
 
        Task( final Status status, final Integer points ) {
            this.status = status;
            this.points = points;
        }
         
        public Integer getPoints() {
            return points;
        }
         
        public Status getStatus() {
            return status;
        }
         
        @Override
        public String toString() {
            return String.format( "[%s, %d]", status, points );
        }
    }
}
```
Task类有一个分数的概念（或者说是伪复杂度），其次是还有一个值可以为OPEN或CLOSED的状态.让我们引入一个Task的小集合作为演示例子：
```java
final Collection< Task > tasks = Arrays.asList(
    new Task( Status.OPEN, 5 ),
    new Task( Status.OPEN, 13 ),
    new Task( Status.CLOSED, 8 ) 
);
```
我们下面要讨论的第一个问题是所有状态为OPEN的任务一共有多少分数？在Java 8以前，一般的解决方式用foreach循环，但是在Java 8里面我们可以使用stream：一串支持连续、并行聚集操作的元素。
```java
// Calculate total points of all active tasks using sum()
final long totalPointsOfOpenTasks = tasks
    .stream()
    .filter( task -> task.getStatus() == Status.OPEN )
    .mapToInt( Task::getPoints )
    .sum();

System.out.println( "Total points: " + totalPointsOfOpenTasks );
```
程序在控制台上的输出如下：
```java
Total points: 18
```
这里有几个注意事项。第一，task集合被转换化为其相应的stream表示。然后，filter操作过滤掉状态为CLOSED的task。下一步，mapToInt操作通过Task::getPoints这种方式调用每个task实例的getPoints方法把Task的stream转化为Integer的stream。最后，用sum函数把所有的分数加起来，得到最终的结果。
在继续讲解下面的例子之前，关于stream有一些需要注意的地方（详情在这里）.stream操作被分成了中间操作与最终操作这两种。
中间操作返回一个新的stream对象。中间操作总是采用惰性求值方式，运行一个像filter这样的中间操作实际上没有进行任何过滤，相反它在遍历元素时会产生了一个新的stream对象，这个新的stream对象包含原始stream
中符合给定谓词的所有元素。
像forEach、sum这样的最终操作可能直接遍历stream，产生一个结果或副作用。当最终操作执行结束之后，stream管道被认为已经被消耗了，没有可能再被使用了。在大多数情况下，最终操作都是采用及早求值方式，及早完成底层数据源的遍历。
stream另一个有价值的地方是能够原生支持并行处理。让我们来看看这个算task分数和的例子。
```java
// Calculate total points of all tasks
final double totalPoints = tasks
   .stream()
   .parallel()
   .map( task -> task.getPoints() ) // or map( Task::getPoints ) 
   .reduce( 0, Integer::sum );
     
System.out.println( "Total points (all tasks): " + totalPoints );
```
这个例子和第一个例子很相似，但这个例子的不同之处在于这个程序是并行运行的，其次使用reduce方法来算最终的结果。
下面是这个例子在控制台的输出：
```java
Total points (all tasks): 26.0
```
经常会有这个一个需求：我们需要按照某种准则来对集合中的元素进行分组。Stream也可以处理这样的需求，下面是一个例子：
```java
/ Group tasks by their status
final Map< Status, List< Task > > map = tasks
    .stream()
    .collect( Collectors.groupingBy( Task::getStatus ) );
System.out.println( map );
```
这个例子的控制台输出如下：
```java
{CLOSED=[[CLOSED, 8]], OPEN=[[OPEN, 5], [OPEN, 13]]}
```
让我们来计算整个集合中每个task分数（或权重）的平均值来结束task的例子。
```java
// Calculate the weight of each tasks (as percent of total points) 
final Collection< String > result = tasks
    .stream()                                        // Stream< String >
    .mapToInt( Task::getPoints )                     // IntStream
    .asLongStream()                                  // LongStream
    .mapToDouble( points -> points / totalPoints )   // DoubleStream
    .boxed()                                         // Stream< Double >
    .mapToLong( weigth -> ( long )( weigth * 100 ) ) // LongStream
    .mapToObj( percentage -> percentage + "%" )      // Stream< String> 
    .collect( Collectors.toList() );                 // List< String > 
         
System.out.println( result );
```
下面是这个例子的控制台输出：
```java
[19%, 50%, 30%]
```
最后，就像前面提到的，Stream API不仅仅处理Java集合框架。像从文本文件中逐行读取数据这样典型的I/O操作也很适合用Stream API来处理。下面用一个例子来应证这一点。
```java
final Path path = new File( filename ).toPath();
try( Stream< String > lines = Files.lines( path, StandardCharsets.UTF_8 ) ) {
    lines.onClose( () -> System.out.println("Done!") ).forEach( System.out::println );
}
```
对一个stream对象调用onClose方法会返回一个在原有功能基础上新增了关闭功能的stream对象，当对stream对象调用close()方法时，与关闭相关的处理器就会执行。
Stream API、Lambda表达式与方法引用在接口默认方法与静态方法的配合下是Java 8对现代软件开发范式的回应。更多详情请参考官方文档。
## 4.3 Date/Time API (JSR 310)
Java 8通过发布新的Date-Time API (JSR 310)来进一步加强对日期与时间的处理。对日期与时间的操作一直是Java程序员最痛苦的地方之一。标准的 java.util.Date以及后来的java.util.Calendar一点没有改善这种情况（可以这么说，它们一定程度上更加复杂）。
这种情况直接导致了Joda-Time——一个可替换标准日期/时间处理且功能非常强大的Java API的诞生。Java 8新的Date-Time API (JSR 310)在很大程度上受到Joda-Time的影响，并且吸取了其精髓。新的java.time包涵盖了所有处理日期，时间，日期/时间，时区，时刻（instants），过程（during）与时钟（clock）的操作。在设计新版API时，十分注重与旧版API的兼容性：不允许有任何的改变（从java.util.Calendar中得到的深刻教训）。如果需要修改，会返回这个类的一个新实例。
让我们用例子来看一下新版API主要类的使用方法。第一个是Clock类，它通过指定一个时区，然后就可以获取到当前的时刻，日期与时间。Clock可以替换System.currentTimeMillis()与TimeZone.getDefault()。
```java
// Get the system clock as UTC offset 
final Clock clock = Clock.systemUTC();
System.out.println( clock.instant() );
System.out.println( clock.millis() );
```
下面是程序在控制台上的输出：
```java
2014-04-12T15:19:29.282Z
1397315969360
```
我们需要关注的其他类是LocaleDate与LocalTime。LocaleDate只持有ISO-8601格式且无时区信息的日期部分。相应的，LocaleTime只持有ISO-8601格式且无时区信息的时间部分。LocaleDate与LocalTime都可以从Clock中得到。
```java
// Get the local date and local time
final LocalDate date = LocalDate.now();
final LocalDate dateFromClock = LocalDate.now( clock );
         
System.out.println( date );
System.out.println( dateFromClock );
         
// Get the local date and local time
final LocalTime time = LocalTime.now();
final LocalTime timeFromClock = LocalTime.now( clock );
         
System.out.println( time );
System.out.println( timeFromClock );
```
下面是程序在控制台上的输出：
```java
2014-04-12
2014-04-12
11:25:54.568
15:25:54.568
```
LocaleDateTime把LocaleDate与LocaleTime的功能合并起来，它持有的是ISO-8601格式无时区信息的日期与时间。下面是一个快速入门的例子。
```java
// Get the local date/time
final LocalDateTime datetime = LocalDateTime.now();
final LocalDateTime datetimeFromClock = LocalDateTime.now( clock );        
System.out.println( datetime );
System.out.println( datetimeFromClock );
```
下面是程序在控制台上的输出：
```java
2014-04-12T11:37:52.309
2014-04-12T15:37:52.309
```
如果你需要特定时区的日期/时间，那么ZonedDateTime是你的选择。它持有ISO-8601格式具具有时区信息的日期与时间。下面是一些不同时区的例子：
```java
// Get the zoned date/time
final ZonedDateTime zonedDatetime = ZonedDateTime.now();
final ZonedDateTime zonedDatetimeFromClock = ZonedDateTime.now( clock );
final ZonedDateTime zonedDatetimeFromZone = ZonedDateTime.now( ZoneId.of( "America/Los_Angeles" ) );
         
System.out.println( zonedDatetime );
System.out.println( zonedDatetimeFromClock );
System.out.println( zonedDatetimeFromZone );
```
下面是程序在控制台上的输出：
```java
2014-04-12T11:47:01.017-04:00[America/New_York]
2014-04-12T15:47:01.017Z
2014-04-12T08:47:01.017-07:00[America/Los_Angeles]
```
最后，让我们看一下Duration类：在秒与纳秒级别上的一段时间。Duration使计算两个日期间的不同变的十分简单。下面让我们看一个这方面的例子。
```java
// Get duration between two dates
final LocalDateTime from = LocalDateTime.of( 2014, Month.APRIL, 16, 0, 0, 0 );
final LocalDateTime to = LocalDateTime.of( 2015, Month.APRIL, 16, 23, 59, 59 );
 
final Duration duration = Duration.between( from, to );
System.out.println( "Duration in days: " + duration.toDays() );
System.out.println( "Duration in hours: " + duration.toHours() );
```
上面的例子计算了两个日期2014年4月16号与2014年4月16号之间的过程。下面是程序在控制台上的输出：
```java
Duration in days: 365
Duration in hours: 8783
```
对Java 8在日期/时间API的改进整体印象是非常非常好的。一部分原因是因为它建立在“久战杀场”的Joda-Time基础上，另一方面是因为用来大量的时间来设计它，并且这次程序员的声音得到了认可。更多详情请参考官方文档。
## 4.4 JavaScript引擎Nashorn
Nashorn，一个新的JavaScript引擎随着Java 8一起公诸于世，它允许在JVM上开发运行某些JavaScript应用。Nashorn就是javax.script.ScriptEngine的另一种实现，并且它们俩遵循相同的规则，允许Java与JavaScript相互调用。下面看一个例子：
```java
ScriptEngineManager manager = new ScriptEngineManager();
ScriptEngine engine = manager.getEngineByName( "JavaScript" );
         
System.out.println( engine.getClass().getName() );
System.out.println( "Result:" + engine.eval( "function f() { return 1; }; f() + 1;" ) );
```
下面是程序在控制台上的输出：
```java
jdk.nashorn.api.scripting.NashornScriptEngine
Result: 2
```
我们在后面的Java新工具章节会再次谈到Nashorn。
## 4.5 Base64
在Java 8中，Base64编码已经成为Java类库的标准。它的使用十分简单，下面让我们看一个例子：
```java
package com.javacodegeeks.java8.base64;
 
import java.nio.charset.StandardCharsets;
import java.util.Base64;
 
public class Base64s {
    public static void main(String[] args) {
        final String text = "Base64 finally in Java 8!";
         
        final String encoded = Base64
            .getEncoder()
            .encodeToString( text.getBytes( StandardCharsets.UTF_8 ) );
        System.out.println( encoded );
         
        final String decoded = new String( 
            Base64.getDecoder().decode( encoded ),
            StandardCharsets.UTF_8 );
        System.out.println( decoded );
    }
}
```
程序在控制台上输出了编码后的字符与解码后的字符：
```java
QmFzZTY0IGZpbmFsbHkgaW4gSmF2YSA4IQ==
Base64 finally in Java 8!
```
Base64类同时还提供了对URL、MIME友好的编码器与解码器（Base64.getUrlEncoder() / Base64.getUrlDecoder(), Base64.getMimeEncoder() / Base64.getMimeDecoder()）。
## 4.6 并行（parallel）数组
Java 8增加了大量的新方法来对数组进行并行处理。可以说，最重要的是parallelSort()方法，因为它可以在多核机器上极大提高数组排序的速度。下面的例子展示了新方法（parallelXxx）的使用。
```java
package com.javacodegeeks.java8.parallel.arrays;
 
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
 
public class ParallelArrays {
    public static void main( String[] args ) {
        long[] arrayOfLong = new long [ 20000 ];        
         
        Arrays.parallelSetAll( arrayOfLong, 
            index -> ThreadLocalRandom.current().nextInt( 1000000 ) );
        Arrays.stream( arrayOfLong ).limit( 10 ).forEach( 
            i -> System.out.print( i + " " ) );
        System.out.println();
         
        Arrays.parallelSort( arrayOfLong );     
        Arrays.stream( arrayOfLong ).limit( 10 ).forEach( 
            i -> System.out.print( i + " " ) );
        System.out.println();
    }
}
```
上面的代码片段使用了parallelSetAll()方法来对一个有20000个元素的数组进行随机赋值。然后，调用parallelSort方法。这个程序首先打印出前10个元素的值，之后对整个数组排序。这个程序在控制台上的输出如下（请注意数组元素是随机生产的）：
```java
Unsorted: 591217 891976 443951 424479 766825 351964 242997 642839 119108 552378 
Sorted: 39 220 263 268 325 607 655 678 723 793
```
## 4.7 并发（Concurrency）
在新增Stream机制与lambda的基础之上，在java.util.concurrent.ConcurrentHashMap中加入了一些新方法来支持聚集操作。同时也在java.util.concurrent.ForkJoinPool类中加入了一些新方法来支持共有资源池（common pool）
新增的java.util.concurrent.locks.StampedLock类提供一直基于容量的锁，这种锁有三个模型来控制读写操作（它被认为是不太有名的java.util.concurrent.locks.ReadWriteLock类的替代者）。
在java.util.concurrent.atomic包中还增加了下面这些类：
DoubleAccumulator
DoubleAdder
LongAccumulator
LongAdder
# 5. 新的Java工具
Java 8也带来了一些新的命令行工具。在这节里我们将会介绍它们中最有趣的部分。
## 5.1 Nashorn引擎: jjs
jjs是个基于Nashorn引擎的命令行工具。它接受一些JavaScript源代码为参数，并且执行这些源代码。例如，我们创建一个具有如下内容的func.js文件：
```java
function f() { 
     return 1; 
}; 
 
print( f() + 1 );
```
我们可以把这个文件作为参数传递给jjs使得这个文件可以在命令行中执行：
jjs func.js
下面是程序在控制台上的输出：
更多详情请参考官方文档
## 5.2 类依赖分析器jdeps
jdeps是一个很有用的命令行工具。它可以显示Java类的包级别或类级别的依赖。它接受一个.class文件，一个目录，或者一个jar文件作为输入。jdeps默认把结果输出到系统输出（控制台）上。
下面我们查看现阶段较流行的Spring框架类库的依赖报告，为了简化这个例子，我们只分析一个jar文件：org.springframework.core-3.0.5.RELEASE.jar
```java
jdeps org.springframework.core-3.0.5.RELEASE.jar
```
这个命令输出的内容很多，所以这里我们只选取一小部分。依赖信息按照包名进行分组。如果依赖不在classpath中，那么就会显示not found。
```java
org.springframework.core-3.0.5.RELEASE.jar -> C:\Program Files\Java\jdk1.8.0\jre\lib\rt.jar
org.springframework.core (org.springframework.core-3.0.5.RELEASE.jar)
   -> java.io                                            
   -> java.lang                                          
   -> java.lang.annotation                               
   -> java.lang.ref                                      
   -> java.lang.reflect                                  
   -> java.util                                          
   -> java.util.concurrent                               
   -> org.apache.commons.logging                         not found
   -> org.springframework.asm                            not found
   -> org.springframework.asm.commons                    not found
org.springframework.core.annotation (org.springframework.core-3.0.5.RELEASE.jar)
   -> java.lang                                          
   -> java.lang.annotation                               
   -> java.lang.reflect                                  
   -> java.util
```
更多详情请参考官方文档
# 6. Java虚拟机（JVM）的新特性
PermGen空间被移除了，取而代之的是Metaspace（JEP 122）。JVM选项-XX:PermSize与-XX:MaxPermSize分别被-XX:MetaSpaceSize与-XX:MaxMetaspaceSize所代替。
