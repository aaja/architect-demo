## 一、概述

HashMap是我们在编程中遇到极其频繁、非常重要的一个集合类，如果能对HashMap做进一步的性能优化是非常有价值的而JDK 1.8做到了，所以非常有必要学习HashMap的重点源码，了解大师的手法。

## 二、底层数据结构

jdk1.7(数组 + 链表)

![](https://aaja.gitee.io/picture/blog-picture/20200719002.png)

jdk1.8（数组 + 链表 + 红黑树）

![](https://aaja.gitee.io/picture/blog-picture/20200719003.png)

画图真的是个累活，好的画图工具很重要啊，上面这两张图分别画出了JDK 1.7、1.8底层数据结构，在JDK 1.7、1.8中都使用
了散列算法，但是在JDK 1.8中引入了红黑树，在链表的长度大于等于8并且hash桶的长度大于等于64的时候，会将链表进行树化。这里的树使用的数据结构是红黑树，红黑树是一个自平衡的二叉查找树，查找效率会从链表的o(n)降低为o(logn)，效率是非常大的提高。

那为什么不将链表全部换成二叉树呢？这里主要有两个方面。

- 第一个是链表的结构比红黑树简单，构造红黑树要比构造链表复杂，所以在链表的节点不多的情况下，从整体的性能看来，
  数组+链表+红黑树的结构不一定比数组+链表的结构性能高。
- 第二个是HashMap频繁的resize（扩容），扩容的时候需要重新计算节点的索引位置，也就是会将红黑树进行拆分和重组其实
  这是很复杂的，这里涉及到红黑树的着色和旋转，有兴趣的可以看看红黑树的原理，这又是一个比链表结构耗时的操作，所以为链表树化设置一个阀值是非常有必要的。



## 三、源码分析

3.1 类结构

![](https://aaja.gitee.io/picture/blog-picture/20200719001.png)

上图是HashMap的类结构，大家看看有个概念

3.2 类注释

我建议大家在读源码时可以先看看类注释，往往类注释会给我们一些重要的信息，这里LZ给大家总结一下。

（1）允许NULL值，NULL键

（2）不要轻易改变负载因子，负载因子过高会导致链表过长，查找键值对时间复杂度就会增高，负载因子过低会导致hash桶的 数量过多，空间复杂度会增高

（3）Hash表每次会扩容长度为以前的2倍

（4）HashMap是多线程不安全的，我在JDK1.7进行多线程put操作，之后遍历，直接死循环，CPU飙到100%，在JDK 1.8中进行多线程操作会出现节点和value值丢失，为什么JDK1.7与JDK1.8多线程操作会出现很大不同，是因为JDK 1.8的作者对resize方法进行了优化不会产生链表闭环。这也是本章的重点之一，具体的细节大家可以去查阅资料。这里我就不解释太多了

（5）尽量设置HashMap的初始容量，尤其在数据量大的时候，防止多次resize

3.3 类常量

```text
 //默认hash桶初始长度16
  static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; 

  //hash表最大容量2的30次幂
  static final int MAXIMUM_CAPACITY = 1 << 30;

  //默认负载因子 0.75
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  //链表的数量大于等于8个并且桶的数量大于等于64时链表树化 
  static final int TREEIFY_THRESHOLD = 8;

  //hash表某个节点链表的数量小于等于6时树拆分
  static final int UNTREEIFY_THRESHOLD = 6;

  //树化时最小桶的数量
  static final int MIN_TREEIFY_CAPACITY = 64;
```

3.4 实例变量

```text
 //hash桶
  transient Node<K,V>[] table;                         

  //键值对的数量
  transient int size;

  //HashMap结构修改的次数
  transient int modCount;

  //扩容的阀值，当键值对的数量超过这个阀值会产生扩容
  int threshold;

  //负载因子
  final float loadFactor;
```



3.5 构造函数

```text
public HashMap(int initialCapacity, float loadFactor) {                                                                   
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        this.loadFactor = loadFactor;
        //下面介绍一下这行代码的作用
        this.threshold = tableSizeFor(initialCapacity);
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
```



HashMap有4个构造函数。



hash桶没有在构造函数中初始化，而是在第一次存储键值对的时候进行初始化。 这里重点看下tableSizeFor(initialCapacity)方法，这个方法的作用是，将你传入的initialCapacity做计算，返回一个大于等于initialCapacity 最小的2的幂次方。



所以这个操作保证无论你传入的初始化Hash桶长度参数是多少，最后hash表初始化的长度都是2的幂次方。比如你输入的是6，计算出来结果就是8。



下面贴出源码。

```text
static final int tableSizeFor(int cap) {                                                                      
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```



3.6 插入

```text
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,                                     
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    //当table为空时，这里初始化table，不是通过构造函数初始化，而是在插入时通过扩容初始化，有效防止了初始化HashMap没有数据插入造成空间浪费可能造成内存泄露的情况
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    //存放新键值对
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        //旧键值对的覆盖
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        //在红黑树中查找旧键值对更新
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            //将新键值对放在链表的最后
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    //当链表的长度大于等于树化阀值，并且hash桶的长度大于等于MIN_TREEIFY_CAPACITY，链表转化为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                //链表中包含键值对
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        //map中含有旧key，返回旧值
        if (e != null) { 
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    //map调整次数加1
    ++modCount;
    //键值对的数量达到阈值需要扩容
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```



HashMap插入跟我们平时使用时的感觉差不多，下面总结一下。

（1）插入的键值对是新键值对，如果hash表没有初始化会进行初始化，否则将键值对插入链表尾部，可能需要链表树化和
扩容

（2）插入的键值对中的key已经存在，更新键值对在put的方法里我们注意看下hash(key)方法，这是计算键值对hash值的方法，下面给出源码

```text
static final int hash(Object key) {                                                                          
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```



hashCode()是一个int类型的本地方法，也就将key的hashCode无符号右移16位然后与hashCode异或从而得到hash值在putVal方法中（n - 1）& hash计算得到桶的索引位置 ，那么现在有两个疑问，为什么要计算hash值？为什么不用 hash % n?

- 为什么要计算hash值，而不用hashCode，用为通常n是很小的，而hashCode是32位，如果（n - 1）& hashCode那么当n大于2的16次方加1，也就是65537后(n - 1)的高位数据才能与hashCode的高位数据相与，当n很小是只能使用上hashCode低
  16位的数据，这会产生一个问题，既键值对在hash桶中分布不均匀，导致链表过长，而把hashCode>>>16无符号右移16位让
  高16位间接的与（n - 1）参加计算，从而让键值对分布均匀。降低hash碰撞。
- 为什么使用（n - 1）& hash 而不使用hash% n呢？其实这两种结果是等价的，但是&的效率比%高，原因因为&运算是二
  进制直接运算，而计算机天生就认得二进制。下面画图说明一下

![](https://aaja.gitee.io/picture/blog-picture/20200719004.png)

上图 hash&(n - 1)的结果是2，而其实hash%n 的结果也是2, hash&(n - 1)与hash%n的结果是等价的。

3.7 扩容

```text
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        //如果旧hash桶不为空
        if (oldCap > 0) {
            //超过hash桶的最大长度，将阀值设为最大值
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //新的hash桶的长度2被扩容没有超过最大长度，将新容量阀值扩容为以前的2倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        //如果hash表阈值已经初始化过
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        //如果旧hash桶，并且hash桶容量阈值没有初始化，那么需要初始化新的hash桶的容量和新容量阀值
        else {              
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        //新的局部变量阀值赋值
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        //为当前容量阀值赋值
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            //初始化hash桶
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        //如果旧的hash桶不为空，需要将旧的hash表里的键值对重新映射到新的hash桶中
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    //只有一个节点，通过索引位置直接映射
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    //如果是红黑树，需要进行树拆分然后映射
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { 
                    //如果是多个节点的链表，将原链表拆分为两个链表，两个链表的索引位置，一个为原索引，一个为原索引加上旧Hash桶长度的偏移量       
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            //链表1
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            //链表2
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        //链表1存于原索引
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        //链表2存于原索引加上原hash桶长度的偏移量
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```



那么什么时候回产生扩容呢？

（1）初始化HashMap时，第一次进行put操作

（2）当键值对的个数大于threshold阀值时产生扩容，threshold=size*loadFactor

上面就是HashMap扩容的源代码，我已经加上了注释，相信大家都能看懂了。总结一下，HaspMap扩容就是就是先计算新的hash表容量和新的容量阀值，然后初始化一个新的hash表，将旧的键值对重新映射在新的hash表里。这里实现的细节当然没有我说的那么简单，如果在旧的hash表里涉及到红黑树，那么在映射到新的hash表中还涉及到红黑树的拆分。

在扩容的源代码中作者有一个使用很巧妙的地方，是键值对分布更均匀，不知道读者是否有看出来。在遍历原hash桶时的一个链表时，因为扩容后长度为原hash表的2倍，假设把扩容后的hash表分为两半，分为低位和高位，如果能把原链表的键值对， 一半放在低位，一半放在高位，这样的索引效率是最高的。那看看源码里是怎样写的。大师通过e.hash & oldCap == 0来判断， 这和e.hash & (oldCap - 1) 有什么区别呢。下面我通过画图来解释一下。

![](https://aaja.gitee.io/picture/blog-picture/20200719005.png)

因为n是2的整次幂，二进制表示除了最高位为1外，其他低位全为0，那么e.hash & oldCap 是否等于0,取决于n对应最高位. 相对于e.hash那一位是0还是1，比如说n = 16，二进制为10000，第5位为1，e.hash & oldCap 是否等于0就取决于e.hash第5
位是0还是1，这就相当于有50%的概率放在新hash表低位，50%的概率放在新hash表高位。大家应该明白了e.hash & oldCap == 0的好处与作用了吧。

其实，到这里基本上HashMap的核心内容都讲完了，相信大家对HashMap的源码有一定了解了。在源码中还有键值对的查询和删除都比较简单，这里就不在过多赘述了，对于红黑树的构造、旋转、着色，我觉得大家有兴趣可以了解一下，毕竟我们不
是HashMap的开发者，不用了解过多的细节，钻墙角。知道大致的原理即可。

3.8 清除

本来到这里就要结束了，但是LZ还是想跟大家聊一下HashMap总的clear()方法，下面贴出源码。

```text
public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }
```



HashMap其实这段代码特别简单，为什么贴出来呢，是因为我在看过别的博客里产生过疑问，到底是clear好还是新建一个HashMap好。我认为clear()比新建一个HashMap好。下面从空间复杂度和时间复杂度来解释一下。



从时间角度来看，这个循环是非常简单无复杂逻辑，并不十分耗资源。而新建一HashMap，首先他在在堆内存中年轻代中查看是否有足够空间能够存储，如果能够存储，那么创建顺利完成，但如果HashMap非常大，年轻代很难有足够的空间存储，如果老年代中有足够空间存储这个HashMap，那么jvm会将HashMap直接存储在老年代中，如果老年代中空间不够，这时候会触发一次minor gc，会产生小规模的gc停顿，如果发生minor gc之后仍不能存储HashMap，那么会发生整个堆的gc，也就是full gc，这个gc停顿是很恐怖的。实际上的gc顺序就是这样的，并且可能发生多次minor gc和full gc,如果发现年轻代和老年代均不能存储HashMap，那么就会触发OOM，而clear()是肯定不会触发OOM的，所以数据里特别大的情况下，千万不要创建一个新的HashMap代替clear()方法。



从空间角度看，原HashMap虽然不用，如果数据未被清空，是不可能被jvm回收的，因为HashMap是强引用类型的，从而造成内存泄漏。所以综上所述我
是不建议新建一个HashMap代替clear()的，并且很多源码中clear()方法很常用，这就是最好的证明。



## 四、总结

（1）HashMap允许NULL值，NULL键

（2）不要轻易改变负载因子，负载因子过高会导致链表过长，查找键值对时间复杂度就会增高，负载因子过低会导致hash桶的数量过多，空间复杂度会增高

（3）Hash表每次会扩容长度为以前的2倍

（4）HashMap是多线程不安全的，我在JDK 1.7进行多线程put操作，之后遍历，直接死循环，CPU飙到100%，在JDK 1.8中
进行多线程操作会出现节点和value值丢失，为什么JDK1.7与JDK1.8多线程操作会出现很大不同，是因为JDK 1.8的作者对resize
方法进行了优化不会产生链表闭环。这也是本章的重点之一，具体的细节大家可以去查阅资料。这里我就不解释太多了

（5）尽量设置HashMap的初始容量，尤其在数据量大的时候，防止多次resize

（6）HashMap在JDK 1.8在做了很好性能的提升，我看到过在JDK1.7和JDK1.8get操作性能对比JDK1.8是要优于JDK 1.7的，大家感兴趣的可以自己做个测试，所以还没有升级到JDK1.8的小伙伴赶紧的吧。