package com.github.liuyueyi.hhui.trace.test.step;

import com.github.liuyueyi.hhui.trace.test.step.service.Index;
import com.github.liuyueyi.hui.components.trace.EnableTraceWatchDog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * @author YiHui
 * @date 2024/8/28
 */
@ComponentScan("com.github.liuyueyi.hhui.trace.test")
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy()
@EnableTraceWatchDog
public class BasicDemo {
    @Autowired
    private Index index;

    @Test
    public void testIndex() {
        Map map = index.buildIndexVo();
        System.out.println(map);
    }
}
