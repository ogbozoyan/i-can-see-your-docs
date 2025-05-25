package ru.ogbozoyan.core.configuration.s3;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "aws")
public class S3Properties {
    private String accessKey;
    private String secretKey;
    private Region region = Region.EU_CENTRAL_1;
    private String s3Bucket;
    private String s3Endpoint;
}
