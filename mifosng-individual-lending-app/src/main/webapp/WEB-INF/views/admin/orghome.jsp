<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request"><spring:message code="page.admin.home.title"/></c:set>
<!DOCTYPE html>
<html lang="en">
<head>
<jsp:include page="../common-head.jsp" />
<c:url value="/portfolio/product/loan/all" var="allLoanProductsUrl" />
<c:url value="/org/office/all" var="allOfficesUrl" />
<c:url value="/" var="rootContext" />
<script>
	$(document).ready(function() {
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
			globalDate: function(localDateAsISOString) {
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
	            o[this.name] = this.value || '';
	        }
	    });
	    return o;
	};
	
	function popupDialogWithFormView(getUrl, postUrl, titleCode, templateSelector, width, height, saveSuccessFunction) {
		 var dialogDiv = $("<div id='dialog-form'></div>");
		 var jqxhr = $.ajax({
			url: getUrl,
			type: 'GET',
			contentType: 'application/json',
			dataType: 'json',
			cache: false,
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
		    	
	  			var form_data = $('#entityform').serialize();
	  			
				var jqxhr = $.ajax({
					  url: postUrl,
					  type: 'POST',
					  contentType: 'application/json',
					  dataType: 'json',
					  data: newFormData,
					  success: saveSuccessFunction,
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
	
	$('#viewloanproducts').click(function(e) {
		refreshLoanProductsView();
	    e.preventDefault();
	});
	
	$('#addloanproduct').click(function(e) {
		var getUrl = 'http://localhost:8085/mifosng-provider/api/v1/loanproducts/template';
		var postUrl = 'http://localhost:8085/mifosng-provider/api/v1/loanproducts';
		//var url = '${rootContext}portfolio/product/loan/new';
		var templateSelector = "#productFormTemplate";
		var width = 800; 
		var height = 550;
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			  $("#dialog-form").dialog("close");
			  refreshLoanProductsView();
		}
		
		popupDialogWithFormView(getUrl, postUrl, "dialog.title.add.loan.product", templateSelector, width, height, saveSuccessFunction);
		e.preventDefault();
	});
		
	function refreshLoanProductsView() {
		var jqxhr = $.ajax({
			  url: '${allLoanProductsUrl}',
			  type: 'GET',
			  contentType: 'application/json',
			  dataType: 'json',
			  cache: false,
			  success: function(data, textStatus, jqXHR) {
				
				var productlistParent = new Object();
				productlistParent.items = data;
				
				var productListHtml = $("#productListTemplate").render(productlistParent);
				$("#listplaceholder").html(productListHtml);
				
				$("a.editproduct").click( function(e) {
					var linkId = this.id;
					var productId = linkId.replace("editproduct", "");
					var url = '${rootContext}portfolio/product/loan/' + productId;
					
					var templateSelector = "#productFormTemplate";
					var width = 800; 
					var height = 550;
					
					var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  $("#dialog-form").dialog("close");
						  refreshLoanProductsView();
					}
					
					popupDialogWithFormView(url, "dialog.title.product.details", templateSelector, width, height, saveSuccessFunction);
					e.preventDefault();
				});
				
				$("a.deactivateproduct").click( function(e) {
					var linkId = this.id;
					var productId = linkId.replace("deactivateproduct", "");
					var url = '${rootContext}portfolio/product/loan/' + productId;
					showNotAvailableDialog('dialog.title.functionality.not.available');
					
					e.preventDefault();
				});
				
				$("a.deleteproduct").click( function(e) {
					var linkId = this.id;
					var productId = linkId.replace("deleteproduct", "");
					var url = '${rootContext}portfolio/product/loan/' + productId;
					showNotAvailableDialog('dialog.title.functionality.not.available');
					
					e.preventDefault();
				});
				
				var oTable = $("#productstable").dataTable( {
					"bSort": true,
					"bInfo": true,
					"bJQueryUI": true,
					"bRetrieve": false,
					"bScrollCollapse": false,
					"bPaginate": false,
					"bLengthChange": false,
					"bFilter": false,
					"bAutoWidth": false,
				});
			  }
		});
		
		jqxhr.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}

	$('#viewoffices').click(function(e) {
		refreshOfficesView();
	    e.preventDefault();
	});
	
	$('#addoffice').click(function(e) {
		var getUrl = 'http://localhost:8085/mifosng-provider/api/v1/offices/template';
		var postUrl = 'http://localhost:8085/mifosng-provider/api/v1/offices';
		var templateSelector = "#officeFormTemplate";
		var width = 600; 
		var height = 400;
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			  $("#dialog-form").dialog("close");
			  refreshOfficesView();
		}
		
		popupDialogWithFormView(url, "dialog.title.add.office", templateSelector, width, height, saveSuccessFunction);
		e.preventDefault();
	});
		
	function refreshOfficesView() {
		var jqxhr = $.ajax({
			  url: 'http://localhost:8085/mifosng-provider/api/v1/offices', // '${allOfficesUrl}',
			  type: 'GET',
			  contentType: 'application/json',
			  dataType: 'json',
			  success: function(data, textStatus, jqXHR) {
				
				$.each(data.offices, function(i, item) {
					//alert(item.name);
				});
				console.log(data);  
				var officelistParent = new Object();
				officelistParent.items = data.offices;
				
				var officeListHtml = $("#officeListTemplate").render(officelistParent);
				$("#listplaceholder").html(officeListHtml);  
				
				$("a.edit").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("edit", "");
					var url = '${rootContext}org/office/' + entityId;
					var templateSelector = "#officeFormTemplate";
					var width = 600; 
					var height = 400;
					
					var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  $("#dialog-form").dialog("close");
						  refreshOfficesView();
					}
					
					popupDialogWithFormView(url, "dialog.title.office.details", templateSelector, width, height, saveSuccessFunction);
					e.preventDefault();
				});
				
				$("a.delete").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("delete", "");
					var url = '${rootContext}org/office/' + entityId;
					showNotAvailableDialog('dialog.title.functionality.not.available');
					
					e.preventDefault();
				});
				
				var oTable = $("#officestable").dataTable( {
					"bSort": true,
					"bInfo": true,
					"bJQueryUI": true,
					"bRetrieve": false,
					"bScrollCollapse": false,
					"bPaginate": false,
					"bLengthChange": false,
					"bFilter": false,
					"bAutoWidth": false,
					
				} );
			  }
		});
		
		jqxhr.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}
		
    // currency configuration
	$('#editconfiguration').click(function(e) {
		var url = '${rootContext}org/configuration/edit';
		var templateSelector = "#configurationFormTemplate";
		var width = 900; 
		var height = 400;
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			  $("#dialog-form").dialog("close");
		}
		
		popupDialogWithFormView(url, "dialog.title.configuration.currencies", templateSelector, width, height, saveSuccessFunction);
		e.preventDefault();
	});

    // end of document.ready
});
</script>
</head>

<body>
	<div id="container">
	<jsp:include page="../top-navigation.jsp" />
	
		<div style="float:none; clear:both;">
		<div id="spacer" style="line-height: 25px;">&nbsp;</div>
			<div id="content">
				<div>
					<span style="float: left">
					    <a href="#" id="viewloanproducts"><spring:message code="administration.link.view.products"/></a>
					    |
					    <a href="#" id="addloanproduct"><spring:message code="administration.link.add.product"/></a>
					    |
					    <a href="#" id="viewoffices"><spring:message code="administration.link.view.offices"/></a>
					    |
					    <a href="#" id="addoffice"><spring:message code="administration.link.add.office"/></a>
					    |
					    <a href="#" id="editconfiguration"><spring:message code="administration.link.currency.configuration"/></a>
					</span>
				</div>
			</div>		
		</div>
		
		<div id="listplaceholder" style="clear: both; margin-top: 20px;"></div>
	</div>
	
	<jsp:include page="../administration-view-templates.jsp" />
</body>
</html>