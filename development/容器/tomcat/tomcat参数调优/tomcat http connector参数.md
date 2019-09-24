# tomcat http connector参数

官网参考文件：https://tomcat.apache.org/tomcat-8.5-doc/config/http.html

## acceptCount
官网对*acceptCount*的说明如下所示：
>The maximum queue length for incoming connection requests when all possible request processing threads are in use. Any requests received when the queue is full will be refused. The default value is 100.*

## maxThreads
官网对*maxThread*的说明如下所示：
>The maximum number of request processing threads to be created by this Connector, which therefore determines the maximum number of simultaneous requests that can be handled. If not specified, this attribute is set to 200. If an executor is associated with this connector, this attribute is ignored as the connector will execute tasks using the executor rather than an internal thread pool. Note that if an executor is configured any value set for this attribute will be recorded correctly but it will be reported (e.g. via JMX) as -1 to make clear that it is not used.

## maxConnections
官网对*maxConnections*的说明如下所示：
>The maximum number of connections that the server will accept and process at any given time. When this number has been reached, the server will accept, but not process, one further connection. This additional connection be blocked until the number of connections being processed falls below maxConnections at which point the server will start accepting and processing new connections again. Note that once the limit has been reached, the operating system may still accept connections based on the acceptCount setting. The default value varies by connector type. For NIO and NIO2 the default is 10000. For APR/native, the default is 8192.

>Note that for APR/native on Windows, the configured value will be reduced to the highest multiple of 1024 that is less than or equal to maxConnections. This is done for performance reasons.

>If set to a value of -1, the maxConnections feature is disabled and connections are not counted.
