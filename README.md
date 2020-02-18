# zkConfigStation
## 简介
这是一个zookeeper管理工具，可以通过这个工具对zookeeper的各个节点进行编辑，数据导出到文件或者从文件导入。

## 如何使用
1. 该程序使用java severlet模式进行开发java版本为1.8.0_181，需要使用tomcat运行，开发测试使用的topmcat版本为8.5.0。
2. 直接将release中的war包放到tomcat的webapps目录中，启动tomcat即可启动服务。
3. 浏览器访问地址：http://ip:port/configStation/
4. 默认进入登录页面，默认账户：admin，密码：gxk123，账户密码为程序内写死的，只能通过修改程序才可以修改用户名密码。
![image](https://github.com/DarkGodOne/zkConfigStation/blob/master/login.png)
5. 需要配置zookeeper的服务地址，编辑tomcatPat\webapps\configStation\WEB-INF\web.xml。
<pre><code>&lt;context-param&gt;
  &lt;param-name&gt;zkhost&lt;/param-name&gt;
  &lt;param-value&gt;192.168.26.104:9181&lt;/param-value&gt;
&lt;/context-param&gt;
</code></pre>
![image](https://github.com/DarkGodOne/zkConfigStation/blob/master/config.png)
6. 增加中文支持，并临时提供测试地址：http://148.70.66.50/configStation ，2018-12-05过期
![image](https://github.com/DarkGodOne/zkConfigStation/blob/master/zh.png)
## end
