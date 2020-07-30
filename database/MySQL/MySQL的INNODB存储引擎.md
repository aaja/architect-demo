INNODB存储引擎是第一个完整支持ACID事务的MySQL引擎，其特点是行锁设计、支持MVCC、支持外键、提供一致性非锁定读等。

### 1 INNODB体系

#### 1.1 后台线程

- Master Thread 

  是一个非常核心的后台线程，主要负责将缓存池中的数据异步刷新到磁盘，保证数据的一致性，包括脏页的刷新，合并插入缓存，undo页的回收等。

- IO Thread 

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

