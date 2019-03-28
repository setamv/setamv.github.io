# 创建Docker镜像
有多种方式可以创建Docker镜像，包括：基于已有的镜像容器创建；基于本地模板导入创建；基于Dockerfile文件构建镜像

## 基于已有的镜像容器创建

## 基于本地模板导入创建

## 基于Dockerfile文件构建镜像
1. 创建dockerfile文件
    文件内容如下所示：
    ```
    # 指定一个基础镜像centos
    FROM docker.io/centos:latest
    
    #安装应用执行的环境java
    RUN yum -y install java
    
    #将指定的jar文件复制到容器中
    COPY demo-0.0.1-SNAPSHOT.jar /usr/local/
    
    #执行jar文件
    ENTRYPOINT ["java" ,"-jar","/usr/local/demo-0.0.1-SNAPSHOT.jar"]
    ``