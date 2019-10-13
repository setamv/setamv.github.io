[首页](/index.md) << ... << [索引](index.md)

# vim操作手册的使用
vim程序是带有操作手册的，里面详细说明了各种快捷键的操作等。
注意：vi是没有带操作手册的，vim才有，如果系统没有安装vim，请安装一个

## 如何打开vim的操作手册
只需要使用vim命令进入编辑界面，在命令模式输入`:help`就能打开操作手册

## 操作手册使用的一些技巧
1. 连接的跳转
    在vim帮助文档中，有很多帮助主题列表的链接，通过快捷键`ctrl+]`可以快速的跳转到链接的内容，如下所示：
    ```
    Getting Started  
    |usr_01.txt|  About the manuals
    |usr_02.txt|  The first steps in Vim
    |usr_03.txt|  Moving around
    |usr_04.txt|  Making small changes
    |usr_05.txt|  Set your settings
    |usr_06.txt|  Using syntax highlighting
    |usr_07.txt|  Editing more than one file
    |usr_08.txt|  Splitting windows
    |usr_09.txt|  Using the GUI
    |usr_10.txt|  Making big changes
    |usr_11.txt|  Recovering from a crash
    |usr_12.txt|  Clever tricks
    ```
    上面列出了12个章节，位于“||”中的文字就是跳转链接的标识（如“|usr_01.txt|”），只需要将光标定位到“||”之间，按快捷键`ctrl+]`就可以跳转到具体的内容。
2. 调回上一个页面
    使用快捷键`ctrl+t`就可以跳转到上一个页面