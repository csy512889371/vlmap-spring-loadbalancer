package com.github.vlmap.spring.loadbalancer;

import com.github.vlmap.spring.loadbalancer.core.CurrentServer;
import com.github.vlmap.spring.loadbalancer.core.StrictHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.archaius.ConfigurableEnvironmentConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableConfigurationProperties({GrayLoadBalancerProperties.class})

public class GrayLoadBalancerAutoConfiguration {


    @Bean
    public StrictHandler strictHandler(CurrentServer currentService, GrayLoadBalancerProperties properties) {
        return new StrictHandler(properties, currentService);
    }

    @Bean

    public CurrentServer currentService(ConfigurableEnvironmentConfiguration configuration, Environment environment, InetUtils inetUtils) {

        return new CurrentServer(environment,inetUtils);
    }

    @Autowired
    public void initDefaultIgnorePath(Environment environment) {
        Set<String> urls = new LinkedHashSet<>(Arrays.asList("/webjars/**", "/favicon.ico"));


        String uri = environment.getProperty("management.endpoints.web.base-path", "/actuator");
        String antPath = toAntPath(uri);

        if (StringUtils.isNotBlank(antPath)) {
            urls.add(antPath);
        }
        GrayLoadBalancerProperties.CompatibleIgnore.DEFAULT_IGNORE_PATH.set(new ArrayList<>(urls));
    }


    private static String toAntPath(String uri) {

        if (StringUtils.isBlank(uri)) {
            return null;
        }
        if (StringUtils.endsWith(uri, "/**")) {
            return uri;
        }
        if (uri.endsWith("/")) {
            return uri + "**";
        }
        return uri + "/**";
    }


}
