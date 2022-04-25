package im.cheng.xihe.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "xihe")
public class XiheConfiguration {
    private String userAgent;
    private long timeToLive;
}
