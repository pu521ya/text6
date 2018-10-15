package com.qunar.fresh.http;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.base.meerkat.http.QunarHttpClient;
import com.qunar.base.meerkat.http.data.HttpResult;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.api.json.JsonMapper;
import qunar.api.json.MapperBuilder;

import java.util.Map;

public class HttpJsonManager {
    private static final String VALIDATE_URL = "http://l-qmexp1.f.dev.cn6.qunar.com:10000/httpDemo/userToken/validateToken";
    private static final String SECRET_KEY_URL = "http://l-qmexp1.f.dev.cn6.qunar.com:10000/httpDemo/userToken/getUserInfo";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJson.class);
    private static final JsonMapper MAPPER = MapperBuilder.create().build();
    private static final QunarHttpClient HTTP_CLIENT = QunarHttpClient.createDefaultClient();
    private String cookie = null;

    public HttpJsonManager() {
    }

    /**
     * 发起请求获取token,和需要设置的cookie
     * @return {@code null} 请求失败
     */
    public String getSecretKey() {
        //构造请求
        HttpRequest request = new HttpRequest
                .Builder(SECRET_KEY_URL)
                .addParam("userName","wangliang.wang")
                .addHeader("Accept-Encoding","gzip, deflate")
                .buid();

        //请求处理器
        HttpProcessorInterface httpProcessor = tokenProcessor();
        String secretKey = null;

        if(request.doRequest(HTTP_CLIENT, httpProcessor)
                && httpProcessor.getStatusCode() == 0) {
            secretKey = httpProcessor.getResult();
            cookie = httpProcessor.getHeaderValue("Set-Cookie");
        }
        else {
            LOGGER.error("请求失败,Message:{}",httpProcessor.getResponseMessage());
        }

        return secretKey;
    }

    public String getCookie() {
        return cookie;
    }

    /**
     * 根据获取到的token,和cookie,校验用户
     */
    public boolean checkSecretKey(String secretKey, String cookie) {
        if(Strings.isNullOrEmpty(secretKey)) {
            return false;
        }

        boolean ret = false;

        //构造请求
        HttpRequest request = new HttpRequest
                .Builder(VALIDATE_URL)
                .setType(HttpRequest.requestType.POST)
                .addParam("userName","wangliang.wang")
                .addParam("userToken",secretKey)
                .addHeader("Accept-Encoding","gzip, deflate")
                .addHeader("Cookie",cookie)
                .build();

        //请求处理器
        HttpProcessorInterface httpProcessor = tokenProcessor();

        if(request.doRequest(HTTP_CLIENT, httpProcessor)
                && httpProcessor.getStatusCode() == 0) {
            ret = true;
        }
        else {
            LOGGER.error("请求失败,Message:{}",httpProcessor.getResponseMessage());
        }

        return ret;
    }

    public void closeHttpClient() {
        if(HTTP_CLIENT != null) {
            HTTP_CLIENT.close();
        }
    }

    /**
     * 返回一个自定义的httｐ请求处理接口
     */
    private HttpProcessorInterface tokenProcessor() {
        return new HttpProcessorInterface() {
            private String data = null;
            private int status = -1;
            private String message = null;
            private Map<String, String> headerMap = Maps.newHashMap();

            @Override
            public boolean processResponse(HttpResult result) {
                if(result != null) {
                    RequestResult requestResult = MAPPER.readValue(result.getContent(),
                            RequestResult.class);

                    data = requestResult.getData();
                    status = requestResult.getStatus();
                    message = requestResult.getMessage();
                    Header[] headers = result.getHeaders();

                    if(headers != null) {
                        for (Header header : headers) {
                            headerMap.put(header.getName(),header.getValue());
                        }
                    }
                }

                return true;
            }

            @Override
            public int getStatusCode() {
                return status;
            }

            @Override
            public String getHeaderValue(String headerName) {
                return headerName == null ? null : headerMap.get(headerName);
            }

            @Override
            public String getResponseMessage() {
                return message;
            }

            @Override
            public String getResult() {
                return data;
            }
        };
    }
}
