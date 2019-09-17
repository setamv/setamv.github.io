# Nginx配置项

## client_max_body_size
client_max_body_size用于设置请求body的大小，默认值是1M，如果需要上传文件，1M显然是不够的。可以通过以下设置进行调整：
```
server {
    location {
        client_max_body_size  100m;
    }
}
```