package com.wu.studyniuke;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author me
 * @create 2021-05-14-20:49
 */
@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class LoggerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger(){
        System.out.println(LOGGER.getName());

        LOGGER.debug("debug log");
        LOGGER.info("info log");
        LOGGER.warn("warning log");
        LOGGER.error("error log");
    }

}
