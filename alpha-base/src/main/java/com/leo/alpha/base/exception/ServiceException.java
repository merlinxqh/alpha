package com.leo.alpha.base.exception;

/**
 * 自定义异常
 * @author leo
 * 继承RuntimeException 是因为 spring事务处理 只会针对这个异常 做回滚  
 */
public class ServiceException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6926247165110964054L;
	
	private String code;
	
	public ServiceException(String code, String msg){
		super(msg);
		this.code=code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
