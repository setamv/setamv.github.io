[首页](/index.md) << ... << [索引](index.md)

# sed文本替换

## 内容导航

## 文本替换命令格式
文本替换命令格式为：
    `s/pattern/replacement/flags`
其中，flags有四种可选的值，分别为：
+ A : 一个数字，指第几个匹配的文本应该被替换，
    例如：`echo "Hello world." | sed 's/o/i/2'`的结果是将第二个o替换为i，变为"Hello wirld."
+ g : 替换所有匹配的文本。sed默认值替换第一个匹配的文本
+ p : 输出发生文本替换的行（即发生文本替换的行会被多输出一次）， 该flag一般与sed命令的-n选项结合使用，-n选项将不输出sed的结果，他们结合使用的结果就是：只输出发生文本替换的行。例如：
    ```
    [root@centos-7-192 ~]# cat data 
    This is a cat
    the cat like fish
    can you give the cat a fish?
    [root@centos-7-192 ~]# sed 's/fish/bone/' data
    This is a cat
    the cat like bone
    can you give the cat a bone?
    [root@centos-7-192 ~]# 
    [root@centos-7-192 ~]# sed -n 's/fish/bone/p' data
    the cat like bone
    can you give the cat a bone?
    ```
    data文件有3行内容，如果不指定-n选项和p标志，data文件的所有内容都会输出；但是指定-n选项和p标志后，就只输出发生文本替换的两行内容了。
+ w file : 将发生文本替换的内容写入文件file中
    w标志相当于制定了-n选项和p标志，只是将输出结果写入file指定的文件了。

## 指定字符串界定符
使用sed进行文本替换时，因为文本替换命令格式中使用斜杠“/”界定pattern、replacement、flags等，所以，如果pattern货replacement中本身含有斜杠，将需要转移，如，替换“/etc/passwd”文件中的shell执行文件时，需要按如下格式转移斜杠：`sed 's/\/bin\/bash/\/bin\/csh/' /etc/passwd`
如果不想这么麻烦（有时候还容易出错），可以指定新的字符串界定符，例如：`sed 's!/bin/bash!/bin/csh!' /etc/passwd`，指定惊叹号“!”为界定符。所有原来使用斜杠的地方都替换为惊叹号。

