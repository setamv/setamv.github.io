[首页](/index.md) << ... << [索引](index.md)

# sed指定地址

## 概述
默认情况下，sed中的所有命令会应用到输入的每一行内容，如果只需要应用到部分行，必须使用行地址。
有两种方式可以指定行地址：
1. 使用一个数值范围指定；
2. 使用一个文本模式过滤行

指定行地址的命令格式：
1. [address] command
2. address{
        command1
        command2
        command3
    }
    注意：
    1. 也可以将多个命令写在一行，使用分号隔开：address{command1; command2}
    2. address和后面的大括号之间不能有空格

## 使用一个数值范围指定
使用一个数值范围指定命令将要应用的行时，既可以只指定一行，也可以通过一个范围指定多行，范围的格式为：“起始行号,结束行号”，如果要从某行开始一直到文件末尾，可以使用“$”符号替换结束行号即可。
例如：
`# sed '2s/dog/cat/' file`指定应用到第2行；
`#sed '2,5s/dog/cat/'`指定应用到第2到5行；
`#sed '2,$s/dog/cat'`指定应用到第2到最后一行；

### 示例
```
[root@centos-7-192 ~]# cat data
This is a cat
the cat like fish
can you give the cat a fish?
[root@centos-7-192 ~]# sed '2,3s/cat/dog/; 3s/fish/bone/' data
This is a cat
the dog like fish
can you give the dog a bone?
```
上面的示例，将文件data中第2、3行中的cat替换成dog，并且将第3行的fish替换成bone

## 使用一个文本模式过滤行
sed允许使用一个文本模式过滤命令应用的行，其格式为：
    `/pattern/command`
其中，pattern可以包含正则表达式
### 示例
```
[root@centos-7-192 ~]# sed -n '/root/s/bash/csh/p' /etc/passwd
root:x:0:0:root:/root:/bin/csh
```
上面的示例，将/etc/passwd文件中包含“root”的行中的bash替换为csh。

## 分组命令
如果需要在指定的行地址上执行多个命令，可以使用分组命令，格式：
    address {
        command1
        command2
        command3
    }

### 示例
```
[root@centos-7-192 ~]# sed -n '/root.*bash/{
s/root/system/
s/bash/sh/p
}' /etc/passwd
system:x:0:0:root:/root:/bin/sh
```
上面的命令将/etc/passwd文件中包含“root.*bash”的行的文本，root替换为system、bash替换为sh后输出文本发生替换的行。
也可以将多个命令写在一行：`sed -n '/root.*bash/{s/root/system/; s/bash/sh/p}' /etc/passwd`