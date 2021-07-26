package com.wu.studyniuke.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author me
 * @create 2021-07-26-16:20
 */
@Configuration
public class WKConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(WKConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init(){
        File file = new File(wkImageStorage);
        if(!file.exists()){
            file.mkdirs();
            LOGGER.info("创建目录");
        }
    }

}
