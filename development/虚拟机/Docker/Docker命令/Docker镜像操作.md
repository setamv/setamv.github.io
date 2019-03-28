# Docker镜像操作

+ `# docker search 镜像名称关键字` 
    搜索镜像仓库中的镜像

+ `# docker pull 镜像名`    
    从仓库中获取镜像到本地

+ `# docker images`
    列出本地的镜像列表

+ `# docker commit 容器ID 新的镜像名称`        
    基于一个容器创建一个新的镜像。

+ `# docker save -o image_export.tar`    
    将镜像导出到本地文件image_export.tar

+ `# docker load -i image_export.tar`
    将本地文件image_export.tar导入为本地镜像。（image_export.tar 为 `# docker save` 命令导出的文件）

+ `# docker rmi 镜像ID/镜像名称`    
    从本地移除镜像。可以指定 镜像ID 或 镜像名称。  
    在移除镜像之前要确保该镜像下没有容器了（包括已经停止的容器），否则该镜像无法进行删除，先要使用docker rm删除该镜像下所有的容器之后才能移除该镜像


# 基于dockerfile创建镜像
