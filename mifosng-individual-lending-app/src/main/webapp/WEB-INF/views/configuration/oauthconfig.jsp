<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request" value="Configure OAuth Settings"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="../common-head.jsp" />
	<script type="text/javascript">  
	$().ready(function() {  
	    $('#updateSettings').button();
	});  
	</script>
</head>

<body>
<div id="container">
	<div style="float:none; clear:both;">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>
		
		<div id="content">
	
		<div id="formcontainer">
			<p>All fields are required.</p>
			
			<form:form commandName="oauthSettingsFormBean">
			<fieldset>
				<legend>OAuth Settings</legend>
				
				<spring:hasBindErrors name="oauthSettingsFormBean">
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
				
				<label for="oauthProviderUrl">Sever Url (e.g http://localhost:8080/mifosng-server/):</label>
				<form:input path="oauthProviderUrl" title="The url at which to contact application service provider." />
				
				<label for="consumerkey">Consumer Key:</label>
				<form:input path="consumerkey" title="The key that represents a consuming applications identity." />
				
				<label for="sharedSecret">Consumer Secret:</label>
				<form:input path="sharedSecret" title="The secret that is shared between the service provider and the consuming application." />
				<br/>
			</fieldset>
			
			<div id="formbuttons" style="clear: left;">
				<button id="updateSettings" type="submit" title="Update oauth settings.">Update</button>
			</div>
			</form:form>
		</div>
		
		</div>
	</div>
</div>
</body>
</html>