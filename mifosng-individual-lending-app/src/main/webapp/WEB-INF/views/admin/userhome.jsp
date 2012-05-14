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
	
	<c:url value="/admin/user/all" var="allUsersUrl" />
	<c:url value="/admin/role/all" var="allRolesUrl" />
	<c:url value="/admin/permission/all" var="allPermissionsUrl" />
	<c:url value="/" var="rootContext" />
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
		  	var valErrors = jsonErrors.errors;
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
	
	function displayListView(listUrl, templateSelector, displayAreaDivSelector, singleEntityPrefixUrl, singleEntityTemplateSelector, saveSuccessFunction) {
		
		var jqxhr = $.ajax({
			  url: listUrl,
			  type: 'GET',
			  contentType: 'application/json',
			  dataType: 'json',
			  cache: false,
			  success: function(data, textStatus, jqXHR) {
				console.log(data);
				var tableHtml = $(templateSelector).render(data);
				
				$(displayAreaDivSelector).html("");
				$(displayAreaDivSelector).html(tableHtml);
				
				$("a.edit").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("edit", "");
					var url = singleEntityPrefixUrl + entityId;
					
					var width = 1000; 
					var height = 550;
					popupDialogWithFormView(url, url, 'PUT', 'dialog.title.edit.details', singleEntityTemplateSelector, width, height, saveSuccessFunction);
					
					e.preventDefault();
				});
				
				$("a.delete").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("delete", "");
					var url = singleEntityPrefixUrl + entityId;
					
					var jqxhr = $.ajax({
						  url: url,
						  type: 'DELETE',
						  contentType: 'application/json',
						  dataType: 'json',
						  success: function(data, textStatus, jqXHR) {
							  $('#listusers').click();
						  }
					 });
					
					jqxhr.error(function(jqXHR, textStatus, errorThrown) {
						handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
					});
					
					e.preventDefault();
				});
				
				var oTable = $("#entitytable").dataTable({
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
	
	$('#listusers').click(function(e) {
		var listUrl = "http://localhost:8080/mifosng-provider/api/v1/users";
		refreshUsersView();		
	    e.preventDefault();
	});
	
	$('#adduser').click(function(e) {
		var url = 'http://localhost:8080/mifosng-provider/api/v1/users/template';
		var postUrl = "http://localhost:8080/mifosng-provider/api/v1/users";
		var templateSelector = "#userFormTemplate";
		var width = 1000; 
		var height = 550;
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
		  	$("#dialog-form").dialog("close");
		  	refreshUsersView();
		}
		
		popupDialogWithFormView(url, postUrl, 'POST', 'dialog.title.add.user', templateSelector, width, height, saveSuccessFunction);
	    e.preventDefault();
	});
	
	$('#listroles').click(function(e) {
		var listUrl = "${allRolesUrl}";
		var templateSelector = "#roleListTemplate";
		var displayAreaDivSelector = "#contentplaceholder";
		var singleEntityPrefixUrl = '${rootContext}admin/role/';
		var singleEntityTemplateSelector = "#roleFormTemplate";
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
		  	
			$("#dialog-form").dialog("close");
		  	
			var listUrl = "${allRolesUrl}";
			var templateSelector = "#roleListTemplate";
			var displayAreaDivSelector = "#contentplaceholder";
			var singleEntityPrefixUrl = '${rootContext}admin/role/';
			var singleEntityTemplateSelector = "#roleFormTemplate";
			
			displayListView(listUrl, templateSelector, displayAreaDivSelector, singleEntityPrefixUrl, singleEntityTemplateSelector);
		}
		
		displayListView(listUrl, templateSelector, displayAreaDivSelector, singleEntityPrefixUrl, singleEntityTemplateSelector, saveSuccessFunction);
		
	    e.preventDefault();
	});
	
	$('#addrole').click(function(e) {
		var url = '${rootContext}admin/role/new';
		var templateSelector = "#roleFormTemplate";
		var width = 1000; 
		var height = 550;
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
		  	
			$("#dialog-form").dialog("close");
		  	
			var listUrl = "${allRolesUrl}";
			var templateSelector = "#roleListTemplate";
			var displayAreaDivSelector = "#contentplaceholder";
			var singleEntityPrefixUrl = '${rootContext}admin/role/';
			var singleEntityTemplateSelector = "#roleFormTemplate";
			
			displayListView(listUrl, templateSelector, displayAreaDivSelector, singleEntityPrefixUrl, singleEntityTemplateSelector);
		}
	
		popupDialogWithFormView(url, 'dialog.title.add.role', templateSelector, width, height, saveSuccessFunction);
	    e.preventDefault();
	});
	
	$('#listpermissions').click(function(e) {
		var listUrl = "${allPermissionsUrl}";
		var templateSelector = "#permissionListTemplate";
		var displayAreaDivSelector = "#contentplaceholder";
		var singleEntityPrefixUrl = '${rootContext}admin/permission/';
		var singleEntityTemplateSelector = "#roleFormTemplate";
		
		displayListView(listUrl, templateSelector, displayAreaDivSelector, singleEntityPrefixUrl, singleEntityTemplateSelector);
		
	    e.preventDefault();
	});
	
	function refreshUsersView() {
		
		var listUrl = 'http://localhost:8080/mifosng-provider/api/v1/users';
		var templateSelector = "#usersListTemplate";
		var displayAreaDivSelector = "#contentplaceholder";
		var singleEntityPrefixUrl = 'http://localhost:8080/mifosng-provider/api/v1/users/';
		var singleEntityTemplateSelector = "#userFormTemplate";
		
		var jqxhr = $.ajax({
			  url: listUrl, 
			  type: 'GET',
			  contentType: 'application/json',
			  dataType: 'json',
			  success: function(data, textStatus, jqXHR) {
				console.log(data);  
				var officeListHtml = $(templateSelector).render(data);
				$(displayAreaDivSelector).html(officeListHtml);  
				
				$("a.edit").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("edit", "");
					var getUrl = 'http://localhost:8080/mifosng-provider/api/v1/users/' + entityId;
					var putUrl = 'http://localhost:8080/mifosng-provider/api/v1/users/' + entityId;
					
					var templateSelector = "#userFormTemplate";
					var width = 600; 
					var height = 400;
					
					var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  $("#dialog-form").dialog("close");
						  refreshUsersView();
					}
					
					popupDialogWithFormView(getUrl, putUrl, 'PUT', "dialog.title.edit.details", templateSelector, width, height, saveSuccessFunction);
					e.preventDefault();
				});
				
				$("a.delete").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("delete", "");
					var url = 'http://localhost:8080/mifosng-provider/api/v1/users/' + entityId;
					showNotAvailableDialog('dialog.title.functionality.not.available');
					
					e.preventDefault();
				});
				
				var oTable = $("#entitytable").dataTable( {
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
				    <a href="#" id="listusers"><spring:message code="administration.link.view.users"/></a>
				    |
				    <a href="#" id="adduser"><spring:message code="administration.link.add.user"/></a>
				    |
				    <a href="#" id="listroles"><spring:message code="administration.link.view.roles"/></a>
				    |
				    <a href="#" id="addrole"><spring:message code="administration.link.add.role"/></a>
				    |
				    <a href="#" id="listpermissions"><spring:message code="administration.link.view.permissions"/></a>
				</span>
			</div>
		</div>
	</div>
	
	<div id="contentplaceholder" style="clear: both; margin-top: 20px;"></div>
</div>

<jsp:include page="../administration-view-templates.jsp" />
</body>
</html>