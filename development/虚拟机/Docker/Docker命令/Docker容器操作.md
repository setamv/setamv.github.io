# Docker 容器操作

+ `# docker run`
    该命令用于新建并启动容器。
    - `# docker run -itd 镜像名称 /bin/bash`
        创建Docker容器并启动一个伪终端，使用交互运行的方式启动一个容器。
        其中，-d参数表示docker以守护态进程运行。如果不加-d参数，容器将在命令执行完后退出。

+ `# docker ps -al`
    查看已创建的容器清单

+ `# docker start 容器ID`        
    启动一个容器。
    容器ID克通过`# docker ps -al`获取

+ `# docker exec -it 容器ID /bin/bash`    
    进入容器
    必须1.3.X版本之后才有该命令。