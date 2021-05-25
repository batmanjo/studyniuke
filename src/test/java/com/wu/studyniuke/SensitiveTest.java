package com.wu.studyniuke;

import com.wu.studyniuke.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author me
 * @create 2021-05-24-15:39
 */
@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String s = "@@@###赌@@#博。嫖娼，吸毒#吸@@粉，贵%%物，沈阳大街";
        s=sensitiveFilter.filter(s);
        System.out.println(s);
    }
}
