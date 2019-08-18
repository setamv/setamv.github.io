# Eureka Instance相关配置
## eureka.instance.ip-address
+ 官方文档说明为：   
    Get the IPAdress of the instance. This information is for academic purposes only as the communication from other instances primarily happen using the information supplied in {@link #getHostName(boolean)}.
+ 实践
    该配置项用于指定注册到eureka server的实例的ip地址，当从eureka server获取服务实例列表时，该示例的访问地址就是该 ip-address的值 + 端口。
    比如设置`eureka.instance.ip-address=localhost`，则客户端获取到实例后转换成的访问地址就是：localhost:端口
    该配置项的默认值为主机的ip地址，其使用场景包括：
    1. 指定域名作为实例的访问地址
        比如，当有一台注册到eureka server的实例需要被外部网络访问时，显然该配置项的值不能是一个内部地址，此时，就需要通过该配置项来指定（比如域名为：www.setamv.tech）：`eureka.instance.ip-address=www.setamv.tech:8080`
        注意：必须同时指定`eureka.instance.hostname=www.setamv.tech`才能最终生效，具体原因未知！！！！