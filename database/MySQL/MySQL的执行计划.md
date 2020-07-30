MYSQL 执行计划我知道三种，分别是： 

### EXPLAIN

使用explain 查看执行计划， 5.6后可以加参数 explain format=json xxx 输出json格式的信息

```SQL
mysql> explain select 1 from test;
+----+-------------+-------+-------+---------------+------------+---------+------+------+-------------+
| id | select_type | table | type  | possible_keys | key        | key_len | ref  | rows | Extra       |
+----+-------------+-------+-------+---------------+------------+---------+------+------+-------------+
|  1 | SIMPLE      | test  | index | NULL          | test_index | 4       | NULL | 7    | Using index |
+----+-------------+-------+-------+---------------+------------+---------+------+------+-------------+
1 row in set (0.03 sec)

mysql> explain format = json select 1 from test;
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| EXPLAIN                                                                                                                                                                                                                                                               |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| {
  "query_block": {
    "select_id": 1,
    "table": {
      "table_name": "test",
      "access_type": "index",
      "key": "test_index",
      "key_length": "4",
      "used_key_parts": ["id"],
      "rows": 7,
      "filtered": 100,
      "using_index": true
    }
  }
} |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.10 sec)

mysql> 
```

### PROFILING

使用profiling详细的列出在每一个步骤消耗的时间，**前提是先执行一遍语句**。

> ```sql
> #打开profiling 的设置
> SET profiling = 1;
> SHOW VARIABLES LIKE '%profiling%';
> 
> #查看队列的内容
> show profiles;  
> #来查看统计信息
> show profile block io,cpu for query 3;
> ```

```sql
mysql> SET profiling = 1; 
Query OK, 0 rows affected (0.00 sec)

mysql> SHOW VARIABLES LIKE '%profiling%';
+------------------------+-------+
| Variable_name          | Value |
+------------------------+-------+
| have_profiling         | YES   |
| profiling              | ON    |
| profiling_history_size | 15    |
+------------------------+-------+
3 rows in set (0.03 sec)

mysql> show profiles;  
+----------+------------+-----------------------------------+
| Query_ID | Duration   | Query                             |
+----------+------------+-----------------------------------+
|        1 | 0.00125900 | SHOW VARIABLES LIKE '%profiling%' |
+----------+------------+-----------------------------------+
1 row in set (0.03 sec)

mysql> show profile block io,cpu for query 3;
Empty set

mysql> 
```

### OPTIMIZER_TRACE

Optimizer trace是MySQL5.6添加的新功能，可以看到大量的内部查询计划产生的信息, 先打开设置，然后执行一次sql,最后查看`information_schema`.`OPTIMIZER_TRACE`的内容

```sql
#打开设置
SET optimizer_trace='enabled=on';  
#最大内存根据实际情况而定， 可以不设置
SET OPTIMIZER_TRACE_MAX_MEM_SIZE=1000000;
SET END_MARKERS_IN_JSON=ON;
SET optimizer_trace_limit = 1;
SHOW VARIABLES LIKE '%optimizer_trace%';

#执行所需sql后，查看该表信息即可看到详细的执行过程
SELECT * FROM `information_schema`.`OPTIMIZER_TRACE`;
```

