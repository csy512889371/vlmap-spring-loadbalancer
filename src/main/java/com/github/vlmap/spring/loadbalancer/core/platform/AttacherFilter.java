package com.github.vlmap.spring.loadbalancer.core.platform;

import com.github.vlmap.spring.loadbalancer.GrayLoadBalancerProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.List;

public class AttacherFilter extends CommandsListener<RequestMatchParamater> implements Ordered {
     protected Logger logger = LoggerFactory.getLogger(this.getClass());
     protected MatcherProcess matcher = new MatcherProcess();


    public AttacherFilter(GrayLoadBalancerProperties properties) {
        super(properties);
    }

    @Override
    public Class<RequestMatchParamater> getParamaterType() {
        return RequestMatchParamater.class;
    }



    @Override
    protected boolean validate(RequestMatchParamater paramater) {
        return paramater.isState() && StringUtils.isNotBlank(paramater.getValue());
    }

    @Override
    protected List<String> getCommands(GrayLoadBalancerProperties properties) {
        return properties.getAttacher() == null ? null : properties.getAttacher().getCommands();
    }

    public List<RequestMatchParamater> getParamaters() {
        return getCommandObject();
    }



    @Override
    public int getOrder() {
        return FilterOrder.ORDER_ATTACH_FILTER;
    }


}
