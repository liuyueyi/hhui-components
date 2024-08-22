trace-watch-dog
---

用于记录项目中，某一个链路的执行耗时情况，同时也支持将链路中的某些同步调用，改成异步执行，从而提高整体的性能表现

### 迭代记录

- [x] v1.0.1 支持在上下文中，随时获取 `TraceBridge` 进行手动埋点, 支持传入自定义的logSpEL，控制是否输出日志
- [x] v1.0.0 实现一个基础的 `TraceWatch` 工具类，通过手动埋点的方式记录整体的耗时分布情况

### 系列教程

- [x] [trace-watch-dog 诞生的契机]()
- [x] [封装一个基础的耗时统计工具类]()
- [x] [从0到1封装一个通用的耗时统计工具类]()
- [x] [便捷的同步转异步的实现策略]()
- [ ] 解决异步的上下文信息丢失问题（如traceId）
- [ ] 借助上下文，支持随时使用
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

