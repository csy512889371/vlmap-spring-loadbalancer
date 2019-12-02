package com.github.vlmap.spring.tools.loadbalancer;


import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DelegatingLoadBalancer implements
        ILoadBalancer {
    List<TagProcess> tagProcesses;
    private ILoadBalancer target;
     private AtomicReference<Map<String,Set<String>>> tagsInProgress;

    public DelegatingLoadBalancer(ILoadBalancer target, List<TagProcess> tagProcesses,  AtomicReference<Map<String,Set<String>>> tagsInProgress) {
        this.target = target;
        this.tagProcesses = tagProcesses;
         this.tagsInProgress = tagsInProgress;
    }

    @PostConstruct
    public void init() {
        if (CollectionUtils.isNotEmpty(tagProcesses)) {
            AnnotationAwareOrderComparator.sort(tagProcesses);

        }
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

        Map<String,Set<String>> map = tagsInProgress.get();
        if(map.isEmpty()){
            return servers;             // 如果所有节点都没配标签，返回所有列表，

        }
        String tagValue = tag();
        List<Server> list = new ArrayList<>(servers.size());

        if(StringUtils.isBlank(tagValue)){
            //无标签请求，排除包含标签的节点

                for (Server server : servers) {
                    Set<String> tags=map.get(server.getId());
                    if(tags!=null&&tags.contains(tagValue)){
                        list.add(server);
                    }

                }
                return list;

        }else{
            //有标签的请求,优先匹配标签

            for (Server server : servers) {
                Set<String> tags=map.get(server.getId());
                if(tags!=null&&tags.contains(tagValue)){
                    list.add(server);
                }

            }
            //匹配不到则返回，无标签节点
            if(list.isEmpty()){
                for (Server server : servers) {
                    Set<String> tags=map.get(server.getId());
                    if(CollectionUtils.isEmpty(tags)){
                        list.add(server);
                    }

                }
            }


        }

        return Collections.unmodifiableList(list);
    }


}
