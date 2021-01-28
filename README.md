## 部署要点
1. 修改 conf/server.xml 中 SIP Connector 可接收请求的 IP 地址。默认为 127.0.0.1，可修改为 0.0.0.0，即接收全部可用 IP 地址的请求。
2. 修改 conf/dars/mobicents-dar.properties，完成路由规则配置，使指定的应用处理特定请求。
3. webapps 目录下安装的其他应用可以全部删除，只保留需要部署的应用。
4. 如果需要便捷管理 dar，可以在 webapps 中保留 sip-servlets-management（需同时部署 jolokia），就能在 Web 控制台管理 dar。
5. sip.xml 中需要有 <app-name>，不然无法进行路由。
6. 对于 SIP 和 HTTP 融合应用，Sip Servlet 和 Http Servlet 在注册时不可以使用相同的名字，即 sip.xml 和 web.xml 中的 <servlet-name> 不能相同。不然，发起 Sip 请求时会报 404。
7. Sip servlet 注册时，只需要 <servlet>，不需要 <servlet-mapping>。路由由 Application Router 完成，配置 <servlet-mapping> 也不会起作用。