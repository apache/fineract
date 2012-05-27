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
<script>
$(document).ready(function() {

	// basic auth details
	var base64 = "${basicAuthKey}";
	var baseApiUrl = "${baseApiUrl}";
	
	// these helpers are registered for the jsViews and jsRender functionality to fix bug with display zero!
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
			    	
			    	  var year = dateParts[0];
			    	  var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  var day = dateParts[2];
			    	  
			    	  var d = new Date();
			    	  d.setFullYear(year,month,day);
			    	  
			    	  return Globalize.format(d,"dd MMMM yyyy");
			      } catch(e) {
			        return "??";
			      }
			},
			globalDateAsISOString: function(localDateAsISOString) {
				
			      try {
			    	  var dateParts = localDateAsISOString.split("-")
			    	  var year = dateParts[0];
			    	  var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  var day = dateParts[2];
			    	  
			    	  var d = new Date();
			    	  d.setFullYear(year,month,day);
			    	  
			    	  return Globalize.format(d,"dd MMMM yyyy");
			      } catch(e) {
			        return "??";
			      }
			},
			globalDateTime: function(dateInMillis) {
			      try {
			    	  var d = new Date(dateInMillis);
			    	  
			    	  return Globalize.format(d,"F");
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
		  	console.log(jsonErrors);
		  	var valErrors = jsonErrors.errors;
		  	console.log(valErrors);
		  	var errorArray = new Array();
		  	var arrayIndex = 0;
		  	$.each(valErrors, function() {
		  	  var fieldId = '#' + this.parameterName;
		  	  $(fieldId).addClass("ui-state-error");
		  	  
		  	  var errorObj = new Object();
		  	  errorObj.field = this.parameterName;
		  	  errorObj.code = this.userMessageGlobalisationCode;
		  	  
		  	  var argArray = new Array();
		  	  var argArrayIndex = 0;
		  	  $.each(this.args, function() {
		  		argArray[argArrayIndex] = this.value;
		  		argArrayIndex++;
		  	  });
		  	  // hardcoded support for six arguments
		  	  errorObj.message = jQuery.i18n.prop(this.userMessageGlobalisationCode, argArray[0], argArray[1], argArray[2], argArray[3], argArray[4], argArray[5]);
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
	
	function showNotAvailableDialog(titleCode) {
		var dialogDiv = $("<div id='notavailable-dialog-form'></div>");
		
		dialogDiv.append("<p>" + jQuery.i18n.prop('dialog.messages.functionality.not.available') + "</p>");
		
		var okButton = jQuery.i18n.prop('dialog.button.ok');
		
		var buttonsOpts = {};
		buttonsOpts[okButton] = function() {$(this).dialog("close");};
		
		dialogDiv.dialog({
	  		title: jQuery.i18n.prop(titleCode), 
	  		width: 300, 
	  		height: 200, 
	  		modal: true,
	  		buttons: buttonsOpts,
	  		close: function() {
	  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
	  			$(this).remove();
			}
		 }).dialog('open');
	}
	
	$.fn.serializeObject = function()
	{
	    var o = {};
	    var a = this.serializeArray();
	    $.each(a, function() {
	        if (o[this.name] !== undefined) {
	            if (!o[this.name].push) {
	                o[this.name] = [o[this.name]];
	            }
	            o[this.name].push(this.value || '');
	        } else {
	        	
	        	if (this.name === 'selectedItems' || this.name === 'notSelectedItems') {
	        		o[this.name] = new Array();
	        		o[this.name].push(this.value || '');
	        	} else {
	        		o[this.name] = this.value || '';	
	        	}
	        }
	    });
	    return o;
	};
	
	function popupDialogWithFormView(getUrl, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction) {
		 var dialogDiv = $("<div id='dialog-form'></div>");
		 var jqxhr = $.ajax({
			url: getUrl,
			type: 'GET',
			contentType: 'application/json',
			dataType: 'json',
			cache: false,
			beforeSend: function(xhr) {
	            xhr.setRequestHeader("Authorization", "Basic " + base64);
			},
			success: function(data, textStatus, jqXHR) {
				console.log(data);
				var formHtml = $(templateSelector).render(data);
				
				dialogDiv.append(formHtml);
				
				var saveButton = jQuery.i18n.prop('dialog.button.save');
				var cancelButton = jQuery.i18n.prop('dialog.button.cancel');
				
				var buttonsOpts = {};
				buttonsOpts[saveButton] = function() {
					
					$('#notSelectedItems option').each(function(i) {  
				    	   $(this).attr("selected", "selected");  
				    });
			    	
			    	$('#selectedItems option').each(function(i) {  
			    	   $(this).attr("selected", "selected");  
			    	});
			    	
			    	var newFormData = JSON.stringify($('#entityform').serializeObject());
			    	console.log(newFormData);
			    	
					var jqxhr = $.ajax({
						  url: postUrl,
						  type: submitType,
						  contentType: 'application/json',
						  dataType: 'json',
						  data: newFormData,
						  success: saveSuccessFunction,
						  cache: false,
						  beforeSend: function(xhr) {
					            xhr.setRequestHeader("Authorization", "Basic " + base64);
						  },
						  error: function(jqXHR, textStatus, errorThrown) {
						    handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
						  }
					});
				};
				
				buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
				
				dialogDiv.dialog({
				  		title: jQuery.i18n.prop(titleCode), 
				  		width: width, 
				  		height: height, 
				  		modal: true,
				  		buttons: buttonsOpts,
				  		close: function() {
				  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
				  			$(this).remove();
						},
				  		open: function (event, ui) {
				  			
				  			$('#add').click(function() {  
				  			     return !$('#notSelectedItems option:selected').remove().appendTo('#selectedItems');  
				  			});
				  			
				  			$('#remove').click(function() {  
				  				return !$('#selectedItems option:selected').remove().appendTo('#notSelectedItems');  
				  			});
				  			
				  			$('.datepickerfield').datepicker({constrainInput: true, maxDate: 0, dateFormat: 'dd MM yy'});
				  			
				  			$("#entityform textarea").first().focus();
				  			$('#entityform input').first().focus();
				  		}
				  	}).dialog('open');
		  	}
		 });
		 
		jqxhr.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}
	
	$("#tabs").tabs({
		"ajaxOptions": {
			type: 'GET',
			dataType: 'json',
			contentType: 'application/json; charset=utf-8',
			cache: false,
			beforeSend: function(xhr) {
				console.log("before send");
	            xhr.setRequestHeader("Authorization", "Basic " + base64);
			},
	        success: function(data, status, xhr) {
	        },
	        error: function(xhr, status, index, anchor) {
	            $(anchor.hash).html("error occured while ajax loading.");
	        }
	    },
	    select: function(event, ui) {
	    	console.log("selected..");
	    },
	    load: function(event, ui) {
	    	console.log("load..");
	    },
	    show: function(event, ui) {
	    	console.log("show..");
	    	
	    	var jqxhr = $.ajax({
				  url: baseApiUrl + 'clients',
				  type: 'GET',
				  contentType: 'application/json',
				  dataType: 'json',
				  cache: false,
				  beforeSend: function(xhr) {
			            xhr.setRequestHeader("Authorization", "Basic " + base64);
				  },
				  success: function(data) {
				  		var clientObject = new Object();
			        	clientObject.clients = data;
			        	console.log(clientObject);
			        	
				    	var tableHtml = $("#clientSearchTabTemplate").render(clientObject);
						$("#searchtab").html(tableHtml);
						
						$('#client').change(function() {
				        	$('#viewClient').submit();
				    	});
				  },
				  error: function(jqXHR, textStatus, errorThrown) {
				    handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
				  }
			});
	    }
	});
	
	var addClientSuccessFunction = function(data, textStatus, jqXHR) {
		  $('#dialog-form').dialog("close");
		  window.location.href = '${rootContext}/portfolio/client/' + data.entityId;
	}
	
	$("#addclient").button().click(function(e) {
		var getUrl = baseApiUrl + 'clients/template';
		var postUrl = baseApiUrl + 'clients';
		var templateSelector = "#clientFormTemplate";
		var width = 600; 
		var height = 350;
		
		popupDialogWithFormView(getUrl, postUrl, 'POST', 'dialog.title.add.client', templateSelector, width, height, addClientSuccessFunction);
		
	    e.preventDefault();
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
			<sec:authorize access="hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_ENROLL_NEW_CLIENT_ROLE')">
			<button id="addclient" style="clear: both;"><spring:message code="link.add.new.client"/></button>
			</sec:authorize>
			<div id="tabs">
				<ul>
					<li><a href="#searchtab" title="searchtab"><spring:message code="tab.search"/></a></li>
				</ul>
				<div id="searchtab"></div>
			</div>
		</div>
	</div>
</div>

<jsp:include page="view-templates.jsp" />

</body>
</html>