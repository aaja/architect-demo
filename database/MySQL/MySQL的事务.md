### 1 事务

**数据库事务**（简称：**事务**）是[数据库管理系统](https://baike.baidu.com/item/数据库管理系统)执行过程中的一个逻辑单位，由一个有限的[数据库](https://baike.baidu.com/item/数据库)操作序列构成。

```sql
begin Transaction
update account set money = money -100 where user = ‘A
update account set money = money + 100 where user = ‘B’
commit /rollback 
要么全部成功，要么全部失败
```

### 2 事务的ACID特性

MYSQL传统关系数据库的ACID模型有以下特性

- Atomicity（原子性）

  表示将事务中所进行的操作捆绑成一个不可分割的单元，即对事务所进行的数据修改等操作，要么全部执行，要么全都不执行。

- Consistency（一致性）

   表示事务完成时，必须使所有的数据都保持一致状态。

- Isolation（隔离性）

  指一个事务的执行不能被其他事务干扰，即一个事务内部的操作及使用的数据对并发的其他事务是隔离的，并发执行的各个事务之间不能互相干扰。

- Durability（持久性）

  一旦事务提交成功后，事务中所有的数据操作都必须被持久化到数据库中。即使在事务提交后，数据库马上崩溃，在数据库重启时，也必须保证能够通过某种机制恢复数据。

### 3 事务的隔离级别

#### **3.1 事务并发问题**

- **脏读**

指一个事务读取到另一个事务未提交的数据。

- **不可重复读**

指一个事务对同一行数据重复读取两次，但得到的结果不同。

- **幻读**

指一个事务执行两次查询，但第二次查询的结果包含了第一次查询中未出现的数据。

- **丢失更新**

指两个事务同时更新一行数据，后提交（或撤销）的事务将之前事务提交的数据覆盖了。

#### **3.2 事务隔离级别解决并发问题**

- 读未提交（Read Uncommited）

一个事务在执行过程中，既可以访问其他事务未提交的新插入的数据，又可以访问未提交的修改数据。如果一个事务已经开始写数据，则另外一个事务不允许同时进行写操作，但允许其他事务读此行数据。此隔离级别可防止丢失更新。

- 读已提交（Read Commited）

一个事务在执行过程中，既可以访问其他事务成功提交的新插入的数据，又可以访问成功修改的数据。读取数据的事务允许其他事务继续访问该行数据，但是未提交的写事务将会禁止其他事务访问该行。此隔离级别可有效防止脏读。

- 可重复读（Repeatable Read）

一个事务在执行过程中，可以访问其他事务成功提交的新插入的数据，但不可以访问成功修改的数据。读取数据的事务将会禁止写事务（但允许读事务），写事务则禁止任何其他事务。此隔离级别可有效防止不可重复读和脏读。

- 串行化（Serializable）

提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，不能并发执行。此隔离级别可有效防止脏读、不可重复读和幻读。但这个级别可能导致大量的超时现象和锁竞争，在实际应用中很少使用。

#### **3.3 不同隔离级别解决的事务并发问题**

| **并发问题**  **事务隔离级别** | **脏读** | **不可重复读** | **幻读** |
| ------------------------------ | -------- | -------------- | -------- |
| **读未提交**                   | **Y**    | **Y**          | **Y**    |
| **读已提交**                   | N        | **Y**          | **Y**    |
| **可重复读**                   | N        | N              | **Y**    |
| **串行化**                     | N        | N              | N        |

**丢失更新：悲观锁、乐观锁；**

### 4 MYSQL-ACID模型的实现原理如下

- 事务的原子性是通过 undo log 来实现的
- 事务的持久性性是通过 redo log 来实现的
- 事务的隔离性是通过 (读写锁+MVCC)来实现的
- 而事务的终极大 boss 一致性是通过原子性，持久性，隔离性来实现的！！！

下面就逐一介绍其实现原理

#### 原子性（Atomicity）原理

一个事务必须被视为不可分割的最小工作单位，一个事务中的所有操作要么全部成功提交，要么全部失败回滚，对于一个事务来说不可能只执行其中的部分操作，这就是事务的原子性。

数据库是通过回滚操作来实现原子性的。 所谓回滚操作就是当发生错误异常或者显式的执行rollback语句时需要把数据还原到原先的模样，所以这时候就需要用到undo log来进行回滚。undo log 就是用于记录更新或新增操作之前的数据状态，当出现需要回滚的情况时，将原数据刷回到数据库中，从而保证操作的原子性，具体实现方式如下：

- ![img](https:////upload-images.jianshu.io/upload_images/14523959-9506ac929353f510.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

上面从银行账户转账到理财账户的操作步骤如下

- 1.事务开始
- 2.查询数据
- 3.进行update操作，balance=balance-400;
- 4.记录zhangsan（1000）到undo log 日志中，回滚时需要将数据更新回来
- 5.进行update操作，amount=amount+400；
- 6.记录amount（0）到undo log日志中，回滚的时候需要将数据刷新回来
- 7.事务提交/回滚

#### 持久性（Durability）原理

事务一旦提交，其所作做的修改会永久保存到数据库中，此时即使系统崩溃修改的数据也不会丢失。
 MySQL的数据存储，表数据是存放在磁盘上的，因此想要存取的时候都要经历磁盘IO,然而即使是使用SSD磁盘IO也是非常消耗性能的。 为此，为了提升性能InnoDB提供了缓冲池(Buffer Pool)，Buffer Pool中包含了磁盘数据页的映射，可以当做缓存来使用：

- 读数据：会首先从缓冲池中读取，如果缓冲池中没有，则从磁盘读取在放入缓冲池；
- 写数据：会首先写入缓冲池，缓冲池中的数据会定期同步到磁盘中；

上面这种缓冲池的措施虽然在性能方面带来了质的飞跃，但是它也带来了新的问题，当MySQL系统宕机，断电的时候可能会丢数据！因为我们的数据已经提交了，但此时是在缓冲池里头，还没来得及在磁盘持久化，所以我们急需一种机制需要存一下已提交事务的数据，为恢复数据使用。redo log就派上用场了。

redo log来记录已成功提交事务的修改信息，并且会把redo log持久化到磁盘，系统重启之后在读取redo log恢复最新数据。

- ![img](https:////upload-images.jianshu.io/upload_images/14523959-7fa5e14d9cf54d66.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

#### 隔离性（Isolation）原理

Mysql 隔离级别有以下四种（级别由低到高）：

- READ UNCOMMITED (读未提交)
- READ COMMITED (读提交)
- REPEATABLE READ (可重复读)
- SERIALIZABLE (串行化)

隔离性是要管理多个并发读写请求的访问顺序。 这种顺序包括串行或者是并行，从隔离性的实现可以看出这是一场数据的可靠性与性能之间的权衡，可靠性性高的，并发性能低(比如 Serializable)，可靠性低的，并发性能高(比如 Read Uncommited)，不同的隔离级别会有不同的问题

| -          | 脏读 | 不可重复读 | 幻读 |
| ---------- | ---- | ---------- | ---- |
| 读未提交   | √    | √          | √    |
| 读已提交   | ×    | √          | √    |
| 不可重复读 | ×    | ×          | √    |
| 串行化     | ×    | ×          | ×    |

- 脏读：事务中读取到了其他事务没有提交的数据，主要是读写完全没有加锁造成的
- 不可重复读：事务中多次读取结果不一致，因为多次读取中间，其他事务修改并提交了数据（主要原因是修改）
- 幻读：事务中多次范围读取结果不一致，因为多次读取中间，其他事务修改并提交了数据（主要原因是新增/删除）

#### 读未提交（READ UNCOMMITED）

![\color{red}{概念}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E6%A6%82%E5%BF%B5%7D)：在该隔离级别下，事务中的修改即使还没提交，对其他事务是可见的。其他事务可以读取其未提交的数据，造成脏读。![\color{red}{原理}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E5%8E%9F%E7%90%86%7D)：因为读不会加任何锁，所以写操作在读的过程中修改数据，所以会造成脏读。好处是可以提升并发处理性能，能做到读写并行。

- ![img](https:////upload-images.jianshu.io/upload_images/14523959-ee501b6df11e4255.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

#### 读提交（READ COMMITTED）

![\color{red}{概念}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E6%A6%82%E5%BF%B5%7D)：在该隔离级别下，事务中的修改如果还没提交，对其他事务是不可见的。不会造成脏读，但是多次读取会造成数据不一致的情况，会有不可重复度的问题，例如：一个事务中两次读取，在这中间他事务进行了一个更新并提交，那么两次读取的内容会不一样。![\color{red}{原理}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E5%8E%9F%E7%90%86%7D)：InnoDB在该隔离级别下读取数据不加锁而是使用了MVCC机制（详情如下）

#### 可重复读  （REPEATABLE READ）

Mysql![\color{red}{默认}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E9%BB%98%E8%AE%A4%7D)隔离级别。在一个事务内的多次读取的结果是一样的。这种级别下可以避免，脏读，不可重复读等查询问题，Innodb可以解决还可以解决幻读问题。Mysql 有两种机制可以达到这种隔离级别的效果，分别是采用读写锁和MVCC机制来实现。

- 采用MVCC的实现：使用![\color{red}{快照读}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E5%BF%AB%E7%85%A7%E8%AF%BB%7D) 的方式支持并行读写并行内部使用MVCC原理（后面介绍）
- 采用锁的实现：使用![\color{red}{当前读}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E5%BD%93%E5%89%8D%E8%AF%BB%7D) 对于SELECT... FOR UPDATE  ，SELECT ... LOCK IN SHARE MODE 等情况使用的是加锁解决机制（记录锁，[间隙锁](https://www.jianshu.com/p/d5b771e36533)等实现）

#### 串行化（SERIALIZABLE）

- 该隔离级别理解起来最简单，实现也最单。在隔离级别下除了不会造成数据不一致问题，没其他优点。

### 5 MVCC （多版本控制）

MVCC (MultiVersion Concurrency Control) 叫做多版本并发控制，主要是针对事务中并行普通读取的优化。
 InnoDB在实现的 MVCC的时候使用一致性视图来保证RC（读提交），和RR（可重复读）事务隔离级别的实现 ，![\color{red}{原理}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E5%8E%9F%E7%90%86%7D)是事务开启时，对整个库创建快照（read view），是通过每行记录的后面保存两个隐藏的列来实现的。这两个列， 一个保存了行的创建时间，一个保存了行的过期时间， 当然存储的并不是实际的时间值，而是系统版本号。每当修改数据时，版本号加一。当事务读取时，如果数据的当前版本号大于自己的事务，则查询的时候抛弃。从而实现不加锁读进而做到读写并行。MVCC在mysql中的实现依赖的是undo log与read view

- undo log :undo log 中记录某行数据的多个版本的数据。
- read view :用来判断当前版本数据的可见性
- ![img](https:////upload-images.jianshu.io/upload_images/14523959-0a36c67269dedac6.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

在不同的隔离级别下，MVCC创建read view历史版本的时机也是不同的

- 在读提交隔离级别下：视图 read-view的创建是在语句执行的时候创建的
- 在可重复读隔离级别下：视图 read-view的创建是在事务启动的时候创建的

#### 5.1 幻读问题详解

幻读在业务中存在两种情况，快照读，和当前读，MVCC策略能够解决快照读的问题，但是对于当前读则需要使用间隙锁。![\color{red}{当前读}](https://math.jianshu.com/math?formula=%5Ccolor%7Bred%7D%7B%E5%BD%93%E5%89%8D%E8%AF%BB%7D)是指读取数据库中最新版本的数据，在多个update的时候不能基于快照读。读取历史版本的数据进行更新，会导致数据不一致问题

- 快照读：当使用普通select * 进行统计的时候，使用MVCC可以保证幻读问题
- 当前读：当使用select for update 或者 select ... lock in share mode 操作的时候，insert，update，delete等操作都会被阻塞。当前读是通过手动加record lock(记录锁)和gap lock([间隙锁](https://www.jianshu.com/p/d5b771e36533) )来实现的