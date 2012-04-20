<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="session" value="Mifos - Individual Lending: Signup"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="../common-head.jsp" />
	<script>
	$(document).ready(function() {
		$('#openingDate').datepicker({
			constrainInput: true,
			maxDate: '0',
			dateFormat: 'yy-mm-dd'
		});
		
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
				
				<form:form modelAttribute="signupFormBean">
				<fieldset>
					<legend>Sign up</legend>

					<spring:hasBindErrors name="signupFormBean">
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
		
					<label for="organisationName">Organisation Full Name:</label>
					<form:input path="organisationName" size="75" title="The name of the organisation." />
		
					<label for="openingDate">Founded Date:</label>
					<form:input path="openingDate" size="75" title="The date the organisation began providing microfinance services." />
					
					<label for="contactEmail">Contact Email:</label>
					<form:input path="contactEmail" size="75" title="A contact email for someone in the organisation concerned with MIS." />
					
					<label for="contactName">Contact Name:</label>
					<form:input path="contactName" size="75" title="Contacts full name." />
				</fieldset>
				
				<div id="formbuttons" style="clear: left;">
					<button type="submit" id="_eventId_submit" name="_eventId_submit" title="Sign up organisation.">Sign-up</button>
				</div>
				</form:form>
			</div>
		</div>
	</div>
</body>
</html>