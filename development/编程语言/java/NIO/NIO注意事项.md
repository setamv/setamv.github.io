# NIO注意事项

## SocketChennal.configureBlocking(boolean block)
该方法用于设置SocketChannel的阻塞模式。如果参数值为true，表示以阻塞模式运行；否则，以非阻塞模式运行。
如果当前SocketChannel已经注册到Selector，则设置block为true将抛出IllegalBlockingModeException异常。因为注册到Selector的SocketChannel必须运行在非阻塞模式下。

## Selector.wakeup()
要注意的是，一次Selector.wakeup()只能唤醒一个因Selector.select()阻塞的线程。如果当前有多个线程因Selector.select()阻塞，只会有一个线程被唤醒。