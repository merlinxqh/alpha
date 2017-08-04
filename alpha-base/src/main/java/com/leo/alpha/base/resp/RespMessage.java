package com.leo.alpha.base.resp;

/**
 * Created by leo on 2017/8/3.
 * 返回结果封装
 */
public class RespMessage<T> {

    private Long code;

    private String msg;

    private T data;

    public RespMessage(Long code,String msg,T data){
        this.code=code;
        this.msg=msg;
        this.data=data;
    }

    public RespMessage(Long code,String msg){
        this.code=code;
        this.msg=msg;
    }

    public RespMessage(T data){
        this.data=data;
    }

    public RespMessage(){}

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
