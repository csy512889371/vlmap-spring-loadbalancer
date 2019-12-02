package com.github.vlmap.spring.tools.loadbalancer.config;


import com.github.vlmap.spring.tools.event.listener.DelegatePropChangeListener;
import com.github.vlmap.spring.tools.event.listener.PropChangeListener;
import com.github.vlmap.spring.tools.loadbalancer.DelegatingLoadBalancer;
import com.github.vlmap.spring.tools.loadbalancer.TagProcess;
import com.github.vlmap.spring.tools.loadbalancer.Ribbon;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.event.EventSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

//@EnableConfigurationProperties({SpringToolsProperties.class})

@Configuration
public class TagRibbonClientConfiguration {

    @Autowired

    private Environment env;


    @Bean
    public AtomicReference<Map<String, Set<String>>> tagsInProgress() {

        return new AtomicReference<Map<String, Set<String>>>(Collections.emptyMap());
    }


    @Bean
    @ConditionalOnBean(TagProcess.class)
    public String tagStateProgress(IClientConfig clientConfig,
                                   ILoadBalancer lb,
                                   IRule rule,
                                   AtomicReference<Map<String, Set<String>>> tagsInProgress,
                                   @Autowired(required = false) List<TagProcess> tagProcesses,
                                   @Autowired(required = false) DelegatePropChangeListener delegatePropChangeListener) {


        if (lb instanceof BaseLoadBalancer) {
            BaseLoadBalancer target = (BaseLoadBalancer) lb;


            DelegatingLoadBalancer delegating = new DelegatingLoadBalancer(target, tagProcesses == null ? Collections.emptyList() : tagProcesses, tagsInProgress);
            rule.setLoadBalancer(delegating);
            this.tagStateInProgress(clientConfig, tagsInProgress);


            String prefix = clientConfig.getClientName();
            if (delegatePropChangeListener != null) {
                PropChangeListener listener = new PropChangeListener(prefix, () -> {
                    if (lb instanceof BaseLoadBalancer) {
                        tagStateInProgress(clientConfig, tagsInProgress);
                    }

                });
                delegatePropChangeListener.addListener(listener);
            }
        }


        return "tagStateProgress";
    }


    protected void tagStateInProgress(IClientConfig clientConfig, AtomicReference<Map<String, Set<String>>> tagsInProgress) {


        org.apache.commons.configuration.Configuration configuration = ConfigurationManager.getConfigInstance().subset(clientConfig.getClientName());
        if(configuration instanceof EventSource){
            EventSource eventSource=(EventSource)configuration;
            eventSource.addConfigurationListener((event -> {
                String  name=event.getPropertyName();
                Object  value=event.getPropertyValue();

            }));
        }
        MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource();
        Iterator<String> iterator = configuration.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = configuration.getString(key);
            propertySource.put(key, value);
        }

        Binder binder = new Binder(propertySource);
        Ribbon ribbon = new Ribbon();
        binder.bind("ribbon", Bindable.ofInstance(ribbon));

        List<Ribbon.TagOfServers> tagOfServers = ribbon.getTagOfServers();
        if (tagOfServers != null) {
            Map<String, Set<String>> map = new HashMap<>(tagOfServers.size());

            for (Ribbon.TagOfServers tagOfServer : tagOfServers) {

                if (tagOfServer != null && CollectionUtils.isNotEmpty(tagOfServer.getTags()) && StringUtils.isNotBlank(tagOfServer.getId())) {
                    map.put(tagOfServer.getId(), tagOfServer.getTags());
                }
            }
            tagsInProgress.set(map);


        }


    }


}
