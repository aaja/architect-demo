## 前言

MySQL 的 innodb 引擎之所以使用 B+tree 来存储索引，就是想尽量减少数据查询时磁盘 IO 次数。树的高度直接影响了查询的性能。一般树的高度在 3~4 层较为适宜。数据库分表的目的也是为了控制树的高度。那么如何获取树的高度呢？下面使用一个示例来说明如何获取树的高度。

## 示例数据准备

建表语句如下：

```sql
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET latin1 DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `age` (`age`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

表中插入100万条数据。数据如下：

```sql
mysql> select * from user limit 2\G
*************************** 1. row ***************************
  id: 110000
name: ab
 age: 100
*************************** 2. row ***************************
  id: 110001
name: ab
 age: 100
2 rows in set (0.00 sec)
```

## 通过查询相关数据表获取树的高度

以 MySQL5.6 版本为例说明如何获取树的高度。

### 首先获取 page_no

```sql
mysql> SELECT b.name, a.name, index_id, type, a.space, a.PAGE_NO FROM information_schema.INNODB_SYS_INDEXES a, information_schema.INNODB_SYS_TABLES b WHERE a.table_id = b.table_id AND a.space <> 0 and b.name='test/user';
+-----------+---------+----------+------+-------+---------+
| name      | name    | index_id | type | space | PAGE_NO |
+-----------+---------+----------+------+-------+---------+
| test/user | PRIMARY |       22 |    3 |     6 |       3 |
| test/user | name    |       23 |    0 |     6 |       4 |
| test/user | age     |       24 |    0 |     6 |       5 |
+-----------+---------+----------+------+-------+---------+
3 rows in set (0.00 sec)
```

page_no 是索引树中Root页的序列号。其它各项的含义可以参照：
https://dev.mysql.com/doc/refman/5.6/en/innodb-sys-indexes-table.html

### 再读取页的大小

```sql
mysql> show global variables like 'innodb_page_size';
+------------------+-------+
| Variable_name    | Value |
+------------------+-------+
| innodb_page_size | 16384 |
+------------------+-------+
1 row in set (0.00 sec) 
```

### 最后读取索引树的高度

```sql
$ hexdump -s 49216 -n 10 ./user.ibd
000c040 0200 0000 0000 0000 1600
000c04a
```

可以发现 PAGE_LEVEL 为 0200，表示这棵二级索引树的高度为 3。后面的 1600 是索引的 index_id 值。十六进制的 16 转换为十进制数字是 22。这个 22 正好就是上面主键的 index_id。
上面 hexdump 命令中 49216 是怎么算出来的？公式是 page_no * innodb_page_size + 64。
3*16384+64=49216

我们在用这个方式查看下其他两个索引的高度。

```sql
$ hexdump -s 65600 -n 10 ./user.ibd
0010040 0100 0000 0000 0000 1700
001004a
$ hexdump -s 81984 -n 10 ./user.ibd
0014040 0200 0000 0000 0000 1800
001404a
```

可见，name 索引的高度是 2，age 索引的高度是 3。

## 根据索引的结构估算

如果你没有数据库服务器的权限。自己也可以根据数据库索引结构进行估算树的高度。
根据 B+Tree 结构，非叶子节点存储的是索引数据，叶子节点存储的是每行的所有数据。
非叶子节点每个索引项的大小是，数据大小+指针大小。假设指针大小为 8 个字节。每页不会被占满，预留1/5的空隙。下面我们估算下 name 和 age 两个索引的高度。

### name 索引高度估算

非叶子节点每页存放的索引项数量。每页大小是 16k。name 的值为 ab。占2个字节。每项数据大小是 2+8=10字节。每页能存放的索引项数量是 16384 * 0.8 / 10 = 1310 个。
叶子节点每页存放的索引数量。每页大小是 16k。每项数据大小是 4+2+8=14 个字节。没页能存放的索引数量是 16384 * 0.8 / 14 = 936 个。
两层能存放 1310*936=1226160 个数据记录。可见120万条记录以下，树的高度为2。

### age 索引高度估算

非叶子节点每页存放的索引项数量。每页大小是 16k。age 的类型为 int。占4个字节。每项数据大小是 4+8=12字节。每页能存放的索引项数量是 16384 * 0.8 / 12 = 1092 个。
叶子节点每页存放的索引数量。每页大小是 16k。每项数据大小是 4+4+8=16 个字节。没页能存放的索引数量是 16384 * 0.8 / 16 = 819 个。
两层能存放 1092*819=894348 个数据记录。可见90万条记录以下，树的高度为2。100万条为 3 层。

## 其它工具

还有一个小工具可以查看。InnoDB 表空间可视化工具innodb_ruby
https://www.cnblogs.com/cnzeno/p/6322842.html