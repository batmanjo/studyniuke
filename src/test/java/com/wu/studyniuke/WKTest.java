package com.wu.studyniuke;

import java.io.IOException;

/**
 * @author me
 * @create 2021-07-26-16:13
 */
public class WKTest {
    public static void main(String[] args) {
        String cmd = "D:\\UsefulTools\\WK\\wkhtmltox\\bin\\wkhtmltoimage --quality 75 https://www.nowcoder.com/ D:\\work\\data\\wk-image\\2.png";

        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
