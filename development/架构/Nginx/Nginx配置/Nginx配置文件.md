# Nginx配置文件

## Nginx配置文件的位置
Nginx默认的配置文件为`nginx.conf`，一般位于`/etc/nginx`目录中。

## Nginx配置文件的组成

### 指令（Directives）
Nginx配置文件由指令(Directives)和指令的参数组成。
简单的指令一般占一行，以分号结束。其他的指令可以包含一个指令块（即以{}开始和结束，中间包含多条指令）。
例如：
```
user  nginx;
worker_processes  1;

# 下面是指令快
events {
    worker_connections  1024;
}
```

## 配置文件切割
Nginx的配置信息可以写入多个配置文件，每个配置文件只对指定方面的配置信息进行设置，然后通过`include`指令在`nginx.conf`主配置文件中引入。如下所示：
```
include conf.d/http;
```
切割后的配置文件推荐保存到`/etc/nginx/conf.d`目录下。

## 上下文
Nginx的指令中，有以下一级指令可用于定义指令块（一个指令块也称为一个上下文），他们用于聚合一类相关的指令用于对相同的网络请求类型进行设置，他们包括：
+ events 指令 - 通用的连接处理
+ http 指令 - 用于设置HTTP通信
+ mail 指令 - 用于设置邮件通信
+ stream 指令 - 用于设置TCP和UDP通信

未放到上面这些上下文内的指令，统一被放入一个默认的“主上下文”中。

## 虚拟服务器
在每个通信处理的上下文中，可以包含一个或多个`server`指令块用于对该虚拟服务器进行配置。
对于可以配置的指令，取决于该`server`指令块位于哪种通信上下文中（mail、stream、http）。

## 继承
当一个指令块被定义在另一个指令块中时，当前指令块被称为“子指令块”（或子上下文），另一个指令块被称为“父指令块”（或父上下文）。当同一个指令在父、子指令块中都存在时，子指令块中的值将覆盖父指令块的设置。

## 负载均衡算法
Nginx的负载均衡算法有好几种，包括：轮询负载均衡算法(Round robin)、最少连接数负载均衡算法(Least connections)、最短响应时间负载均衡算法(least time)、通用散列负载均衡算法(Generic hash)、IP 散列负载均衡算法(IP hash)

### 轮询负载均衡算法(Round robin)
NGINX 服务器默认的负载均衡算法，该算法将请求分发到 upstream 指令块中配置的应用服务器列表中的任意一个服务器。可以通过应用服务器的负载能力，为应用服务器指定不同的分发权重(weight)。权重的值设置的越大，将被分发更多的请求访问。权重算法的核心技术是，依据访问权重求均值进行概率统计。轮询作为默认的负载均衡算法，将在没有指定明确的负载均衡指令的情况下启用。
#### 示例
```
upstream backend {
    server backend.example.com weight=1;
    server backend1.example.com weight=2;
    server backend1.example.com backup;
}
```

### 最少连接数负载均衡算法(Least connections)
NGINX 服务器提供的另一个负载均衡算法。它会将访问请求分发到upstream 所代理的应用服务器中，当前打开连接数最少的应用服务器实现负载均衡。最少连接数负载均衡，提供类似轮询的权重选项，来决定给性能更好的应用服务器分配更多的访问请求。该指令的指令名称是least_conn。
#### 示例
```
upstream backend {
    least_conn;
    server backend.example.com;
    server backend1.example.com;
}
```

### 最短响应时间负载均衡算法(least time)
该算法仅在 NGINX PLUS 版本中提供，和最少连接数算法类似，它将请求分发给平均响应时间更短的应用服务器。它是负载均衡算法最复杂的算法之一，能够适用于需要高性能的 Web 服务器负载均衡的业务场景。该算法是对最少连接数负载均衡算法的优化实现，因为最少的访问连接并非意味着更快的响应。该指令的配置名称是 least_time。
#### 示例
```
upstream backend {
    least_time;
    server backend.example.com;
    server backend1.example.com;
}
```

### 通用散列负载均衡算法(Generic hash)
服务器管理员依据请求或运行时提供的文本、变量或文本和变量的组合来生成散列值。通过生成的散列值决定使用哪一台被代理的应用服务器，并将请求分发给它。在需要对访问请求进行负载可控，或将访问请求负载到已经有数据缓存的应用服务器的业务场景下，该算法会非常有用。需要注意的是，在 upstream 中有应用服务器被加入或删除时，会重新计算散列进行分发，因而，该指令提供了一个可选的参数选项来保持散列一致性，减少因应用服务器变更带来的负载压力。该指令的配置名称是 hash。
#### 示例
```
upstream backend {
    hash;
    server backend.example.com;
    server backend1.example.com;
}
```

### IP 散列负载均衡算法(IP hash)
该算法仅支持 HTTP 协议，它通过计算客户端的 IP 地址来生成散列值。不同于采用请求变量的通用散列算法，IP 散列算法通过计算 IPv4 的前三个八进制位或整个 IPv6 地址来生成散列值。这对需要存储使用会话，而又没有使用共享内存存储会话的应用服务来说，能够保证同一个客户端请求，在应用服务可用的情况下，永远被负载到同一台应用服务器上。该指令同样提供了权重参数选项。该指令的配置名称是 ip_hash。
#### 示例
```
upstream backend {
    ip_hash;
    server backend.example.com;
    server backend1.example.com;
}
```
