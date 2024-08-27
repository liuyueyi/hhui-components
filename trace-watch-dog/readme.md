trace-watch-dog
---

用于记录项目中，某一个链路的执行耗时情况，同时也支持将链路中的某些同步调用，改成异步执行，从而提高整体的性能表现

### 迭代记录

- [x] v1.0.1 支持在上下文中，随时获取 `TraceBridge` 进行手动埋点, 支持传入自定义的logSpEL，控制是否输出日志
- [x] v1.0.0 实现一个基础的 `TraceWatch` 工具类，通过手动埋点的方式记录整体的耗时分布情况

### 系列教程

- [x] [trace-watch-dog 诞生的契机 | 一灰灰的站点](https://hhui.top/tutorial/column/app/trace-watch-dog/01.%E8%AF%9E%E7%94%9F%E7%9A%84%E5%A5%91%E6%9C%BA.html) 
- [x] [封装一个基础的耗时统计工具类 | 一灰灰的站点](https://hhui.top/tutorial/column/app/trace-watch-dog/02.%E5%9F%BA%E7%A1%80%E8%80%97%E6%97%B6%E5%B7%A5%E5%85%B7%E5%B0%81%E8%A3%85.html)
- [x] [从0到1封装一个通用的耗时统计工具类 | 一灰灰的站点](https://hhui.top/tutorial/column/app/trace-watch-dog/03.%E5%B0%81%E8%A3%85%E4%B8%80%E4%B8%AA%E9%80%9A%E7%94%A8%E7%9A%84%E8%80%97%E6%97%B6%E5%88%86%E5%B8%83%E5%B7%A5%E5%85%B7%E7%B1%BB.html)
- [x] [异步耗时统计能力增强的实现策略 | 一灰灰的站点](https://hhui.top/tutorial/column/app/trace-watch-dog/04.%E5%BC%82%E6%AD%A5%E6%94%AF%E6%8C%81%E7%AD%96%E7%95%A5.html)
- [x] [上下文信息传递 | 一灰灰的站点](https://hhui.top/tutorial/column/app/trace-watch-dog/05.%E4%B8%8A%E4%B8%8B%E6%96%87%E4%BF%A1%E6%81%AF%E4%BC%A0%E9%80%92.html)
- [x] [日志集成与全链路traceId透传](https://hhui.top/tutorial/column/app/trace-watch-dog/06.%E6%97%A5%E5%BF%97%E8%BE%93%E5%87%BA%E4%B8%8E%E5%85%A8%E9%93%BE%E8%B7%AFtraceId%E9%80%8F%E4%BC%A0.html)
- [x] [借助上下文，支持随时使用]()
- [ ] 借助AOP，实现非侵入式的埋点
- [ ] AOP的使用缺陷
- [ ] 基于Agent的实现方式
- [ ] 耗时输出重定向(标准输出，slf4j输出，日志上报三方平台)

### 使用姿势

引入依赖，基于jitpack的如下

> 版本根据实际需要进行选择，当前最新版本 0.0.1 

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

基础核心使用，可应用非Spring应用场景

```xml
<dependency>
    <groupId>com.github.liuyueyi.hhui-components</groupId>
    <artifactId>trace-watch-dog</artifactId>
    <version>0.0.1</version>
</dependency>
```

对于SpringBoot的应用场景下，可以引入下面的依赖，支持基于AOP的埋点方式

```xml
<dependency>
    <groupId>com.github.liuyueyi.hhui-components</groupId>
    <artifactId>trace-watch-dog-spring</artifactId>
    <version>0.0.1</version>
</dependency>
```

