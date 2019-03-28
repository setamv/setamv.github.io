# 启动、关闭
Nginx安装完后，会将可执行文件nginx加入Path路径中，启动Nginx，直接执行命令：`# nginx` 即可。
Nginx启动后，可以通过以下命令关闭、重新加载配置：
`nginx -s signal`
其中，signale可以是以下列表中的一个：
+ stop 快速关闭nginx
+ quit 安全的关闭nginx
+ reload 重新加载nginx的配置信息
+ reopen 重新打开日志文件