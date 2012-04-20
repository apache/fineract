<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request" value="First Time Login: Change Password"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="common-head.jsp" />
	<script>
	$(document).ready(function() {
		$('#_eventId_submit').button();
	});
	</script>
</head>
<body>
	<div id="container">
		<div id="content">
			<div id="spacer" style="line-height: 25px;">&nbsp;</div>
			<div id="formcontainer">
				<p>All fields are required.</p>
				
				<form:form commandName="firstTimeLoginFormBean">
				<fieldset>
					<legend>Change username and password</legend>

					<spring:hasBindErrors name="firstTimeLoginFormBean">
					<div class="ui-widget">
						<div class="ui-state-error ui-corner-all">
							<span class="ui-icon ui-icon-alert" style="float: left; margin-right: 5px;" ></span>
							<span>You have the following errors:</span>
							<div style="margin-left: 5px;">
							<form:errors path="*" />
							</div>
						</div>
					</div>
					</spring:hasBindErrors>
					
					<label for="oldUsername">Old username:</label>
					<form:input path="oldUsername" size="75" title="The current username associated with this user account." disabled="true" />
		
					<label for="username">New username:</label>
					<form:input path="username" size="75" title="The new username associated with this user account." />
		
					<label for="password">New password:</label>
					<form:password path="password" size="75" title="The new password associated with this user account." />
					
					<form:hidden path="successView" />
				</fieldset>
				
				<div id="formbuttons" style="clear: left;">
					<button type="submit" id="_eventId_submit" name="_eventId_submit" title="Update username password details.">Submit</button>
				</div>
				</form:form>
			</div>
		</div>
	</div>
</body>
</html>