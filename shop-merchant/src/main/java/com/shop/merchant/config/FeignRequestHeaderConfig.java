package com.shop.merchant.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignRequestHeaderConfig {

    @Bean
    public RequestInterceptor authHeaderForwardInterceptor() {
        return requestTemplate -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (!(attributes instanceof ServletRequestAttributes)) {
                return;
            }
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                requestTemplate.header("Authorization", authorization);
            }
        };
    }
}
