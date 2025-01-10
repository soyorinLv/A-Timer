package com.soyo.api.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ContextFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        log.error("hello hello hellohello");
        // 从请求上下文中获取userId
        // 从请求头中获取 X-User-Id
        List<String> userId= (List<String>)template.headers().get("A-User-Id");
        if (userId != null) {
            // 将 userId 放入上下文（Context）中
            //MyContextHolder.setUserId(userId);
        }
        if (userId != null) {
            // 将userId添加到请求头中，以便传递给下游的微服务
            template.header("A-User-Id", userId);
        }
    }
}

