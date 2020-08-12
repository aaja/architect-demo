### 什么是幻读

事务不是独立执行时发生的一种现象，例如第一个事务对一个表中的数据进行了修改，这种修改涉及到表中的全部数据行。 同时，第二个事务也修改这个表中的数据，这种修改是向表中插入一行新数据。那么，以后就会发生操作第一个事务的用户发现表中还有没有修改的数据行，就好象 发生了幻觉一样。

### mysql如何实现避免幻读

- 在快照读读情况下，mysql通过mvcc来避免幻读。
- 在当前读读情况下，mysql通过next-key来避免幻读

#### 什么是mvcc

mvcc全称是multi version concurrent control（多版本并发控制）。mysql把每个操作都定义成一个事务，每开启一个事务，系统的事务版本号自动递增。每行记录都有两个隐藏列：创建版本号和删除版本号

- select：事务每次只能读到创建版本号小于等于此次系统版本号的记录，同时行的删除版本号不存在或者大于当前事务的版本号。
- update：插入一条新记录，并把当前系统版本号作为行记录的版本号，同时保存当前系统版本号到原有的行作为删除版本号。
- delete：把当前系统版本号作为行记录的删除版本号
- insert：把当前系统版本号作为行记录的版本号

#### 什么是next-key锁

可以简单的理解为X锁（recode lock记录锁）+GAP锁（间隙锁）

#### 什么是快照读和当前读

##### 快照读

简单的select操作，属于快照读，不加锁。(当然，也有例外，下面会分析)

- select * from table where ?;

##### 当前读

特殊的读操作，插入/更新/删除操作，属于当前读，需要加锁。

- select * from table where ? lock in share mode;
- select * from table where ? for update;
- insert into table values (…);
- update table set ? where ?;
- delete from table where ?;