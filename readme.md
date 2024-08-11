hhui-components
---

从0到1，带你实现一些实用性非常强的组件

技术栈: java + spring + springboot

- jdk1.8 + springboot2.x
- jdk17  + springboot3.x

### 1. 组件列表

- [ ] [trace-watch-dog](/trace-watch-dog)
  - [x] [trace-watch-dog-spring](/trace-watch-dog-spring) 基于Spring AOP提供的使用版本 
  - 执行链路耗时分布统计
- [ ] async-run-job
  - 异步执行调度组件，像本地方法调用一样，实现线程池/MQ的异步执行
- [ ] dynamic-datasource
  - 动态数据源，多数据源自由切换
- [ ] spi-executor
  - 实现一个SPI框架
- [ ] call-log-recorder
  - 请求日志记录组件
- [ ] exclusive-schedule
  - 多实例之间的排他任务调度组件 - 不使用xxljob之类的工具，如何确保多个实例的定时任务，只会有一个在执行呢？
- [ ] perpetual-runner
  - 永动任务调度器，不断地从数据源中取数据作为任务的输入，然后驱动任务的执行
- [ ] data-flow-engine
  - 数据调度引擎，从指定数据源中获取输入，然后控制这些数据在不同的执行者之间流转、汇总，最后进行输出
- [ ] runtime-doctor-helper
  - 程序运行时诊断助手