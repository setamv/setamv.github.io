[Back](index.md)

# Introduction
jdb（Java Debugger）是一个可以对JVM执行断点跟踪调试的命令行工具。该命令行工具可以设置断点、查看断点处对象或变量的值。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [启动jdb会话](#ss)
- [设置和清除断点、监视变量的值](#sb)
- [参考文档](#ref)

# Content

## 概述 <a id="o">[≡](#≡)</a>
The Java Debugger (JDB) is a simple command-line debugger for Java classes. The jdb command demonstrates the Java Platform Debugger Architecture (JDBA) and provides inspection and debugging of a local or remote Java Virtual Machine (JVM)

使用jdb对JVM进程进行跟踪和调试，包括以下几个步骤：
1. 启动jdb会话
2. 设置断点
3. 跟踪断点

## 启动jdb会话 <a id="ss">[≡](#≡)</a>
有两种常用的方式开启一个jdb会话：
1. 通过jdb命令运行指定的Main Class
    这种方式适用于JVM还未启动的情况。此时，jdb将启动一个虚拟机并将指定的Main Class作为入口。    
    开启方式和java命令一样，你只需将java命令关键字替换成jdb即可，java命令支持的参数都可以用在jdb命令中，如：   
    `$ jdb HelloWorld`   
    其中，HelloWorld为当前目录下的HelloWorld.class。
2. 将jdb挂载到一个已经启动的JVM上
    这种方式适用于JVM已经启动并处于运行中的情况。此时，jdb将挂载到已运行的JVM进程。     
    - 这种方式有一个限制，就是必须以指定的参数启动JVM进程，如下所示：    
        `$ java -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n MainClass` 
        其中：   
        * address 指定JDWP（Java Debug Wire Protocol）协议的端口号，如果不指定，将自动分配一个随机端口号，并且会将其打印到标准输出中。如下所示："Listening for transport dt_socket at address: 37973"。      
        * MainClass JVM启动程序的入口，即Main方法所在的类。  
    - 然后，将jdb挂载到JVM进程即可，命令如下：    
        `$ jdb -attach address`   
        其中，addressJDWP协议端口号，即JVM启动时-agentlib参数中指定的address选项的值。

## 设置和清除断点、监视变量的值 <a id="sb">[≡](#≡)</a>     
### 设置断点      
当开启了jdb会话之后，就可以在jdb会话中进行断点的设置了，有两种方法：   
- stop at
    `stop at package.path.ClassName:n` 表示在类ClassName的第n行设置断点 
- stop in  
    `stop in java.lang.String.length` 表示在类String的length方法的开始处设置断点 

### 清除断点
如果要清除一个断点，直接将设置断点命令中的`stop at`或`stop in`替换成`clear`即可，如：
`clear package.path.ClassName:n` 表示清除类ClassName的第n行处的断点。  
`clear java.lang.String.length` 表示清除类String的length方法的开始处的断点。

### 查看已设置的断点
当需要查看设置了哪些断点时，只需要执行`stop`或`clear`命令即可。

### 断点继续执行
要继续断点的执行，首先要确保jdb会话中的当前线程是正处于断点处的那一个线程，因为可能同时存在多个线程处于断点处等待。
如果当前有多个线程在断点处，则可以：   
1. 通过命令`$ threads`查看当前JVM中的所有线程，并记住线程号    
2. 通过命令`$ thread n`将线程号为n的线程切换为当前线程（假设该线程为需要继续断点执行的线程）。    
最后通过`$ cont`命令即可让当前线程继续断点执行。

### 查看断点处的变量值
`$ print variable`    
`$ dump variable`    
上面两个命令都可以在断点处打印变量或属性variable的值。区别是：如果variable是一个对象（非primitive类型），`print`命令只打印一个对象的描述信息，而`dump`命令将打印对象中所有属性的值。

### 查看线程的堆栈信息
`where`命令可以打印出当前线程的堆栈信息；   
`whereall`命令可以打印所有线程的堆栈信息。

## 参考文档 <a id="ref">[≡](#≡)</a>  
### man帮助文档
`$ man jdb`

### 官网关于JPDA(Java Platform Debugger Architecture)的文档
http://docs.oracle.com/javase/8/docs/technotes/guides/jpda/index.html        