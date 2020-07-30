![](https://aaja.gitee.io/picture/architect-demo/InnoDB存储引擎模块.png)

INNODB存储引擎是第一个完整支持ACID事务的MySQL引擎，其特点是行锁设计、支持MVCC、支持外键、提供一致性非锁定读等。

### 1 INNODB体系架构

#### 1.1 后台线程

##### 1.1.1 Master Thread 

是一个非常核心的后台线程，主要负责将缓存池中的数据异步刷新到磁盘，保证数据的一致性，包括脏页的刷新，合并插入缓存，undo页的回收等。

##### 1.1.2 IO Thread 

在innodb中大量的使用了AIO来处理写IO的请求，相关参数如下：

查看INNODB的版本信息

```sql
mysql> SHOW VARIABLES LIKE 'INNODB_VERSION';
+----------------+---------+
| Variable_name  | Value   |
+----------------+---------+
| innodb_version | 10.4.13 |
+----------------+---------+
1 row in set (0.08 sec)
```

查看INNODB的IO Thread

```sql
mysql> SHOW VARIABLES LIKE 'INNODB_%IO_THREADS';
+-------------------------+-------+
| Variable_name           | Value |
+-------------------------+-------+
| innodb_read_io_threads  | 4     |
| innodb_write_io_threads | 4     |
+-------------------------+-------+
2 rows in set (0.03 sec)
```

查看引擎的状态

```sql
| InnoDB |      |
=====================================
2020-07-30 16:27:18 0x67dc INNODB MONITOR OUTPUT
=====================================
Per second averages calculated from the last 48 seconds
-----------------
BACKGROUND THREAD
-----------------
srv_master_thread loops: 0 srv_active, 0 srv_shutdown, 50514 srv_idle
srv_master_thread log flush and writes: 50514
----------
SEMAPHORES
----------
OS WAIT ARRAY INFO: reservation count 23
OS WAIT ARRAY INFO: signal count 9
RW-shared spins 0, rounds 30, OS waits 4
RW-excl spins 0, rounds 4, OS waits 0
RW-sx spins 0, rounds 0, OS waits 0
Spin rounds per wait: 30.00 RW-shared, 4.00 RW-excl, 0.00 RW-sx
------------
TRANSACTIONS
------------
Trx id counter 543
Purge done for trx's n:o < 539 undo n:o < 0 state: running but idle
History list length 36
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 284003757822504, not started
0 lock struct(s), heap size 1128, 0 row lock(s)
---TRANSACTION 284003757818288, not started
0 lock struct(s), heap size 1128, 0 row lock(s)
---TRANSACTION 284003757814072, not started
0 lock struct(s), heap size 1128, 0 row lock(s)
---TRANSACTION 284003757809856, not started
0 lock struct(s), heap size 1128, 0 row lock(s)
--------
FILE I/O
--------
I/O thread 0 state: native aio handle (insert buffer thread)
I/O thread 1 state: native aio handle (log thread)
I/O thread 2 state: native aio handle (read thread)
I/O thread 3 state: native aio handle (read thread)
I/O thread 4 state: native aio handle (read thread)
I/O thread 5 state: native aio handle (read thread)
I/O thread 6 state: native aio handle (write thread)
I/O thread 7 state: native aio handle (write thread)
I/O thread 8 state: native aio handle (write thread)
I/O thread 9 state: native aio handle (write thread)
Pending normal aio reads: [0, 0, 0, 0] , aio writes: [0, 0, 0, 0] ,
 ibuf aio reads:, log i/o's:, sync i/o's:
Pending flushes (fsync) log: 0; buffer pool: 0
429 OS file reads, 135 OS file writes, 4 OS fsyncs
0.00 reads/s, 0 avg bytes/read, 0.00 writes/s, 0.00 fsyncs/s
-------------------------------------
INSERT BUFFER AND ADAPTIVE HASH INDEX
-------------------------------------
Ibuf: size 1, free list len 0, seg size 2, 0 merges
merged operations:
 insert 0, delete mark 0, delete 0
discarded operations:
 insert 0, delete mark 0, delete 0
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
Hash table size 4441, node heap has 0 buffer(s)
0.00 hash searches/s, 0.00 non-hash searches/s
---
LOG
---
Log sequence number 435624
Log flushed up to   435624
Pages flushed up to 435624
Last checkpoint at  435615
0 pending log flushes, 0 pending chkp writes
11 log i/o's done, 0.00 log i/o's/second
----------------------
BUFFER POOL AND MEMORY
----------------------
Total large memory allocated 33554432
Dictionary memory allocated 36288
Buffer pool size   1002
Free buffers       580
Database pages     422
Old database pages 0
Modified db pages  0
Percent of dirty pages(LRU & free pages): 0.000
Max dirty pages percent: 75.000
Pending reads 0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 0, not young 0
0.00 youngs/s, 0.00 non-youngs/s
Pages read 291, created 131, written 131
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 422, unzip_LRU len: 0
I/O sum[0]:cur[0], unzip sum[0]:cur[0]
--------------
ROW OPERATIONS
--------------
0 queries inside InnoDB, 0 queries in queue
0 read views open inside InnoDB
Process ID=24932, Main thread ID=23804, state: sleeping
Number of rows inserted 0, updated 0, deleted 0, read 12
0.00 inserts/s, 0.00 updates/s, 0.00 deletes/s, 0.00 reads/s
Number of system rows inserted 0, updated 0, deleted 0, read 0
0.00 inserts/s, 0.00 updates/s, 0.00 deletes/s, 0.00 reads/s
----------------------------
END OF INNODB MONITOR OUTPUT
============================
```

可以看到IO Thread 0为insert buffer thread，IO Thread 1 为log thread，之后就是根据参数innode_read_io_threads和innodb_write_io_threads来设置的读写线程，并且读线程的ID总是小于写线程。

##### 1.1.3 Purge Thread

事务被提交后，其所使用的undo log可能不再需要，因此需要PurgeThread来回收已经使用并分配的undo页。在InnoDB 1.1 版本之前，purge 操作仅在InnoDB存储引擎的MasterThread中完成。而从InnoDB1.1版本开始，purge操作可以独立到单独的线程中进行，以此来减轻Master Thread的工作，从而提高CPU的使用率以及提升存储引擎的性能。用户可以在MySQL数据库的配置文件中添加如下命令来启用独立的Purge Thread:

```shell
[mysqld]
innodb_purge_threads=1
```

设置

```sql
mysql> SELECT VERSION();
+-----------------+
| VERSION()       |
+-----------------+
| 10.4.13-MariaDB |
+-----------------+
1 row in set (0.06 sec)
mysql> SHOW VARIABLES LIKE 'innodb_purge_threads';
+----------------------+-------+
| Variable_name        | Value |
+----------------------+-------+
| innodb_purge_threads | 4     |
+----------------------+-------+
1 row in set (0.03 sec)
```

##### 1.1.4 Page Cleaner Thread

Page Cleaner Thread是在InnoDB 1.2.x 版本中引人的。其作用是将之前版本中脏页的刷新操作都放人到单独的线程中来完成。而其目的是为了减轻原Master Thread的工作及对于用户查询线程的阻塞，进- -步提高InnoDB存储引擎的性能。

#### 1.2 内存

##### 1.2.1 缓存池

```sql
mysql> SHOW VARIABLES LIKE 'innodb_buffer_pool_size';
+-------------------------+----------+
| Variable_name           | Value    |
+-------------------------+----------+
| innodb_buffer_pool_size | 16777216 |
+-------------------------+----------+
1 row in set (0.11 sec)
```

具体来看，缓冲池中缓存的数据页类型有:索引页、数据页、undo 页、插入缓冲(insert buffer)、自适应哈希索引(adaptive hash index)、InnoDB 存储的锁信息( lock info)、数据字典信息(data dictionary)等。下面是innodb内存数据对象的结构：

![](https://aaja.gitee.io/picture/architect-demo/innodb内存数据对象.png)

InnoDB1.0开始允许有多个缓存池实例，每个页根据哈希值平均分配到不同的缓存池实例中，这样做的好处是减少数据库内部的资源竞争，增加数据库的并发处理能力，配置缓存池实例：

```sql
mysql> SHOW VARIABLES LIKE 'innodb_buffer_pool_instances';
+------------------------------+-------+
| Variable_name                | Value |
+------------------------------+-------+
| innodb_buffer_pool_instances | 1     |
+------------------------------+-------+
1 row in set (0.03 sec)
```

继续看

```sql
SHOW ENGINE INNODB STATUS;
```

MySQL5.6开始可以通过information_schema架构下的表INNODB_BUFFER_POOL_STATS查看缓存池的使用状态

```sql
mysql> SELECT POOL_ID, POOL_SIZE, FREE_BUFFERS, DATABASE_PAGES FROM INNODB_BUFFER_POOL_STATS;;
+---------+-----------+--------------+----------------+
| POOL_ID | POOL_SIZE | FREE_BUFFERS | DATABASE_PAGES |
+---------+-----------+--------------+----------------+
|       0 |      1002 |          580 |            422 |
+---------+-----------+--------------+----------------+
1 row in set (0.07 sec)
```

##### 1.2.2 LRU List、Free List 和 Flush List

- LRU List : Latest Recent Used : 最近最少使用算法，在InnoDB存储引擎中，缓存池中页的大小默认是16KB；

  查看使用情况命令

  ```sql
  SHOW ENGINE INNODB STATUS;
  ```

##### 1.2.3 重做日志缓存（redo log buffer）

```sql
mysql> SHOW VARIABLES LIKE 'innodb_log_buffer_size';
+------------------------+---------+
| Variable_name          | Value   |
+------------------------+---------+
| innodb_log_buffer_size | 8388608 |
+------------------------+---------+
1 row in set (0.04 sec)

```

##### 1.2.4 额外内存池



#### 1.3 InnoDB的关键特性

- 插入缓存（Insert Buffer）
- 两次写（Double Write）
- 自适应哈希索引（Adaptive Hash Index）
- 异步IO（Async IO）
- 刷新领接页（Flush Neighbor Page）

##### 1.3.1 插入缓存

###### 1.3.1.1 Insert Buffer

###### 1.3.1.2 Change Buffer

###### 1.3.1.3 Merge Insert Buffer

##### 1.3.2 两次写

##### 1.3.3 自适应哈希索引

##### 1.3.4 异步IO

##### 1.3.5 刷新领接页

#### 1.4 启动、关闭与恢复