#### JOIN查询原理

 如果有两张数据结构一样的表（id-主键） ,（a有索引） ,（b无索引）。其中表t1（100条数据） 和t2（1000条数据），他们做join查询的时候，内部执行的原理是什么呢？

#### INLJ-（Index Nested-Loop Join）

```csharp
// 使用straight_join表示，固定指定 t1是驱动表，t2是被驱动表，防止优化器优化
select * from t1 straight_join t2 on (t1.a=t2.a);
```

- 1.从表t1中读取一行数据R
- 2.从R中取出字段a去t2中查找
- 3.取出t2中满足要求的数据，和R合并组成结果集中的一条数据
- 4.重复1到3的步骤，流程图如下

![img](https:////upload-images.jianshu.io/upload_images/14523959-4e45dbca74f3dac1.png?imageMogr2/auto-orient/strip|imageView2/2/w/1050/format/webp)

上述因为被驱动表中使用了索引故，该join方法我们称之为（NLJ），这个流程中。对于表t1扫描的全表，故扫描了100行。对于表t2因为走了索引的树搜索，故t2表也是扫描了100行，索引这个join操作执行了200次扫描。这时如果反过来t2作为驱动表，则需要扫描2000次数据，故使用NLJ的时候，尽量使用小表作为驱动表

试想以下，如果上述t2没有使用索引，那么t1查询出的R对应查询t2的数据时，每次都要全表遍历1000次，那么查询的次数就要达到，100*1000=10W次查询了，这种查询方法叫做Simple Nested-Loop Join（SNLJ），因为效率实在太低，所以mysql根本没有使用这种方法。而是使用的Block Nested-Loop Join

#### BNLJ （Block Nested-Loop Join）

对于t1的数据并没有一条条读取，而是将t1的数据一次性加载到join_buffer的缓存中，然后扫描表t2与join_buffer中的每条数据做比对，最终一共扫描数据的次数是100+1000=1100次，大大增加了效率



![img](https:////upload-images.jianshu.io/upload_images/14523959-81c1bb6ce5898acb.png?imageMogr2/auto-orient/strip|imageView2/2/w/995/format/webp)



不过join_buffer 的内存是有限的，如果join_buffer中放不下t1的表的所有数据，那么他会将数据分几次来放，所以驱动表t1的数据越小，分的次数也就越小，查询的效率就会越高
 从上诉的BLJ还是NLJ算法得知，驱动表尽可能的要使用小表，但是什么数据条数少的表就是小表么？

#### 案例一：

对于上面的数据，我们执行以下语句

```csharp
select * from t1 straight_join t2 on(t1.b=t2.b) where t2.id<50
select * from t2 straight_join t1 on(t1.b=t2.b) where t2.id<50
```

这时，t2增加了where条件，那么t2作为驱动表，加载到join_buffer中的大小则只有50条，这时t2才是小表

#### 案例二：

```csharp
select t1.id ,t2.* from t1 straight_join t2 on(t1.b=t2.b) where t1.id<100 and t2.id < 100
select t1.id ,t2.* from t2 straight_join t1 on(t1.b=t2.b) where t1.id<100 and t2.id < 100
```

这时，t1 和t2都增加了where条件，条数都是100条，但是t1只查询了id列，所以这时t1是小表