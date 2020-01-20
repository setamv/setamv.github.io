[首页](/index.md) << ... << [索引](index.md)

# sed转换命令

## 概述
sed转换命令（transform command）对单个字符执行转换，单一次可以指定多个需要转换的字符，命令格式如下所示：
    [address]y/inchars/outchars/
其中：
+ address : 需要执行转换的行地址，参见[sed指定地址](./sed指定地址.md)
+ y : 转换的命令符
+ inchars : 被转换的多个字符序列，如：abc，表示目标行中的abc需要执行转换
+ outchars : 转换后的新的多个字符序列，inchars和outchars中的字符按出现顺序执行转换，即inchars中的第1个字符被转换成outchars中的第1个字符；inchars中的第2个字符被转换成outchars中的第2个字符，依次类推

注意：转换命令会将所有出现的匹配字符都执行转换

## 示例

+ 替换指定的字符
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4
    [root@centos-7-192 ~]# sed '2,4y/234/678/' linedata
    This is line number 1
    This is line number 6
    This is line number 7
    This is line number 8
    ```
    上面的命令将2~4行中出现的“2”替换成“6”，“3”替换成“7”，“4”替换成“8”