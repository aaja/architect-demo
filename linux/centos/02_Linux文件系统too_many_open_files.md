都知道Linux系统的特性，一切皆文件，所以在运行zabbix这样的服务时，其中重要的一个调优就是调整linux系统的最大文件句柄数，解决“too many open files”的问题，增大程序运行允许打开的文件数，提高性能。

# 一．查看系统运行打开的文件句柄数

```shell
# ulimit -a
core file size          (blocks, -c) 0
data seg size           (kbytes, -d) unlimited
scheduling priority             (-e) 0
file size               (blocks, -f) unlimited
pending signals                 (-i) 7179
max locked memory       (kbytes, -l) 64
max memory size         (kbytes, -m) unlimited
open files                      (-n) 1024
pipe size            (512 bytes, -p) 8
POSIX message queues     (bytes, -q) 819200
real-time priority              (-r) 0
stack size              (kbytes, -s) 8192
cpu time               (seconds, -t) unlimited
max user processes              (-u) 7179
virtual memory          (kbytes, -v) unlimited
file locks                      (-x) unlimited
```

在默认情况下，open files参数为1024，就是linux允许打开的最大文件数。当运行的服务需要大并发进程运行时，这显然是不够的，就会报出“too many open files”。在使用zabbix的过程中，当增大开起的进程数后会出现无法启动的情况，产看日志会有类似的报错，此时就需要修改最大文件句柄数。

# 二．修改最大文件句柄数-临时修改

```shell
# ulimit -n 2048
# ulimit -a
core file size          (blocks, -c) 0
data seg size           (kbytes, -d) unlimited
scheduling priority             (-e) 0
file size               (blocks, -f) unlimited
pending signals                 (-i) 7179
max locked memory       (kbytes, -l) 64
max memory size         (kbytes, -m) unlimited
open files                      (-n) 2048
pipe size            (512 bytes, -p) 8
POSIX message queues     (bytes, -q) 819200
real-time priority              (-r) 0
stack size              (kbytes, -s) 8192
cpu time               (seconds, -t) unlimited
max user processes              (-u) 7179
virtual memory          (kbytes, -v) unlimited
file locks                      (-x) unlimited
```

可以看到open files参数调整到了2048，这种修改方式只对当前进程有效。若重新打开一个终端或是重启进程，这个参数都不会生效，所以不建议这样使用。

# 三．修改最大文件句柄数-内核参数修改

在设置前，我们一定要考虑到系统的限制，如果你修改的参数超过了系统默认值的限制，会带来另一个悲剧的，此坑踩过。

## （1）系统默认的最大值

file-max是内核可分配的最大文件数

```shell
# cat /proc/sys/fs/file-max 
181044
```

nr_open是单个进程可分配的最大文件数

```shell
# cat /proc/sys/fs/nr_open 
1048576
```

一般系统默认内核可分配的最大文件数是内存的10%左右，可以调整到50%左右。

```shell
# grep MemTotal /proc/meminfo |awk '{printf("%d",$2/10)}'
186726
```

## （2）调整最大值

修改两个内核文件中允许的最大值，需要注意的是nr_open中的参数要小于file-max中的参数。
系统允许的最大值调整为内存的50%。

```shell
# grep MemTotal /proc/meminfo |awk '{printf("%d",$2/2)}'
933632
# echo 933632 > /proc/sys/fs/file-max 
```

单个进程可分配的最大值适当增大。

```shell
# echo 233632 > /proc/sys/fs/nr_open
```

## （3）修改系统内核参数

```shell
# vim /etc/security/limits.conf
*   soft     nofile      65535
*   hard     nofile      65535
# vim /etc/security/limits.d/20-nproc.conf
*   soft     nproc       65535
*   hard    nproc       65535
```

这里的“*”号表示对所有用户生效，可以设置指定的用户，修改后保存退出，带看一个新的终端就可生效了。

## （4）总结

a.所有进程打开的文件描述符数不能超过/proc/sys/fs/file-max
b.单个进程打开的文件描述符数不能超过user limit中nofile的soft limit
c.nofile的soft limit不能超过其hard limit
d. nofile的hard limit不能超过/proc/sys/fs/nr_open

## （5）特别提醒

有一种意外情况，如果没有注意修改系统默认允许的最大值，在limits.conf中设置的参数大于系统默认值，退出终端后，你会发现ssh无法链接的悲剧，此时如果你还有未关闭的终端链接，那恭喜你还有拯救的余地，修改sshd的配置文件。

```shell
# vim /etc/ssh/sshd_config
UsePAM yes 将这里的yes改为no
```

重启sshd服务
`#systemctl restart sshd.service`
此时可以链接终端了，调整系统内核允许的最大值，再改回sshd的配置。

# 四．关于打开文件的查看命令

## （1）查看所有进程的文件打开数

```shell
# lsof |wc -l
```

## （2）查看整个系统目前使用的文件句柄数

```shell
# cat /proc/sys/fs/file-nr
```

## （3）查看某个进程开的进程

```shell
#lsof -p pid
```

## （4）查看某个进程的的文件句柄数

```shell
#lsof -p pid|wc -l
```

## （5）查看某个目录，文件被什么进程占用

```shell
#lsof path(file)
```