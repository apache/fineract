<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" scope="request" value="Signup Success"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="../common-head.jsp" />
</head>

<body>
<div id="container">
	<div id="content">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>	
		<div class="ui-widget">
			<div class="ui-corner-all">
				<span class="ui-icon ui-icon-circle-check" style="float: left; margin-right: 5px;" ></span>
				<span>You successfully registered your organisation <b>${signupFormBean.organisationName}</b> on a <b>mifosng community platform</b>.</span>
				<br/>
				<br/>
				<p><b>Contact Name:</b> ${signupFormBean.contactName}</p>
				<p><b>Contact Email:</b> ${signupFormBean.contactEmail}</p>
				<br/>
				<p><b>What happens next?</b></p>
				<br/>
				<p>An email with username and password details is sent to the inbox of <b>${signupFormBean.contactEmail}</b>. When logging in for the first time, you will be asked to change the username and password associated 
				with the account.
				</p>
				<br/>
				<p><b>Where do I login?</b></p>
				<br/>
				<p>
				You have registered your organisation with the platform. The only capabilities exposed through the platforms user interface are that around <b>signup, login</b> and <b>change password</b>. 
				All other capabilities are exposed through a HTTP API (documentation to follow) which is secured using <a href="http://tools.ietf.org/html/rfc5849#page-8" target="_blank">OAuth 1.0 protocol</a>.
				</p>
				<br/>
				<p>
				This means that <b>client (or consumer in OAuth terminology) applications</b> can be developed which make use of <b>backend (or provider in OAuth terminology)</b> capabilities.
				</p>
				<br/>
				<p>Below is a list of client applications that are registered on this platform at present which you can use:</p>
				<br/>
				<table>
					<thead>
						<tr>
							<th>Application Name</th>
							<th>Application Description</th>
							<th>Developed By</th>
							<th>Location</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>MifosNG: Individual Lending</td>
							<td>Provides capabilities specific to the individual lending credit methodology. Specifically capture of new customer information, loan appraisal, cashflow analysis, loan disbursement, loan monitoring etc</td>
							<td><a href="http://www.mifos.org" target="_blank">Mifos Community</a></td>
							<td><a href="http://localhost:8080/mifosng-individual-lending-app/">http://localhost:8080/mifosng-individual-lending-app/</a></td>
						</tr>
					</tbody>
				</table>			
			</div>
		</div>
	</div>
</div>
</body>
</html>