# Nginx反向代理配置

## location指令


## proxy_pass指令
`proxy_pass`指令使用在`location`指令块中。用于将一个http请求转发到被代理的服务器。如下所示：
```
location /some/path/ {
    proxy_pass http://www.example.com/link/;
}
```
真实的请求地址被转换为代理服务器访问地址的规则为：
+ 当`proxy_pass`指令的值后面跟了URI（如上面的/link/），真实的请求地址中的"/some/path/"部分将被替换为"http://www.example.com/link/"
+ 当`proxy_pass`指令的值后面没有跟URI（例如：`proxy_pass http://www.example.com`），真实的请求地址将被追加到指令值后面，如：`http://www.example.com/some/path/`