package com.qunar.fresh.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.base.meerkat.http.QunarHttpClient;
import com.qunar.base.meerkat.http.data.HttpResult;
import com.qunar.base.meerkat.http.data.PostParameter;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HttpRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private final requestType REQUEST_TYPE;
    private final String REQUEST_URL;
    private final String REQUEST_CHARSET;
    private final Map<String, String> REQUEST_PARAM;
    private final Map<String, String> REQUEST_Header;

    private HttpRequest(Builder builder) {
        REQUEST_URL = builder.url;
        REQUEST_PARAM = builder.requestParam;
        REQUEST_TYPE = builder.requestType;
        REQUEST_Header = builder.requestHeader;
        REQUEST_CHARSET = builder.charset;
    }

    public enum requestType {
        GET,
        POST
    }

    public static class Builder {
        private final String url;

        private  String charset = "UTF-8";
        private Map<String, String> requestParam = Maps.newHashMap();
        private requestType requestType = HttpRequest.requestType.GET;
        private Map<String, String> requestHeader = Maps.newHashMap();

        public Builder(String url) {
            this.url = url;
        }

        /**
         * 输入同名的参数会，值会进行覆盖
         * 输入名称为“”或者null,则不会添加参数
         */
        public Builder addParam(String name, String value) {
            if(!Strings.isNullOrEmpty(name)) {
                requestParam.put(name, value);
            }

            return this;
        }

        public Builder setType(HttpRequest.requestType type) {
            requestType = type;

            return this;
        }

        /**
         * 字符集,默认UTF-8
         */
        public Builder setCharset(String charset) {
            if(!Strings.isNullOrEmpty(charset)) {
                this.charset = charset;
            }

            return this;
        }

        public Builder addHeader(Map<String, String> requestHeader) {
            if(requestHeader != null) {

                for (Map.Entry<String, String> header: requestHeader.entrySet()) {
                    addHeader(header.getKey(), header.getValue());
                }
            }

            return this;
        }

        public Builder addHeader(String name, String value) {
            if(!Strings.isNullOrEmpty(name)) {
                requestHeader.put(name, value);
            }

            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    /**
     * 执行请求
     * ＠return HttpResult　请求结果
     */
    public HttpResult doRequest(QunarHttpClient httpClient) throws IOException {
        Preconditions.checkNotNull(httpClient);

        HttpUriRequest request = getRequest();

        //设置请求头
        request = setHeaders(request);
        //执行请求
        HttpResult result = httpClient.httpExecute(request, REQUEST_CHARSET);
        //释放连接
        releaseConnection(request);

        return result;
    }

    /**
     * 执行请求，并执行回调函数httpProcessor
     * ＠return ｛＠code true｝　请求成功
     */
    public boolean doRequest(QunarHttpClient httpClient, HttpProcessorInterface httpProcessor) {
        Preconditions.checkNotNull(httpClient);
        Preconditions.checkNotNull(httpProcessor);

        try
        {
            httpProcessor.processResponse(doRequest(httpClient));
        }
        catch (IOException e) {
            LOGGER.error("发起http请求错误", e);
            return false;
        }

        return true;
    }

    /**
     * 根据当前的请求类型获取对应的请求实例
     */
    private HttpUriRequest getRequest() {
        HttpUriRequest request = null;

        switch (REQUEST_TYPE) {
            case GET:
                request = httpGetRequest();
                break;
            case POST:
                request = httpPostRequest();
                break;
        }

        return request;
    }

    /**
     * 释放http连接
     */
    private void releaseConnection(HttpUriRequest request) {
        if(request instanceof HttpGet) {
            HttpGet get = (HttpGet)request;
            get.releaseConnection();
        }
        else if(request instanceof HttpPost) {
            HttpPost post = (HttpPost)request;
            post.releaseConnection();
        }
    }

    /**
     * 返回get请求实例
     */
    private HttpUriRequest httpGetRequest() {
        StringBuilder ret = new StringBuilder(REQUEST_URL);
        boolean firstParam = true;

        for (Map.Entry<String, String> entry : REQUEST_PARAM.entrySet()) {

            if(!firstParam) {
                ret.append("&");
            }
            else {
                ret.append("?");
                firstParam = false;
            }

            ret.append(entry.getKey());
            ret.append("=");
            ret.append(entry.getValue());
        }

        HttpGet get = new HttpGet(ret.toString());

        return get;
    }

    /**
     * 返回post请求实例
     */
    private HttpUriRequest httpPostRequest() {
        HttpPost post = new HttpPost(REQUEST_URL);
        PostParameter parameter = new PostParameter();

        for (Map.Entry<String, String> entry : REQUEST_PARAM.entrySet()) {
            parameter.put(entry.getKey(), entry.getValue());
        }

        try {
            post.setEntity(new UrlEncodedFormEntity(parameter.getNvps(), "UTF-8"));
        } catch (UnsupportedEncodingException var6) {
            LOGGER.warn("httpPost parameter UrlEncodedFormEntity UnsupportedEncodingException:" + var6.getMessage());
        }

        return post;
    }

    private HttpUriRequest setHeaders(HttpUriRequest request) {
        for (Map.Entry<String, String> entry : REQUEST_Header.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }

        return request;
    }
}
