
//带校验的form表单 提交
function validateCallback(form, callback, thenCall) {
    disableSubmitBtn();
	var $form = $(form);
	var validator = $form.data('bs.validator');
	validator.validate();
    if (validator.isIncomplete() || validator.hasErrors()) {
        ableSubmitBtn();
        return false;
    }
    $.ajax({
        type : form.method || 'POST',
        url : $form.attr("action"),
        data : $form.serializeArray(),
        cache : false,
        dataType:'json',
        success : function(response){
            (callback || ajaxDone)(response,thenCall);
        },
        error : ajaxError
    });
    return false;
}

/**
 * 防止重复点击 提交按钮
 */
function disableSubmitBtn(){
    $(".modal-footer").find("button[type='submit']").prop("disabled",true);
}

function ableSubmitBtn(){
    $(".modal-footer").find("button[type='submit']").prop("disabled",false);
}

//关闭modal窗口 刷新父页面，或指定div 或根据回调函数刷新
function modalAjaxDone(json){
	if(json.hasOwnProperty('returnValue') && json.returnValue == 0){
		//关闭mode 
		wrapperHideModal('base_modal');

		//提示成功消息
		_successTipsFun(json.errorMessage);
		if(json.hasOwnProperty('callbackMethod')){//有传回调函数
            eval(json['callbackMethod']+'()');//执行回调函数
		}else{//默认刷新列表
            refreshTable();
        }
	}else{
		_errorTipsFun(json.data);
	}
}

function ajaxError(json){
	if(json.status==200){
		if(json.message){
            _errorTipsFun(json.data);
		}
	}
    ableSubmitBtn();
}

function ajaxToDo(url,callback){
    $.ajax({
        type : 'POST',
        url : url,
        cache : false,
        dataType : 'json',
        success : function(response){
            ajaxDone(response,callback);
        },
        error : ajaxError
    });
}

function ajaxToGet(url,data,callback){
    $.ajax({
        type : 'GET',
        url : url,
        data : data,
        cache : false,
        dataType : 'json',
        success : function(response){
            ajaxDone(response,callback);
        },
        error : ajaxError
    });
}

function ajaxToPost(url,data,callback){
    $.ajax({
        type : 'POST',
        url : url,
        data : data,
        cache : false,
        dataType : 'json',
        success : function(response){
            ajaxDone(response,callback);
        },
        error : ajaxError
    });
}

// 刷新父页面，或指定div 或根据回调函数刷新
function ajaxDone(json,callback){
    if(!json) return ;
	if(json.hasOwnProperty('returnValue') && json.returnValue == 0){
		_successTipsFun(json.errorMessage);
		if(callback != undefined){
			eval(callback+'()');
		}else if(json.hasOwnProperty('callbackMethod')){//有传回调函数
            eval(json['callbackMethod']+'()');//执行回调函数
        }else{//默认刷新列表
            refreshTable();
        }
	}else{
		//提示失败消息
		_errorTipsFun(json.data);
	}
}

function tableAjaxTodo(url,callback){
	 var ids = $.map($('[data-toggle="table"]').bootstrapTable('getSelections'), function (row) {
        return row.id;
    });
	 
	 if(ids==''){
	 	_errorTipsFun('请选择一条记录!');
	 }else{
		  url=url+"&ids="+ids;
			$.ajax({
				type:'POST',
				url:url,
				dataType:"json",
				cache: false,
				success:function(response){
				    ajaxDone(response,callback);
				},
				error: ajaxError
			});
	 }
}

function confirmThenPost(url,data,callback){
    $(".notifyjs-wrapper").remove();
    $.Notification.confirm('warning','top center', '操作提示！', function(url){
        ajaxToPost(url,data,callback);
    }, url);
}

function ajaxDel(url,callback){
	confirmThenPost(url,null,callback);
}

/**
 * 获取上传文件参数
 */
function ajaxUploadParam(purpose){
	var dfd=$.Deferred();
	$.ajax({
		type:'GET',
		url:rootPath+'/api/getUploadParams?purpose='+purpose,
		dataType:"json",
		cache: false,
		success:function(response){
		    dfd.resData=response;
			dfd.resolve();
		},
		error: ajaxError
	});
	return dfd;
}

//搜索
function searchClick(form){
	var $form = $(form);
	var bspaginationElement =$("#bspagination");
	var relaodDiv = bspaginationElement.attr("relaodDiv");
	var url =  bspaginationElement.attr("url");
	$.ajax({
		  url:url,
		  type:"POST",
		  data:$form.serializeArray(),
		  cache : false,
          dataType:"json",
		  success: function(data){
			  var pageData = $(data).filter("#pageData").html()||$(data).find("#pageData").html();
			  $("#"+relaodDiv).html(pageData);
			  initHtml();
		  }
		});
	return false;
}

function relaodPageData(){
	var bspaginationElement =$("#bspagination");
	var relaodDiv = bspaginationElement.attr("relaodDiv");
	var url =  bspaginationElement.attr("url");
	var pageSize =  bspaginationElement.attr("pageSize");
	var pageNo =  bspaginationElement.attr("pageNo");
	var keyword =$("#searchKeyword").val();
	var data = '{"pageSize":"'+pageSize+'","pageNo":"'+pageNo+'","keyword":"'+keyword+'"}';
	var jsonData = $.parseJSON(data);
	$.ajax({
		  url:url,
		  type:"POST",
		  data:jsonData,
		  cache : false,
		  success: function(data){
				 var pageData = $(data).filter("#pageData").html();
				  $("#"+relaodDiv).html(pageData);
				  initHtml();
		  }
		});
	return false;
}


