package com.github.vlmap.spring.loadbalancer.platform.servlet;

import com.github.vlmap.spring.loadbalancer.GrayLoadBalancerProperties;
import com.github.vlmap.spring.loadbalancer.runtime.ContextManager;
import com.github.vlmap.spring.loadbalancer.core.StrictHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class GrayServletFilter implements OrderedFilter {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    GrayLoadBalancerProperties properties;
    StrictHandler strictHandler;
    public GrayServletFilter(GrayLoadBalancerProperties properties,  StrictHandler strictHandler) {
        this.properties = properties;
        this.strictHandler=strictHandler;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String headerName = this.properties.getHeaderName();
        String tag = httpServletRequest.getHeader(headerName);
        /**
         * 非兼容模式,请求标签不匹配拒绝响应
         */
        String uri = ((HttpServletRequest) request).getRequestURI();
        if (!strictHandler.validate(uri, tag) ) {
            GrayLoadBalancerProperties.Strict strict = properties.getStrict();

            if (logger.isInfoEnabled()) {

                logger.info("The server is strict model,current request Header[" + headerName + ":" + tag + "] don't match \"[" + StringUtils.join(strictHandler.getGrayTags()) + "]\",response code:" + strict.getCode());

            }

            String message = strict.getMessage();
            if (StringUtils.isBlank(message)) {
                httpServletResponse.setStatus(strict.getCode());

            } else {
                httpServletResponse.sendError(strict.getCode(), message);
            }
            return;
        }

        try {


            ContextManager.getRuntimeContext().setTag(tag);
            chain.doFilter(request, response);

        } finally {
            ContextManager.getRuntimeContext().onComplete();
        }

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}