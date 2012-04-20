<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" scope="request" value="Authorization Required"/>
<!DOCTYPE html>
<html lang="en">
	<head>
	<jsp:include page="common-head.jsp" />
	</head>
<body>
	<div id="container">
		<jsp:include page="top-navigation.jsp" />
	
		<div style="float:none; clear:both;">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>
			<div id="content">
			
			<div class="ui-widget">
				<div class="ui-state-highlight ui-corner-all" style="margin-top: 20px; padding: 0 .7em;"> 
					<p>
					<span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
					<strong>You do not have authorization to perform that operation.</strong>
					</p>
				</div>
			</div>
			</div>		
		</div>
	</div>
</body>
</html>