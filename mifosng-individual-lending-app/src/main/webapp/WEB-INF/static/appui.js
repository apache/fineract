(function($) {

	$.appui = {};
	
	$.views.registerHelpers({
		decimal: function(number, digits) {
	      try {
	        return number.toFixed(digits);
	      } catch(e) {
	        return number +"(NaN)";
	      }
	    },
		number: function(number) {
	      try {
	        return number.toFixed(0);
	      } catch(e) {
	        return number +"(NaN)";
	      }
	    },
	    json: function(obj) {
		      try {
		        return JSON.stringify(obj);
		      } catch(e) {
		        return "" + e;
		      }
		},
		localDate: function(dateParts) {
		      try {
		    	
		    	  var year = dateParts[0];
		    	  var month = parseInt(dateParts[1]) - 1; // month is zero indexed
		    	  var day = dateParts[2];
		    	  
		    	  var d = new Date();
		    	  d.setFullYear(year,month,day);
		    	  
		          return d.toDateString();
		      } catch(e) {
		        return "??";
		      }
		},
		toISODate: function(dateParts) {
		      try {
		    	
		    	  var year = dateParts[0];
		    	  var month = parseInt(dateParts[1]);
		    	  var day = parseInt(dateParts[2]);
		    	  
		    	  var monthStr = '' + month;
		    	  if (month < 10) {
		    		  monthStr = '0' + month;
		    	  }
		    	  var dayStr  = '' + day;
		    	  if (day < 10) {
		    		  dayStr = '0' + day;
		    	  }
		    	  
		          return year + '-' + monthStr + '-' + dayStr;
		      } catch(e) {
		        return "??";
		      }
		}
	});
	
function handleXhrError(jqXHR, textStatus, errorThrown, templateSelector, placeholderDiv) {
  	if (jqXHR.status === 0) {
	    alert('Not connect.\n Verify Network.');
	} else if (jqXHR.status == 404) {
	    alert('Requested page not found. [404]');
	} else if (jqXHR.status == 500) {
	    alert('Internal Server Error [500].');
	} else if (errorThrown === 'parsererror') {
	    alert('Requested JSON parse failed.');
	} else if (errorThrown === 'timeout') {
	    alert('Time out error.');
	} else if (errorThrown === 'abort') {
	    alert('Ajax request aborted.');
	} else {
		// remove error class from all input fields
		var $inputs = $('#entityform :input');
		
	    $inputs.each(function() {
	        $(this).removeClass("ui-state-error");
	    });
		
	  	$(placeholderDiv).html("");
	  	
	  	var jsonErrors = JSON.parse(jqXHR.responseText);
	  	
	  	var errorArray = new Array();
	  	var arrayIndex = 0;
	  	$.each(jsonErrors, function() {
	  	  var fieldId = '#' + this.field;
	  	  $(fieldId).addClass("ui-state-error");
	  	  
	  	  var errorObj = new Object();
	  	  errorObj.field = this.field;
	  	  errorObj.code = this.code;
	  	  errorObj.message = this.code;
	  	  errorObj.value = this.value;
	  	  
	  	  errorArray[arrayIndex] = errorObj;
	  	  arrayIndex++
	  	});
	  	
	  	var templateArray = new Array();
	  	var templateErrorObj = new Object();
	  	templateErrorObj.title = "You have the following errors:";
	  	templateErrorObj.errors = errorArray;
	  	
	  	templateArray[0] = templateErrorObj;
	  	
	  	var formErrorsHtml = $(templateSelector).render(templateArray);
	  	
	  	$(placeholderDiv).append(formErrorsHtml);
	}
}
	
function popupDialogWithFormView(url, title, templateSelector, width, height, successFunction) {
	  var dialogDiv = $("<div id='dialog-form'></div>");
	  var jqxhr = $.ajax({
		  url: url,
		  type: 'GET',
		  contentType: 'application/json',
		  dataType: 'json',
		  success: function(data, textStatus, jqXHR) {
			
			var formHtml = $(templateSelector).render(data);
			
			dialogDiv.append(formHtml);
			
			dialogDiv.dialog({
			  		title: title, 
			  		width: width, 
			  		height: height, 
			  		modal: true,
			  		buttons: {
			  			"Save": function() {
		  			    	$('.multiSelectedItems option').each(function(i) {  
		  			    	   $(this).attr("selected", "selected");  
		  			    	});
			  				
			  				 var form_data = $('#entityform').serialize();
			  				 
							 var jqxhr = $.ajax({
								  url: url,
								  type: 'POST',
								  data: form_data,
								  success: successFunction,
								  error: function(jqXHR, textStatus, errorThrown) {
								    handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
								  }
							 });
						},				  			
			  			"Cancel": function() {
							$(this).dialog( "close" );
						}
						// end of buttons
			  		},
			  		close: function() {
			  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
			  			$(this).remove();
					},
			  		open: function (event, ui) {
			  			$('.multiadd').click(function() {  
			  			     return !$('.multiNotSelectedItems option:selected').remove().appendTo('#selectedItems');  
			  			});
			  			
			  			$('.multiremove').click(function() {  
			  				return !$('.multiSelectedItems option:selected').remove().appendTo('#notSelectedItems');  
			  			});
			  			
			  			$('.datepickerfield').datepicker({constrainInput: true, maxDate: '0', dateFormat: 'yy-mm-dd'});
			  		}
			  	}).dialog('open');
		  }
	  });
}
	
})(jQuery);