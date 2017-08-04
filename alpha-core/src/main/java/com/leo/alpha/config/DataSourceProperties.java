package com.leo.alpha.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 
 * @author leo
 * 
 */
@Component
@ConfigurationProperties(prefix="jdbc")
public class DataSourceProperties {
	
	private String driverClassName;
	
	private String url;
	
	private String username;
	
	private String password;
	
	/**
	 * 初始化连接池大小
	 */
	private int initPoolSize;
	/**
	 * 最大连接数
	 */
	private int minPoolSize;
	
	/**
	 * 连接池最小空闲
	 */
	private int maxPoolSize;
	
	
	private String validationQuery;


	public String getDriverClassName() {
		return driverClassName;
	}


	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public int getInitPoolSize() {
		return initPoolSize;
	}


	public void setInitPoolSize(int initPoolSize) {
		this.initPoolSize = initPoolSize;
	}


	public int getMinPoolSize() {
		return minPoolSize;
	}


	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}


	public int getMaxPoolSize() {
		return maxPoolSize;
	}


	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}


	public String getValidationQuery() {
		return validationQuery;
	}


	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}
}
