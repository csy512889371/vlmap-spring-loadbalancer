package com.github.vlmap.spring.loadbalancer.util;

import com.github.vlmap.spring.loadbalancer.GrayTagOfServersProperties;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.cloud.commons.util.InetUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class GrayUtils {
    public static   Map<String, Set<String>> tagOfServer(String clientName){
        clientName=StringUtils.upperCase(clientName);
        Configuration configuration = ConfigurationManager.getConfigInstance().subset(clientName);


        MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource();
        Iterator<String> iterator = configuration.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = configuration.getString(key);
            propertySource.put(key, value);
        }

        Binder binder = new Binder(propertySource);
        GrayTagOfServersProperties ribbon = new GrayTagOfServersProperties();
        binder.bind("ribbon", Bindable.ofInstance(ribbon));

        List<GrayTagOfServersProperties.TagOfServers> tagOfServers = ribbon.getTagOfServers();
        if (tagOfServers != null) {
            Map<String, Set<String>> map = new HashMap<>(tagOfServers.size());

            for (GrayTagOfServersProperties.TagOfServers tagOfServer : tagOfServers) {

                if (tagOfServer != null && CollectionUtils.isNotEmpty(tagOfServer.getTags()) && StringUtils.isNotBlank(tagOfServer.getId())) {
                    map.put(tagOfServer.getId(), tagOfServer.getTags());
                }
            }
           return  Collections.unmodifiableMap(map);


        }
        return Collections.emptyMap();

    }

    public static String ip(InetUtils inetUtils, String networkInterface) throws SocketException {
        String ip=null;
        if (org.springframework.util.StringUtils.isEmpty(networkInterface)) {
            ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        }
        else {
            NetworkInterface netInterface = NetworkInterface
                    .getByName(networkInterface);
            if (null == netInterface) {
                throw new IllegalArgumentException(
                        "no such interface " + networkInterface);
            }

            Enumeration<InetAddress> inetAddress = netInterface.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress currentAddress = inetAddress.nextElement();
                if (currentAddress instanceof Inet4Address
                        && !currentAddress.isLoopbackAddress()) {
                    ip = currentAddress.getHostAddress();
                    break;
                }
            }

            if (org.springframework.util.StringUtils.isEmpty(ip)) {
                throw new RuntimeException("cannot find available ip from"
                        + " network interface " + networkInterface);
            }

        }
        return ip;
    }
}
