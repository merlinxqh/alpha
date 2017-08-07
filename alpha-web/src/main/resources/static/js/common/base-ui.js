/**
 * 
 */
var _globalParamObj={};//存储查询参数
$(function() {
	// 处理session 超时
	$.ajaxSetup({
		contentType : "application/x-www-form-urlencoded;charset=utf-8",
        cache : false,// 关闭AJAX相应的缓存
		dataType : 'json',
		complete : function(xhr, status) {
            switch(xhr.status) {
                case 404 :
                    wrapperHideModal('base_modal');
                    _errorTipsFun("此链接地址已失效，请联系管理员！");
                    break;
                case 500 :
                    wrapperHideModal('base_modal');
                    _errorTipsFun("服务器处理请求过程中出现未处理异常，请联系管理员！");
                    break;
                case 403 :
                    wrapperHideModal('base_modal');
                    _errorTipsFun("你没有操作权限！");
                    break;
                case 401 :
                    _errorTipsFun("登录信息已失效,请重新登录");
                    setTimeout(function () {
                        location.href = rootPath + '/login';
                    }, 1500);
                    break;
            }
		}
	});
	
	// navTab 方式打开页面
	$("body").delegate("*[data-target='navTab']","click",function() {
		var url = $(this).attr("href") || $(this).data("url");
		$("#navTab").load(url, initHtml);
		return false;
	});
	
	// navTab 方式打开页面
	$("body").delegate( "*[data-target='divload']", "click", function() {
		var url = $(this).attr("href") || $(this).data("url");
		var div = $(this).data("div");
		$("#"+div).data("url",url);
		$("#"+div).load(url, initHtml);
		
		//只加载菜单特殊处理
		if(div=="sidebar-menu"){
			$("#baseContainer").html("");
		}
		return false;
	});
	
	// 防止modal缓存
	$("#base_modal,#base_modal_center").on("loaded.bs.modal", function() {
		initHtml();
		// 重置校验
		resetValidator();
	});

	$("#base_modal,#base_modal_center").on("hidden.bs.modal", function() {
		// 清除modal缓存
        modalHideClearCache($(this));
		resetValidator();
	});
	
	// modal 打开完后激活 校验
	$('#base_modal,#base_modal_center').on('shown.bs.modal', function() {
		initValidator();
	});

    //回车事件
    document.onkeydown=function(e){
        var theEvent = e || window.event;
        var code = theEvent.keyCode || theEvent.which || theEvent.charCode;
        if (code == 13) {
            e.stopPropagation();
            enterKeyPressSearch();
            return false;
        }
    };
	//解决modal 弹框 select2搜索框不能输入问题
    $.fn.modal.Constructor.prototype.enforceFocus = function () { };
});

/**
 * modal 隐藏清缓存 方法
 * @param obj
 */
function modalHideClearCache(obj){
    obj.removeData("bs.modal");
    obj.find(".modal-content").empty();
}

/**
 * 隐藏modal方法
 * @param targetId
 */
function wrapperHideModal(targetId){
    $("#"+targetId).modal('hide');
    modalHideClearCache($("#"+targetId));
}

/**
 * 初始化html
 */
function initHtml() {
    //设定 panel 高度
    $(".adapt-window-height").css({
        maxHeight:($(window).height()-173)+'px'
    });

    //设定左边树高度
    if($(".tree-fixed-height").length>0){
        $(".tree-fixed-height").css({
            height: ($(window).height()-173)+'px'
        });
    }

    wrapperSetStoreSearchParam($("#paramForm"));
	// 绑定 ajaxToDo 事件
	$(".ajaxToDo").unbind().bind( "click", function() {
		var url = $(this).attr("href");
		$.Notification.confirm('success', 'top center', '操作提示！', ajaxToDo, url);
	});

    // bootstrapTable渲染
    initBootstrapTable();

    /**
	 * 设置 dataTable最小宽度 为了解决 小屏电脑 显示问题
     * @type {*}
     * @private
     */
    var _panel=$(".table-hover").parents(".adapt-window-height");
    if(!_panel.hasClass("panel-data-table")
		&& !_panel.hasClass("auth-width")){//不需要最小宽度1200px的 添加这个class
        _panel.addClass("panel-data-table");
	}
    /**
	 * 清空按钮
     */
    $(".clearSearch").unbind().bind("click",function(){

        wrapperClearFun(this);
    });
    /**
	 * 查询按钮
     */
    $(".searchBtn").unbind().bind("click",function(){

        wrapperFormSearch(this);
    });

	// 绑定 tableAjaxTodo 事件
	$(".tableAjaxTodo").unbind().bind( "click", function() {
		var url = $(this).attr("href");
		$.Notification.confirm('success', 'top center', '操作提示！',
				tableAjaxTodo, url);
	});
	
	$(".ajaxDel").unbind().bind( "click", function(event) {
		event.preventDefault();
		var url = $(this).attr("href");
		ajaxDel(url);
	});
    /**
	 * 滚动条问题 回到顶部
     */
}

function refreshTable(){
    enterKeyPressSearch();
}

/**
 * 封装通用查询方法
 * @param obj
 */
function wrapperFormSearch(obj){
    var $form = $(obj).parents("form");
    if($form.attr("searchFunction") != undefined){
        //特殊页面自己实现 查询方法
        var _searchFun=$form.attr("searchFunction");
        eval(_searchFun+'()');
        return;
    }
    wrapperStoreSearchParam($form);
    var tableId = $form.attr("tableId");
    var table = $("#"+tableId);
    var urlJson ={};
    urlJson.url=wrapperParamFormUrl($form);
    if(table.parents(".bootstrap-table").length > 0){
        table.bootstrapTable('refresh', urlJson);
    }else{
        table.bootstrapTable(urlJson);
    }
}

/**
 * 当前页面有没有 modal
 * @returns {boolean}
 */
function currentHasModal(){
    if($(".modal-body").length>0){
        return true;
    }
    return false;
}

/**
 * 判断当前 是否有 需要渲染的table
 * @param obj
 */
function judgeHasBootstrapTable(obj){
   if(obj == undefined){
       //查询全局
       if($('[data-toggle="table"]').length>0){
           return true;
       }
   }else{
       if(obj.find('[data-toggle="table"]').length>0){
           return true;
       }
   }

   return false;
}

/**
 * 回车键 查询事件
 */
function enterKeyPressSearch(){
    //当前 有 弹窗 并且 有需要渲染的 分页table
    if(currentHasModal()){
        if(judgeHasBootstrapTable($(".modal-body"))){
            if($(".modal-body").find(".searchBtn").length>0){
                $(".modal-body").find(".searchBtn").click();
            }
        }
    }else{
        //没有模态框 找查询按钮 click
        $(".searchBtn").click();
    }
}

/**
 * 初始化bootstrap table
 */
function initBootstrapTable(){
    if(judgeHasBootstrapTable()){//有需要渲染的table
        var urlJson ={};
        if(currentHasModal()){
            //如果 是弹窗 modal 只 查询 modal内的paramForm
            if($(".modal-body").find("#paramForm").length>0){
                urlJson.url=wrapperParamFormUrl($(".modal-body").find("#paramForm"));
            }else if($(".modal-body").find(".table-hover").length>0){
                urlJson.url=$(".modal-body").find(".table-hover").attr("data-url");
            }
        }else{
            urlJson.url=wrapperParamFormUrl($("#paramForm"));
        }
        $('[data-toggle="table"]').bootstrapTable(urlJson);
    }
}

/**
 * 获取form 拼接url
 * @param form
 * @returns {*}
 */
function wrapperParamFormUrl(form){
    var param = form.serialize();
    var tableId = form.attr("tableId");
    var table = $("#"+tableId);
    var baseUrl = table.attr("data-url");
    if(baseUrl.indexOf("?")==-1){
        baseUrl += "?" + param;
    }else{
        baseUrl += "&"+param;
    }
    return baseUrl;
}

/**
 * 查询form 需要tableId唯一
 * 封装存储 查询参数方法
 * @param obj 对应form对象
 */
function wrapperStoreSearchParam(obj){
   var _curParList=obj.serializeArray();
   var _tableId=obj.attr("tableId");
   var _curPar={};
   $.each(_curParList,function(){
       _curPar[this.name]=this.value;
   });
   _globalParamObj[_tableId]=_curPar;
}

/**
 * 清空 存储查询参数方法
 * @param obj
 */
function wrapperClearSearchParam(obj){
    var _tableId=$(obj).attr("tableId");
    if(_globalParamObj.hasOwnProperty(_tableId)){
    	delete _globalParamObj[_tableId];
	}
}

/**
 * 设置 存储查询参数
 * @param obj
 */
function wrapperSetStoreSearchParam(obj){
    var _tableId=obj.attr("tableId");
    if(_globalParamObj.hasOwnProperty(_tableId)){
        var _curPar=_globalParamObj[_tableId];
        obj.find("input[type!='checkbox']").each(function(){
            var _this=this;
            var _name=$(_this).attr("name");
            $(_this).val(_curPar[_name]);
        });
        obj.find("input[type='checkbox']").each(function(){
            var _this=this;
            var _name=$(_this).attr("name");
            if(_curPar.hasOwnProperty(_name) && $(_this).val() == _curPar[_name]){
                $(_this).prop("checked",true);
            }
        });
        obj.find(".select2").each(function(){
            var _this=this;
            var _name=$(_this).attr("name");
            $(_this).val(_curPar[_name]);
            $(_this).change();//设置select2 选中对应的值
        });
    }
}

/**
 * 通用 清除查询方法
 * @param table
 */
function wrapperClearFun(obj){
    var _form=$(obj).parents("form")[0];
    var tableId = $(_form).attr("tableId");
    _form.reset();
    $(_form).find(".select2").each(function(){
        var _value=$(this).find("option:eq(0)").val();
        $(this).val(_value).trigger("change");
    });
    wrapperClearSearchParam(_form);
    wrapperFormSearch(obj);
}

function initNavTabValidator() {
	// modal form 校验 及提交
	$('.navTabform').bootstrapValidator({
		excluded : ':disabled, :hidden, :not(:visible)',
		submitHandler : function(validator, form, submitButton) {
			$.ajax({
				type : form.method || 'POST',
				url : form.attr("action"),
				data : form.serializeArray(),
				dataType : "json",
				cache : false,
				success : modalAjaxDone,
				error : ajaxError
			});
			this.disableSubmitButtons(true);
		}
	});
	// 带上传文件的form 校验
	$('.iframenavTabform').bootstrapValidator();
}


function initValidator() {
	//TODO
}



function reloadDiv(callback){
	var url = $("#baseContainer").data("url");
	$("#baseContainer").load(url, function(){
	    if(callback){
	        callback();
	    }
	    initHtml();
	});
}

function reloadDivTip(data,callback){
	modalAjaxDone(data);
	var url = $("#baseContainer").data("url");
	$("#baseContainer").load(url, function(){
	    if(callback){
	        callback();
	    }
	    initHtml();
	});
}

function resetValidator() {
	if ($('.bvform').data('bootstrapValidator')) {
		$('.bvform').data('bootstrapValidator').resetForm();
	}
	if ($('.iframeform').data('bootstrapValidator')) {
		$('.iframeform').data('bootstrapValidator').resetForm();
	}
}

/**
 * 分页组件
 * @param targetDiv 目标元素
 * @param targetElementId 分页组件渲染标签id
 * @param pageSize  每页数量
 * @param total     总条数
 * @param curPage   当前页
 * @returns
 */
function initPagination(targetDiv,targetElementId,pageSize,total,curPage) {
	//总条数 等于0 不执行分页
	if(isNaN(total) || parseInt(total)==0){
		return;
	}
	var element = targetDiv.find('#'+targetElementId);
	var totalPages = Math.ceil(total/pageSize)
	var url = targetDiv.attr("url");
	var numberOfPages = totalPages>10?10:totalPages;
	var options = {
		bootstrapMajorVersion: 3,
		currentPage: curPage,
		numberOfPages: numberOfPages,
		totalPages: totalPages,
		itemTexts: function(type, page, current) {
			switch(type) {
				case "first":
					return "首页";
				case "prev":
					return "上一页";
				case "next":
					return "下一页";
				case "last":
					return "尾页";
				case "page":
					return page;
			}
		},
		useBootstrapTooltip:true,
		pageUrl: function(type, page, current) {
			return "javascript:void(0);";
		},
		tooltipTitles: function(type, page, current) {
			return "第"+page+"页";
		},
		onPageClicked:function(event, originalEvent,type,page){
			var param={size:pageSize,page:page};
			//获取 查询参数
			var _searchParamFun=targetDiv.attr("searchParamFun");
			if(_searchParamFun){
				var _resParam=eval(_searchParamFun+"()");
				param=$.extend(param,_resParam);
			}
			var ajaxRes=ajaxBsPaginatorData(url,param);
			ajaxRes.then(function(){
				 var resData=ajaxRes.pageData;//分页返回数据
				//渲染数据方法
				  var renderPageFun=targetDiv.attr("renderPageFun");
				  if(renderPageFun){
					  eval(renderPageFun+'(resData)');
				  }
			});
		}
	};
	element.bootstrapPaginator(options);
}

/**
 * 异步加载分页数据
 */
function ajaxBsPaginatorData(url, param){
	var dfd=$.Deferred();
	$.ajax({
		  url:url,
		  type:"POST",
		  data: param,
		  cache : false,
		  success: function(data){
			  dfd.pageData=data;
			  dfd.resolve();
		  }
		});
	return dfd;
}


/**
 * 打开点击阴影层&esc键 不自动关闭的模态框
 */
function manualModal(url,divId){
	$("#"+divId).modal({
		backdrop: 'static', //点击空白处 不关闭
		keyboard: false, //点esc不关闭
		remote: url
	});
}

/**
 * 跳转到列表页
 * @param url
 */
function returnToListPage(url){
    $("#baseContainer").load(url, initHtml);
}

/**
 * 校验标签对象 必填 并且提示信息
 * @param tagObj jquery 对象
 * @param dataObj 如果 验证通过 将 值赋到对应obj
 */
function checkTagAndTips(tagObj,dataObj){
    var _flag=true;
    if(tagObj.val() == ''){
        _flag=false;
        var tipMsg='请完善信息';
        if(tagObj.attr("data-placeholder") != undefined){
            tipMsg=tagObj.attr("data-placeholder");
		}else if(tagObj.attr("placeholder") != undefined){
            tipMsg=tagObj.attr("placeholder");
		}
        _errorTipsFun(tipMsg);
        tagObj.focus();
    }
    dataObj[tagObj.attr("id")]=tagObj.val();
    return _flag;
}


/**
 * 渲染图片通用方法
 * @param value
 * @param row
 * @param index
 */
function commonRenderImg(value, row, index){
    if(value != '' && value != undefined){
        return '<img src="'+imgPath+'/'+value+'" style="width: 60px;height: 40px;" />';
    }
    return '';
}

var _color_reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/;
/*16进制颜色转为RGB格式*/
String.prototype.colorRgb = function(){
    var sColor = this.toLowerCase();
    if(sColor && _color_reg.test(sColor)){
        if(sColor.length === 4){
            var sColorNew = "#";
            for(var i=1; i<4; i+=1){
                sColorNew += sColor.slice(i,i+1).concat(sColor.slice(i,i+1));
            }
            sColor = sColorNew;
        }
        //处理六位的颜色值
        var sColorChange = [];
        for(var i=1; i<7; i+=2){
            sColorChange.push(parseInt("0x"+sColor.slice(i,i+2)));
        }
        return "RGB(" + sColorChange.join(",") + ")";
    }else{
        return "error_color_val";
    }};

/*RGB颜色转换为16进制*/
String.prototype.colorHex = function(){
    var that = this;
    if(/^(rgb|RGB)/.test(that)){
        var aColor = that.replace(/(?:\(|\)|rgb|RGB)*/g,"").split(",");
        var strHex = "#";
        for(var i=0; i<aColor.length; i++){
            var hex = Number(aColor[i]).toString(16);
            if(hex === "0"){
                hex += hex;
            }
            strHex += hex;
        }
        if(strHex.length !== 7){
            strHex = that;
        }
        return strHex;
    }else if(_color_reg.test(that)){
        var aNum = that.replace(/#/,"").split("");
        if(aNum.length === 6){
            return that;
        }else if(aNum.length === 3){
            var numHex = "#";
            for(var i=0; i<aNum.length; i+=1){
                numHex += (aNum[i]+aNum[i]);
            }
            return numHex;
        }
    }else{
        return that;
    }
};


/**
 * 状态渲染方法
 * 1: 有效 0:失效
 * @param value
 * @param row
 * @param index
 */
function statusRender(value, row ,index){
   if(value == 1){
   	return '有效';
   }
   return '无效';
}

/**
 * 上线 下线
 * @param value
 * @param row
 * @param index
 */
function statusRenderOnline(value, row, index){
	if(value == 1){
		return '已上线';
	}
	return '已下线';
}

/**
 * 自动换行
 * @param value
 */
function autoWrapRender(value){
	if(value != undefined){
        return '<div style="max-width: 153px;word-wrap: break-word;word-break:break-all;">'+value+'</div>';
    }else{
		return '';
	}
}


/**
 * 限制输入两位小数点 正数
 * @param obj
 */
function limitTwoPoint(obj){
    obj.value = obj.value.replace(/[^\d.]/g,"");  //清除“数字”和“.”以外的字符
    obj.value = obj.value.replace(/\.{2,}/g,"."); //只保留第一个. 清除多余的
    obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$",".");
    obj.value = obj.value.replace(/^(\-)*(\d+)\.(\d\d).*$/,'$1$2.$3');//只能输入两个小数
    if(obj.value.indexOf(".")< 0 && obj.value !=""){//以上已经过滤，此处控制的是如果没有小数点，首位不能为类似于 01、02的金额
        obj.value= parseFloat(obj.value);
    }
}

/**
 * 限制输入数字
 * @param obj
 */
function limitInputNumber(obj){
    obj.value = obj.value.replace(/[^\d]/g,"");  //清除“数字”以外的字符
    if(obj.value.indexOf(".")< 0 && obj.value !=""){//以上已经过滤，此处控制的是如果没有小数点，首位不能为类似于 01、02的金额
        obj.value= parseFloat(obj.value);
    }
}


/**
 * 处理 名称过长
 * @param fileName
 * @param lenFlag 需要保留的长度 默认 10
 * @returns {*}
 */
function handleLongFileName(fileName,lenFlag){
    if(lenFlag == undefined){
        lenFlag=10;
    }
    if(fileName.length > lenFlag){
        return fileName.substr(fileName.length-lenFlag,fileName.length);
    }
    return fileName;
}


/**
 * 颜色 组件input绑定事件
 * @param obj
 */
function colorInputBindingEvent(obj){
    obj.keydown(function(e) {
        if (e.keyCode == 13) {
            commonRenderColorPlugin(obj);
        }
    });
    obj.focusout(function(){
        commonRenderColorPlugin(obj);
    });
}


/**
 * 封装颜色选择组件
 * 根据颜色值显示颜色 方法
 */
function commonRenderColorPlugin(_input){
	if(_input.val() == ''){
		//没有输入值 不操作
		return;
	}
	//颜色值加 "#" 只有七位
	if(_input.val().length > 7){
		_input.val(_input.val().substr(0,7));
	}
    var rgbVal=_input.val().colorRgb();
    if(rgbVal == 'error_color_val'){
    	console.log(_input.val()+"颜色值无效...");
        _input.val("");
    	return;
	}
    var _parentDiv=_input.parents(".col-xs-11");
    _parentDiv.find("i").css({
        backgroundColor: rgbVal
    });
    _parentDiv.find(".colorpicker-default").attr("data-color",rgbVal);
}


/**
 * 错误消息提示
 * @param msg
 */
function _errorTipsFun(msg,delayTime){
    if(delayTime == undefined){
        delayTime=1500;//默认1.5秒
    }
    //解决 多个提示同时弹出
    if($(".notifyjs-wrapper").length == 0){
        $.Notification.autoHideNotify('error', 'top center', '消息提示！',msg, delayTime);
    }
}

/**
 * 警告消息提示
 * @param msg
 */
function _warnTipsFun(msg, delayTime){
    if(delayTime == undefined){
        delayTime=1500;//默认1.5秒
    }
    if($(".notifyjs-wrapper").length == 0) {
        $.Notification.autoHideNotify('warning', 'top center', '消息提示！', msg, delayTime);
    }
}

/**
 * 提示信息
 * @param msg
 * @private
 */
function _infoTipsFun(msg,delayTime){
    if(delayTime == undefined){
        delayTime=1500;//默认1.5秒
    }
    if($(".notifyjs-wrapper").length == 0){
        $.Notification.autoHideNotify('info', 'top center', '消息提示！',msg, delayTime);
    }
}

/**
 * 成功消息提示
 * @param msg
 */
function _successTipsFun(msg,delayTime){
    if(delayTime == undefined){
        delayTime=1500;//默认1.5秒
    }
    if($(".notifyjs-wrapper").length == 0) {
        $.Notification.autoHideNotify('success', 'top center', '消息提示！', msg, delayTime);
    }
}

/**
 * 此方法使用场景:
 *     从列表页跳转至详情, 从导航栏 点返回列表页按钮 保持原列表页状态
 *     做法是在 baseContainer div下加载两个div 来回切换
 * @param url  需要跳转的链接
 * @param infoId  详情页div id
 * @param centerId  列表页div id
 */
function showInfoHideCenter(url,infoId,centerId){
    //在baseContainer内初始化两个div
    if($("#"+centerId).length == 0){
        var _cloneHtml=$("#baseContainer").clone();
        _cloneHtml.removeAttr("id").attr("id",centerId).hide();
        $("#baseContainer").html("");
        $("#baseContainer").append(_cloneHtml.prop("outerHTML"));
        $("#baseContainer").append("<div class=\"container\" id=\""+infoId+"\"></div>");
    }
    $("#"+centerId).fadeOut(function(){
        $("#"+infoId).fadeIn();
    });
    $("#"+infoId).load(url, initHtml);
}

/**
 * 返回 列表页方法
 * @param infoId  详情页ID
 * @param centerId  列表页ID
 */
function returnToCenter(infoId,centerId){
    $("#"+infoId).fadeOut(function(){
        $("#"+centerId).fadeIn();
    });
}

/**
 * 时间格式化
 * @param value
 * @returns {String}
 */
function defaultDateFormatter(value) {
    if(value == undefined){//没有值 不显示
        return '';
    }
    var d = new Date(value);

    var month;
    var date;
    var hours;
    var minutes;
    var seconds;

    if (d.getMonth() < 10 ) {
        month = "0" + (d.getMonth()+1);
    } else
        month = d.getMonth()+1;

    if (d.getDate() < 10 ) {
        date = "0" + d.getDate();
    } else
        date = d.getDate();

    if (d.getHours() < 10 ) {
        hours = "0" + d.getHours();
    } else
        hours = d.getHours();

    if (d.getMinutes() < 10 ) {
        minutes = "0" + d.getMinutes();
    } else
        minutes = d.getMinutes();

    if (d.getSeconds() < 10 ) {
        seconds = "0" + d.getSeconds();
    } else
        seconds = d.getSeconds();


    var formatdate= d.getFullYear()+"-"+(month)+"-"+date+" "+hours+":"+minutes+":"+seconds;
    return formatdate;
}