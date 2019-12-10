package com.github.vlmap.spring.tools.loadbalancer.client.webclient;

import com.github.vlmap.spring.tools.SpringToolsProperties;
import com.github.vlmap.spring.tools.context.ContextManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class TagWebClientInterceptor implements ExchangeFilterFunction {

    @Autowired

    private SpringToolsProperties properties;


    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

        HttpHeaders headers = request.headers();
        String headerName = properties.getTagHeaderName();
        String header = headers.getFirst(headerName);
        String tag = header;
        if (StringUtils.isBlank(tag)) {
            tag = ContextManager.getRuntimeContext().getTag();

        }
        if (StringUtils.isBlank(tag)) {
            tag = properties.getTagLoadbalancer().getHeader();

        }
        if (StringUtils.isNotBlank(tag) && !StringUtils.equals(tag, header)) {
            request = ClientRequest.from(request).header(headerName, tag).build();

        }

        try {
            ContextManager.getRuntimeContext().setTag(tag);
            return next.exchange(request);

        } finally {
            ContextManager.getRuntimeContext().onComplete();

        }

    }
}
