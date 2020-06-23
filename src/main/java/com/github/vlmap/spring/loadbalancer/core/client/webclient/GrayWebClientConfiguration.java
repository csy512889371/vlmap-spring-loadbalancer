package com.github.vlmap.spring.loadbalancer.core.client.webclient;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.loadbalancer.reactive.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "vlmap.spring.loadbalancer.web-client.enabled", matchIfMissing = true)
@ConditionalOnClass( name={
        "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration",
        "org.springframework.web.reactive.function.client.ExchangeFilterFunction"
})

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)

public class GrayWebClientConfiguration {
    @Bean
    public GrayWebClientInterceptor grayWebClientInterceptor() {
        return new GrayWebClientInterceptor();
    }

    @Bean
    public WebClientCustomizer grayLoadbalanceClientWebClientCustomizer(
            GrayWebClientInterceptor filterFunction) {
        return builder -> builder.filters((List<ExchangeFilterFunction> exchangeFilterFunctions) -> {
            List<ExchangeFilterFunction> list = new ArrayList<>();
            if (!exchangeFilterFunctions.contains(filterFunction)) {
                list.add(filterFunction);
            }
            list.addAll(exchangeFilterFunctions);
            exchangeFilterFunctions.clear();
            exchangeFilterFunctions.addAll(list);

        });
    }


}
