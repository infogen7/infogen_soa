# 项目版本信息
项目名称：infogen_soa
提供了Rest+Json和RPC+Protobuf的服务发布
提供了服务注册
服务客户端软负载调用(Http/RPC)
服务治理
加密框架
调用链分析

子项目:
infogen_core:项目基础包
包含一系列用于AOP的框架和组件
包含基础工具类:一致性hash,LRU map,jackson工具类,NativePath(获取当前web或jar环境的根目录)
 
 infogen_authc:安全验证框架,功能类似shiro
 使用自定义实现的session机制
 可以通过header或cookie参数传递sessionid
 
 infogen_hibernate:封装hibernate
 实现@AutoClose注解 用于自动关闭session
 封装初始化时通过指定包名自动扫描添加Model类

