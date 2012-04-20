<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:url value="/index" var="indexUrl" />
<c:url value="/tenant/all" var="allTenants" />
<c:url value="/tenant/signup" var="signupTenant" />
<c:url value="/j_spring_security_logout" var="signOutUrl"/>

<ul>
	<li><a href="${indexUrl}" title="">Home</a></li>
	<li><a href="${allTenants}" title="">Show All</a></li>
	<li><a href="${signupTenant}" title="">Signup</a></li>
	<li><a href="${signOutUrl}" title="">Signout</a></li>
</ul>
