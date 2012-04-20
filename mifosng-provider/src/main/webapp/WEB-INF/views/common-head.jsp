<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<title>${applicationName} ${pageTitle}</title>
<c:url value="/resources/favicon.ico" var="favicon" />
<link rel="SHORTCUT ICON" href="${favicon}"/>
<c:url value="/resources/allinone.css" var="allinoneCssUrl" />
<link rel="stylesheet" type="text/css" href="${allinoneCssUrl}">
<c:url value="/resources/jquery-ui-1.8.16/redmond/jquery-ui-1.8.16.custom.css" var="jQueryUiCssUrl" />
<link rel="stylesheet" type="text/css" href="${jQueryUiCssUrl}">
<c:url value="/resources/jquery-1.6.3.min.js" var="jqueryUrl" />
<script type="text/javascript" src="${jqueryUrl}"></script>
<c:url value="/resources/jquery-ui-1.8.16/jquery-ui-1.8.16.custom.min.js" var="jQueryUiUrl" />
<script type="text/javascript" src="${jQueryUiUrl}"></script>