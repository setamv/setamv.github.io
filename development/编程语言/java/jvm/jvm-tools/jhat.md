[Back](index.md)

# Introduction
JVM堆Dump分析工具jat（JVM Heap Analysis Tool）介绍。

# Catalogue

# Content

## 概述 <a id="o">[≡](#≡)</a>
供jhat（JVM Heap Analysis Tool）命令与jmap搭配使用，来分析jmap生成的堆转储快照。jhat内置了一个微型的HTTP/HTML服务器，生成dump文件的分析结果后，可以在浏览器中查看。不过实事求是地说，在实际工作中，除非笔者手上真的没有别的工具可用，否则一般都不会去直接使用jhat命令来分析dump文件，主要原因有二：一是一般不会在部署
应用程序的服务器上直接分析dump文件，即使可以这样做，也会尽量将dump文件复制到其他机器[1]上进行分析，因为分析工作是一个耗时而且消耗硬件资源的过程，既然都要在其他机器进行，就没有必要受到命令行工具的限制了；另一个原因是jhat的分析功能相对来说（比如和JVisualVM相比）比较简陋，