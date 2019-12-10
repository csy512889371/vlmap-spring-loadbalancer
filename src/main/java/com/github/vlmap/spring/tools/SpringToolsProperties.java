package com.github.vlmap.spring.tools;


import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.tools")

public class SpringToolsProperties {

    public static final String DEFAULT_TOOLS_PROPERTIES_NAME = "defaultToolsProperties";



    private String propertySourceName = DEFAULT_TOOLS_PROPERTIES_NAME;

    private Compatible compatible=new Compatible();

    private TagLoadbalancer tagLoadbalancer = new TagLoadbalancer();

    private Zookeeper zookeeper = new Zookeeper();

    public Zookeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(Zookeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getPropertySourceName() {
        return propertySourceName;
    }

    public void setPropertySourceName(String propertySourceName) {
        this.propertySourceName = propertySourceName;
    }


    public TagLoadbalancer getTagLoadbalancer() {
        return tagLoadbalancer;
    }

    public void setTagLoadbalancer(TagLoadbalancer tagLoadbalancer) {
        this.tagLoadbalancer = tagLoadbalancer;
    }


    public String getTagHeaderName() {
        return tagLoadbalancer.getHeaderName();
    }

    public Compatible getCompatible() {
        return compatible;
    }

    public void setCompatible(Compatible compatible) {
        this.compatible = compatible;
    }

    static public class Zookeeper {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    static public class Feign {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    static public class RestTemplate {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    static public class WebClient {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    /**
     * 兼容模式
     */
    static public class Compatible{

        private boolean enabled=false;
       private int code=403;
       private String message="Forbidden";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    static public class TagLoadbalancer {
        private boolean enabled = true;
        private String headerName = "Loadbalancer-Tag";

        /**
         * 只在 gateway,zuul 网关才会用到
         */

        private String header;

        private Feign feign = new Feign();
        private RestTemplate restTemplate = new RestTemplate();
        private WebClient webClient = new WebClient();

        public WebClient getWebClient() {
            return webClient;
        }

        public void setWebClient(WebClient webClient) {
            this.webClient = webClient;
        }

        public RestTemplate getRestTemplate() {
            return restTemplate;
        }

        public void setRestTemplate(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public Feign getFeign() {
            return feign;
        }

        public void setFeign(Feign feign) {
            this.feign = feign;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }
    }
}
