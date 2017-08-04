package com.leo.alpha.utils;


import com.alibaba.fastjson.JSON;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author leo
 *
 */
public abstract class HttpUtils {
	
    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create()
            //设置默认时间
            .setUserAgent("www.leo.com")
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(10000)         //连接超时时间(ms)
                    .setSocketTimeout(10000)          //读超时时间（等待数据超时时间）(ms)
                    .setConnectionRequestTimeout(500)    //从池中获取连接超时时间(ms)
                    .build())
            .setConnectionManager(new PoolingHttpClientConnectionManager() {{
                setMaxTotal(200);//最大连接数
                setDefaultMaxPerRoute(10);//默认的每个路由的最大连接数
                setDefaultSocketConfig(SocketConfig.custom()
                        .setSoReuseAddress(true) //是否可以在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
                        .setSoTimeout(10000)       //接收数据的等待超时时间，单位ms
                        .setSoKeepAlive(true)    //开启监视TCP连接是否有效
                        .build()
                );
            }})

            .build();

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 执行get请求
     */
    public static String get(String url) {
        return get(url, null, null);
    }
    
    /**
     * 执行get请求
     */
    public static String get(String url, Map<String, String> query) {
        return get(url, null, query);
    }

    /**
     * 执行get请求
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> query) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.setCharset(StandardCharsets.UTF_8);
            if (!CollectionUtils.isEmpty(query)) {
                query.entrySet().forEach(entry -> {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                });
            }

            HttpGet request = new HttpGet(uriBuilder.build());

            //设置请求头
            if (!CollectionUtils.isEmpty(headers)) {
                headers.entrySet().forEach(entry -> {
                    request.setHeader(entry.getKey(), entry.getValue());
                });
            }
            //执行请求
            return DEFAULT_HTTP_CLIENT.execute(request, response -> {
                //HTTP响应信息默认处理方法
                try {
                    String responseBody = StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        logger.info("http请求结果异常：{} {}",response.getStatusLine(),responseBody);
                        throw new HttpException(request, response, responseBody);
                    }
                    return responseBody;
                } catch (IOException ex) {
                    logger.info("http请求结果异常：{} {}",response.getStatusLine(),ex.getMessage());
                    throw new HttpException(request, response, null);
                }
            });
        } catch (Exception e) {
        	logger.error("",e);
        }
        return null;
    }

    /**
     * 执行post请求，将使用x-www-form-urlencode格式提交form表单对象
     */
    public static String post(String url, Map<String, String> form) {
        return post(url, null, form);
    }

    /**
     * 执行post请求
     */
    public static String post(String url, Map<String, String> headers, Map<String, String> form) {
        HttpEntity entity = null;
        if (!CollectionUtils.isEmpty(form)) {
            entity = new UrlEncodedFormEntity(
                    form.entrySet().stream()
                            .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList()),
                    StandardCharsets.UTF_8
            );
        }
        return post(url, headers, entity);
    }
    
    /**
     * POST 上传文件
     * @param url
     * @param file
     * @param headers
     * @return
     */
    public static String postFile(String url, File file,Map<String, String> headers,Map<String,String> form){
    	try {
            HttpPost request = new HttpPost(url);
            if (null == file || !file.exists() || !file.isFile() || !file.canRead()) {
                Assert.isTrue(false, "上传文件无效");
            }
            //设置请求头
            if (!CollectionUtils.isEmpty(headers)) {
                headers.entrySet().forEach(entry -> {
                    request.setHeader(entry.getKey(), entry.getValue());
                });
            }

            MultipartEntityBuilder builder=MultipartEntityBuilder.create();
            //添加form表单
            if(null != form && form.keySet().size()>0){
                for(String key:form.keySet()){
                    builder.addTextBody(key, form.get(key), ContentType.DEFAULT_TEXT);
                }
            }
            //添加文件
            builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());

            //设置请求体
            request.setEntity(builder.build());

            return DEFAULT_HTTP_CLIENT.execute(request, response -> {
                //HTTP响应信息默认处理方法
                try {
                    String responseBody = StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        logger.info("http请求结果异常：{} {}",response.getStatusLine(),responseBody);
                        throw new HttpException(request, response, responseBody);
                    }
                    return responseBody;
                } catch (IOException ex) {
                    logger.info("http请求结果异常：{} {}",response.getStatusLine(),ex.getMessage());
                    throw new HttpException(request, response, null);
                }
            });
        } catch (IOException e) {
        	logger.error("",e);
        }
    	return null;
    }

    /**
     * 执行post请求
     */
    public static String post(String url, Map<String, String> headers, HttpEntity entity) {
        try {
            HttpPost request = new HttpPost(url);
            //设置请求头
            if (!CollectionUtils.isEmpty(headers)) {
                headers.entrySet().forEach(entry -> {
                    request.setHeader(entry.getKey(), entry.getValue());
                });
            }
            //设置请求体
            if (entity != null) {
                request.setEntity(entity);
            }

            return DEFAULT_HTTP_CLIENT.execute(request, response -> {
                //HTTP响应信息默认处理方法
                try {
                    String responseBody = StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        logger.info("http请求结果异常：request url:{},{} {}",url,response.getStatusLine(),responseBody);
                        throw new HttpException(request, response, responseBody);
                    }
                    return responseBody;
                } catch (IOException ex) {
                    logger.info("http请求结果异常：request url:{},{} {}",url,response.getStatusLine(),ex.getMessage());
                    throw new HttpException(request, response, null);
                }
            });
        } catch (IOException e) {
        	logger.error("",e);
        }
        return null;
    }

    /**
     * 执行get请求获取json
     */
    public static <T> T getForJson(String url, Map<String, String> query, Class<T> responseClass) {
        Map<String, String> headers = new HashMap<String, String>() {{
            put(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE);
        }};
        String response = get(url, headers, query);
        Assert.hasText(response, "HTTP响应体未包含任何信息");
        return JSON.parseObject(response, responseClass);
    }

    /**
     * 执行post请求获取json，将使用x-www-form-urlencode格式提交form表单对象
     */
    public static <T> T postForJson(String url, Map<String, String> form, Class<T> responseClass) {
        Map<String, String> headers = new HashMap<String, String>() {{
            put(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE);
        }};
        String response = post(url, headers, form);
        Assert.hasText(response, "HTTP响应体未包含任何信息");
        return JSON.parseObject(response, responseClass);
    }

    /**
     * 执行post请求获取json，将使用json格式提交指定对象
     */
    public static <T> T postJsonForJson(String url, Object object, Class<T> responseClass) {
        HttpEntity entity = object == null ? null
                : new StringEntity(JSON.toJSONString(object), ContentType.APPLICATION_JSON);
        String response = post(url, null, entity);
        Assert.hasText(response, "HTTP响应体未包含任何信息");
        return JSON.parseObject(response, responseClass);
    }


    public static class HttpException extends RuntimeException {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private HttpRequest request;

        private HttpResponse response;

        private String responseBody;

        public HttpException(HttpRequest request, HttpResponse response, String responseBody) {
            this.request = request;
            this.response = response;
            this.responseBody = responseBody;
        }

        public HttpRequest getRequest() {
            return request;
        }

        public void setRequest(HttpRequest request) {
            this.request = request;
        }

        public HttpResponse getResponse() {
            return response;
        }

        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }
    }
}
