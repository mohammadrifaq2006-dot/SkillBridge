package com.skillbridge.lms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class DatabaseUrlNormalizerConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUrlNormalizerConfig.class);

    @Bean
    public static BeanPostProcessor dataSourcePropertiesNormalizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSourceProperties props) {
                    String rawUrl = props.getUrl();
                    if (rawUrl != null && !rawUrl.isBlank()) {
                        String normalized = normalizeMysqlJdbcUrl(rawUrl.trim(), props);
                        props.setUrl(normalized);
                    }
                }
                return bean;
            }
        };
    }

    static String normalizeMysqlJdbcUrl(String url, DataSourceProperties props) {
        String working = url;

        // Support Aiven service URI format: mysql://user:pass@host:port/defaultdb?ssl-mode=REQUIRED
        if (working.startsWith("mysql://")) {
            try {
                URI uri = URI.create(working);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    int idx = userInfo.indexOf(':');
                    String uriUser = userInfo.substring(0, idx);
                    String uriPass = userInfo.substring(idx + 1);
                    if (props.getUsername() == null || props.getUsername().isBlank() || "root".equals(props.getUsername())) {
                        props.setUsername(uriUser);
                    }
                    if (props.getPassword() == null || props.getPassword().isBlank()) {
                        props.setPassword(uriPass);
                    }
                }
                String query = uri.getRawQuery();
                working = "jdbc:mysql://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                        + uri.getPath() + (query != null && !query.isBlank() ? "?" + query : "");
                log.info("Normalized Aiven mysql:// URI into JDBC URL format for host '{}'", uri.getHost());
            } catch (Exception ex) {
                working = "jdbc:" + working;
            }
        }

        // Normalize Aiven's ssl-mode parameter to Connector/J's sslMode
        if (working.contains("ssl-mode=")) {
            working = working.replace("ssl-mode=", "sslMode=");
        }

        return working;
    }
}
