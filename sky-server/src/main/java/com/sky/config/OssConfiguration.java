package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-12
 * Time: 14:14
 */
@Slf4j
@Configuration
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean//保证只有唯一一个bean对象
    public AliOssUtil getAliOssUtil(AliOssProperties properties){
        log.info("开始创建阿里云文件上传工具类对象 {}",properties);
        return new AliOssUtil(properties.getEndpoint(),
                       properties.getAccessKeyId(),
                       properties.getAccessKeySecret(),
                       properties.getBucketName());
    }
}
