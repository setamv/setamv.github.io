[首页](/index.md) << ... << [索引](index.md)

# for循环语句

## 语法
```
for 变量 in 字符串
do
    执行语句块
done
```

## 示例
+ 示例代码
    ```
    #!/bin/bash
    # desc: for循环语句
    # author: setamv
    # created: 2019-02-05

    # 打印seq数字列表。`seq n`用于创建一个由1到n组成的数组
    echo '------------打印`seq 3`--------------'
    for i in `seq 3`
    do
            echo $i
    done

    # 求和
    echo '-----------求1到100的和-------------'
    total=0
    for ((i=1; i<=100; i++));
    do
            total=`expr $i + $total`    # 也可以使用这个代替expr：$(( $i + $total ))
    done
    echo $total

    # 打印目录下的所有文件列表
    echo '----------打印目录下的文件列表----------'
    dir=/root/shell
    for file in `find $dir -name "*.sh"`
    do
            if [ -f $file ]; then
                    echo $file
            fi
    done

    # 读取文件的前三行内容并打印
    echo '----------读取文件的前三行内容并打印-----------'
    file=/root/shell/for.sh
    i=0
    for line in `cat $file`
    do
            if [[ $i -ge 3 ]]; then
                    break;
            fi
            (( i++ ))
            echo $line
    done 
    ```

+ 执行结果
    ```
    [root@centos-7-192 shell]# ./for.sh 
    ------------打印`seq 3`--------------
    1
    2
    3
    -----------求1到100的和-------------
    5050
    ----------打印目录下的文件列表----------
    /root/shell/bracket.sh
    /root/shell/double_bracket.sh
    /root/shell/ex_var.sh
    /root/shell/if.sh
    /root/shell/for.sh
    /root/shell/brace.sh
    ----------读取文件的前三行内容并打印-----------
    #!/bin/bash
    #
    desc:
    ```