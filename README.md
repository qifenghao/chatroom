## 部署要点
1. 修改 conf/server.xml 中 SIP Connector 可接收请求的 IP 地址。默认为 127.0.0.1，可修改为 0.0.0.0，即接收全部可用 IP 地址的请求。
2. 修改 conf/dars/mobicents-dar.properties，完成路由规则配置，使指定的应用处理特定请求。
3. webapps 目录下安装的其他应用可以全部删除，只保留需要部署的应用。
4. 如果需要便捷管理 dar，可以在 webapps 中保留 sip-servlets-management（需同时部署 jolokia），就能在 Web 控制台管理 dar。
5. sip.xml 中需要有 <app-name>，不然无法进行路由。
6. 对于 SIP 和 HTTP 融合应用，Sip Servlet 和 Http Servlet 在注册时不可以使用相同的名字，即 sip.xml 和 web.xml 中的 <servlet-name> 不能相同。不然，发起 Sip 请求时会报 404。
7. Sip servlet 注册时，只需要 <servlet>，不需要 <servlet-mapping>。路由由 Application Router 完成，配置 <servlet-mapping> 也不会起作用。

## 调试
### 调试要点
1. Restcomm Sip Servlets 对 Tomcat 进行了定制，使之能支持 HTTP 和 SIP 融合应用。在使用 IDEA 或 Eclipse 进行调试时，使用配置官方 Tomcat 的方式并不能发布服务。为了正常进行调试，需要修改一些配置。
2. 在 IDE(IDEA/Eclipse) 调试时，会修改 CATALINA_BASE。调试时，必须将 CATALINA_BASE 改为 Tomcat 的安装目录。在 IDEA 中，通过添加 Tomcat 启动时的 CATALINA_BASE 环境变量来实现。
3. IDE 调试时，通常会指定 WAR 包或应用目录（WAR解包后的目录）的地址，IDEA 和 Eclipse 均是使用 <Context> 元素指定，但是 Restcomm Sip Servlets 检测到 <Context> 元素后，只能发布 HTTP 服务，无法发布 Sip 服务。有 2 种方案可以避免 <Context> 带来的问题：
    - 方案一：将 ${TOMCAT_HOME}/conf/server.xml 中 <Host> 的 appBase 设置为 WAR 包或应用目录的上级目录。
    - 方案二（推荐）：将 WAR 包或应用目录的输出地址改为 ${TOMCAT_HOME}/webapps。在 IDEA 中，修改 Project Structure/Project Settings/Artifacts 的 Output directory。(注意：Maven reimport 后会将 Output directory 复原，需要再次修改。)
4. 完成配置后，可进行调试和热部署。

### 参考资料
- [Sip Servlet application not started](https://stackoverflow.com/questions/27543562/sip-servlet-application-not-started)
- [详解Tomcat 配置文件server.xml](https://www.cnblogs.com/kismetv/p/7228274.html#title3-5)
- [IntelliJ IDEA通过Tomcat启动项目过程分析](https://blog.csdn.net/u013938484/article/details/69389836)