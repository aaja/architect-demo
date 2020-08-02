### 1 参数文件

> mysql --help | grep my.cnf  ##MySQL的参数文件是以文本的方式存储的

#### 1.1 参数类型

mysql中的参数大致分两类，分别是：

- 静态参数（static）
- 动态参数（dynamic）

动态参数意味着可以在实例运行过程中进行更改，静态参数说明在整个实例的生命周期内都不能被更改。

### 2 日志文件

#### 2.1 错误日志文件

```
mysql> SHOW VARIABLES LIKE 'LOG_ERROR';
+---------------+-------------------+
| Variable_name | Value             |
+---------------+-------------------+
| log_error     | .\mysql_error.log |
+---------------+-------------------+
1 row in set (0.03 sec)
```

#### 2.2 慢查询日志

```
mysql> SHOW VARIABLES LIKE 'LONG_QUERY_TIME';
+-----------------+-----------+
| Variable_name   | Value     |
+-----------------+-----------+
| long_query_time | 10.000000 |
+-----------------+-----------+
1 row in set (0.03 sec)
```

参数log_output指定了慢查询输出的格式，默认为FILE，可以将他设置为TABLE，然后查询mysql架构下的slow_log表；如下

```
mysql> SHOW VARIABLES LIKE 'log_output';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_output    | FILE  |
+---------------+-------+
1 row in set (0.05 sec)
```

修改输出格式的命令是

```
mysq1>SET GLOBAL 1og_ output= 'TABLE' ;
```

人为设置睡眠十秒

```
SELECT SLEEP(10);
```



#### 2.3 查询日志

默认文件名是：主机名.log

#### 2.4 二进制日志(binary log)

记录了对mysql数据库执行更改的所有操作，不包括SELETE AND SHOW，查询语句

```
mysql> SHOW MASTER STATUS;
File: mysqld. 000008
mysql> SHOW BINLOG EVENTS IN mysqld. 000008;

```

##### 2.4.1 二进制日志的主要作用

- 恢复

- 复制

- 审计

  通过配置log_bin [=name]可以启动二进制日志，如果不指定name,则默认二进制日志文件名是主机名，所在路径是datadir目录下，相关配置：

  ```shell
  max_binlog_size
  binlog_cache_size
  sync_binlog
  binlog-do-db
  binlog-ignore-db
  log-slave-update
  binlog_format
  ```

  

### 3 套接字文件

前面提到过，在UNIX系统下本地连接MySQL可以采用UNIX域套接字方式，这种方式需要一个套接字(socket) 文件。套接字文件可由参数socket控制。- -般在/tmp目录下，名为mysql.sock:

```sql
mysql> SHOW VARIABLES LIKE 'socket';
+---------------+---------------------------+
| Variable_name | Value                     |
+---------------+---------------------------+
| socket        | E:/xampp/mysql/mysql.sock |
+---------------+---------------------------+
1 row in set (0.09 sec)
```



### 4 pid文件

当MySQL实例启动时，会将自己的进程ID写人一个文件中一该文件即为 pid文件.该文件可由参数pid_ file 控制，默认位于数据库目录下，文件名为主机名.pid;

```
mysql> SHOW VARIABLES LIKE 'pid_file';
+---------------+-------------------------------+
| Variable_name | Value                         |
+---------------+-------------------------------+
| pid_file      | E:\xampp\mysql\data\mysql.pid |
+---------------+-------------------------------+
1 row in set (0.06 sec)
```



### 5 表结构定义文件

### 6 innoDB存储引擎文件