# 代理服务器

* 注意不是http代理服务器，否则会报错<br>
* 测试时要结合echo项目，并且确保EchoClient类中的HOST和PORT接口与HexDumpProxy类中的REMOTE_HOST和REMOTE_PORT一致<br>
* 让EchoClient通过代理服务器HexDumpProxy去访问EchoServer，代理服务器也会收到EchoClient发送的消息<br>
* 启动顺序：EchoServer->HexDumpProxy->EchoClient
