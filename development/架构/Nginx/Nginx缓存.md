# Nginx不缓存的原因
1. 未设置指令 `proxy_cache_valid`
    必须显示设置指令 `proxy_cache_valid`，Nginx才会缓存响应数据。如设置为：`proxy_cache_valid 200 304 1d;`