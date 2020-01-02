package com.github.vlmap.spring.loadbalancer.annotation;

import com.github.vlmap.spring.loadbalancer.config.RibbonClientSpecificationAutoConfiguration;
import com.github.vlmap.spring.loadbalancer.core.client.feign.GrayFeignConfiguration;
import com.github.vlmap.spring.loadbalancer.core.client.resttemplate.GrayRestTemplateConfiguration;
import com.github.vlmap.spring.loadbalancer.core.client.webclient.GrayWebClientConfiguration;
import com.github.vlmap.spring.loadbalancer.core.platform.reactive.GrayReactiveConfiguration;
import com.github.vlmap.spring.loadbalancer.core.platform.reactive.GrayRequestMappingHandlerAdapterConfiguration;
import com.github.vlmap.spring.loadbalancer.core.platform.servlet.GrayServletConfiguration;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Order(Ordered.LOWEST_PRECEDENCE - 100)

public class EnableGrayLoadBalancerImportSelector extends SpringFactoryImportSelector<EnableGrayLoadBalancer> {


    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        String[] imports = super.selectImports(metadata);
        List<String> importsList = new ArrayList<>(Arrays.asList(imports));
        importsList.add(RibbonClientSpecificationAutoConfiguration.class.getName());
        importsList.add(GrayReactiveConfiguration.class.getName());

        importsList.add(GrayRequestMappingHandlerAdapterConfiguration.class.getName());
        importsList.add(GrayServletConfiguration.class.getName());

        importsList.add(GrayFeignConfiguration.class.getName());
        importsList.add(GrayRestTemplateConfiguration.class.getName());
        importsList.add(GrayWebClientConfiguration.class.getName());


        imports = importsList.toArray(new String[0]);


        return imports;
    }

    @Override
    protected boolean isEnabled() {

        Environment env = getEnvironment();
        return env.getProperty("vlmap.spring.loadbalancer.enabled", Boolean.class, true);



    }

    @Override
    protected boolean hasDefaultFactory() {
        return true;
    }
}
