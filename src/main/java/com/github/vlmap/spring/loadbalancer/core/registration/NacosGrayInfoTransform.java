package com.github.vlmap.spring.loadbalancer.core.registration;

import com.netflix.client.config.IClientConfig;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;

import java.util.Map;

public class NacosGrayInfoTransform extends AbstractGrayInfoTransform<NacosServer> {
    public NacosGrayInfoTransform(IClientConfig config) {
        super(config);
    }

    @Override
    public Map<String, String> metadata(NacosServer server) {

        return server.getMetadata();
    }
}
