package com.github.vlmap.spring.tools;

import com.github.vlmap.spring.tools.actuator.ProspEndPoint;
import com.github.vlmap.spring.tools.event.listener.RefreshListener;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties({SpringToolsProperties.class})

public class SpringToolsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean

    public DynamicToolProperties dynamicToolProperties(Environment env, SpringToolsProperties properties) {
        return new DynamicToolProperties(env, properties);
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public ProspEndPoint prospEndPoint(DynamicToolProperties properties) {
        return new ProspEndPoint(properties);
    }
    @Bean

    public RefreshListener refreshListener() {
        return new RefreshListener();
    }
}
