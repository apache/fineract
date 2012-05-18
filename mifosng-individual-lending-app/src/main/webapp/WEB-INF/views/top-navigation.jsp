<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url value="/home" var="homeUrl" />
<c:url value="/org/admin" var="orgAdminUrl" />
<c:url value="/org/admin/user" var="orgUserAdminUrl" />
<c:url value="/reporting/flexireport" var="reportingUrl" />
<c:url value="/resources/mifos.jpg" var="logoUrl" />

<div id="logowrapper">
	<span style="float: left">
		<img style="float:left; border: 0;" alt="" src="${logoUrl}"/>
	</span>
</div>
<div id="navwrapper">
	<ul id="nav" class="floatleft">
		<li><a href="${homeUrl}"><spring:message code="link.topnav.clients"/></a></li>
		<sec:authorize access="hasAnyRole('USER_ADMINISTRATION_SUPER_USER_ROLE')">
		<li><a href="${orgUserAdminUrl}"><spring:message code="link.topnav.users"/></a></li>
		</sec:authorize>
		<sec:authorize access="hasAnyRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')">
		<li><a href="${orgAdminUrl}"><spring:message code="link.topnav.organisation"/></a></li>
		</sec:authorize>
		<sec:authorize access="hasAnyRole('REPORTING_SUPER_USER_ROLE')">
        <li><a href="${reportingUrl}"><spring:message code="link.reports"/></a></li>
		</sec:authorize>
	</ul>
	<ul id="nav" class="floatright">
		<li class="dmenu"><a href="#"><spring:message code="link.topnav.theme"/></a>
			<ul>
				<li><a href="?theme=default">smoothness</a></li>
				<li><a href="?theme=ui-lightness">lightness</a></li>
				<li><a href="?theme=redmond">redmond</a></li>
			</ul>
		</li>
		<li class="dmenu"><a href="#"><spring:message code="link.topnav.culture"/></a>
			<ul>
				<li><a href="?lang=en">en</a></li>
				<li><a href="?lang=fr">fr</a></li>
				<li><a href="?lang=es">es</a></li>
				<li><a href="?lang=pt">pt</a></li>
				<li><a href="?lang=zh">zh</a></li>
			</ul>
		</li>
		<sec:authentication property="principal.username" var="username"/>
		<c:url value="/org/admin/settings" var="accountsettingsUrl"/>
		<li><a href="${accountsettingsUrl}" class="dmenu">${username}</a>
			<ul>
				<li><a href="${accountsettingsUrl}"><spring:message code="link.topnav.account.settings"/></a></li>
			</ul>
		</li>
		<c:url value="/signout" var="signOutUrl"/>
		<li><a href="${signOutUrl}"><spring:message code="link.signout"/></a></li>
	</ul>

	<br class="clear">
</div>