想更详细的了解JDK10新特性可以浏览[官方介绍](http://cr.openjdk.java.net/~iris/se/10/latestSpec/)

JDK10 新特性目录导航：

- 局部变量类型推断
- 将JDK多存储库合并为单储存库
- 垃圾回收接口
- 并行Full GC 的G1
- 应用数据共享
- 线程局部管控
- 移除Native-Header Generation Tool （javah）
- Unicode 标签扩展
- 备用内存设备上分配堆内存
- 基于实验JAVA 的JIT 编译器
- Root 证书
- 基于时间的版本控制

## 局部变量类型推断

很多人抱怨Java是一种强类型，需要引入大量的样板代码。甚至在这些情况下，给定好变量名，通常很清楚发生了什么，明显类型声明往往被认为是不必要的。许多流行的编程语言都已经支持某种形式的局部变量类型推断：如C++ (auto), C# (var), Scala (var/val), Go (declaration with :=)等。

JDK10 可以使用var作为局部变量类型推断标识符，此符号仅适用于局部变量，增强for循环的索引，以及传统for循环的本地变量；它不能使用于方法形式参数，构造函数形式参数，方法返回类型，字段，catch形式参数或任何其他类型的变量声明。

标识符var不是关键字；相反，它是一个保留的类型名称。这意味着var用作变量，方法名或则包名称的代码不会受到影响；但var不能作为类或则接口的名字（但这样命名是比较罕见的，因为他违反了通常的命名约定，类和接口首字母应该大写）。

参考一下示例：

```java
var str = "ABC"; //根据推断为 字符串类型
var l = 10L;//根据10L 推断long 类型
var flag = true;//根据 true推断 boolean 类型
var flag1 = 1;//这里会推断boolean类型。0表示false 非0表示true
var list = new ArrayList<String>();  // 推断 ArrayList<String>
var stream = list.stream();          // 推断 Stream<String>
```

反编译class文件：

```java
String str = "ABC";
long l = 10L;
boolean flag = true;
int flag1 = true;
ArrayList<String> list = new ArrayList();
Stream<String> stream = list.stream();
```

从上面示例可以看出，当我们是用复杂的方法时，不需要特意去指定他的具体类型返回，可以使用var推断出正确的数据类型，这在编码中，可以大幅减少我们对方法返回值的探究。

## 将JDK多存储库合并为单存储库

为了简化和简化开发，将JDK多存储库合并到一个存储库中。多年来，JDK的完整代码已经被分解成多个存储库。在JDK9 中有八个仓库：root、corba、hotspot、jaxp、jaxws、jdk、langtools和nashorn。在JDK10中被合并为一个存储库。

虽然这种多存储库模型具有一些有点，但它也有许多缺点，并且在支持各种可取的源代码管理操作方面做得很差。特别是，不可能在相互依赖的变更存储库之间执行原子提交。例如，如果一个bug修复或RFE的代码现在同时跨越了jdk和hotspot 存储库，那么对于两个存储库来说，在托管这两个不同的存储库中，对两个存储库的更改是不可能实现的。跨多个存储库的变更是常见。

## 垃圾回收接口

这不是让开发者用来控制垃圾回收的接口；而是一个在 JVM 源代码中的允许另外的垃圾回收器快速方便的集成的接口。

垃圾回收接口为HotSpot的GC代码提供更好的模块化；在不影响当前代码的基础情况下，将GC添加到HotSpot变的更简单；更容易从JDK构建中排除GC。实际添加或删除GC不是目标，这项工作将使HotSpot中GC算法的构建时间隔离取得进展，但它不是完全构建时间隔离的目标。

## 并行Full GC 的G1

JDK10 通过并行Full GC，改善G1的延迟。G1垃圾收集器在JDK 9中是默认的。以前的默认值并行收集器中有一个并行的Full GC。为了尽量减少对使用GC用户的影响，G1的Full GC也应该并行。

G1垃圾收集器的设计目的是避免Full收集，但是当集合不能足够快地回收内存时，就会出现完全GC。目前对G1的Full GC的实现使用了单线程标记-清除-压缩算法。JDK10 使用并行化标记-清除-压缩算法，并使用Young和Mixed收集器相同的线程数量。线程的数量可以由-XX:ParallelGCThreads选项来控制，但是这也会影响用Young和Mixed收集器的线程数量。

## 应用数据共享

为了提高启动和内存占用，扩展现有的类数据共享（CDS）特性，允许将应用程序类放置在共享档案中。

- 通过在不同的Java进程间共享公共类元数据来减少占用空间。
- 提升启动时间。
- CDS允许将来自JDK的运行时映像文件（$JAVA_HOME/lib/modules）的归档类和应用程序类路径加载到内置平台和系统类加载器中。
- CDS允许将归档类加载到自定义类加载器中。

## 线程局部管控

在不执行全局VM安全点的情况下对线程执行回调的方法。让它停止单个线程而不是全部线程。

## 移除Native-Header Generation Tool （javah）

JDK10 从JDK中移除了javah 工具。该工具已被JDK8 （[JDK-7150368](https://bugs.openjdk.java.net/browse/JDK-7150368)）中添加javac高级功能所取代。此功能提供了在编译java源代码时编写本机头文件的功能，从而无需使用单独的工具。

## Unicode 标签扩展

JDK10 改善 java.util.Locale 类和相关的 API 以实现额外 [BCP 47](http://www.rfc-editor.org/rfc/bcp/bcp47.txt) 语言标签的 Unicode 扩展。尤其以下扩展支持：

- cu：货币类型
- fw：一周的第一天
- rg：区域覆盖
- tz：时区

为支持以上扩展，JDK10对以下API进行更改：

- java.text.DateFormat::get*Instance：将根据扩展ca、rg或tz返回实例。
- java.text.DateFormatSymbols::getInstance：将根据扩展rg返回实例。
- java.text.DecimalFormatSymbols::getInstance：将根据扩展rg返回实例。
- java.text.NumberFormat::get*Instance：将根据nu或rg返回实例。
- java.time.format.DateTimeFormatter::localizedBy：将返回DateTimeFormatter 根据ca，rg或rz的实例。
- java.time.format.DateTimeFormatterBuilder::getLocalizedDateTimePattern：将根据rg返回String。
- java.time.format.DecimalStyle::of：将返回DecimalStyle根据nu或rg的实例。
- java.time.temporal.WeekFields::of：将返回WeekFields根据fw或rg的实例。
- java.util.Calendar::{getFirstDayOfWeek,getMinimalDaysInWeek}：将根据fw或rg返回值。
- java.util.Currency::getInstance：将返回Currency根据cu或rg返回实例。
- java.util.Locale::getDisplayName：将返回一个包含这些U扩展名的显示名称的字符串。
- java.util.spi.LocaleNameProvider：将为这些U扩展的键和类型提供新的SPI。

## 备用内存设备上分配堆内存

启用HotSpot VM以在用户指定的备用内存设备上分配Java对象堆。随着廉价的NV-DIMM内存的可用性，未来的系统可能配备了异构的内存架构。这种技术的一个例子是英特尔的3D XPoint。这样的体系结构，除了DRAM之外，还会有一种或多种类型的非DRAM内存，具有不同的特征。具有与DRAM具有相同语义的可选内存设备，包括原子操作的语义，因此可以在不改变现有应用程序代码的情况下使用DRAM代替DRAM。所有其他的内存结构，如代码堆、metaspace、线程堆栈等等，都将继续驻留在DRAM中。

参考以下使用案例：

- 在多JVM部署中，某些JVM（如守护进程，服务等）的优先级低于其他JVM。与DRAM相比，NV-DIMM可能具有更高的访问延迟。低优先级进程可以为堆使用NV-DIMM内存，允许高优先级进程使用更多DRAM。
- 诸如大数据和内存数据库等应用程序对内存的需求不断增加。这种应用可以将NV-DIMM用于堆，因为与DRAM相比，NV-DIMM可能具有更大的容量，成本更低。

## 基于实验JAVA 的JIT 编译器

启用基于Java的JIT编译器Graal，将其用作Linux / x64平台上的实验性JIT编译器。Graal是一个基于Java的JIT编译器,它是JDK 9中引入的Ahead-of-Time（AOT）编译器的基础。使它成为实验性JIT编译器是Project Metropolis的一项举措，它是下一步是研究JDK的基于Java的JIT的可行性。

使Graal可用作实验JIT编译器，从Linux / x64平台开始。Graal将使用JDK 9中引入的JVM编译器接口（JVMCI）。Graal已经在JDK中，因此将它作为实验JIT将主要用于测试和调试工作。要启用Graal作为JIT编译器，请在java命令行上使用以下选项：

```shell
-XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler
```

## Root 证书

在JDK中提供一组默认的root 认证权威（CA）证书。在Oracle的Java SE根CA程序中开源root证书，以使OpenJDK构建对开发人员更有吸引力，并减少这些构建和Oracle JDK构建之间的差异。

cacerts密钥存储库是JDK的一部分，它的目的是包含一组root证书，这些root证书可以用来在各种安全协议中使用的证书链中建立信任。然而，JDK源代码中的cacerts密钥库目前是空的。因此，诸如TLS之类的关键安全组件在OpenJDK构建中不会默认工作。为了解决这个问题，用户必须配置和填充cacerts密钥库，并使用一组root证书来记录，例如， [JDK 9 release notes](http://www.oracle.com/technetwork/java/javase/9all-relnotes-3704433.html#JDK-8189131)。

## 基于时间的版本控制

在[JEP 223](http://openjdk.java.net/jeps/223) 引入的版本字符串方案比以往有了显著的改进，但是，该方案并不适合未来，现在Java SE平台和JDK的新版本[严格按照六个月](https://mreinhold.org/blog/forward-faster)的节奏发布。JEP 223方案的主要困难在于发行版的版本号对于其前身的重要性和兼容性进行了编码。然而，在基于时间发布模式中，这些品质并不是事先知道的。在发布的开发周期中，它们可能会发生变化，直到最终的功能被集成为止。因此发布的版本号也是未知的。

使用JEP 223的版本号语义，每个在JDK发布版上工作或者在其上构建或使用组件的人都必须先说明发布的发布日期，然后切换到说版本号，已知。维护库，框架和工具的开发人员必须准备好更改在每个JDK发布周期后期检查版本号的代码。这对所有参与者来说都很尴尬和混乱。

因此，这里提出的主要改变是重新编制版本号来编码，而不是编码的兼容性和重要性，而是按照发布周期的时间推移。这是更适合基于时间的发布模型，因为每个发布周期，因此每个发布的版本号，总是提前知道。

后续的版本格式为：[1-9][0-9]*((\.0)*\.[1-9][0-9]*)*

该格式可以是任意长度，但前四个被赋予特定含义，如：$FEATURE.$INTERIM.$UPDATE.$PATCH

- $FEATURE：功能发布计数器，不管发布内容如何，都会为每个功能发布增加。功能可能会添加到功能发布中; 如果提前通知提前至少发布一次功能发布，它们也可能会被删除。如果合理，可能会做出不兼容的更改。
- $INTERIM：临时版本计数器，对于包含兼容错误修复和增强功能的非功能版本递增，但没有不兼容的更改，没有功能移除，也没有对标准API的更改。
- $UPDATE：更新版本计数器增加了兼容更新版本，可解决新功能中的安全问题，回归和错误。
- $PATCH：紧急修补程序释放计数器只有在需要生成紧急释放以解决关键问题时才会增加。

版本号永远不会有零元素结尾。如果一个元素及其后的所有元素在逻辑上具有零值，那么它们全部被省略。

在严格六个月的发布模式下，版本号如下所示：

- $FEATURE 每六个月增加一次。如：2018年3月发布的是JDK 10，2018年9月发布的是JDK 11，等等。
- $INTERIM 总是为零，因为六个月的模型不包括临时版本。在此为保留其灵活性，以便将来对版本模型的修订可能包括此类版本。
- $UPDATE 在$FEATURE发布后的一个月递增，之后每三个月递增一次：如2018年4月发布JDK 10.0.1。7月发布的是JDK 10.0.2等等。