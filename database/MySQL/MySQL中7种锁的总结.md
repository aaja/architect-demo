## MySQL的8种锁

### 1 行锁（Record Locks）

​	 行锁是对索引记录的锁定，行锁始终锁定索引记录。即使没有索引的表，`InnoDB`也会创建一个隐藏的聚集索引，并将该索引用于行锁。示例如下:

```sql
SELECT c1 FROM t WHERE c1 = 10 FOR UPDATE
```

#### 1.1 FOR UPDATE

​		SELECT ... FOR UPDATE 走的是IX锁(意向排它锁)，即在符合条件的rows上都加了排它锁，其他session也就无法在这些记录上添加任何的S锁或X锁。如果不存在一致性非锁定读的话，那么其他session是无法读取和修改这些记录的，但是INNODB有非锁定读(快照读并不需要加锁)，for update之后并不会阻塞其他session的快照读取操作，除了select ...lock in share mode和select ... for update这种显示加锁的查询操作。

```SQL
SELECT amount FROM product WHERE product_name = 'XX' FOR UPDATE
```

#### 1.2 LOCK IN SHARE MODE

​		SELECT ... LOCK IN SHARE MODE走的是IS锁(意向共享锁)，即在符合条件的rows上都加了共享锁，这样的话，其他session可以读取这些记录，也可以继续添加IS锁，但是无法修改这些记录直到你这个加锁的session执行完成(否则直接锁等待超时)。

```SQL
SELECT amount FROM product WHERE product_name = 'XX' LOCK IN SHARE MODE
```

通过对比，lock in share mode适用于两张表存在业务关系时的一致性要求，for  update适用于操作同一张表时的一致性要求。

### 2 间隙锁（Gap Locks）

​	间隙锁是对索引记录之间的间隙的锁定。间隙锁一定是**开区间**，比如（3，5）。

​	间隙锁在本质上是不区分共享间隙锁或互斥间隙锁的，而且间隙锁是不互斥的，即两个事务可以同时持有包含共同间隙的间隙锁。这里的共同间隙包括两种场景：其一是两个间隙锁的间隙区间完全一样；其二是一个间隙锁包含的间隙区间是另一个间隙锁包含间隙区间的子集。间隙锁本质上是用于阻止其他事务在该间隙内插入新记录，而自身事务是允许在该间隙内插入数据的。也就是说间隙锁的应用场景包括并发读取、并发更新、并发删除和并发插入。

​	在RU和RC两种隔离级别下，即使你使用select ... in share mode（**意向共享锁**）或select ... for update（**意向排它锁**），也无法防止幻读（读后写的场景）。因为这两种隔离级别下只会有行锁，而不会有间隙锁。这也是为什么示例中要规定隔离级别为RR的原因。示例如下：

```sql
SELECT c1 FROM t WHERE c1 BETWEEN 10 and 20 FOR UPDATE
```

### 3 临健锁（Next-key Locks）

​	临键锁是行锁+间隙锁，即临键锁是一个左开右闭的区间，比如（3，5]。

​	InnoDB的默认事务隔离级别是RR，在这种级别下，如果你使用select ... in share mode或者select ... for update语句，那么InnoDB会使用临键锁，因而可以防止幻读；但即使你的隔离级别是RR，如果你这是使用普通的select语句，那么InnoDB将是快照读，不会使用任何锁，因而还是无法防止幻读。

#### 3.1 快照读（照片）

​	读取的是记录数据的可见版本（可能是过期的数据），不用加锁

#### 3.2 当前读

​	读取的是记录数据的最新版本，并且当前读返回的记录都会加上锁，保证其他事务不会再并发的修改这条记录

​	innodb默认隔离级别是RR， 是通过MVVC来实现了，读方式有两种，执行select的时候是快照读，其余是当前读，所以，mvvc不能根本上解决幻读的情况

#### 3.3 MVVC

##### 3.3.1 什么是MVCC？

　　MVCC (Multi-Version Concurrency Control) (注：与MVCC相对的，是基于锁的并发控制，Lock-Based Concurrency Control)是一种基于多版本的并发控制协议，只有在InnoDB引擎下存在。MVCC是为了实现事务的隔离性，通过版本号，避免同一数据在不同事务间的竞争，你可以把它当成基于多版本号的一种乐观锁。当然，这种乐观锁只在事务级别未提交锁和已提交锁时才会生效。MVCC最大的好处，相信也是耳熟能详：读不加锁，读写不冲突。在读多写少的OLTP应用中，读写不冲突是非常重要的，极大的增加了系统的并发性能。具体见下面介绍。

##### 3.3.2 MVCC的实现机制

　　InnoDB在每行数据都增加两个隐藏字段，一个记录创建的版本号，一个记录删除的版本号。

　　在多版本并发控制中，为了保证数据操作在多线程过程中，保证事务隔离的机制，降低锁竞争的压力，保证较高的并发量。在每开启一个事务时，会生成一个事务的版本号，被操作的数据会生成一条新的数据行（临时），但是在提交前对其他事务是不可见的，对于数据的更新（包括增删改）操作成功，会将这个版本号更新到数据的行中，事务提交成功，将新的版本号更新到此数据行中，这样保证了每个事务操作的数据，都是互不影响的，也不存在锁的问题。

##### 3.3.3 MVCC下的CRUD

**SELECT：**
　　当隔离级别是REPEATABLE READ时select操作，InnoDB必须每行数据来保证它符合两个条件：
　　1、InnoDB必须找到一个行的版本，它至少要和事务的版本一样老(也即它的版本号不大于事务的版本号)。这保证了不管是事务开始之前，或者事务创建时，或者修改了这行数据的时候，这行数据是存在的。
　　2、这行数据的删除版本必须是未定义的或者比事务版本要大。这可以保证在事务开始之前这行数据没有被删除。
符合这两个条件的行可能会被当作查询结果而返回。

**INSERT：**

　　InnoDB为这个新行记录当前的系统版本号。
**DELETE：**

　　InnoDB将当前的系统版本号设置为这一行的删除ID。
**UPDATE：**

　　InnoDB会写一个这行数据的新拷贝，这个拷贝的版本为当前的系统版本号。它同时也会将这个版本号写到旧行的删除版本里。
　　这种额外的记录所带来的结果就是对于大多数查询来说根本就不需要获得一个锁。他们只是简单地以最快的速度来读取数据，确保只选择符合条件的行。这个方案的缺点在于存储引擎必须为每一行存储更多的数据，做更多的检查工作，处理更多的善后操作。
　　MVCC只工作在REPEATABLE READ和READ COMMITED隔离级别下。READ UNCOMMITED不是MVCC兼容的，因为查询不能找到适合他们事务版本的行版本；它们每次都只能读到最新的版本。SERIABLABLE也不与MVCC兼容，因为读操作会锁定他们返回的每一行数据。

### 4 共享锁/排他锁（Shared and Exclusive Locks）

​	共享锁/排他锁都只是行锁，与间隙锁无关，这一点很重要，后面还会强调这一点。其中共享锁是一个事务并发读取某一行记录所需要持有的锁，比如select ... in share mode；排他锁是一个事务并发更新或删除某一行记录所需要持有的锁，比如select ... for update。

​	不过这里需要重点说明的是，尽管共享锁/排他锁是行锁，与间隙锁无关，但一个事务在请求共享锁/排他锁时，获取到的结果却可能是行锁，也可能是间隙锁，也可能是临键锁，这取决于数据库的隔离级别以及查询的数据是否存在。关于这一点，后面分析场景一和场景二的时候还会提到。

### 5 意向共享锁/意向排他锁（Intention Shared and Exclusive Locks）

意向共享锁/意向排他锁属于**表锁**，且取得意向共享锁/意向排他锁是取得共享锁/排他锁的**前置条件**。

共享锁/排他锁与意向共享锁/意向排他锁的兼容性关系：

|      |  X   |  IX  |  S   |  IS  |
| :--: | :--: | :--: | :--: | :--: |
|  X   | 互斥 | 互斥 | 互斥 | 互斥 |
|  IX  | 互斥 | 兼容 | 互斥 | 兼容 |
|  S   | 互斥 | 互斥 | 兼容 | 兼容 |
|  IS  | 互斥 | 兼容 | 兼容 | 兼容 |

这里需要重点关注的是**IX锁和IX锁是相互兼容**的，这是导致上面场景一发生死锁的前置条件，后面会对死锁原因进行详细分析。

### 6 插入意向锁（Insert Intention Locks）

​		尽管插入意向锁是一种特殊的间隙锁，但不同于间隙锁的是，该锁只用于并发插入操作。如果说间隙锁锁住的是一个区间，那么插入意向锁锁住的就是一个点。因而从这个角度来说，插入意向锁确实是一种特殊的间隙锁。与间隙锁的另一个非常重要的差别是：尽管插入意向锁也属于间隙锁，但两个事务却不能在同一时间内一个拥有间隙锁，另一个拥有该间隙区间内的插入意向锁（当然，插入意向锁如果不在间隙锁区间内则是可以的）。这里我们再回顾一下共享锁和排他锁：共享锁用于读取操作，而排他锁是用于更新或删除操作。也就是说插入意向锁、共享锁和排他锁涵盖了常用的增删改查四个动作。

### 7 自增锁（Auto-inc Locks）

​	自增锁是一种特殊的表级锁，主要用于事务中插入自增字段，也就是我们最常用的自增主键id。通过innodb_autoinc_lock_mode参数可以设置自增主键的生成策略。为了便于介绍innodb_autoinc_lock_mode参数，我们先将需要用到自增锁的Insert语句进行分类：

#### 7.1 Insert语句分类

​	1.“INSERT-like” statements(类INSERT语句) （这种语句实际上包含了下面的2、3、4）

所有可以向表中增加行的语句，包括INSERT, INSERT ... SELECT, REPLACE, REPLACE ... SELECT, and LOAD DATA。包括“simple-inserts”, “bulk-inserts”, and “mixed-mode” inserts.

2. “Simple inserts”

可以预先确定要插入的行数（当语句被初始处理时）的语句。 这包括没有嵌套子查询的单行和多行INSERT和REPLACE语句，但不包括INSERT ... ON DUPLICATE KEY UPDATE。

3. “Bulk inserts”

事先不知道要插入的行数（和所需自动递增值的数量）的语句。 这包括INSERT ... SELECT，REPLACE ... SELECT和LOAD DATA语句，但不包括纯INSERT。 InnoDB在处理每行时一次为AUTO_INCREMENT列分配一个新值。

4. “Mixed-mode inserts”

这些是“Simple inserts”语句但是指定一些（但不是全部）新行的自动递增值。 示例如下，其中c1是表t1的AUTO_INCREMENT列：

```SQL
INSERT INTO t1 (c1,c2) VALUES (1,'a'), (NULL,'b'), (5,'c'), (NULL,'d');
```

另一种类型的“Mixed-mode inserts”是INSERT ... ON DUPLICATE KEY UPDATE，其在最坏的情况下实际上是INSERT语句随后又跟了一个UPDATE，其中AUTO_INCREMENT列的分配值不一定会在 UPDATE 阶段使用。

#### 7.2 InnoDB AUTO_INCREMENT锁定模式分类

```shell
innodb_autoinc_lock_mode = 0 (“traditional” lock mode)
```

这种锁定模式提供了在MySQL 5.1中引入innodb_autoinc_lock_mode配置参数之前存在的相同行为。传统的锁定模式选项用于向后兼容性，性能测试以及解决“Mixed-mode inserts”的问题，因为语义上可能存在差异。

在此锁定模式下，所有“INSERT-like”语句获得一个特殊的表级AUTO-INC锁，用于插入具有AUTO_INCREMENT列的表。此锁定通常保持到语句结束（不是事务结束），以确保为给定的INSERT语句序列以可预测和可重复的顺序分配自动递增值，并确保自动递增由任何给定语句分配的值是连续的。

在基于语句复制(statement-based replication)的情况下，这意味着当在从服务器上复制SQL语句时，自动增量列使用与主服务器上相同的值。多个INSERT语句的执行结果是确定性的，SLAVE再现与MASTER相同的数据（反之，如果由多个INSERT语句生成的自动递增值交错，则两个并发INSERT语句的结果将是不确定的，并且不能使用基于语句的复制可靠地传播到从属服务器）。

```shell
innodb_autoinc_lock_mode = 1 (“consecutive” lock mode)
```

这是默认的锁定模式。在这个模式下,“bulk inserts”仍然使用AUTO-INC表级锁,并保持到语句结束.这适用于所有INSERT ... SELECT，REPLACE ... SELECT和LOAD DATA语句。同一时刻只有一个语句可以持有AUTO-INC锁。

而“Simple inserts”（要插入的行数事先已知）通过在mutex（轻量锁）的控制下获得所需数量的自动递增值来避免表级AUTO-INC锁， 它只在分配过程的持续时间内保持，而不是直到语句完成。 不使用表级AUTO-INC锁，除非AUTO-INC锁由另一个事务保持。 如果另一个事务保持AUTO-INC锁，则“简单插入”等待AUTO-INC锁，如同它是一个“批量插入”。

此锁定模式确保,当行数不预先知道的INSERT存在时(并且自动递增值在语句过程执行中分配)由任何“类INSERT”语句分配的所有自动递增值是连续的，并且对于基于语句的复制(statement-based replication)操作是安全的。

这种锁定模式显著地提高了可扩展性,并且保证了对于基于语句的复制(statement-based replication)的安全性。此外，与“传统”锁定模式一样，由任何给定语句分配的自动递增数字是连续的。 与使用自动递增的任何语句的“传统”模式相比，语义没有变化，但有个特殊场景需要注意：

> The exception is for “mixed-mode inserts”, where the user provides explicit values for an AUTO_INCREMENT column for some, but not all, rows in a multiple-row “simple insert”. For such inserts, InnoDB allocates more auto-increment values than the number of rows to be inserted. However, all values automatically assigned are consecutively generated (and thus higher than) the auto-increment value generated by the most recently executed previous statement. “Excess” numbers are lost.

也就说对于混合模式的插入，可能会有部分多余自增值丢失。

在连续锁定模式下，InnoDB可以避免为“Simple inserts”语句使用表级AUTO-INC锁，其中行数是预先已知的，并且仍然保留基于语句的复制的确定性执行和安全性。

```shell
innodb_autoinc_lock_mode = 2 (“interleaved” lock mode)
```

在这种锁定模式下,所有类INSERT(“INSERT-like” )语句都不会使用表级AUTO-INC lock,并且可以同时执行多个语句。这是最快和最可扩展的锁定模式，但是当使用基于语句的复制或恢复方案时，从二进制日志重播SQL语句时，这是不安全的。

在此锁定模式下，自动递增值保证在所有并发执行的“类INSERT”语句中是唯一且单调递增的。但是，由于多个语句可以同时生成数字（即，跨语句交叉编号），为任何给定语句插入的行生成的值可能不是连续的。

如果执行的语句是“simple inserts”，其中要插入的行数已提前知道，则除了“混合模式插入”之外，为单个语句生成的数字不会有间隙。然而，当执行“批量插入”时，在由任何给定语句分配的自动递增值中可能存在间隙。

如果不使用二进制日志作为恢复或复制的一部分来重放SQL语句，则可以使用interleaved lock模式来消除所有使用表级AUTO-INC锁，以实现更大的并发性和性能,其代价是由于并发的语句交错执行,同一语句生成的AUTO-INCREMENT值可能会产生GAP。

`innodb_autoinc_lock_mode`参数的修改

编辑/etc/my.cnf，加入如下行:

```shell
innodb_autoinc_lock_mode=2
```

直接通过命令修改会报错:

```sql
mysql(mdba@localhost:(none) 09:32:19)>set global innodb_autoinc_lock_mode=2;

ERROR 1238 (HY000): Variable 'innodb_autoinc_lock_mode' is a read only variable
```

#### 7.3 InnoDB AUTO_INCREMENT锁定模式含义

​	1.在复制环节中使用自增列

​		如果你在使用基于语句的复制(statement-based replication)请将innodb_autoinc_lock_mode设置为0或1，并在主从上使用相同的值。 如果使用innodb_autoinc_lock_mode = 2（“interleaved”）或主从不使用相同的锁定模式的配置，自动递增值不能保证在从机上与主机上相同。

如果使用基于行的或混合模式的复制，则所有自动增量锁定模式都是安全的，因为基于行的复制对SQL语句的执行顺序不敏感（混合模式会在遇到不安全的语句是使用基于行的复制模式）。

2. “Lost” auto-increment values and sequence gaps

在所有锁定模式（0,1和2）中，如果生成自动递增值的事务回滚，那些自动递增值将“丢失”。 一旦为自动增量列生成了值，无论是否完成“类似INSERT”语句以及包含事务是否回滚，都不能回滚。 这种丢失的值不被重用。 因此，存储在表的AUTO_INCREMENT列中的值可能存在间隙。

3. Specifying NULL or 0 for the AUTO_INCREMENT column

在所有锁定模式（0,1和2）中，如果用户在INSERT中为AUTO_INCREMENT列指定NULL或0，InnoDB会将该行视为未指定值，并为其生成新值。

4. 为AUTO_INCREMENT列分配一个负值

在所有锁定模式（0,1和2）中，如果您为AUTO_INCREMENT列分配了一个负值，则InnoDB会将该行为视为未指定值，并为其生成新值。

5. 如果AUTO_INCREMENT值大于指定整数类型的最大整数

在所有锁定模式（0,1和2）中，如果值大于可以存储在指定整数类型中的最大整数，则InnoDB会将该值设置为指定类型所允许的最大值。

6. Gaps in auto-increment values for “bulk inserts”

当innodb_autoinc_lock_mode设置为0（“traditional”）或1（“consecutive”）时,任何给定语句生成的自动递增值是连续的，没有间隙，因为表级AUTO-INC锁会持续到 语句结束,并且一次只能执行一个这样的语句。

当innodb_autoinc_lock_mode设置为2（“interleaved”）时，在“bulk inserts”生成的自动递增值中可能存在间隙，但只有在并发执行“INSERT-Like”语句时才会产生这种情况。

对于锁定模式1或2，在连续语句之间可能出现间隙，因为对于批量插入，每个语句所需的自动递增值的确切数目可能不为人所知，并且可能进行过度估计。

7. 由“mixed-mode inserts”分配的自动递增值

考虑一下场景,在“mixed-mode insert”中,其中一个“simple insert”语句指定了一些（但不是全部）行的AUTO-INCREMENT值。 这样的语句在锁模式0,1和2中表现不同。innodb_autoinc_lock_mode=0时,auto-increment值一次只分配一个,而不是在开始时全部分配。当innodb_autoinc_lock_mode=1时，不同于innodb_autoinc_lock_mode=0时的情况，因为auto-increment值在语句一开始就分配了，但实际可能使用不完。当innodb_autoinc_lock_mode=2时，取决于并发语句的执行顺序。

8. 在INSERT语句序列的中间修改AUTO_INCREMENT列值

在所有锁定模式（0,1和2）中，在INSERT语句序列中间修改AUTO_INCREMENT列值可能会导致duplicate key错误。

#### 7.4 InnoDB AUTO_INCREMENT计数器初始化

如果你为一个Innodb表创建了一个AUTO_INCREMENT列，则InnoDB数据字典中的表句柄包含一个称为自动递增计数器的特殊计数器，用于为列分配新值。 此计数器仅存在于内存中，而不存储在磁盘上。

要在服务器重新启动后初始化自动递增计数器，InnoDB将在首次插入行到包含AUTO_INCREMENT列的表时执行以下语句的等效语句。

```sql
SELECT MAX(ai_col) FROM table_name FOR UPDATE;
```

InnoDB增加语句检索的值，并将其分配给表和表的自动递增计数器。 默认情况下，值增加1。此默认值可以由auto_increment_increment配置设置覆盖。

如果表为空，InnoDB使用值1。此默认值可以由auto_increment_offset配置设置覆盖。

如果在自动递增计数器初始化前使用SHOW TABLE STATUS语句查看表, InnoDB将初始化计数器值,但不会递增该值。这个值会储存起来以备之后的插入语句使用。这个初始化过程使用了一个普通的排它锁来读取表中自增列的最大值。InnoDB遵循相同的过程来初始化新创建的表的自动递增计数器。

在自动递增计数器初始化之后，如果您未明确指定AUTO_INCREMENT列的值，InnoDB会递增计数器并将新值分配给该列。如果插入显式指定列值的行，并且该值大于当前计数器值，则将计数器设置为指定的列值。

只要服务器运行，InnoDB就使用内存中自动递增计数器。当服务器停止并重新启动时，InnoDB会重新初始化每个表的计数器，以便对表进行第一次INSERT，如前所述。

服务器重新启动还会取消`CREATE TABLE`和`ALTER TABLE`语句中的`AUTO_INCREMENT = N`表选项的效果（可在建表时可用`AUTO_INCREMENT=n`选项来指定一个自增的初始值，也可用`alter table table_name AUTO_INCREMENT=n`命令来重设自增的起始值）。

### 8 预测锁

`InnoDB`支持`SPATIAL` 包含空间列的列的索引（请参见 [第11.4.9节“优化空间分析”](https://dev.mysql.com/doc/refman/8.0/en/optimizing-spatial-analysis.html)）。

要处理涉及`SPATIAL`索引的操作的锁定 ，临健锁在支持[`REPEATABLE READ`](https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html#isolevel_repeatable-read)或 [`SERIALIZABLE`](https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html#isolevel_serializable)事务隔离级别上效果不佳。多维数据中没有绝对排序概念，因此尚不清楚哪个是 “ 下一个 ”键。

要支持具有`SPATIAL`索引的表的隔离级别 ，请`InnoDB` 使用预测锁。`SPATIAL`索引包含最小外接矩形（`MBR`）值，因此， `InnoDB`通过设置用于查询的`MBR`值的谓词锁强制上的索引一致的读取。其他事务不能插入或修改将匹配查询条件的行。

## 示例

| session1                                                     | session2                                                     |
| :----------------------------------------------------------- | ------------------------------------------------------------ |
| begin;                                                       |                                                              |
|                                                              | begin;                                                       |
| select * from test where id = 12 for update;先请求IX锁并成功获取;再请求X锁，但因行记录不存在，故得到的是间隙锁（10，15） |                                                              |
|                                                              | select * from test where id = 13 for update;先请求IX锁并成功获取再请求X锁，但因行记录不存在，故得到的是间隙锁（10，15） |
| insert into test(id, name) values(12, "test1");请求插入意向锁（12），因事务二已有间隙锁，请求只能等待 |                                                              |
| 锁等待中                                                     | insert into test(id, name) values(13, "test2");请求插入意向锁（13），因事务一已有间隙锁，请求只能等待 |
| 锁等待解除                                                   | 死锁，session 2的事务被回滚                                  |

在场景一中，因为IX锁是表锁且IX锁之间是兼容的，因而事务一和事务二都能同时获取到IX锁和间隙锁。另外，需要说明的是，因为我们的隔离级别是RR，且在请求X锁的时候，查询的对应记录都不存在，因而返回的都是间隙锁。接着事务一请求插入意向锁，这时发现事务二已经获取了一个区间间隙锁，而且事务一请求的插入点在事务二的间隙锁区间内，因而只能等待事务二释放间隙锁。这个时候事务二也请求插入意向锁，该插入点同样位于事务一已经获取的间隙锁的区间内，因而也不能获取成功，不过这个时候，MySQL已经检查到了死锁，于是事务二被回滚，事务一提交成功。

分析并理解了场景一，那场景二理解起来就会简单多了：

| session1                                                     | session2                                                     |
| :----------------------------------------------------------- | ------------------------------------------------------------ |
| begin;                                                       |                                                              |
|                                                              | begin;                                                       |
| select * from test where id = 12 for update; --先请求IX锁并成功获取;再请求X锁，但因行记录不存在，故得到的是间隙锁（10，15） |                                                              |
|                                                              | select * from test where id = 13 for update;先请求IX锁并成功获取再请求X锁，但因行记录不存在，故得到的是间隙锁（10，15） |
| insert into test(id, name) values(12, "test1");请求插入意向锁（12），获取成功 |                                                              |
| commit;                                                      | insert into test(id, name) values(16, "test2");请求插入意向锁（16），获取成功 |
|                                                              | commit;                                                      |

场景二中，两个间隙锁没有交集，而各自获取的插入意向锁也不是同一个点，因而都能执行成功。

## 锁相关配置

```sql
innodb_lock_wait_timeout
```

## 官网

https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html