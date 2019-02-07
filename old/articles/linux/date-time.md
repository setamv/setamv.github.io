[Back](index.md)

# Date And Time In Linux System

# Introduct
  
  Record commands and ways to look at or change system date, system time or system timezone.

# Contents Catalogue


# Contents <a id="≡">≡</a>

- [Change TimeZone](#CTZ)

- [Change Time](#CT)
    + [Change Kernel Time](#CT-CKT)


## Change TimeZone <a id="CTZ">[≡](#≡)</a>

Linux系统的时区使用系统环境变量`TZ`来进行设置，其值为一个有效的时区枚举值。例如下面的命令是将系统时区设置为北京时区：
```
$ TZ='Asia/Shanghai'
```

如果要永久性的使之生效，可以将该脚本添加到用户Home目录的.bash_profile文件中，并使用`export`命令导出，如在.bash_profile文件末尾添加如下一行内容：
```
export TZ='Asia/Shanghai'
```

要得到正确的时区枚举值，你可以通过`tzselect`命令，你只需要输入`tzselect`命令，然后按照提示一路选择时区所属的州（亚洲、欧洲、南美洲等...）、国家、城市，该命令最终将为你生成对应时区的枚举值。

## Change Time <a id="CT">[≡](#≡)</a>

linux系统时钟有两个：

1. 硬件时钟，即BIOS时间，就是我们进行CMOS设置时看到的时间，
2. 系统时钟，是linux系统Kernel时间。

当Linux启动时，系统Kernel会去读取硬件时钟的设置，然后系统时钟就会独立于硬件运作。有时我们会发现系统时钟和硬件时钟不一致，因此需要执行时间同步，下面描述时间设置及时钟同步的命令使用方法。

### Change Kernel Time <a id="CT-CKT">[≡](#≡)</a>

Linux系统的Kernel时间可以通过`date`命令来进行查看和修改。

查看时间时，直接输入`$ date`即可

修改时间时，需要使用`date`命令的`-s`选项，格式如下：   
`$ date -s STRING`      
其中，STRING是一个用户友好的时间字符串，可以是： "Sun, 29 Feb 2004 16:21:42  -0800"、"2004-02-29 16:21:42"、"next  Thursday"等。