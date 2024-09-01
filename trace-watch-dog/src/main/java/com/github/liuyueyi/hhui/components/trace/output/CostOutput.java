package com.github.liuyueyi.hhui.components.trace.output;

import java.util.Map;

/**
 * @author YiHui
 * @date 2024/8/31
 */
@FunctionalInterface
public interface CostOutput {

    /**
     * 输出
     *
     * @param cost      任务耗时分布
     * @param traceName Trace
     */
    void output(Map<String, Long> cost, String traceName);

}
