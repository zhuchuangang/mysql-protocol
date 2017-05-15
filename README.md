# mysql-protocol
java parse mysql protocol packet

reference：
https://github.com/xiaoma20082008/mysql-protocol


1. 根据报文类型
mysql响应的报文类型有下面5种：
```
OK packet,LOCAL_INFILE packet,EOF packet,ERROR packet,ResultSet Row
```
除了ResultSet Row报文长度不确定之外，其他都可以通过header确定类型，确定了类型也就确定了长度。
对于ResultSet Row报文通过报文状态获取每一块的报文类型，ResultSet Row报文大致可以分2大块，一块是列信息，一块是行信息，可做分别输出。


2. 一个一个报文分别输出
