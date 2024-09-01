package com.github.liuyueyi.hhui.components.trace.output;

import com.github.liuyueyi.hhui.components.trace.recoder.DefaultTraceRecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.text.NumberFormat;
import java.util.Map;

/**
 * 默认的日志输出
 *
 * @author YiHui
 * @date 2024/8/31
 */
public class LogOutput {
    private static final Logger log = LoggerFactory.getLogger(DefaultTraceRecoder.class);

    /**
     * 输出日志
     *
     * @param cost      耗时分布
     * @param traceName 总任务
     */
    public static void logPrint(Map<String, Long> cost, String traceName) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        long totalCost = cost.get(traceName);
        sb.append("TraceWatch '").append(traceName).append("': running time = ").append(totalCost).append(" ms");
        sb.append('\n');
        if (cost.isEmpty()) {
            sb.append("No task info kept");
        } else {
            sb.append("---------------------------------------------\n");
            sb.append("ms         %     Task name\n");
            sb.append("---------------------------------------------\n");
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(2);
            pf.setMinimumFractionDigits(2);
            pf.setGroupingUsed(false);
            for (Map.Entry<String, Long> entry : cost.entrySet()) {
                if (entry.getKey().equals(traceName)) {
                    // 总耗时情况，不打印在分布中
                    continue;
                }

                sb.append(entry.getValue()).append("\t\t");
                sb.append(pf.format(entry.getValue() / (double) totalCost)).append("\t\t");
                sb.append(entry.getKey()).append("\n");
            }
        }

        if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
            // 若项目中没有Slfj4的实现，则直接使用标准输出
            System.out.printf("\n---------------------\n%s\n--------------------\n%n", sb);
        } else if (log.isInfoEnabled()) {
            log.info("\n---------------------\n{}\n--------------------\n", sb);
        }
    }

}
