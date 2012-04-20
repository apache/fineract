<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" scope="request" value="View All Offices"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="../common-head.jsp" />
	<script type='text/javascript' src='https://www.google.com/jsapi'></script>
    <script type='text/javascript'>
      google.load('visualization', '1', {packages:['orgchart', 'table']});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
    	  // Create and populate the data table.
    	  var data = new google.visualization.DataTable();
    	  data.addColumn('string', 'Office');
    	  data.addColumn('string', 'Parent');
    	  data.addColumn('string', 'ExternalId');
    	  
    	  var size = ${fn:length(offices)};
    	  data.addRows(size);
    	  
    	  var c = 1;
    	  <c:forEach items="${offices}" var="office">
				<c:url value="/org/office/${office.id}" var="viewOfficeUrl"/>
				<c:choose>
					<c:when test="${empty office.parentName}">
						data.setCell(0, 0, '<a href="${viewOfficeUrl}">${office.name}</a>');
	    	  			data.setCell(0, 1, null);
	    	  			data.setCell(0, 2, '${office.externalId}');					
					</c:when>
					<c:otherwise>
						<c:url value="/org/office/${office.parentId}" var="viewParentOfficeUrl"/>
						data.setCell(c, 0, '<a href="${viewOfficeUrl}">${office.name}</a>');
		    	  		data.setCell(c, 1, '<a href="${viewParentOfficeUrl}">${office.parentName}</a>');
		    	  		data.setCell(c, 2, '${office.externalId}');
		    	  		c = c + 1;
					</c:otherwise>
				</c:choose>
		  </c:forEach>
		  
		  var orgchart = new google.visualization.OrgChart(document.getElementById('orgchart'));
		  orgchart.draw(data, {allowHtml:true});

		  var table = new google.visualization.Table(document.getElementById('table'));
		  table.draw(data, {allowHtml:true});
		  
		  // When the table is selected, update the orgchart.
		  google.visualization.events.addListener(table, 'select', function() {
		    orgchart.setSelection(table.getSelection());
		  });

		  // When the orgchart is selected, update the table visualization.
		  google.visualization.events.addListener(orgchart, 'select', function() {
		    table.setSelection(orgchart.getSelection());
		  });  
    	}
    </script>
</head>

<body>
	<div id="container">
		<jsp:include page="../top-navigation.jsp" />
	
		<div style="float:none; clear:both;">
			<div id="spacer" style="line-height: 25px;">&nbsp;</div>
			
			<div id='orgchart'></div>
			<br/>
			<div id='table'></div>
		</div>
	</div>	
</body>
</html>