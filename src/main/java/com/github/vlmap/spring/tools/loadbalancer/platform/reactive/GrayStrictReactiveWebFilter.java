package com.github.vlmap.spring.tools.loadbalancer.platform.reactive;

import com.github.vlmap.spring.tools.GrayLoadBalancerProperties;
import com.github.vlmap.spring.tools.loadbalancer.platform.IStrictHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class GrayStrictReactiveWebFilter implements OrderedWebFilter, IStrictHandler {
    private GrayLoadBalancerProperties properties;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public GrayStrictReactiveWebFilter(GrayLoadBalancerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String headerName = this.properties.getHeaderName();
        String tag = null;
        if (exchange != null) {
            tag = exchange.getRequest().getHeaders().getFirst(headerName);
        }

        String uri = exchange.getRequest().getURI().toString();

        /**
         * 严格模式,请求标签不匹配拒绝响应
         */
        if (should(properties, tag) && !shouldIgnore(properties, uri)) {
            GrayLoadBalancerProperties.Strict strict = properties.getStrict();
            if (logger.isInfoEnabled()) {
                logger.info("The server isn't compatible model,current request Header[" + headerName + ":" + tag + "] don't match \"" + tag + "\",response code:" + strict.getCode());

            }
            String message = strict.getMessage();
            HttpStatus status = HttpStatus.valueOf(strict.getCode());
            if (StringUtils.isBlank(message)) {
                throw new ResponseStatusException(status);

            } else {
                throw new ResponseStatusException(status, message);
            }

        }


        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
