package com.github.vlmap.cloud.loadbalancer.rule;


import com.github.vlmap.cloud.loadbalancer.tag.TagProcess;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DelegatingLoadBalancer implements
        ILoadBalancer {
    List<TagProcess> tagProcesses;
    private ILoadBalancer target;
    private AtomicBoolean tagRuleInProgress;
    private AtomicReference<Map<Server, String>> tagsInProgress;

    public DelegatingLoadBalancer(ILoadBalancer target, AtomicBoolean tagRuleInProgress, AtomicReference<Map<Server, String>> tagsInProgress) {
        this.target = target;
        this.tagRuleInProgress = tagRuleInProgress;
        this.tagsInProgress = tagsInProgress;
    }


    @Override
    public void addServers(List<Server> newServers) {
        target.addServers(newServers);
    }

    @Override
    public Server chooseServer(Object key) {
        return target.chooseServer(key);
    }

    @Override
    public void markServerDown(Server server) {
        target.markServerDown(server);
    }

    @Override
    public List<Server> getServerList(boolean availableOnly) {
        return (availableOnly ? getReachableServers() : getAllServers());
    }


    @Override
    public List<Server> getReachableServers() {
        List<Server> servers = target.getReachableServers();
        return processServers(servers);
    }

    @Override
    public List<Server> getAllServers() {

        List<Server> servers = target.getAllServers();
        return processServers(servers);

    }

    protected String tag() {
        for (TagProcess process : tagProcesses) {
            String tag = process.getTag();
            if (StringUtils.isNotBlank(tag)) {
                return tag;
            }
        }
        return null;
    }
    protected List<Server> processServers(List<Server> servers) {
        if (tagRuleInProgress == null) return servers;
        String tagValue=tag();
        boolean state = tagRuleInProgress.get();
        Map<Server, String> map = tagsInProgress.get();
        List<Server> list = new ArrayList<>(servers.size());
        if (state) {
            for(Server server:servers){
                String tag=map.get(server);
                if(StringUtils.equals(tagValue,tag)){
                    list.add(server);
                }
            }
        } else {
            for(Server server:servers){
                String tag=map.get(server);
                if(StringUtils.isBlank(tag)){
                    list.add(server);
                }
            }

        }
        return Collections.unmodifiableList(list);
    }


}