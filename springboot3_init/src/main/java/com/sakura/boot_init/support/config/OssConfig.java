package com.sakura.boot_init.support.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * OSS 閰嶇疆
 *
 * @author sakura
 */
@Configuration
@ConditionalOnProperty(value = "oss.enable")
public class OssConfig {


    /**
     * 闃块噷浜慜ss鏈嶅姟
     * @param aliOssProperties 閰嶇疆绫?
     * @return Oss
     */
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties aliOssProperties) {
        String endpoint = aliOssProperties.getEndpoint();
        String region = aliOssProperties.getRegion();
        String accessKey = aliOssProperties.getAccessKey();
        String secretKey = aliOssProperties.getSecretKey();
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKey, secretKey);
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    /**
     * OSS瀵硅薄瀛樺偍 閰嶇疆灞炴€?
     *
     * @author sakura
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "oss")
    @ToString
    public static class OssProperties {
        /**
         * 璁块棶绔欑偣
         */
        private String endpoint;

        /**
         * 鑷畾涔夊煙鍚?
         */
        private String domain;

        /**
         * ACCESS_KEY
         */
        private String accessKey;

        /**
         * SECRET_KEY
         */
        private String secretKey;

        /**
         * 鍓嶇紑
         */
        private String prefix;

        /**
         * 瀛樺偍绌洪棿鍚?
         */
        private String bucketName;

        /**
         * 瀛樺偍鍖哄煙
         */
        private String region;

        /**
         * 鏄惁https锛圷=鏄?N=鍚︼級
         */
        private String isHttps;

        /**
         * 妗舵潈闄愮被鍨?0private 1public 2custom)
         */
        private String accessPolicy;
    }
}


