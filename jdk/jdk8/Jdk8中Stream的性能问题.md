# Java8中Stream的性能问题

_摘要：本文介绍了Java8中流的几个特性，以告诫开发者流并不是高性能的代名词，需谨慎使用流。以下是译文。_
流（Stream）是Java8为了实现最佳性能而引入的一个全新的概念。在过去的几年中，随着硬件的持续发展，编程方式已经发生了巨大的改变，程序的性能也随着并行处理、实时、云和其他一些编程方法的出现而得到了不断提高。
Java8中，流性能的提升是通过**并行化（parallelism）**、**惰性（Laziness）**和**短路操作（short-circuit operations）**来实现的。但它也有一个缺点，在选择流的时候需要非常小心，因为这可能会降低应用程序的性能。
下面来看看这三项支撑起流强大性能的因素吧。
## 并行化
流的并行化充分利用了硬件的相关功能。由于现在计算机上通常都有多个CPU核心，所以在多核系统中如果只使用一个线程则会极大地浪费系统资源。设计和编写多线程应用非常具有挑战性，并且很容易出错，因此，流存在两种实现：顺序和并行。使用并行流非常简单，无需专业知识即可轻松处理多线程问题。
在Java的流中，并行化是通过`Fork-Join`原理来实现的。根据`Fork-Join`原理，系统会将较大的任务切分成较小的子任务（称之为`forking`），然后并行处理这些子任务以充分利用所有可用的硬件资源，最后将结果合并起来（称之为`Join`）组成完整的结果。
在选择顺序和并行的时候，需要非常谨慎，因为并行并一定意味着性能会更好。
让我们来看一个例子。
**StreamTest.java:**
```java
package test;
import java.util.ArrayList;
import java.util.List;
public class StreamTest {
 static List < Integer > myList = new ArrayList < > ();
 public static void main(String[] args) {
  for (int i = 0; i < 5000000; i++)
   myList.add(i);
  int result = 0;
  long loopStartTime = System.currentTimeMillis();
  for (int i: myList) {
   if (i % 2 == 0)
    result += i;
  }
  long loopEndTime = System.currentTimeMillis();
  System.out.println(result);
  System.out.println("Loop total Time = " + (loopEndTime - loopStartTime));
  long streamStartTime = System.currentTimeMillis();
  System.out.println(myList.stream().filter(value -> value % 2 == 0).mapToInt(Integer::intValue).sum());
  long streamEndTime = System.currentTimeMillis();
  System.out.println("Stream total Time = " + (streamEndTime - streamStartTime));
  long parallelStreamStartTime = System.currentTimeMillis();
  System.out.println(myList.parallelStream().filter(value -> value % 2 == 0).mapToInt(Integer::intValue).sum());
  long parallelStreamEndTime = System.currentTimeMillis();
  System.out.println("Parallel Stream total Time = " + (parallelStreamEndTime - parallelStreamStartTime));
 }
}
```
运行结果：
```java
820084320
Loop total Time = 17
820084320
Stream total Time = 81
820084320
Parallel Stream total Time = 30• 1
```
正如你所见，在这种情况下，for循环更好。因此，在没有正确的分析之前，不要用流代替for循环。在这里，我们可以看到，并行流的性能比普通流更好。
注意：结果可能会因为硬件的不同而不同。
## 惰性
我们知道，Java8的流有两种类型的操作，分别为中间操作（Intermediate）和最终操作（Terminal）。这两种操作分别用于处理和提供最终结果。如果最终操作不与中间操作相关联，则无法执行。
总之，中间操作只是创建另一个流，不会执行任何处理，直到最终操作被调用。一旦最终操作被调用，则开始遍历所有的流，并且相关的函数会逐一应用到流上。中间操作是惰性操作，所以，流支持惰性。
注意：对于并行流，并不会在最后逐个遍历流，而是并行处理流，并且并行度取决于机器CPU核心的个数。
考虑一下这种情况，假设我们有一个只有中间操作的流片段，而最终操作要稍后才会添加到应用中（可能需要也可能不需要，取决于用户的需求）。在这种情况下，流的中间操作将会为最终操作创建另一个流，但不会执行实际的处理。这种机制有助于提高性能。
我们来看一下有关惰性的例子：
**StreamLazinessTest.java:**
```java
package test;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class StreamLazinessTest {
 /** Employee class **/
 static class Employee {
  int id;
  String name;
  public Employee(int id, String name) {
   this.id = id;
   this.name = name;
  }
  public String getName() {
   return this.name;
  }
 }
 public static void main(String[] args) throws InterruptedException {
  List < Employee > employees = new ArrayList < > ();
  /** Creating the employee list **/
  for (int i = 1; i < 10000000; i++) {
   employees.add(new StreamLazinessTest.Employee(i, "name_" + i));
  }
  /** Only Intermediate Operations; it will just create another streams and 
   * will not perform any operations **/
  Stream < String > employeeNameStreams = employees.parallelStream().filter(employee -> employee.id % 2 == 0)
   .map(employee -> {
    System.out.println("In Map - " + employee.getName());
    return employee.getName();
   });
  /** Adding some delay to make sure nothing has happen till now **/
  Thread.sleep(2000);
  System.out.println("2 sec");
  /** Terminal operation on the stream and it will invoke the Intermediate Operations
   * filter and map **/
  employeeNameStreams.collect(Collectors.toList());
 }
}
```
运行上面的代码，你可以看到在调用最前操作之前，中间操作不会被执行。
## 短路行为
这是优化流处理的另一种方法。 一旦条件满足，短路操作将会终止处理过程。 有许多短路操作可供使用。 例如，`anyMatch`、`allMatch`、`findFirst`、`findAny`、`limit`等等。
我们来看一个例子。
**StreamShortCircuitTest.java:**
```java
package test;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class StreamShortCircuitTest {
 /** Employee class **/
 static class Employee {
  int id;
  String name;
  public Employee(int id, String name) {
   this.id = id;
   this.name = name;
  }
  public int getId() {
   return this.id;
  }
  public String getName() {
   return this.name;
  }
 }
 public static void main(String[] args) throws InterruptedException {
  List < Employee > employees = new ArrayList < > ();
  for (int i = 1; i < 10000000; i++) {
   employees.add(new StreamShortCircuitTest.Employee(i, "name_" + i));
  }
  /** Only Intermediate Operations; it will just create another streams and 
   * will not perform any operations **/
  Stream < String > employeeNameStreams = employees.stream().filter(e -> e.getId() % 2 == 0)
   .map(employee -> {
    System.out.println("In Map - " + employee.getName());
    return employee.getName();
   });
  long streamStartTime = System.currentTimeMillis();
  /** Terminal operation with short-circuit operation: limit **/
  employeeNameStreams.limit(100).collect(Collectors.toList());
  System.out.println(System.currentTimeMillis() - streamStartTime);
 }
}
```
运行上面的代码，你会看到性能得到了极大地提升，在我的机器上只需要6毫秒的时间。 在这里，`limit()`方法在满足条件的时候会中断运行。
最后要注意的是，根据状态的不同有两种类型的中间操作：有状态（Stateful）和无状态（Stateless）中间操作。
### 有状态中间操作
这些中间操作需要存储状态，因此可能会导致应用程序的性能下降，例如，`distinct()`、`sort()`、`limit()`等等。
### 无状态中间操作
这些中间操作可以独立处理，因为它们不需要保存状态，例如， `filter()`，`map()`等。在这里，我们了解到，流的出现是为了获得更高的性能，但并不是说使用了流之后性能肯定会得到提升，因此，我们需要谨慎使用。
