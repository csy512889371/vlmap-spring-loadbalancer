package com.github.vlmap.spring.tools;

import com.github.vlmap.spring.tools.loadbalancer.InstanceTag;
import com.netflix.appinfo.ApplicationInfoManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableConfigurationProperties({GrayLoadBalancerProperties.class})

public class GrayLoadBalancerAutoConfiguration {

    @Autowired
    public void initDefaultIgnorePath(Environment environment) {
        Set<String> urls = new LinkedHashSet<>();
        urls.add("/webjars/**");
        urls.add("/favicon.ico");
        String uri = environment.getProperty("management.endpoints.web.base-path");
        String antPath = toAntPath(uri);

        if (StringUtils.isNotBlank(antPath)) {
            urls.add(antPath);
        }
         GrayLoadBalancerProperties.CompatibleIgnore.DEFAULT_IGNORE_PATH.set(new ArrayList<>(urls));
    }


    @Bean
    public InstanceTag currentService(ApplicationInfoManager applicationInfoManager){
        return new InstanceTag(applicationInfoManager);
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
