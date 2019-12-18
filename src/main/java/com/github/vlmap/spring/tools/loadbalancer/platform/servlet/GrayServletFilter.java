package com.github.vlmap.spring.tools.loadbalancer.platform.servlet;

import com.github.vlmap.spring.tools.GrayLoadBalancerProperties;
import com.github.vlmap.spring.tools.context.ContextManager;
import com.github.vlmap.spring.tools.loadbalancer.platform.IStrictHandler;
import com.github.vlmap.spring.tools.loadbalancer.platform.Platform;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
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
import java.util.*;


public class GrayServletFilter implements OrderedFilter, IStrictHandler {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    GrayLoadBalancerProperties properties;

    public GrayServletFilter(GrayLoadBalancerProperties properties) {
        this.properties = properties;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String name = this.properties.getHeaderName();
        String tag = httpServletRequest.getHeader(name);
        /**
         * 非兼容模式,请求标签不匹配拒绝响应
         */
        String uri = ((HttpServletRequest) request).getRequestURI();
        if (should(properties, tag) && !shouldIgnore(properties, uri)) {
            GrayLoadBalancerProperties.Strict strict = properties.getStrict();

            if (logger.isInfoEnabled()) {
                logger.info("The server isn't compatible model,current request Header[" + name + ":" + tag + "] don't match \"" + tag + "\",response code:" + strict.getCode());

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

            if (StringUtils.isBlank(tag)&&Platform.getInstnce().isGatewayService()) {

                    tag = properties.getHeader();
                    if (StringUtils.isNotBlank(tag)) {

                        Map<String, List<String>> headers = getHeaders(httpServletRequest);

                        addHeader(headers, name, tag);

                        request = new HttpServletRequestWrapper(httpServletRequest, headers);


                    }


            }

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

    protected void addHeader(Map<String, List<String>> headers, String name, String value) {

        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<>();
            headers.put(name, values);

        }
        values.add(value);
    }

    protected Map<String, List<String>> getHeaders(HttpServletRequest httpServletRequest) {

        Enumeration<String> enumeration = httpServletRequest.getHeaderNames();
        Map<String, List<String>> headers = new HashMap<>();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement();
            headers.put(headerName, EnumerationUtils.toList(httpServletRequest.getHeaders(headerName)));
        }
        return headers;

    }

    private static class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
        Map<String, List<String>> headers;

        public HttpServletRequestWrapper(HttpServletRequest request, Map<String, List<String>> headers) {
            super(request);
            this.headers = headers;
        }

        @Override
        public Enumeration<String> getHeaderNames() {


            return new IteratorEnumeration(headers.keySet().iterator());
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> headerValues = headers.get(name);
            headerValues = headerValues == null ? Collections.emptyList() : headerValues;

            return new IteratorEnumeration(headerValues.iterator());
        }

        @Override
        public String getHeader(String name) {
            List<String> headerValues = headers.get(name);
            if (CollectionUtils.isEmpty(headerValues)) {
                return null;
            }
            return headerValues.get(0);
        }
    }
}
