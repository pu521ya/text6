package com.qunar.fresh.http;
import com.qunar.base.meerkat.http.data.HttpResult;
public interface HttpProcessorInterface {
    /**
     *处理http请求
     */
    boolean processResponse(HttpResult result);

    /**
     * 返回标识处理结果的状态码，对于当前接口，status = 0 表示返回成功
     */
    int getStatusCode();

    /**
     *根据要查询的头名称，返回其值信息
     */
    String getHeaderValue(String headerName);

    /**
     * 获取http请求返回message
     */
    String getResponseMessage();

    /**
     * 返回请求的需要获得的数据
     */
    String getResult();
}
