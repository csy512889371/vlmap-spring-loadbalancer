package com.github.vlmap.spring.tools.loadbalancer.config;


import com.github.vlmap.spring.tools.DynamicToolProperties;
import com.github.vlmap.spring.tools.loadbalancer.process.SpringMvcTagProcess;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.ribbon.RibbonClientSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableConfigurationProperties({SpringToolsProperties.class})
 public class RibbonClientSpecificationAutoConfiguration {



    @Bean
    public RibbonClientSpecification specification() {
        Class[] classes = new Class[]{TagRibbonClientConfiguration.class};
        return new RibbonClientSpecification("default." + TagRibbonClientConfiguration.class.getName(), classes);
    }

    @ConditionalOnClass(org.springframework.web.servlet.DispatcherServlet.class)
    @Configuration
    static class SpringMVCConfiguration {
        @Bean
        public SpringMvcTagProcess SpringMVCTagProcess(DynamicToolProperties properties) {
            return new SpringMvcTagProcess(properties);
        }
    }


}
