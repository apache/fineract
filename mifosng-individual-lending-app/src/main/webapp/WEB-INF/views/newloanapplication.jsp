<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request"><spring:message code="page.home.title"/></c:set>
<!DOCTYPE html>
<html lang="en">
<head>
<jsp:include page="common-head.jsp" />
<c:url value="/" var="rootContext" />
<c:url value="/portfolio/client/${clientId}/loan/new/workflow/one" var="newLoanUrl" />
<script>
$(document).ready(function() {

	$.views.registerHelpers({
		
		money: function(monetaryObj) {
			
			Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
			
			var digits = monetaryObj.currencyDigitsAfterDecimal.toFixed(0);
			return Globalize.format(monetaryObj.amount, "n" + digits); 
		},
		moneyWithCurrency: function(monetaryObj) {
			
			Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
			
			var digits = monetaryObj.currencyDigitsAfterDecimal.toFixed(0);
			return Globalize.format(monetaryObj.amount, "c" + digits); 
		},
		decimal: function(number, digits) {
		      try {
		    	return Globalize.format(number, "n" + digits); 
		      } catch(e) {
		        return number +"(NaN)";
		      }
		},
		number: function(number) {
		      try {
		    	  return Globalize.format(number, "n0"); 
		      } catch(e) {
		        return number +"(NaN)";
		      }
		},
	    numberGreaterThanZero: function(number) {
		      try {
		    	var num = number.toFixed(0);
		        return num > 0;
		      } catch(e) {
		        return false;
		      }
		},
		globalDate: function(dateParts) {
		      try {
		    	  if (dateParts === null) {
		    		  return "";
		    	  } else {
			    	  var year = dateParts[0];
			    	  var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  var day = dateParts[2];
			    	  
			    	  var d = new Date();
			    	  d.setFullYear(year,month,day);
			    	  
			    	  return Globalize.format(d,"dd MMMM yyyy");
		    	  }
		      } catch(e) {
		        return "??";
		      }
		},
		globalDateTime: function(dateParts) {
		      try {
		    	  if (dateParts === null) {
		    		  return "";
		    	  } else {
		    		  var d = new Date(dateParts);
			    	  
			    	  return Globalize.format(d,"F");
		    	  }
		      } catch(e) {
		        return "??";
		      }
		}
		
	});

	function removeErrors(placeholderDiv) {
		// remove error class from all input fields
		var $inputs = $('#entityform :input');
		
	    $inputs.each(function() {
	        $(this).removeClass("ui-state-error");
	    });
		
	  	$(placeholderDiv).html("");
	}
	
	function handleXhrError(jqXHR, textStatus, errorThrown, templateSelector, placeholderDiv) {
	  	if (jqXHR.status === 0) {
		    alert('No connection. Verify application is running.');
	  	} else if (jqXHR.status == 401) {
			alert('Unauthorized. [401]');
		} else if (jqXHR.status == 404) {
		    alert('Requested page not found. [404]');
		} else if (jqXHR.status == 405) {
			alert('HTTP verb not supported [405]: ' + errorThrown);
		} else if (jqXHR.status == 500) {
		    alert('Internal Server Error [500].');
		} else if (errorThrown === 'parsererror') {
		    alert('Requested JSON parse failed.');
		} else if (errorThrown === 'timeout') {
		    alert('Time out error.');
		} else if (errorThrown === 'abort') {
		    alert('Ajax request aborted.');
		} else {
			
			removeErrors(placeholderDiv);
			
		  	var jsonErrors = JSON.parse(jqXHR.responseText);
		  	
		  	var errorArray = new Array();
		  	var arrayIndex = 0;
		  	$.each(jsonErrors, function() {
		  	  var fieldId = '#' + this.field;
		  	  $(fieldId).addClass("ui-state-error");
		  	  
		  	  var errorObj = new Object();
		  	  errorObj.field = this.field;
		  	  errorObj.code = this.code;
		  	  errorObj.message = jQuery.i18n.prop(this.code, this.args);
		  	  errorObj.value = this.value;
		  	  
		  	  errorArray[arrayIndex] = errorObj;
		  	  arrayIndex++
		  	});
		  	
		  	var templateArray = new Array();
		  	var templateErrorObj = new Object();
		  	templateErrorObj.title = jQuery.i18n.prop('error.msg.header');
		  	templateErrorObj.errors = errorArray;
		  	
		  	templateArray[0] = templateErrorObj;
		  	
		  	var formErrorsHtml = $(templateSelector).render(templateArray);
		  	
		  	$(placeholderDiv).append(formErrorsHtml);
		}
	}
	
	function calculateAnnualPercentageRate() {
		var periodInterestRate = parseFloat($('#nominalInterestRate').val());
		if (isNaN(periodInterestRate)) {
			periodInterestRate = 0;
		}
		
		var periodsInYear = 12;
		var periodType = $('#selectedInterestFrequencyOption').val();
		if (periodType == 3) {
			periodsInYear = 1;
		} else if (periodType == 2) {
			periodsInYear = 12;
		} else if (periodType == 1) {
			periodsInYear = 52;
		}
		
		var apr = parseFloat(periodsInYear * periodInterestRate);
        $('#interestRatePerYear').val(Globalize.format(apr, "n4"));
	}
	
	function repopulateFullForm(clientId, productId) {
		
		var url = '${rootContext}portfolio/client/' + clientId + '/product/' + productId + '/new';
		
		var jqxhr = $.ajax({
			url: url,
			type: 'GET',
			contentType: 'application/json',
			dataType: 'json',
			cache: false,
			success: function(data, textStatus, jqXHR) {
			
				var formHtml = $("#newLoanFormTemplate").render(data);
			
				$("#inputarea").html(formHtml);

				$('#productId').change(function() {
					var productId = $('#productId').val();
					repopulateFullForm(clientId, productId);
				});
				
				$('.datepickerfield').datepicker({constrainInput: true, defaultDate: 0, maxDate: 0, dateFormat: 'dd MM yy'});
				
				calculateAnnualPercentageRate();
				
				calculateLoanSchedule();
				
				// change detection
				$('#principalMoney').change(function() {
					calculateLoanSchedule();
				});
				
				$('#nominalInterestRate').change(function() {
					calculateAnnualPercentageRate();
					calculateLoanSchedule();
				});
				
				$('#selectedInterestFrequencyOption').change(function() {
					calculateAnnualPercentageRate();
					calculateLoanSchedule();
				});
				
				$('#selectedInterestMethodOption').change(function() {
					calculateLoanSchedule();
				});
				
				$('#repaidEvery').change(function() {
					calculateLoanSchedule();
				});
				
				$('#selectedRepaymentFrequencyOption').change(function() {
					calculateLoanSchedule();
				});
				
				$('#numberOfRepayments').change(function() {
					calculateLoanSchedule();
				});
				
				$('#selectedAmortizationMethodOption').change(function() {
					calculateLoanSchedule();
				});
				
				$('#expectedDisbursementDate').change(function() {
					calculateLoanSchedule();
				});
				
				$('#repaymentsStartingFromDate').change(function() {
					calculateLoanSchedule();
				});
				
				$('#interestCalculatedFromDate').change(function() {
					calculateLoanSchedule();
				});
				
				$('#submitloanapp').button().click(function(e) {
					submitLoanApplication();
				    e.preventDefault();
				});
				$('button#submitloanapp span').text(jQuery.i18n.prop('dialog.button.submit'));
				
				$('#cancelloanapp').button().click(function(e) {
					var url = '${rootContext}portfolio/client/' + clientId;
					window.location.href = url;
				    e.preventDefault();
				});
				$('button#cancelloanapp span').text(jQuery.i18n.prop('dialog.button.cancel'));
			}
		});
			
		jqxhr.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}
	
	function calculateLoanSchedule() {
		var calculateLoanScheduleurl = '${rootContext}portfolio/loanschedule/calculate';
		
		var form_data = $('#entityform').serialize();
			
		var jqxhr2 = $.ajax({
			  url: calculateLoanScheduleurl,
			  type: 'POST',
			  data: form_data,
			  success: function(data, textStatus, jqXHR) {
				  removeErrors("#formerrors");
				  var loanScheduleHtml = $("#newLoanScheduleTemplate").render(data);
				  $("#schedulearea").html(loanScheduleHtml);
			  }
		});
		
		jqxhr2.error(function(jqXHR, textStatus, errorThrown) {
			 $("#schedulearea").html("");
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}
	
	function submitLoanApplication() {
		var submitLoanApplicationUrl = '${rootContext}portfolio/client/${clientId}/loan/new';
		
		var form_data = $('#entityform').serialize();
			
		var jqxhr2 = $.ajax({
			  url: submitLoanApplicationUrl,
			  type: 'POST',
			  data: form_data,
			  success: function(data, textStatus, jqXHR) {
				  var url = '${rootContext}portfolio/client/${clientId}';
				  window.location.href = url;
			  }
		});
		
		jqxhr2.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}
	
	
	
	var jqxhr = $.ajax({
		url: '${newLoanUrl}',
		type: 'GET',
		contentType: 'application/json',
		dataType: 'json',
		cache: false,
		success: function(data, textStatus, jqXHR) {
		
			var formHtml = $("#newLoanFormTemplateMin").render(data);
		
			$("#inputarea").html(formHtml);

			$('#productId').change(function() {
				
				var clientId = '${clientId}';
				var productId = $('#productId').val();
				repopulateFullForm(clientId, productId);
			});
		}
	});
		
	jqxhr.error(function(jqXHR, textStatus, errorThrown) {
		handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	});
});
</script>
</head>
<body>

<div id="container">
	<jsp:include page="top-navigation.jsp" />
    
	<div style="float:none; clear:both;">
		<div id="spacer" style="line-height: 15px;">&nbsp;</div>
		<div id="content">
		
			<div id="inputarea"></div>
			
			<div id="schedulearea"></div>
		</div>
	</div>
</div>

<jsp:include page="view-templates.jsp" />

</body>
</html>