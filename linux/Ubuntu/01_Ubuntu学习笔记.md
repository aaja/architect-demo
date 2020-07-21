# Ubuntu简介

Ubuntu是一个以桌面应用为主的Linux操作系统，其名称来自非洲南部祖鲁语或豪萨语的“ubuntu”一词，意思是“和善人”、“众人皆同源”，是非洲传统的一种价值观。Ubuntu基于Debian发行版和GNOME桌面环境，而从11.04版起，Ubuntu发行版放弃了Gnome桌面环境，改为[Unity](https://link.jianshu.com?t=http://baike.baidu.com/subview/1097775/5930556.htm)，与Debian的不同在于它每6个月会发布一个新版本。Ubuntu的目标在于为一般用户提供一个最新的、同时又相当稳定的主要由自由软件构建而成的操作系统。Ubuntu具有庞大的社区力量，用户可以方便地从社区获得帮助。Ubuntu是世界最受欢迎操作系统之一，是一款完全开源，很适用于初学Linux的用户。

# Ubuntu的安装流程

1. [安装镜像下载地址](https://link.jianshu.com?t=http://releases.ubuntu.com/16.10/ubuntu-16.10-desktop-amd64.iso)
2. 制作USB启动盘。
   - 准备一个容量大于4GB的U盘（请提前将U盘中内容备份，然后使用FAT32格式格式化该U盘）；
   - 在Windows操作系统下使用 [Universal USB Installer]([http://www.pendrivelinux.com/universal-](https://link.jianshu.com?t=http://www.pendrivelinux.com/universal-)  usb-installer-easy-as-1-2-3/) 选择下载的安装镜像（ISO）文件并选择U盘所在盘符制作USB启动盘。
3. 双系统安装：在现有硬盘上调整出不少于40GB的干净硬盘空间（不要分区），同时还要确保硬盘上除 Windows 系统分区这一个主分区以外，其他都是逻辑分区。
4. 使用制作好的USB启动盘引导计算机，并按照安装向导完成后续安装步骤。

# Ubuntu使用过程中遇到的问题

- 中文输入法问题。日常生活中我们常常需要输入中文，例如：网络聊天、编辑文档，查询网页等，Ubuntu中虽然可以使用IBus或者Linux版的搜狗输入法，但其在性能和用户体验上均存在或多或少的缺陷，该问题给初次使用Ubuntu的用户带来了诸多不便。
- 聊天工具问题。Windows下最流行的聊天工具QQ在Ubuntu系统中默认是不支持的，对于习惯了使用QQ进行网络聊天的用户只能使用WebQQ啦，感觉较为麻烦。（PS：虽然可以安装Linux版的QQ，可是挺不好用的哈）
- 网络视频播放器问题。由于Ubuntu对中文的支持不够，因此其下的各种软件对中文的解码会存在诸多问题。Ubuntu中的网络视频播放器（例如：SMPlayer、VLC media player）几乎只能播放本地视频，对于网络视频的支持度不理想。
- 网络游戏问题。Windows下比较受欢迎的网游（例如：LOL）尚未有Linux版本，因此对于这些网游只能去Windows下玩喽。

# 常用快捷键

Ctrl+Alt+F1：进入终端界面。
 Ctrl+Alt+F7：回到图形界面。
 Ctrl+Alt+T：进入伪终端，当然我们的大部分操作只需要在伪终端下操作即可。
 Ctrl+D：关闭伪终端。
 Ctrl+Alt+L：锁屏。

# 终端常用快捷键

Shift+Pageup/Page down：向上向下翻页
 Tab：命令补全功能
 Ctrl+Shift+c：复制
 Ctrl+Shift+v：粘贴
 Ctrl+a：移动到当前行开始位置
 Ctrl+e：移动到当前行结尾
 Ctrl+k：删除此处至末尾所有内容
 Ctrl+u：删除此处至开始所有内容
 Ctrl+l：刷新屏幕
 Ctrl+c：杀死当前任务
 Ctrl+s：挂起当前shell
 Ctrl+q：重新启用挂起的shell
 Alt+u：把当前词转化为大写
 Alt+l：把当前词转化为小写
 Alt+c：把当前词变成首字母大写

# 总结

经过这几天对Ubuntu的学习，觉得Linux系统相对于Windows系统更多使用的是命令行方式而非图形界面，Windows精于图形界面，Linux则更注重系统操作。Ubuntu16.10的桌面系统相比于早期的Ubuntu系统有了很大的改进，这给习惯使用图形界面的用户带来了诸多便利。Windows就像是一台组装好的电脑，用户可以直接进行使用；Ubuntu就像是一堆用于组装电脑的零件，用户可以根据自身需求进行组装，甚至创造新的零件，这个过程能够使我们对Ubuntu系统的认识更加深刻。为了学习娱乐两不误，我们可以安装双系统，Ubuntu用于学习，Windows用于娱乐_。

- [Ubuntu官网](https://link.jianshu.com?t=http://cn.ubuntu.com/)
- [在线简单体验Ubuntu的桌面环境](https://link.jianshu.com?t=http://tour.ubuntu.com/zh-CN/)
- [Ubuntu新手指南](https://link.jianshu.com?t=http://thoughtworks-academy.github.io/linux-guide/zh-hans/)
- [Ubuntu中文社区](https://link.jianshu.com?t=http://forum.ubuntu.org.cn/)