# 登录远程redis服务端命令
将如下的内容保存到任意的.bat文件中，双击执行即可。（下面假设redis-cli.exe命令位于目录：D:\DevTools\redis 3.2）
```
D:
cd D:\DevTools\redis 3.2
redis-cli.exe -h 192.168.0.139 -a Zxrc!607# -n 1
cmd /k echo.
```
其中，
命令`cmd /k`是执行完此命令后保留窗口，而不是自动退出。
命令`cmd /k echo.`的意思是批处理执行完后，回车，然后保留窗口