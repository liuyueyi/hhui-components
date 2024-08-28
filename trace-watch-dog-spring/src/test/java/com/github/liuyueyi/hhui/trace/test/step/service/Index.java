package com.github.liuyueyi.hhui.trace.test.step.service;

import com.github.liuyueyi.hui.components.trace.aop.Propagation;
import com.github.liuyueyi.hui.components.trace.aop.TraceDog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class Index {
    @Autowired
    private DemoService demoService;

    @TraceDog(propagation = Propagation.REQUIRED)
    public Map buildIndexVo() {
        Map<String, Object> ans = new HashMap<>();
        demoService.sync();
        ans.put("ignore", demoService.ignoreCost());
        ans.put("c", demoService.c());
        CompletableFuture<Integer> f = demoService.d();
        demoService.e();
        ans.put("d", f.join());
        return ans;
    }
}