<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request" value="Create Cashflow"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="../common-head.jsp" />
	<style>
	#toolbar {
		padding: 12px 3px;
		text-align: center;
	}
	
	.ntable th {
		font: bold 11px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif;
		border-right: 1px solid #C1DAD7;
		border-bottom: 1px solid #C1DAD7;
		border-top: 1px solid #C1DAD7;
		text-transform: uppercase;
		text-align: left;
		padding: 6px 6px 6px 12px;
	}
	
	.ntable td {
		border-right: 1px solid #C1DAD7;
		background: #fff;
		padding: 2px 12px 2px 12px;
		color: #6D929B;
	}
	
	.ntable td .subtotalrow {
	    font-weight: bold;
	    color: black;
		border-bottom: 1px solid black;
		border-top: 1px solid black;
	}
	
	</style>
	<script type="text/javascript">
        $(document).ready(function() {
        	$('button.toolbarbutton').button();
        	
        	$('input.calc').change(function(e) {
        		alert("click on calc detected: ");
               
                var $producttable = $(this).closest('tbody');
                
                var sold = $producttable.find('input.unitsold').val();
                var cost = $producttable.find('input.unitcost').val();
                var price = $producttable.find('input.unitprice').val();
                
                var expense = parseFloat(sold * cost);
                var income = parseFloat(sold * price);
                
                $producttable.find('.expensesubtotal').text(expense);
                $producttable.find('.incomesubtotal').text(expense);
               
                $producttable.find('.nettotal').text(income - expense);
            });
        });
    </script>
</head>

<body>
<div id="container">
	<jsp:include page="../top-navigation.jsp" />

	<div style="float:none; clear:both;">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>
		<div id="content">
		
			<div id="formcontainer">
			
			<form:form commandName="cashflowFormBean">
			<spring:hasBindErrors name="cashflowFormBean">
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
			
			<table class="pretty">
				<tr>
					<td>Cashflow Analysis for Individual Loan</td>
				</tr>
				
				<tr>
					<td>Business name: ${cashflowFormBean.businessName}</td>
					<td>Client name: ${cashflowFormBean.clientName}</td>
				</tr>
			</table>
			
			<table class="ntable">
				<thead>
					<tr><th>Expected Loan Requirements:</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>Loan Term (Months):</td>
					</tr>
					<tr>
						<td><form:input path="expectedLoanTerm" /></td>
					</tr>
				</tbody>
			</table>
			
			<table class="ntable">
				<thead>
					<tr><th>Fixed investments:</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>Term (in months)</td>
						<td>Amount (CFA)</td>
					</tr>
					<c:forEach items="${cashflowFormBean.investments}" var="investment" varStatus="gridrow">
					<tr>
						<td><form:input path="investments[${gridrow.index}].name" /></td>
						<td><form:input path="investments[${gridrow.index}].fullTermInMonths" /></td>
						<td><form:input path="investments[${gridrow.index}].fullAmount" /></td>
					</tr>
					</c:forEach>
				</tbody>
			</table>
			
			<table class="ntable">
				<thead>
					<tr><th>Fixed expenses:</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>Amount (CFA)</td>
					</tr>
					<c:forEach items="${cashflowFormBean.fixedExpenses}" var="expenses" varStatus="gridrow">
					<tr>
						<td><form:input path="fixedExpenses[${gridrow.index}].name" /></td>
						<td><form:input path="fixedExpenses[${gridrow.index}].amount" /></td>
					</tr>
					</c:forEach>
				</tbody>
			</table>
			
			<c:forEach items="${cashflowFormBean.products}" var="product" varStatus="gridrow">
			<table class="ntable producttable">
				<thead>
					<tr><th colspan="2">Product related income/expenses:</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>Name:</td>
						<td><form:input path="products[${gridrow.index}].name" /></td>
					</tr>
					<tr>
						<td># Units Sold:</td>
						<td><form:input path="products[${gridrow.index}].unitsSold" cssClass="calc unitsold"/></td>
					</tr>
					<tr>
						<td>Cost Per Unit:</td>
						<td><form:input path="products[${gridrow.index}].costPerUnit" cssClass="calc unitcost"/></td>
					</tr>
					<tr>
						<td class="subtotalrow">Product Expenses:</td>
						<td class="subtotalrow expensesubtotal">xxx.xx</td>
					</tr>
					<tr>
						<td>Unit Selling Price:</td>
						<td><form:input path="products[${gridrow.index}].pricePerUnit" cssClass="calc unitprice" /></td>
					</tr>
					<tr>
						<td class="subtotalrow">Product Income:</td>
						<td class="subtotalrow incomesubtotal">xxx.xx</td>
					</tr>
					<tr>
						<td class="subtotalrow">Net Income:</td>
						<td class="subtotalrow nettotal">xxx.xx</td>
					</tr>
				</tbody>
			</table>
			</c:forEach>
			
			<br />	
			<span id="toolbar" class="ui-widget-header ui-corner-all">
				<button type="submit" class="toolbarbutton" id="_eventId_continue" name="_eventId_continue" title="Continue with creation of cashflow analysis.">Continue</button>
				<button type="submit" class="toolbarbutton" id="_eventId_cancel" name="_eventId_cancel" title="Cancel cashflow analysis.">Cancel</button>
			</span>
			</form:form>
			</div>
		</div>
	</div>
</div>
</body>
</html>