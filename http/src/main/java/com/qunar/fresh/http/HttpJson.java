package com.qunar.fresh.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpJson {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJson.class);

    public static void main(String[] args) {
        HttpJsonManager manager = new HttpJsonManager();
        String secretKey = manager.getSecretKey();
        String cookie = manager.getCookie();

        if(manager.checkSecretKey(secretKey, cookie)) {
            LOGGER.info("校验用户成功");
        }
        else {
            LOGGER.info("校验用户失败");
        }

        //关闭资源
        manager.closeHttpClient();
    }
}
