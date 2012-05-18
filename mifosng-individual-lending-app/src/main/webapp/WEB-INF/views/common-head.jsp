<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<title><spring:message code="app.name"/>&nbsp;${pageTitle}</title>

<spring:theme code="allinone" var="themeName"/>
<spring:theme code="tabletools" var="tableTools"/>
<spring:theme code="datatable.demopage" var="demoPage"/>
<spring:theme code="datatable.demotable" var="demoTable"/>
<spring:theme code="jqueryui" var="jQueryUI"/>

<c:url value="/resources" var="resValue" />

<c:set var="language" scope="session">${sessionScope['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE']}</c:set>

<link rel="SHORTCUT ICON" href="${resValue}/favicon.ico" />
<link rel="stylesheet" type="text/css" href="${resValue}<spring:theme code="allinone"/>" />
<link rel="stylesheet" type="text/css" href="${resValue}<spring:theme code="tabletools"/>" />
<link rel="stylesheet" type="text/css" href="${resValue}<spring:theme code="datatable.demopage"/>" />
<link rel="stylesheet" type="text/css" href="${resValue}<spring:theme code="datatable.demotable"/>" />
<link rel="stylesheet" type="text/css" href="${resValue}<spring:theme code="jqueryui"/>" />
<link rel="stylesheet" type="text/css" href="${resValue}<spring:theme code="excelcss"/>" />

<script type="text/javascript" src="${resValue}/libs/jquery-1.7.min.js"></script>
<script type="text/javascript" src="${resValue}/libs/jquery-ui-1.8.16.custom/js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="${resValue}/libs/DataTables-1.8.2/media/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="${resValue}/libs/jquery.dataTables.TitleNumeric.js"></script>
<script type="text/javascript" src="${resValue}/libs/DataTables-1.8.2/extras/TableTools/media/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="${resValue}/libs/DataTables-1.8.2/extras/TableTools/media/js/TableTools.min.js"></script>
<script type="text/javascript" src="${resValue}/libs/jquery.i18n.properties-min-1.0.9.js"></script>
<script type="text/javascript" src="${resValue}/libs/jsrender.js"></script>
<script type="text/javascript" src="${resValue}/libs/jquery.views.js"></script>
<script type="text/javascript" src="${resValue}/libs/jquery.observable.min.js"></script>
<script type="text/javascript" src="${resValue}/libs/globalize/globalize.js"></script>
<script type="text/javascript" src="${resValue}/stretchydata-0.947.js"></script>

<c:choose>
  <c:when test="${not empty language}">
  	<script type="text/javascript" src="${resValue}/libs/globalize/cultures/globalize.culture.${fn:replace(language, '_', '-')}.js"></script>
    <script type="text/javascript" src="${resValue}/libs/jquery-ui-1.8.16.custom/js/i18n/jquery.ui.datepicker-${fn:replace(language, '_', '-')}.js"></script>
    <script>
    $(document).ready(function() {
    	// find language parts
    	var language = '${language}';
    	var parts = language.split('_');
    	
    	//alert('parts: (' + parts.length + ') ' + parts[0]);
    	
    	var languagePart = parts[0];
    	var localePart = parts[0];
    	var culture = parts[0];
    	if (parts.length > 1) {
    		culture = culture + '-' + parts[1];
    		localePart = localePart + '_' + parts[1];
    	}
    	
    	if (parts.length > 2) {
    		culture = culture + '-' + parts[2];
    		localePart = localePart + '_' + parts[2];
    	}
    	
		Globalize.culture(culture); // culture format is with '-' e.g en-GB
    	
    	$.datepicker.setDefaults( $.datepicker.regional[languagePart]);
    	
    	jQuery.i18n.properties({
			name:'messages', 
			path: '${resValue}/global-translations/', 
			mode:'map',
			cache: true,
			language: localePart, // messages format is with '_' e.g en_GB
			callback: function() {
			}
		});
    });
    </script>
  </c:when>
  <c:otherwise>
    <script>
    $(document).ready(function() {
    	// default to 'en' culture - no need to import extra javascript files for globalize.js or datapicker
    	Globalize.culture('en');
    	
    	$.datepicker.setDefaults( $.datepicker.regional['en']);
    	
    	jQuery.i18n.properties({
			name:'messages', 
			path: '${resValue}/global-translations/',
			mode:'map',
			cache: true,
			language: '', // leave blank so defaults to 'en' 
			callback: function() {
			}
		});
    });
    </script>
  </c:otherwise>
</c:choose>