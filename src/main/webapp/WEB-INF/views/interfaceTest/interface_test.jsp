<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>接口测试</title>
	<script type="text/javascript" src="${ctx}/static/scripts/jquery/jquery.min.js"></script>
	<script type="text/javascript" src="${ctx}/static/scripts/jquery/jquery.ui.core.js"></script>
	<c:set var="ctx" value="${pageContext.request.contextPath}" />
	<script>
		function showParamInput(){
			var javaClassName = $("#javaClassName").val();
			var javaMethodName = $("#javaMethodName").val();
			if(javaClassName == ""){
				alert("请选择javaClassName");
				return
			}
			if(javaMethodName == ""){
				alert("请选择javaMethodName");
				return
			}
			var methodType = $("#javaMethodName").val();
			if((methodType.indexOf("get") == -1) && (methodType.indexOf("find")  == -1)){
				$("#showParamList").html("非查询方法，ppe,prod环境请勿使用");
			}
			$.ajax({
				url: '/interfaceTest/getParamList',
				type: 'post',
				dataType : "json",
				data:{
					"javaClassName":javaClassName,
					"javaMethodName":javaMethodName
				},
				error : function(data) {
					alert("内部错误！"+data);
				},
				success: function(data){
					$("#paramInput").html(data);
					/*if(javaMethodName.indexOf("find") >= 0 || javaMethodName.indexOf("get") >= 0){
					 $("#paramInput").html(data.ms);
					 }else{
					 $("#paramInput").html(data.ms).append("</br> style="+"color:red"+"adasdadasd");
					 }*/
				}
			});
		}

		function showMethodList(){
			var javaClassName = $("#javaClassName").val();
			if(javaClassName == ""){
				alert("请选择javaClassName");
				return
			}
			$.ajax({
				url: '/interfaceTest/getMethodList',
				type: 'post',
				dataType : "json",
				data:{
					"javaClassName":javaClassName
				},
				error : function(data) {
					alert("内部错误！"+data);
				},
				success: function(data){
					$("#javaMethodName").html(data);
				}
			});
		}
	</script>
</head>
<body>
<br>
<div style="color:red;text-align:center"><strong>请确认当前环境非PPE、PROD环境，避免造成事故</strong></div>
<br/>
<form id="interfaceInvokeInfo">
	<table>
		<tr>
			<td width="300"></td>
			<td>

				<select name="javaClassName" id="javaClassName"
						onchange="showMethodList();">
					<%
						List<String> classNameList = (List<String>) request.getAttribute("classNameList");
						for (String className : classNameList) {
					%>
					<option value="<%=className%>"><%=className%></option>
					<%
						}
					%>
				</select>
			</td>
		</tr>

	</table>
	<div style="height:5px"></div>
	<table>
		<tr>
			<td width="300"></td>
			<td>
				<select name="javaMethodName" id="javaMethodName"
						onchange="showParamInput();">
					<%
						List<String> firstClassMethodList = (List<String>) request.getAttribute("firstClassMethodList");
						for (String classMethodName : firstClassMethodList) {
					%>
					<option value="<%=classMethodName%>"><%=classMethodName%></option>
					<%
						}
					%>
				</select>
			</td>
			<br />
			<td><input style="color:red" id="showParamList" type="button" name="clic" value="显示并输入参数" onclick="showParamInput();" /></td>

		</tr>
	</table>

	<div id="paramInputDiv">
		<table>
			<tr><br />
				<td width="260"></td>
				<td><table id="paramInput" style="align: center; width: 800px"></table></td>
			</tr>
		</table>
	</div>
	<table>
		<tr>
			<td width="200"></td>
			<div id="radioSelect" style="text-align:center">
				<br />
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				返回结果展示方式:
				<input type="radio" name="showType" value="0" checked/> json格式
				<input type="radio" name="showType" value="1" /> 竖着排
				<input type="radio" name="showType" value="2" /> 横着排
				<br />
				<br />
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<input id ="excuteMethodSubmit" type="button" value="执&nbsp;&nbsp;&nbsp;行" style="width:150px; height:25px;"/>
			</div>
			</td>
		</tr>
	</table>
</form>
<div id="testInterfaceResultDiv">
	<table>
		<tr>
			<td width="260"></td>
			<td><table id="testInterfaceResult" class="table table-bordered padding"></table></td>
		</tr>
	</table>
</div>
<script>
	$(function() {
		$("#excuteMethodSubmit").click(function(e) {
//			alert("请确认当前环境非PPE、PROD环境，避免造成事故");
			if(!confirm("确认当前环境非PPE、PROD环境，避免造成事故,继续执行吗？")){
				return;
			}
			var showTypeSelected = $('#radioSelect input[name="showType"]:checked ').val();
			if(0 == showTypeSelected){
				$.ajax({
					url: '/interfaceTest/invokeInterface',
					type: 'post',
					dataType : "json",
					data : $("#interfaceInvokeInfo").serialize(),
					success: function(data){
						$("#testInterfaceResult").html(JSON.stringify(data));
					},
					error : function(data) {
						alert("内部错误！"+data);
					}
				});
			}else{
				$.ajax({
					url: '/interfaceTest/invokeInterface',
					type: 'post',
					dataType : "json",
					data : $("#interfaceInvokeInfo").serialize(),
					success: function(data){
						$("#testInterfaceResult").html(data);
					},
					error : function(data) {
						alert("内部错误！"+data);
					}
				});
			}
		});
	});
</script>
</body>
</html>