# redis cluster相关的命令
redis cluster有很多命令可用于对cluster进行操作和设置，以下所有的说明或示例代码都基于以下[cluster环境搭建.md](cluster环境搭建.md)中的cluster节点分布：
| 节点类型 | IP:PORT              | 所属master           |
|----------|----------------------|----------------------|
| master   | 192.168.199.130:7000 |                      |
| master   | 192.168.199.130:7001 |                      |
| master   | 192.168.199.130:7002 |                      |
| slave    | 192.168.199.130:8000 | 192.168.199.130:7000 |
| slave    | 192.168.199.130:8001 | 192.168.199.130:7001 |
| slave    | 192.168.199.130:8002 | 192.168.199.130:7002 |

redis cluster的相关命令分两种形式：
1. 通过`# redis-cli --cluster`的方式执行，参考[`redis-cli --cluster`选项](../redis-cli命令选项/--cluster选项.md)
2. 登录cluster中的节点后，通过`> cluster `的方式对整cluster的节点进行设置，参考[`cluster相关命令`](../命令/cluster相关命令.md)