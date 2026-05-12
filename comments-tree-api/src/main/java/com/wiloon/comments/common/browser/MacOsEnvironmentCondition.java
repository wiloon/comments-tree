package com.wiloon.comments.common.browser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


@Slf4j
public class MacOsEnvironmentCondition implements Condition {
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        log.info("env, property, os name: {}", context.getEnvironment().getProperty("os.name"));
        return context.getEnvironment().getProperty("os.name").contains("Mac");
    }
}