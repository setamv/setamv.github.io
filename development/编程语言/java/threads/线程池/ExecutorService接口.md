# ExecutorService接口

## shutdown()、shutdownNow()、awaitTermination(long timeout, TimeUnit unit)的区别
shutdown()、shutdownNow()两个方法都可以关闭线程池。区别是：
+ shutdown()
    在shutdown()之前已提交（通过execute、submit方法）的任务都会被执行，新提交的任务将会被拒绝。
    该方法不会一直阻塞直到所有任务都执行结束，而是会立即返回，此时，线程池已经被标记为“shutdown”了。
+ shutdownNow()
    在shutdownNow()之前已提交（通过execute、submit方法）的且还未开始执行任务都不会被执行，正在执行的任务，也将尽力中断任务的执行（比如interrupt任务执行线程），新提交的任务将会被拒绝。

awaitTermination(long timeout, TimeUnit unit)方法用于和shutdown()配合使用，该方法在线程池关闭后，将阻塞直到线程池中已经在执行中的任务全部执行结束或等到超时时间。如：
```
ExecutorService executorService = ....
executorService.shutdownNow();
boolean shutdownSucceed = executorService.awaitTermination(1, TimeUnit.MICROSECONDS);
if (!shutdownSucceed) {
    // 如果在指定超时时间内还未执行完，强制终止线程池中的任务
    executorService.shutdownNow();
}
```