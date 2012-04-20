<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="applicationName" scope="page" value="Mifos NG: " />
<c:set var="pageTitle" scope="page" value="Session Timeout"/>
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
		<div id="formcontainer">
		<form method="post" action="j_spring_security_check">
			<fieldset>
				<legend>Login (Session Timeout)</legend>
				<label for="username">Username:</label>
				<input size="75" type="text" name="j_username" id="username" title="Username for authentication." />
				<label for="password">Password:</label>
				<input size="75" type="password" name="j_password" id="password" title="Password for authentication."/>
				<div id="formbuttons">
					<button type="submit" id="login" name="login" title="Log in to application.">Login</button>
				</div>
			</fieldset>
		</form>
		</div>
	</div>
</div>
</body>
</html>