<%@ page session="true" %>
<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="applicationName" scope="session" value="Mifos - Individual Lending: " />
<c:set var="pageTitle" scope="session" value="Login"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="common-head.jsp" />
	<script>
	$(document).ready(function() {
		$('#login').button();
	});
	</script>
</head>
<body>

<div id="container">	
	<div id="content">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>
	</div>
</div>
</body>
</html>


		<!-- 
		<h1>Mifos - Individual Lending: Login</h1>
		<div id="formcontainer">
		
		<c:if test="${!empty sessionScope.SPRING_SECURITY_LAST_EXCEPTION}">
		<div class="ui-widget">
			<div class="ui-state-error ui-corner-all">
				<span class="ui-icon ui-icon-alert" style="float: left; margin-right: 5px;" ></span>
				<span>You have the following errors:</span>
				<div style="margin-left: 5px;">
				<p>Your login attempt was not successful. (<%= ((AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).getMessage() %>)</p>
				</div>
			</div>
		</div>
	    </c:if>
	    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>
		
		<c:url value="/j_spring_security_check" var="loginFilterUrl" />
		<c:url value="${successUrl}" var="authSuccessUrl" />
		
		<form method="post" action="${loginFilterUrl}">
			<fieldset>
				<legend>Login</legend>
				<label for="username">Username:</label>
				<input type="hidden" name="successUrl" value="${successUrl}" />
				
				<c:choose>
					<c:when test="${param.oauth_token == null}">
						<input id="testone" type="hidden" name="oauth_token" value="${requestScope.oauth_token}" />
					</c:when>
					<c:otherwise>
						<input id="testtwo" type="hidden" name="oauth_token" value="${param.oauth_token}" />
					</c:otherwise>
				</c:choose>
				
				
				<input size="75" type="text" name="j_username" id="username" title="Username for authentication." />
				<label for="password">Password:</label>
				<input size="75" type="password" name="j_password" id="password" title="Password for authentication."/>
				<div id="formbuttons">
					<button type="submit" id="login" name="login" title="Log in to application.">Login</button>
				</div>
			</fieldset>
		</form>
		 -->