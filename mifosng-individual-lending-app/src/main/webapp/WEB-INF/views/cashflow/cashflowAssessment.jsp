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
	
	#formcontainer input {
    	width: 100%;
		border: 0px;
		padding: 0px;
		font-size: 11pt;
	}
	</style>
	<script type="text/javascript">
        $(document).ready(function() {
        	$('button.toolbarbutton').button();
        	
        	function calcExpense() {
        		var totalexpense = parseFloat(0);
                $('.expensestart').each(function (i) {
                	var applicable = parseFloat($(this).find('input.calcexpense').val());
                	totalexpense = totalexpense + applicable;
                });
                $('.totalexpense').text(totalexpense);
        	}
        	
        	function calcInvestment() {
				var loanTerm = $('.loanterm').val();
				
				$('.investmentstart').each(function (i) {
	                var amount = $(this).find('input.investamount').val();
	                var term = $(this).find('input.investterm').val();
	                
	                var termMult = parseFloat(loanTerm / term);
	                
	                var applicable = parseFloat(termMult * amount);
	                if (isNaN(applicable)) {
	                	applicable = 0;
	                }
	                $(this).find('input.investapplicable').val(applicable);
	            });
				
                var totalinvestment = parseFloat(0);
                $('.investmentstart').each(function (i) {
                	var applicable = parseFloat($(this).find('input.investapplicable').val());
                	totalinvestment = totalinvestment + applicable;
                });
                $('.totalinvestment').text(totalinvestment);
        	}
        	
        	function calcProducts() {
        		$('.productstart').each(function (i) {
					 var sold = $(this).find('input.unitsold').val();
			         var cost = $(this).find('input.unitcost').val();
			         var price = $(this).find('input.unitprice').val();
			         
			         var expense = parseFloat(sold * cost);
			         var income = parseFloat(sold * price);
			         
			         $(this).find('.expensesubtotal').text(expense);
			         $(this).find('.incomesubtotal').text(income);
                });
        	}
        	
        	function calcTotals() {
        		
        		var totalincome = 0;
        		var totalexpenses = 0;
        		$('.productstart').each(function (i) {
					 var sold = $(this).find('input.unitsold').val();
			         var cost = $(this).find('input.unitcost').val();
			         var price = $(this).find('input.unitprice').val();
			         
			         var expense = parseFloat(sold * cost);
			         var income = parseFloat(sold * price);
			         
			         totalincome = totalincome + income;
			         totalexpenses = totalexpenses + expense;
               });
        		
        		var totalinvestments = parseFloat($('.totalinvestment').text());
        		var totalfixedexpenses = parseFloat($('.totalexpense').text());
        		
        		totalexpenses = totalexpenses + totalinvestments + totalfixedexpenses;
        		
        		$('.totalincoming').text(totalincome);
        		$('.totaloutgoing').text(totalexpenses);
        		
        		var amountavailable = totalincome + parseFloat($('.entrepreneurinput').val());
        		
        		$('.minamountrequired').text(totalexpenses - amountavailable);
        	}
        	
        	$('input.calcexpense').change(function(e) {
        		calcExpense();
        		calcTotals();
        	});
        	
        	$('input.calcinvest').change(function(e) {
        		calcInvestment();
        		calcTotals();
            });
        	
        	$('input.calc').change(function(e) {
        		calcProducts();
        		calcTotals();
            });
        	
        	$('input.entrepreneurinput').change(function(e) {
        		calcTotals();
            });
        	
        	calcInvestment();
        	calcExpense();
        	calcProducts();
        	calcTotals();
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
			
			<p>Business name: ${cashflowFormBean.businessName}</p>
	        <p>Client name: ${cashflowFormBean.clientName}</p>
			
			<table id="TheTable" border="1" class="ExcelTable2007">
				<tr>
					<th class="heading">&nbsp;</th>
					<th>A</th>
					<th>B</th>
					<th>C</th>
					<th>D</th>
				</tr>
				<tr>
					<td align="left" valign="bottom" class="heading">1</td>
					<td align="left" valign="bottom">Loan Term (in months)</td>
					<td align="right" valign="bottom"><form:input path="expectedLoanTerm" cssClass="calcinvest loanterm" /></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">2</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">3</td>
					<td align="left" valign="bottom">Investments</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">4</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">Total</td>
					<td align="right" valign="bottom">Term (in months)</td>
					<td align="right" valign="bottom">Amount for Loan Term (CFA)</td>
				</tr>
				
				<c:set var="row" value="5" />
				<c:forEach items="${cashflowFormBean.investments}" var="investment" varStatus="gridrow">
				<tbody class="investmentstart">
				<tr>
					<td class="heading">${row}</td>
					<td align="left" valign="bottom"><form:input path="investments[${gridrow.index}].name" /></td>
					<td align="right" valign="bottom"><form:input path="investments[${gridrow.index}].fullAmount" cssClass="calcinvest investamount" /></td>
					<td align="right" valign="bottom"><form:input path="investments[${gridrow.index}].fullTermInMonths" cssClass="calcinvest investterm" /></td>
					<td align="right" valign="bottom"><form:input path="investments[${gridrow.index}].applicableAmount" disabled="true" cssClass="investapplicable"/></td>
				</tr>
				<c:set var="row" value="${row + 1}" />
				</tbody>
				</c:forEach>
				<tr>
					<td class="heading">${row + 1}</td>
					<td align="left" valign="bottom">Total</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom" class="totalinvestment">25000</td>
				</tr>
				<tr>
					<td class="heading">${row + 2}</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${row + 3}</td>
					<td align="left" valign="bottom"><b>Expenses</b></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${row + 4}</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">Amount</td>
				</tr>
				
				<c:set var="expenserow" value="${row + 5}" />
				<c:forEach items="${cashflowFormBean.fixedExpenses}" var="expenses" varStatus="gridrow">
				<tbody class="expensestart">
				<tr>
					<td class="heading">${expenserow}</td>
					<td align="left" valign="bottom"><form:input path="fixedExpenses[${gridrow.index}].name" /></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom"><form:input path="fixedExpenses[${gridrow.index}].amount" cssClass="calcexpense"/></td>
				</tr>
				</tbody>
				<c:set var="expenserow" value="${expenserow + 1}" />
				</c:forEach>

				<c:set var="row" value="${expenserow + 1}" />
				<tr>
					<td class="heading">${row}</td>
					<td align="left" valign="bottom">Total</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom" class="totalexpense">1000</td>
				</tr>
				<tr>
					<td class="heading">${row + 1}</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>	
				<tr>
					<td class="heading">${row + 2}</td>
					<td align="left" valign="bottom">Products</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				
				<c:set var="productrow" value="${row + 3}" />
				<c:forEach items="${cashflowFormBean.products}" var="product" varStatus="gridrow">
				<tbody class="productstart">
				<tr>
					<td class="heading">${productrow}</td>
					<td align="left" valign="bottom">Name</td>
					<td align="right" valign="bottom"><form:input path="products[${gridrow.index}].name" /></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${productrow + 1}</td>
					<td align="left" valign="bottom"># Units sold</td>
					<td align="right" valign="bottom"><form:input path="products[${gridrow.index}].unitsSold" cssClass="calc unitsold"/></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${productrow + 2}</td>
					<td align="left" valign="bottom">Cost per unit</td>
					<td align="right" valign="bottom"><form:input path="products[${gridrow.index}].costPerUnit" cssClass="calc unitcost"/></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${productrow + 3}</td>
					<td align="left" valign="bottom">Unit selling price</td>
					<td align="right" valign="bottom"><form:input path="products[${gridrow.index}].pricePerUnit" cssClass="calc unitprice" /></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${productrow + 4}</td>
					<td align="left" valign="bottom">Total Expenses</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom" class="expensesubtotal">12000</td>
				</tr>
				<tr>
					<td class="heading">${productrow + 5}</td>
					<td align="left" valign="bottom">Total Income</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom" class="incomesubtotal">14000</td>
				</tr>
				<tr>
					<td class="heading">${productrow + 6}</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<c:set var="productrow" value="${productrow + 7}" />
				</tbody>
				</c:forEach>

				<c:set var="row" value="${productrow}" />
				<tr>
					<td class="heading">${row}</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">Incoming</td>
					<td align="right" valign="bottom">Outgoing</td>
				</tr>	
				<tr>
					<td class="heading">${row + 1}</td>
					<td align="left" valign="bottom">Totals</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom" class="totalincoming">0</td>
					<td align="right" valign="bottom" class="totaloutgoing">0</td>
				</tr>
				<tr>
					<td class="heading">${row + 2}</td>
					<td align="left" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${row + 3}</td>
					<td align="left" valign="bottom">Capital Input (from Entrepreneur)</td>
					<td align="right" valign="bottom"><form:input path="entrepreneurInput" cssClass="entrepreneurinput" /></td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${row + 4}</td>
					<td align="left" valign="bottom">Minimum Loan Amount Needed</td>
					<td align="right" valign="bottom" class="minamountrequired">0</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
				<tr>
					<td class="heading">${row + 5}</td>
					<td align="left" valign="bottom">Projected Interest</td>
					<td align="right" valign="bottom">0</td>
					<td align="right" valign="bottom">&nbsp;</td>
					<td align="right" valign="bottom">&nbsp;</td>
				</tr>
			</table>
			
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