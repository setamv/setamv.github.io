# Java应用占用CPU过高定位
当Java应用占用CPU资源过高时，可以通过JVM的线程堆栈信息查看当前占用CPU资源较高的线程执行信息。步骤如下：

```
# 使用top命令查看占用CPU资源较高的进程
$ top

# 使用top命令查看指定进程中，占用CPU资源过高的线程。下面命令中，pid为进程ID
$ top -H -p pid    

# 打印虚拟机线程堆栈。下面命令中，pid为进程ID
$ jstack -l pid >> jstack.log    

# 将耗用CPU过高的线程ID转为16进制。下面命令中，tid为线程ID，即命令 `top -H -p pid` 的结果
$ printf "%x\n" tid         

# 如果是内存耗尽，使得垃圾收集器在不停的回收内存，导致CPU耗用过高，使用jmap查看当前虚拟机的内存统计信息，下面命令中，pid为进程ID
$ jmap -heap pid            

# 如果要导出当前虚拟机的堆镜像，可以使用jmap，下面命令中，filename为保存堆镜像的文件全路径；pid为java虚拟机进程ID
$ jmap -dump:format=b,file=filename.hprof pid        

# jmap导出的堆镜像文件，可以下载下来，可以使用 jvisualvm 查看对象图
```  