<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request"><spring:message code="page.home.title"/></c:set>
<!DOCTYPE html>
<html lang="en">
<head>
<jsp:include page="common-head.jsp" />

<script>
$(document).ready(function() {

});
</script>
</head>
<body>

<div id="container">	
	<div id="content">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>
		
		<div id="loginformholder"></div>
	</div>
</div>


<script id="formLoginTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<label for="username">Username:</label>
	<input size="75" type="text" name="j_username" id="username" title="Username for authentication." />
	
	<label for="password">Password:</label>
	<input size="75" type="password" name="j_password" id="password" title="Password for authentication."/>
</form>
</script>

</body>
</html>