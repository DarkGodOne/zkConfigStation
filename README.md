# zkConfigStation
## 简介
这是一个zookeeper管理工具，可以通过这个工具对zookeeper的各个节点进行编辑，数据导出到文件或者从文件导入。

## 如何使用
1. 该程序使用java severlet模式进行开发java版本为1.8.0_181，需要使用tomcat运行，开发测试使用的topmcat版本为8.5.0。
2. 直接将release中的war包放到tomcat的webapps目录中，启动tomcat即可启动服务。
3. 需要配置zookeeper的服务地址，编辑tomcatPat\webapps\configStation\WEB-INF\web.xml,修改param-value的值为zookeeper的地址。
```
<context-param>
  <param-name>zkhost</param-name>;
  <param-value>192.168.26.104:9181</param-value>;
<context-param>
```
4. 需要修改tomcat的配置，以防止中文乱码的问题，编辑server.xml
```
<Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
```
添加URIEncoding="UTF-8" useBodyEncodingForURI="true"，修改为：
```
<Connector port="8080" protocol="HTTP/1.1"
               URIEncoding="UTF-8" useBodyEncodingForURI="true"
               connectionTimeout="20000"
               redirectPort="8443" />
```
5. 浏览器访问地址：http://ip:port/configStation/
6. 默认进入登录页面，默认账户：admin，密码：gxk123，账户密码为程序内写死的，只能通过修改程序才可以修改用户名密码。
![image](https://github.com/DarkGodOne/zkConfigStation/blob/master/login.png)
7. 打开后是这个样子的：
![image](https://github.com/DarkGodOne/zkConfigStation/blob/master/config.png)
6. 增加中文支持，并临时提供测试地址：http://www.darkgod.online:8080/configStation
![image](https://github.com/DarkGodOne/zkConfigStation/blob/master/zh.png)
## end
