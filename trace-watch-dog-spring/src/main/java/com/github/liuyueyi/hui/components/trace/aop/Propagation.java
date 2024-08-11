package com.github.liuyueyi.hui.components.trace.aop;

/**
 * 传播属性
 *
 * @author YiHui
 * @date 2024/8/11
 */
public enum Propagation {
    /**
     * 支持当前trace记录，如果当前不存在trace存在，则开启记录
     */
    REQUIRED(0),
    /**
     * 支持当前trace记录，如果当前不存在trace存在，则以同步的方式执行
     */
    SUPPORTS(1),
    /**
     * 不支持记录，不管当前存不存在，都以同步的方式执行，且不参与记录
     */
    NEVER(2),
    ;

    private final int value;

    private Propagation(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}