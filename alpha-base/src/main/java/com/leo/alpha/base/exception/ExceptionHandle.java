package com.leo.alpha.base.exception;

import com.leo.alpha.base.resp.RespMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常捕获
 * @author leo
 *
 */
@ControllerAdvice
public class ExceptionHandle {
   
	
	@ExceptionHandler(value=Exception.class)
	@ResponseBody
	public RespMessage<Exception> handle(Exception e){
		if(e instanceof ServiceException){
			//TODO 
		}
		
		return new RespMessage<>(1l, e.getMessage());
	}
}

