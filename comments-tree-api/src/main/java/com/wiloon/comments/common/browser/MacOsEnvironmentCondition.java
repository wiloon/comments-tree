package com.wiloon.comments.common.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class MacOsEnvironmentCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(MacOsEnvironmentCondition.class);

    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        logger.info("env, property, os name: {}", context.getEnvironment().getProperty("os.name"));
        return context.getEnvironment().getProperty("os.name").contains("Mac");
    }
}