<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="applicationName" scope="page" value="Mifos NG: " />
<c:set var="pageTitle" scope="page" value="Signed out"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="common-head.jsp" />
</head>

<body>
	<div id="container">

<c:url value="/resources/logo.gif" var="logoUrl" />
<div id="login-wrapper" style="width:550px; height:200px; position:absolute; left:46%; top:50%; margin:-100px 0 0 -150px;">
	<div id="logo-column" style="width: 160px; height: 90px; float: left; margin-top: -12px; margin-right: 2px; background: url(${logoUrl}) no-repeat;">
	</div>
	
	<div id="form-column" style="float: left;">
		<div id="errors">
		</div>
		<div>
		<p>You have successfully logged out. <a href="<c:url value="/home"/>">Click here to login</a></p>		
		</div>
	</div>
</div>

	</div>
</body>
</html>	