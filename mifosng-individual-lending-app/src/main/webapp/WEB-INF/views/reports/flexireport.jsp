<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request" value="Reports: Individual Lending Reports"/>
<c:url value="/" var="rootContext" />
<c:url value="/resources" var="resValue" />
<c:url value="../.." var="currentDir" />
<!DOCTYPE html>
<html lang="en">
	<head>
	<jsp:include page="../common-head-stretchyreporting.jsp" />
	
	<style type="text/css">
	#toptable {
		display: none;
	}
	</style>
	</head>
<body>

<jsp:include page="../top-navigation.jsp" />
<div id="spacer" style="line-height: 25px;">&nbsp;</div>
		
<table id=toptable>
 <tr>
  <td valign="top"><div id=myListOfReports></div></td>
  <td valign="bottom"><div id=myInputParameters></div></td>
  <td valign="top"><div id=myRunReportButton></div></td>
  <td valign="top"><div id=myClearReportButton></div></td>
  <td valign="bottom">
		<select id=decimalsChoice onChange="selectNewDecimals(options[selectedIndex].value)" >
		<option value="" selected="selected">Decimals</option>
		<option value="4">4</option>
		<option value="3">3</option>
		<option value="2">2</option>
		<option value="1">1</option>
		<option value="0">0</option>
		<option value="-1">-1</option>
		<option value="-2">-2</option>
		<option value="-3">-3</option>
		</select>
   </td>
  <td valign="bottom">
		<select id=decimalsThousandsSep onChange="selectNewThousandsSep(options[selectedIndex].value)" >
		<option value="" selected="selected">Format</option>
		<option value=",.">1,234,567.89</option>
		<option value=".,">1.234.567,89</option>
		<option value=" ,">1 234 567,89</option>
		<option value=" .">1 234 567.89</option>
		<option value=".'">1.234.567'89</option>
		<option value="',">1'234'567,89</option>
		<option value="INDIAN">Indian 12,34,567.89</option>
		<option value="NONE">None 1234567.89</option>
		</select>
   </td>


 </tr>
</table>

<div id=myOutput></div>


<script type="text/javascript">

function selectNewDecimals(selectedVal) {
	if (!(selectedVal == "")) jQuery.stretchyReporting.changeDecimals(selectedVal);
}

function selectNewThousandsSep(selectedVal) {

	if (!(selectedVal == "")) 
	{

		switch(selectedVal )
		{
			case "INDIAN":
				jQuery.stretchyReporting.changeSeparator(",", ".", true);
  				break;
			case "NONE":
				jQuery.stretchyReporting.changeSeparator("", ".", false);
  				break;
			default:
				jQuery.stretchyReporting.changeSeparator(selectedVal.substr(0,1), selectedVal.substr(1,1), false);
		}
	}
}

var reportingParams = {
	//rptDB: "mifosngproviderJ",
	RESTUrl: "${baseApiUrl}reports",
	basicAuthKey: "${basicAuthKey}",
	pentahoUrl: "${baseApiUrl}pentahoreport",
	bundleDir: "${resValue}/stretchyreporting/mifosngbundle/",
	reportsListDiv: "myListOfReports",
	runReportDiv: "myRunReportButton",
	clearReportDiv: "myClearReportButton",
	inputParametersDiv: "myInputParameters",
	reportOutputDiv: "myOutput",
	indianFormat: false,
	highlightMissingXlations: "N",
	loadingImg: "${resValue}/stretchyreporting/dots64.gif",
	//old resValue: "${resValue}/stretchyreporting/"
	resValue: "${resValue}/libs/"
	
	//OAuth 1.0a parameters
	//apiKey: "${consumerKey}",
	//sharedSecret: "${consumerSecret}",
	//accessToken: "${accessToken}",
	//tokenSecret: "${tokenSecret}"
};

jQuery.stretchyReporting.initialise(reportingParams);

$(document).ready(function() {
	$("#toptable").slideToggle("slow");
} );

</script>

</body>
</html>