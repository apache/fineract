<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="org.springframework.security.oauth.consumer.filter.OAuthConsumerContextFilter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="applicationName" scope="session" value="Mifos NG: " />
<c:set var="pageTitle" scope="request" value="OAuth Error"/>
<!DOCTYPE">
<html>
<head>
	<jsp:include page="common-head.jsp" />
</head>
<body>
<div id="container">

    <ul id="mainlinks">
        <li><a href="<c:url value="/home"/>">Application Home</a></li>
        <li><a href="<c:url value="/"/>">Demo Landing Page</a></li>
    </ul>

    <div id="content">
        <c:if test="${!empty sessionScope.OAUTH_FAILURE_KEY}">
            <h1>OAuth Error</h1>

			<div class="ui-widget">
				<div class="ui-state-error ui-corner-all">
					<span class="ui-icon ui-icon-alert" style="float: left; margin-right: 5px;" ></span>
					<span>It appears that the OAuth mechanism failed:</span>
					<div style="margin-left: 5px;"><%= ((Exception) session.getAttribute(OAuthConsumerContextFilter.OAUTH_FAILURE_KEY)).getMessage() %></div>
				</div>
			</div>
			
			<br/>
			<c:url value="/oauth/configuration/edit" var="editOAuthDetailsUrl" />
			<p>You may need to update the OAuth consumer details this application uses <a href="${editOAuthDetailsUrl}">here</a>.</p>
			<br/>		
            <code>
                <%
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);

                    ((Exception) session.getAttribute(OAuthConsumerContextFilter.OAUTH_FAILURE_KEY)).printStackTrace(pw);
					out.print(sw);
				    sw.close();
				    pw.close();
                %>
            </code>
        </c:if>
        <c:remove scope="session" var="OAUTH_FAILURE_KEY"/>
    </div>
</div>
</body>
</html>