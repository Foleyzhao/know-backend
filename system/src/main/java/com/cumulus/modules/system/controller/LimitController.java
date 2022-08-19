package com.cumulus.modules.system.controller;

import com.cumulus.annotation.Limit;
import com.cumulus.annotation.rest.AnonymousGetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流demo
 */
@RestController
@RequestMapping("/api/limit")
public class LimitController {

    /**
     * 接口被访问次数
     */
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    /**
     * 测试限流注解（该接口60秒内最多只能访问10次，保存到redis的键名为limit_test）
     */
    @AnonymousGetMapping
    @Limit(key = "test", period = 60, count = 10, name = "testLimit", prefix = "limit")
    public int test() {
        return ATOMIC_INTEGER.incrementAndGet();
    }

}
