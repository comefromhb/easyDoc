package com.lin.doc.domain;

import java.io.Serializable;
import java.util.List;

public class ApiDoc implements Serializable {

    private static final long serialVersionUID = 11L;
    /**
     * 请求路径
     */
    private String url;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 方法描述
     */
    private String description;
    /**
     * 作者
     */
    private String author;
    /**
     * 请求参数
     */
    private List<ParamDto> requestParam;
    /**
     * 返回参数
     */
    private List<ParamDto> responseParam;

    private Boolean hasVersion;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParamDto> getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(List<ParamDto> requestParam) {
        this.requestParam = requestParam;
    }

    public List<ParamDto> getResponseParam() {
        return responseParam;
    }

    public void setResponseParam(List<ParamDto> responseParam) {
        this.responseParam = responseParam;
    }

    public Boolean getHasVersion() {
        return hasVersion;
    }

    public void setHasVersion(Boolean hasVersion) {
        this.hasVersion = hasVersion;
    }

    @Override
    public String toString() {
        return "InterfaceDoc{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", description='" + description + '\'' +
                ", requestParam=" + requestParam +
                ", responseParam=" + responseParam +
                '}';
    }
}
