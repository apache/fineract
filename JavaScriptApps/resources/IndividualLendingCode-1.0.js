
saveSuccessFunctionReloadClient =  function(data, textStatus, jqXHR) {
						  	$("#dialog-form").dialog("close");
		  					showILClient(currentClientId );
				  		};

formErrorFunction = function(jqXHR, textStatus, errorThrown) {
				    	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
				};





function executeAjaxRequest(url, verbType, jsonData, successFunction, errorFunction) { 

	var jqxhr = $.ajax({ 
				url : baseApiUrl + url, 
				type : verbType, //POST, GET, PUT or DELETE 
				contentType : "application/json; charset=utf-8", 
				dataType : 'json', 
				data : jsonData, 
				cache : false, 
				beforeSend : function(xhr) { 
						xhr.setRequestHeader("Authorization", "Basic " + base64); 
					}, 
				success : successFunction, 
				error : errorFunction 
			}); 
}


// load html functions
function showMainContainer(containerDivName, username) {

	var htmlVar = '<div id="logowrapper">';
	htmlVar += '	<span style="float: left">';
	htmlVar += '		<img style="float:left; border: 0;" alt="" src="resources/mifos.jpg"/>';
	htmlVar += '	</span>';
	htmlVar += '</div>';
	htmlVar += '<div id="navwrapper">';
	htmlVar += '<ul id="nav" class="floatleft">';
	htmlVar += '	<li><a href="unknown.html" onclick="showILClientListing();return false;">' + doI18N("link.topnav.clients") + '</a></li>';
	htmlVar += '	<li><a href="unknown.html" onclick="showILUserAdmin();return false;">' + doI18N("link.topnav.users") + '</a></li>';
	htmlVar += '	<li><a href="unknown.html" onclick="showILOrgAdmin();return false;">' + doI18N("link.topnav.organisation") + '</a></li>';
	htmlVar += '	<li><a href="unknown.html" onclick="showILReporting();return false;">' + doI18N("link.reports") + '</a></li>';
	htmlVar += '</ul>';
	htmlVar += '<ul id="nav" class="floatright">';
	htmlVar += '	<li class="dmenu"><a href="unknown.html" onclick="return false;">' + doI18N("link.topnav.culture") + '</a>';
	htmlVar += '		<ul>';
	htmlVar += '			<li><a href="unknown.html" onclick="setCultureReshowListing(' + "'" + 'en' + "'" + ');return false;">en</a></li>';
	htmlVar += '			<li><a href="unknown.html" onclick="setCultureReshowListing(' + "'" + 'fr' + "'" + ');return false;">fr</a></li>';
	htmlVar += '			<li><a href="unknown.html" onclick="setCultureReshowListing(' + "'" + 'es' + "'" + ');return false;">es</a></li>';
	htmlVar += '			<li><a href="unknown.html" onclick="setCultureReshowListing(' + "'" + 'pt' + "'" + ');return false;">pt</a></li>';
	htmlVar += '			<li><a href="unknown.html" onclick="setCultureReshowListing(' + "'" + 'zh' + "'" + ');return false;">zh</a></li>';
	htmlVar += '		</ul>';
	htmlVar += '	</li>';
	htmlVar += '	<li><a href="unknown.html" onclick="showILAccountSettings();return false;" class="dmenu">' + currentUserName + '</a>';
	htmlVar += '		<ul>';
	htmlVar += '			<li><a href="unknown.html" onclick="showILAccountSettings();return false;">' + doI18N("link.topnav.account.settings") + '</a></li>';
	htmlVar += '		</ul>';
	htmlVar += '	</li>';
	htmlVar += '	<li><a href="unknown.html" onclick="signOut(' + "'" + containerDivName + "'" + ');return false;">' + doI18N("link.signout") + '</a></li>';
	htmlVar += '</ul>';
	htmlVar += '<br class="clear">';
	htmlVar += '</div><div style="float:none; clear:both;">';
	htmlVar += '	<div id="spacer" style="line-height: 15px;">&nbsp;</div>';
	htmlVar += '	<div id="content"></div>';
	htmlVar += '</div>';

	$("#" + containerDivName).html(htmlVar);
}


function showILLogon(logonDivName) {
	var htmlVar = '<div id=theLogonForm><img style="float:left; border: 0;" alt="" src="resources/mifos.jpg"/><div id=appTitle>' + doI18N("app.name") + '</div>';
	htmlVar += '<form name = "logonform"><table id=logonTable><tr><td>' + doI18N("login.username") + ':</td><td><input type="text" name="username"></td></tr>';
	htmlVar += '<tr><td>' + doI18N("login.password") + ': </td><td><input type="password" name="pwd"></td></tr>';
	htmlVar += '<tr><td><input type="button" value="Logon" name="Submit" ';
	htmlVar += 'onclick= "setBasicAuthKey(' + "'" + logonDivName + "'" + ', document.logonform.username.value, document.logonform.pwd.value )"></td><td></td></tr></table></form>';
	htmlVar += '<div id=formerrors></div></div>';

	$("#" + logonDivName).html(htmlVar);
}


function setClientListingContent(divName) {
	var htmlVar = '<button id="addclient" style="clear: both;">' + doI18N("link.add.new.client") + '</button>';
	htmlVar += '<div id="tabs"><ul><li><a href="#searchtab" title="searchtab">' + doI18N("tab.search") + '</a></li></ul><div id="searchtab"></div></div>';

	$("#" + divName).html(htmlVar);
}

function setClientContent(divName) {

	var htmlVar = '<div id="newtabs">	<ul><li><a href="nothing"'; 
	htmlVar += ' title="clienttab" class="topleveltab"><span id="clienttabname">' + doI18N("app.loading") + '</span></a></li></ul><div id="clienttab"></div></div>';
	$("#" + divName).html(htmlVar);
}


function setAddLoanContent(divName) {

	var htmlVar = '<div id="inputarea"></div><div id="schedulearea"></div>'
	$("#" + divName).html(htmlVar);
}


function setOrgAdminContent(divName) {

	var addLoanProductUrl = "maintainLoanProduct('loanproducts/template', 'loanproducts', 'POST', 'dialog.title.add.loan.product');return false;";
	var addOfficeUrl = "maintainOffice('offices/template', 'offices', 'POST', 'dialog.title.add.office');return false;";
	var orgCurrenciesUrl = "maintainOrgCurrencies('configurations/currency', 'configurations/currency', 'PUT', 'dialog.title.configuration.currencies');return false;";
	var htmlVar = '<div id="inputarea"></div><div id="schedulearea"></div>'

	var htmlVar = '<div>';
	htmlVar += '<span style="float: left">';
	htmlVar += '	<a href="unknown.html" onclick="refreshLoanProductsView();return false;" id="viewloanproducts">' + doI18N("administration.link.view.products") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="' + addLoanProductUrl + '" id="addloanproduct">' + doI18N("administration.link.add.product") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="refreshOfficesView();return false;" id="viewoffices">' + doI18N("administration.link.view.offices") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="' + addOfficeUrl + '" id="addoffice">' + doI18N("administration.link.add.office") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="' + orgCurrenciesUrl + '" id="editconfiguration">' + doI18N("administration.link.currency.configuration") + '</a>';
	htmlVar += '</span>';
	htmlVar += '</div>';
	htmlVar += '<br><br>';
	htmlVar += '<div id="listplaceholder" ></div>';
	$("#" + divName).html(htmlVar);
}


function setUserAdminContent(divName) {

	var addUserUrl = "maintainUser('users/template', 'users', 'POST', 'dialog.title.add.user');return false;";
	var addRoleUrl = "maintainRole('roles/template', 'roles', 'POST', 'dialog.title.add.role');return false;";
	var htmlVar = '<div id="inputarea"></div><div id="schedulearea"></div>'

	var htmlVar = '<div>';
	htmlVar += '<span style="float: left">';
	htmlVar += '	<a href="unknown.html" onclick="refreshUsersView();return false;" id="listusers">' + doI18N("administration.link.view.users") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="' + addUserUrl + '" id="adduser">' + doI18N("administration.link.add.user") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="refreshRolesView();return false;" id="listroles">' + doI18N("administration.link.view.roles") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="' + addRoleUrl + '" id="addrole">' + doI18N("administration.link.add.role") + '</a>';
	htmlVar += ' | ';
	htmlVar += '	<a href="unknown.html" onclick="refreshPermissionsView();return false;" id="listpermissions">' + doI18N("administration.link.view.permissions") + '</a>';
	htmlVar += '</span>';
	htmlVar += '</div>';
	htmlVar += '<br><br>';
	htmlVar += '<div id="contentplaceholder" ></div>';
	$("#" + divName).html(htmlVar);
}


function setReportingContent(divName) {

	var htmlVar = '<table id=toptable>';
 	htmlVar += '<tr>';
 	htmlVar += '  <td valign="top"><div id=myListOfReports></div></td>';
 	htmlVar += '  <td valign="bottom"><div id=myInputParameters></div></td>';
 	htmlVar += '  <td valign="top"><div id=myRunReportButton></div></td>';
 	htmlVar += '  <td valign="top"><div id=myClearReportButton></div></td>';
 	htmlVar += '  <td valign="bottom">';
 	htmlVar += '		<select id=decimalsChoice onChange="selectNewDecimals(options[selectedIndex].value)" >';
 	htmlVar += '		<option value="" selected="selected">' + doI18N("reporting.decimals") + '</option>';
 	htmlVar += '		<option value="4">4</option>';
 	htmlVar += '		<option value="3">3</option>';
 	htmlVar += '		<option value="2">2</option>';
 	htmlVar += '		<option value="1">1</option>';
 	htmlVar += '		<option value="0">0</option>';
 	htmlVar += '		<option value="-1">-1</option>';
 	htmlVar += '		<option value="-2">-2</option>';
 	htmlVar += '		<option value="-3">-3</option>';
 	htmlVar += '		</select>';
 	htmlVar += '   </td>';
 	htmlVar += '  <td valign="bottom">';
 	htmlVar += '		<select id=decimalsThousandsSep onChange="selectNewThousandsSep(options[selectedIndex].value)" >';
 	htmlVar += '		<option value="" selected="selected">' + doI18N("reporting.format") + '</option>';
 	htmlVar += '		<option value=",.">1,234,567.89</option>';
 	htmlVar += '		<option value=".,">1.234.567,89</option>';
 	htmlVar += '		<option value=" ,">1 234 567,89</option>';
 	htmlVar += '		<option value=" .">1 234 567.89</option>';
 	htmlVar += '		<option value=".' + "'" + '">1.234.567' + "'" + '89</option>';
 	htmlVar += '		<option value="' + "'" + ',">1'+ "'" + '234' + "'" + '567,89</option>';
 	htmlVar += '		<option value="INDIAN">Indian 12,34,567.89</option>';
 	htmlVar += '		<option value="NONE">None 1234567.89</option>';
 	htmlVar += '		</select>';
 	htmlVar += '   </td>';
 	htmlVar += ' </tr>';
 	htmlVar += '</table>';
 	htmlVar += '<div id=myOutput></div>'; 

	$("#" + divName).html(htmlVar);
}

function setAccountSettingsContent(divName) {

	var htmlVar = '<div id="tabs">';
	htmlVar += '	<ul>';
	htmlVar += '		<li><a href="#settingstab" title="settings">' + doI18N("tab.settings") + '</a></li>';
	htmlVar += '	</ul>';
	htmlVar += '	<div id="settings"></div>';
	htmlVar += '</div>';

	$("#" + divName).html(htmlVar);
}




//all the code for the various functions

function showILClientListing() {

setClientListingContent("content");

//HOME list clients functionality
	$("#tabs").tabs({
	    select: function(event, ui) {
	    	console.log("selected..");
	    },
	    load: function(event, ui) {
	    	console.log("load..");
	    },
	    show: function(event, ui) {
	    	console.log("show..");
		var successFunction =  function(data) {
				  			var clientObject = new Object();
			        			clientObject.clients = data;
			        			console.log(clientObject);
			        	
				    			var tableHtml = $("#clientSearchTabTemplate").render(clientObject);
							$("#searchtab").html(tableHtml);	
				  		};

  		executeAjaxRequest('clients', 'GET', "", successFunction, formErrorFunction);
	    }
	});
	


	var addClientSuccessFunction = function(data, textStatus, jqXHR) {
		  $('#dialog-form').dialog("close");
		  showILClient(data.entityId);
	}
	$("#addclient").button().click(function(e) {
		var getUrl = 'clients/template';
		var postUrl = 'clients';
		var templateSelector = "#clientFormTemplate";
		var width = 600; 
		var height = 350;
		
		popupDialogWithFormView(getUrl, postUrl, 'POST', 'dialog.title.add.client', templateSelector, width, height, addClientSuccessFunction);
		
	    e.preventDefault();
	});
	
}	





function showILClient(clientId) {
	var clientUrl = 'clients/' + clientId
	setClientContent("content");
	$newtabs = $("#newtabs").tabs({
		"add": function( event, ui ) {
			$newtabs.tabs('select', '#' + ui.panel.id);
		}

	});

	var errorFunction = function(jqXHR, status, errorThrown, index, anchor) {
	        	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	            $(anchor.hash).html("error occured while ajax loading.");
	        };

	var successFunction = function(data, status, xhr) {
	        		currentClientId = clientId;
				clientDirty = false; //not coded but intended to refresh client if some date on its display has changed e.g. loan status
	        		var currentTabIndex = $newtabs.tabs('option', 'selected');
	            	var currentTabAnchor = $newtabs.data('tabs').anchors[currentTabIndex];
	            
	        		var tableHtml = $("#clientDataTabTemplate").render(data);
					$("#clienttab").html(tableHtml);
					$("#clienttabname").html(data.displayName);
					
					// retrieve accounts summary info
					refreshLoanSummaryInfo(clientUrl);
					
					// bind click listeners to buttons.

					/* cashflow not in the general app
					$('.casflowbtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("cashflowbtn", "");
						var url = '${rootContext}portfolio/client/' + clientId + '/cashflow/new';
						window.location.href = url;
					    e.preventDefault();
					});
					
					$('button.casflowbtn span').text(doI18N('dialog.button.new.cashflow.analysis'));
					*/

					$('.newloanbtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("newloanbtn", "");
						addILLoan(clientId);
					    e.preventDefault();
					});
					$('button.newloanbtn span').text(doI18N('dialog.button.new.loan.application'));
					
					$('.addnotebtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("addnotebtn", "");
						var postUrl = 'clients/' + clientId + '/notes';
						var templateSelector = "#noteFormTemplate";
						var width = 600; 
						var height = 400;
						
						var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  	$("#dialog-form").dialog("close");
						  	refreshNoteWidget('clients/' + clientId);
						}
						
						popupDialogWithFormView("", postUrl, 'POST', "dialog.title.add.note", templateSelector, width, height,  saveSuccessFunction);
					    e.preventDefault();
					});
					$('button.addnotebtn span').text(doI18N('dialog.button.add.note'));

					refreshNoteWidget(clientUrl);
					
					// retrieve additional info
					var additionalFieldsParams = {
							url: baseApiUrl,
							basicAuthKey: base64,
							datasetType: "portfolio_client",
							datasetPKValue: data.id,
							datasetTypeDiv: "clientadditionaldata", 
							headingPrefix: "", 
							headingClass: "", 
							labelClass: "longrowlabel",
							valueClass:	"rowvalue",
							editLabel: doI18N("link.edit"),	
							saveLabel: doI18N("dialog.button.save"),	
							cancelLabel: doI18N("dialog.button.cancel")				
					};
					jQuery.stretchyData.displayAllExtraData(additionalFieldsParams);
	        };
	    
		executeAjaxRequest(clientUrl, 'GET', "", successFunction, errorFunction);	  

}
	


	// function to retrieve and display loan summary information in it placeholder
	function refreshLoanSummaryInfo(clientUrl) {

		var successFunction =  function(data, textStatus, jqXHR) {
				  			var tableHtml = $("#clientAccountSummariesTemplate").render(data);
				  			$("#clientaccountssummary").html(tableHtml);
			  			}
  		executeAjaxRequest(clientUrl + '/loans', 'GET', "", successFunction, formErrorFunction);	  	
	}
	


	function refreshNoteWidget(clientUrl) {
			  	
		eval(genRefreshNoteWidgetSuccessVar(clientUrl));
  		executeAjaxRequest(clientUrl + '/notes', 'GET', "", successFunction, formErrorFunction);	  
	}
	function genRefreshNoteWidgetSuccessVar(clientUrl) {

		return 'var successFunction = function(data, textStatus, jqXHR) {	' +
				  ' var noteParent = new Object();' + 
				  ' noteParent.title = doI18N("widget.notes.heading");' +
				  ' noteParent.notes = data;' +
				  ' var tableHtml = $("#noteListViewTemplate").render(noteParent);' +
				  ' $("#clienttabrightpane").html(tableHtml);' + 
				  ' $(".editclientnote").click(function(e) { ' +
						' var linkId = this.id;' +
						' var noteId = linkId.replace("editclientnotelink", "");' +
						' var getAndPutUrl = "' + clientUrl + '/notes/" + noteId;' +
						' var templateSelector = "#noteFormTemplate";' +
						' var width = 600;' +
						' var height = 400;' +
						' var saveSuccessFunction = function(data, textStatus, jqXHR) {' +
						  	' $("#dialog-form").dialog("close");' +
						  	' refreshNoteWidget("' + clientUrl + '");' +
						' };' +
						' popupDialogWithFormView(getAndPutUrl, getAndPutUrl, "PUT", "dialog.title.edit.note", templateSelector, width, height,  saveSuccessFunction);' +
					    ' e.preventDefault();' +
			      ' });' +
			  ' };'
	}

	
	function addILLoan(clientId) {
		setAddLoanContent("content");

		eval(genAddLoanSuccessVar(clientId));

  		executeAjaxRequest('loans/template?clientId=' + clientId, 'GET', "", successFunction, formErrorFunction);	  
	}
	function genAddLoanSuccessVar(clientId) {

		return 'var successFunction = function(data, textStatus, jqXHR) { ' +
				' var formHtml = $("#newLoanFormTemplateMin").render(data);' +
				' $("#inputarea").html(formHtml);' +
				' $("#productId").change(function() {' +
					' var productId = $("#productId").val();' +
					' repopulateFullForm(' + clientId + ', productId);' +
				' });' +
			' };'

	}

	function genSaveSuccessFunctionReloadLoan(loanId) {

		return 'var saveSuccessFunctionReloadLoan = function(data, textStatus, jqXHR) { ' + 
						  	' $("#dialog-form").dialog("close");' +
							' loadILLoan(' + loanId + ');' +
							' clientDirty = true;' +
						'};';
	}




	function repopulateFullForm(clientId, productId) {
				
		successFunction =  function(data, textStatus, jqXHR) {
			
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
				$('#principal').change(function() {
					calculateLoanSchedule();
				});
				$('#repaymentEvery').change(function() {
					calculateLoanSchedule();
				});
				$('#repaymentFrequencyType').change(function() {
					calculateLoanSchedule();
				});
				$('#numberOfRepayments').change(function() {
					calculateLoanSchedule();
				});
				$('#expectedDisbursementDate').change(function() {
					calculateLoanSchedule();
				});
				$('#repaymentsStartingFromDate').change(function() {
					calculateLoanSchedule();
				});
				$('#interestRatePerPeriod').change(function() {
					calculateAnnualPercentageRate();
					calculateLoanSchedule();
				});
				$('#interestRateFrequencyType').change(function() {
					calculateAnnualPercentageRate();
					calculateLoanSchedule();
				});
				$('#amortizationType').change(function() {
					calculateLoanSchedule();
				});
				$('#interestType').change(function() {
					calculateLoanSchedule();
				});
				$('#interestCalculationPeriodType').change(function() {
					calculateLoanSchedule();
				});
				$('#interestChargedFromDate').change(function() {
					calculateLoanSchedule();
				});
				$('#submitloanapp').button().click(function(e) {
					submitLoanApplication(clientId);
				    e.preventDefault();
				});
				$('button#submitloanapp span').text(doI18N('dialog.button.submit'));
				
				$('#cancelloanapp').button().click(function(e) {
		  			showILClient(clientId);
				    e.preventDefault();
				});
				$('button#cancelloanapp span').text(doI18N('dialog.button.cancel'));
			};
			  		
		executeAjaxRequest('loans/template?clientId=' + clientId + '&productId=' + productId, 'GET', "", successFunction, formErrorFunction);	  

	}
	

	function calculateAnnualPercentageRate() {
	//	alert('calculating interest');
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

	function calculateLoanSchedule() {
		
		var newFormData = JSON.stringify($('#entityform').serializeObject());
    	
		var successFunction = function(data, textStatus, jqXHR) {
				  		removeErrors("#formerrors");
				  		var loanScheduleHtml = $("#newLoanScheduleTemplate").render(data);
				  		$("#schedulearea").html(loanScheduleHtml);
			  		};
		
		var errorFunction = function(jqXHR, textStatus, errorThrown) {
						 $("#schedulearea").html("");
						handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
					};
		executeAjaxRequest('loans?command=calculateLoanSchedule', "POST", newFormData, successFunction, errorFunction);	  
	}


	function submitLoanApplication(clientId) {
		
		var newFormData = JSON.stringify($('#entityform').serializeObject());
    	
		var successFunction =  function(data, textStatus, jqXHR) {
		  				showILClient(clientId);
			  };
		
		executeAjaxRequest('loans', "POST", newFormData, successFunction, formErrorFunction);	  

	}


function showILLoan(loanId, product) {
	var title = product + ": #" + loanId ;			    
	$newtabs.tabs( "add", "no url", title);
	loadILLoan(loanId);
}


function loadILLoan(loanId) {

	var loanUrl = 'loans/' + loanId + "?associations=ALL";

	var errorFunction = function(jqXHR, status, errorThrown, index, anchor) {
	        	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	            $(anchor.hash).html("error occured while ajax loading.");
	        };

	var successFunction = function(data, status, xhr) {
	        	
	        		var currentTabIndex = $newtabs.tabs('option', 'selected');
	            	var currentTabAnchor = $newtabs.data('tabs').anchors[currentTabIndex];
	            
	            	var offsetToSubmittedDate = 0;
	            	var offsetToApprovalDate = 0;
	            	var offsetToDisbursalDate = 0;
				var maxOffset = 0; // today


	        		var tableHtml = $("#loanDataTabTemplate").render(data);
	        		
	        		var currentTab = $("#newtabs").children(".ui-tabs-panel").not(".ui-tabs-hide");
	        		currentTab.html(tableHtml);

	        		var curTabID = currentTab.prop("id")
	        		
	        		offsetToSubmittedDate = data.convenienceData.maxSubmittedOnOffsetFromToday;
	        		offsetToApprovalDate = data.convenienceData.maxApprovedOnOffsetFromToday;
	        		offsetToDisbursalDate = data.convenienceData.maxDisbursedOnOffsetFromToday;
	        		
	        		var $loantabs = $(".loantabs").tabs({
						"show": function(event, ui) {
							var curTab = $('#newtabs .ui-tabs-panel:not(.ui-tabs-hide)');
			      			var curTabID = curTab.prop("id")
						}
					});
	        		
	        		$('.rejectloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("rejectbtn", "");
						var postUrl = 'loans/' + loanId + '?command=reject';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;

						popupDialogWithPostOnlyFormView(postUrl, 'POST', 'dialog.title.reject.loan', templateSelector, width, height, saveSuccessFunctionReloadClient, offsetToSubmittedDate, defaultOffset, maxOffset);
					    e.preventDefault();
					});
	        		$('button.rejectloan span').text(doI18N('dialog.button.reject.loan'));
					
				$('.withdrawnbyapplicantloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("withdrawnbyapplicantloanbtn", "");
						var postUrl = 'loans/' + loanId + '?command=withdrewbyclient';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(postUrl, 'POST', 'dialog.title.loan.withdrawn.by.client', templateSelector, width, height, saveSuccessFunctionReloadClient,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
				});
				$('button.withdrawnbyapplicantloan span').text(doI18N('dialog.button.withdrawn.by.client.loan'));
					
				$('.approveloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("approvebtn", "");
						var postUrl = 'loans/' + loanId + '?command=approve';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						eval(genSaveSuccessFunctionReloadLoan(loanId));
						popupDialogWithPostOnlyFormView(postUrl, 'POST', 'dialog.title.approve.loan', templateSelector, width, height, saveSuccessFunctionReloadLoan,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
				});
				$('button.approveloan span').text(doI18N('dialog.button.approve.loan'));
					
				$('.undoapproveloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("undoapprovebtn", "");
						var postUrl = 'loans/' + loanId + '?command=undoapproval';
						var templateSelector = "#undoStateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						eval(genSaveSuccessFunctionReloadLoan(loanId));
						popupDialogWithPostOnlyFormView(postUrl, 'POST', 'dialog.title.undo.loan.approval', templateSelector, width, height, saveSuccessFunctionReloadLoan, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
				});
				$('button.undoapproveloan span').text(doI18N('dialog.button.undo.loan.approval'));
					
				$('.deleteloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("deletebtn", "");
						var url = 'loans/' + loanId;
						var width = 400; 
						var height = 225;
						
						var redirectUrl = '${clientUrl}';
						
						popupConfirmationDialogAndPost(url, 'DELETE', 'dialog.title.confirmation.required', width, height, 0, redirectUrl);
					    e.preventDefault();
				});
				$('button.deleteloan span').text(doI18N('dialog.button.delete.loan'));
					
				$('.disburseloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("disbursebtn", "");
						var postUrl = 'loans/' + loanId + '?command=disburse';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						eval(genSaveSuccessFunctionReloadLoan(loanId));
						popupDialogWithPostOnlyFormView(postUrl, 'POST', 'dialog.title.disburse.loan', templateSelector, width, height, saveSuccessFunctionReloadLoan,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
				});
				$('button.disburseloan span').text(doI18N('dialog.button.disburse.loan'));
					
				$('.undodisbursalloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("undodisbursalbtn", "");
						var postUrl = 'loans/' + loanId + '?command=undodisbursal';
						var templateSelector = "#undoStateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						eval(genSaveSuccessFunctionReloadLoan(loanId));
						popupDialogWithPostOnlyFormView(postUrl, 'POST', 'dialog.title.undo.loan.disbursal', templateSelector, width, height, saveSuccessFunctionReloadLoan,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
				});
				$('button.undodisbursalloan span').text(doI18N('dialog.button.undo.loan.disbursal'));
					
				$('.repaymentloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("repaymentbtn", "");
						var getUrl = 'loans/' + loanId + '/transactions/template?command=repayment';
						var postUrl = 'loans/' + loanId + '/transactions?command=repayment';
						
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						eval(genSaveSuccessFunctionReloadLoan(loanId));
			
						popupDialogWithFormView(getUrl, postUrl, 'POST', "dialog.title.loan.repayment", templateSelector, width, height,  saveSuccessFunctionReloadLoan);
						//popupDialogWithFormView(getUrl, postUrl, 'POST', 'dialog.title.loan.repayment', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
				});
				$('button.repaymentloan span').text(doI18N('dialog.button.loan.repayment'));
					
				$('.waiveloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("waivebtn", "");
						
						var getUrl = 'loans/' + loanId + '/transactions/template?command=waiver';
						var postUrl = 'loans/' + loanId + '/transactions?command=waiver';
						
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						
						eval(genSaveSuccessFunctionReloadLoan(loanId));
						
						popupDialogWithFormView(getUrl, postUrl, 'POST', "dialog.title.waive.loan", templateSelector, width, height, saveSuccessFunctionReloadLoan);
					    e.preventDefault();
				});
				$('button.waiveloan span').text(doI18N('dialog.button.loan.waive'));
					
				$('.adjustloanrepayment').button().click(function(e) {
						
						var linkId = this.id;
						var loanAndRepaymentId = linkId.replace("adjustrepaymentbtn", "");
						var ids = loanAndRepaymentId.split("_");
						var loanId = ids[0];
						var transactionId = ids[1];
						var getAndPostUrl = 'loans/' + loanId + '/transactions/' + transactionId;
						
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;

						eval(genSaveSuccessFunctionReloadLoan(loanId));						
						popupDialogWithFormView(getAndPostUrl, getAndPostUrl, 'POST', "dialog.title.adjust.loan.repayment", templateSelector, width,  height, saveSuccessFunctionReloadLoan);
					    e.preventDefault();
				});
				$('button.adjustloanrepayment span').text(doI18N('dialog.button.adjust.loan.repayment'));
					
				// additional data
				var additionalFieldsParams = {
							url: baseApiUrl,
							basicAuthKey: base64,
							datasetType: "portfolio_loan",
							datasetPKValue: data.id,
							datasetTypeDiv: "loanadditionaldata" + data.id, 
							headingPrefix: "", 
							headingClass: "", 
							labelClass: "longrowlabel",
							valueClass:	"rowvalue",
							editLabel: doI18N("link.edit"),	
							saveLabel: doI18N("dialog.button.save"),	
							cancelLabel: doI18N("dialog.button.cancel")						
				};
				jQuery.stretchyData.displayAllExtraData(additionalFieldsParams)
	        };
	    
		executeAjaxRequest(loanUrl, 'GET', "", successFunction, errorFunction);	  

}





/* user admin code */

	function showILUserAdmin() {
		setUserAdminContent("content");
	}

	function refreshUsersView() {
				
		var successFunction = function(data, textStatus, jqXHR) {
				var usersObject = new Object();
				usersObject.users = data;
				var usersListHtml = $("#usersListTemplate").render(usersObject);
				$("#contentplaceholder").html(usersListHtml);  
				
				$("a.edit").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("edit", "");
					var getUrl = 'users/' + entityId + '?template=true';
					var putUrl = 'users/' + entityId;
					maintainUser(getUrl, putUrl, 'PUT', "dialog.title.edit.details");
					e.preventDefault();
				});
				
				$("a.delete").click( function(e) {
					//var linkId = this.id;
					//var entityId = linkId.replace("delete", "");
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
					"bAutoWidth": false
				} );
			  };

  		executeAjaxRequest('users', 'GET', "", successFunction, formErrorFunction);
	}
	
	function maintainUser(getUrl, putOrPostUrl, submitType, dialogTitle) {

		var templateSelector = "#userFormTemplate";
		var width = 1000; 
		var height = 550;
					
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			$("#dialog-form").dialog("close");
			refreshUsersView();
		}		
		popupDialogWithFormView(getUrl, putOrPostUrl, submitType, dialogTitle, templateSelector, width, height, saveSuccessFunction);
	}

	function refreshRolesView() {
		
		var successFunction = function(data, textStatus, jqXHR) {
				  
				var rolesObject = new Object();
				rolesObject.roles = data;
				var listHtml = $("#roleListTemplate").render(rolesObject);
				$("#contentplaceholder").html(listHtml);
				
				$("a.edit").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("edit", "");
					var getUrl = 'roles/' + entityId + '?template=true';
					var putUrl = 'roles/' + entityId;
					maintainRole(getUrl, putUrl, 'PUT', "dialog.title.edit.details");					
					e.preventDefault();
				});
				
				$("a.delete").click( function(e) {
					//var linkId = this.id;
					//var entityId = linkId.replace("delete", "");
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
					"bAutoWidth": false
				} );
			  };
		
  		executeAjaxRequest('roles', 'GET', "", successFunction, formErrorFunction);
	}
	
	function maintainRole(getUrl, putOrPostUrl, submitType, dialogTitle) {

		var templateSelector = "#roleFormTemplate";
		var width = 1000; 
		var height = 550;
					
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  $("#dialog-form").dialog("close");
						  refreshRolesView();
						}

		popupDialogWithFormView(getUrl, putOrPostUrl, submitType, dialogTitle, templateSelector, width, height, saveSuccessFunction);
	}


	function refreshPermissionsView() {
		var templateSelector = "#permissionListTemplate";
		var displayAreaDivSelector = "#contentplaceholder";
		
		var successFunction = function(data, textStatus, jqXHR) {
				var permissionsObject = new Object();
				permissionsObject.permissions = data;
				var listHtml = $(templateSelector).render(permissionsObject);
				$(displayAreaDivSelector).html(listHtml);
				
				var oTable = $("#entitytable").dataTable( {
					"bSort": true,
					"bInfo": true,
					"bJQueryUI": true,
					"bRetrieve": false,
					"bScrollCollapse": false,
					"bPaginate": false,
					"bLengthChange": false,
					"bFilter": false,
					"bAutoWidth": false
				} );
			  };

  		executeAjaxRequest('permissions', 'GET', "", successFunction, formErrorFunction);
	}



/* org admin code */

	function showILOrgAdmin() {
		setOrgAdminContent("content");
	}


	function refreshLoanProductsView() {
 
		var successFunction = function(data, textStatus, jqXHR) {
				
				var productlistParent = new Object();
				productlistParent.products = data;
				
				var productListHtml = $("#productListTemplate").render(productlistParent);
				$("#listplaceholder").html(productListHtml);
				
				$("a.editproduct").click( function(e) {
					var linkId = this.id;
					var productId = linkId.replace("editproduct", "");
					var getUrl = 'loanproducts/' + productId + '?template=true';
					var putUrl = 'loanproducts/' + productId;
					maintainLoanProduct(getUrl, putUrl, 'PUT', "dialog.title.product.details");
					e.preventDefault();
				});
				
				$("a.deactivateproduct").click( function(e) {
					//var linkId = this.id;
					//var productId = linkId.replace("deactivateproduct", "");
					showNotAvailableDialog('dialog.title.functionality.not.available');
					e.preventDefault();
				});
				
				$("a.deleteproduct").click( function(e) {
					//var linkId = this.id;
					//var productId = linkId.replace("deleteproduct", "");
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
					"bAutoWidth": false
				});
			  };

  		executeAjaxRequest('loanproducts', 'GET', "", successFunction, formErrorFunction);
	}

	function maintainLoanProduct(getUrl, putOrPostUrl, submitType, dialogTitle) {
		var templateSelector = "#productFormTemplate";
		var width = 800; 
		var height = 550;
		
		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			  $("#dialog-form").dialog("close");
			  refreshLoanProductsView();
		}
		
		popupDialogWithFormView(getUrl, putOrPostUrl, submitType, dialogTitle, templateSelector, width, height, saveSuccessFunction);
	}


	function refreshOfficesView() {

		var successFunction = function(data, textStatus, jqXHR) {
				
				var officelistParent = new Object();
				officelistParent.offices = data;
				
				var officeListHtml = $("#officeListTemplate").render(officelistParent);
				$("#listplaceholder").html(officeListHtml);  
				
				$("a.edit").click( function(e) {
					var linkId = this.id;
					var entityId = linkId.replace("edit", "");
					var getUrl = 'offices/' + entityId + '?template=true';
					var putUrl = 'offices/' + entityId;
					maintainOffice(getUrl, putUrl, 'PUT', "dialog.title.office.details");
					e.preventDefault();
				});
				
				$("a.delete").click( function(e) {
					//var linkId = this.id;
					//var entityId = linkId.replace("delete", "");
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
					"bAutoWidth": false
					
				} );
			  };
		
  		executeAjaxRequest('offices', 'GET', "", successFunction, formErrorFunction);
	}
	
	function maintainOffice(getUrl, putOrPostUrl, submitType, dialogTitle) {
		var templateSelector = "#officeFormTemplate";
		var width = 600; 
		var height = 400;

		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			  $("#dialog-form").dialog("close");
			  refreshOfficesView();
		}
		
		popupDialogWithFormView(getUrl, putOrPostUrl, submitType, dialogTitle, templateSelector, width, height, saveSuccessFunction);
	}

	function maintainOrgCurrencies(getUrl, putOrPostUrl, submitType, dialogTitle) {
		var templateSelector = "#configurationFormTemplate";
		var width = 900; 
		var height = 400;

		var saveSuccessFunction = function(data, textStatus, jqXHR) {
			  $("#dialog-form").dialog("close");
		}
		
		popupDialogWithFormView(getUrl, putOrPostUrl, submitType, dialogTitle, templateSelector, width, height, saveSuccessFunction);
	}



/* reports code */

function showILReporting() {
	setReportingContent("content");

var reportingParams = {
	RESTUrl: baseApiUrl + "reports",
	basicAuthKey: base64,
	pentahoUrl: baseApiUrl + "pentahoreport",
	initialLanguage: currentCulture,
	bundleDir: "resources/stretchyreporting/mifosngbundle/",
	reportsListDiv: "myListOfReports",
	runReportDiv: "myRunReportButton",
	clearReportDiv: "myClearReportButton",
	inputParametersDiv: "myInputParameters",
	reportOutputDiv: "myOutput",
	indianFormat: false,
	highlightMissingXlations: "N",
	loadingImg: "resources/stretchyreporting/dots64.gif",
	resValue: "resources/libs/"
};

	jQuery.stretchyReporting.initialise(reportingParams);

	$("#toptable").slideToggle("slow");

}

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


//account settings
function showILAccountSettings() {

	setAccountSettingsContent("content"); 
	$tabs = $("#tabs").tabs({
		"add": function( event, ui ) {
			$tabs.tabs('select', '#' + ui.panel.id);
		}

	});

	var errorFunction = function(jqXHR, status, errorThrown, index, anchor) {
	            $(anchor.hash).html("error occured while ajax loading.");
	        };

	var successFunction = function(data, status, xhr) {
				var tableHtml = $("#userSettingsTemplate").render(data);
				$("#settings").html(tableHtml);
				
				$('#changepassword').click(function(e) {
					var putUrl = 'users/' + currentUser;
					var templateSelector = "#changePasswordFormTemplate";
					var width = 600; 
					var height = 350;
					
					var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  $("#dialog-form").dialog("close");
						  $("#tabs").tabs('load', 0);
					}
					
					popupDialogWithPostOnlyFormView(putUrl, 'PUT', 'dialog.title.update.password', templateSelector, width, height, saveSuccessFunction, 0, 0, 0);
				    e.preventDefault();
				});
				
				$('#changedetails').click(function(e) {
					var getAndPutUrl = 'users/' + currentUser;
					var templateSelector = "#userSettingsFormTemplate";
					var width = 600; 
					var height = 350;
					
					var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  $("#dialog-form").dialog("close");
						  $("#tabs").tabs('load', 0);
					}
					
					popupDialogWithFormView(getAndPutUrl, getAndPutUrl, 'PUT', 'dialog.title.update.details', templateSelector, width, height, saveSuccessFunction);
					
				    e.preventDefault();
				});
	        };
    
	executeAjaxRequest("users/" + currentUser, 'GET', "", successFunction, errorFunction);	  
}
	


//authenticate user and set global details
function setBasicAuthKey(logonDivName, username, password) 
{ 
	base64 = "";
	currentUser = -1;
	currentUserName = "";

	var url = "authentication?username=" + username + "&password=" + password;
	var successFunction = function(data, textStatus, jqXHR) { 
					base64 = data.base64EncodedAuthenticationKey; 
					currentUser = data.userId;
					currentUserName = data.username;

					showMainContainer(logonDivName, username);
					showILClientListing();
					return false;
			};

	var errorFunction = function(jqXHR, textStatus, errorThrown) {
	        			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
					return true;
				};

	executeAjaxRequest(url, 'POST', "", successFunction, errorFunction);
}




//Popups used for saving data and confirmation	
function popupDialogWithFormView(getUrl, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction) {

		var successFunction = function(data, textStatus, jqXHR) {
				//console.log(data);
				popupDialogWithFormViewData(data, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction);
		  	};
		
		if (getUrl == "") popupDialogWithFormViewData("", postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction)
		else executeAjaxRequest(getUrl, "GET", "", successFunction, formErrorFunction);

}
function popupDialogWithFormViewData(data, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction)  {
				var dialogDiv = $("<div id='dialog-form'></div>");
				var formHtml = $(templateSelector).render(data);
				dialogDiv.append(formHtml);
				var saveButton = doI18N('dialog.button.save');
				var cancelButton = doI18N('dialog.button.cancel');
				
				var buttonsOpts = {};
				buttonsOpts[saveButton] = function() {
					
					$('#notSelectedPermissions option').each(function(i) {  
						$(this).attr("selected", "selected");  
					});
				    	
				    	$('#permissions option').each(function(i) {  
				    	   	$(this).attr("selected", "selected");  
				    	});


				    	$('#notSelectedRoles option').each(function(i) {  
						$(this).attr("selected", "selected");  
					});
				    	$('#roles option').each(function(i) {  
				    	   	$(this).attr("selected", "selected");  
				    	});
					

					$('#notSelectedItems option').each(function(i) {  
				    	   $(this).attr("selected", "selected");  
				    	});
			    		$('#selectedItems option').each(function(i) {  
			    	   		$(this).attr("selected", "selected");  
			    		});
			    	

					$('#notSelectedCurrencies option').each(function(i) {  
					    	   	$(this).attr("selected", "selected");  
					});
				    	$('#currencies option').each(function(i) {  
				    	   		$(this).attr("selected", "selected");  
				    	});

			    		var newFormData = JSON.stringify($('#entityform').serializeObject());
			    		console.log(newFormData);
			    	
					executeAjaxRequest(postUrl, submitType, newFormData, saveSuccessFunction, formErrorFunction);

				};
				
				buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
				
				dialogDiv.dialog({
				  		title: doI18N(titleCode), 
				  		width: width, 
				  		height: height, 
				  		modal: true,
				  		buttons: buttonsOpts,
				  		close: function() {
				  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
				  			$(this).remove();
						},
				  		open: function (event, ui) {

					  		$('#addpermissions').click(function() {  
					  			return !$('#notSelectedPermissions option:selected').remove().appendTo('#permissions');  
					  		});
					  		$('#removepermissions').click(function() {  
					  			return !$('#permissions option:selected').remove().appendTo('#notSelectedPermissions');  
					  		}); 

				  			$('#addroles').click(function() {  
					  			return !$('#notSelectedRoles option:selected').remove().appendTo('#roles');  
					  		});	
					  		$('#removeroles').click(function() {  
					  			return !$('#roles option:selected').remove().appendTo('#notSelectedRoles');  
					  		}); 

				  			$('#add').click(function() {  
				  			     return !$('#notSelectedItems option:selected').remove().appendTo('#selectedItems');  
				  			});
				  			$('#remove').click(function() {  
				  				return !$('#selectedItems option:selected').remove().appendTo('#notSelectedItems');  
				  			});
				  			

					  		$('#addcurrencies').click(function() {  
					  			return !$('#notSelectedCurrencies option:selected').remove().appendTo('#currencies');  
					  		});
					  		$('#removecurrencies').click(function() {  
					  			return !$('#currencies option:selected').remove().appendTo('#notSelectedCurrencies');  
					  		});


				  			$('.datepickerfield').datepicker({constrainInput: true, maxDate: 0, dateFormat: 'dd MM yy'});
				  			
				  			$("#entityform textarea").first().focus();
				  			$('#entityform input').first().focus();
				  		}
				  	}).dialog('open');
}


function popupDialogWithPostOnlyFormView(postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction, minOffset, defaultOffset, maxOffset) {
		var dialogDiv = $("<div id='dialog-form'></div>");
		var data = new Object();
		var formHtml = $(templateSelector).render(data);
		dialogDiv.append(formHtml);
		
		var saveButton = doI18N('dialog.button.save');
		var cancelButton = doI18N('dialog.button.cancel');
		var buttonsOpts = {};		
		buttonsOpts[saveButton] = function() {
			$('.multiSelectedItems option').each(function(i) {  
		    	   		$(this).attr("selected", "selected");  
		    		});

			var newFormData = JSON.stringify($('#entityform').serializeObject());
			console.log(newFormData);

			executeAjaxRequest(postUrl, submitType, newFormData, saveSuccessFunction, formErrorFunction);
		};
		buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
		
		dialogDiv.dialog({
		  		title: doI18N(titleCode), 
		  		width: width, 
		  		height: height, 
		  		modal: true,
		  		buttons: buttonsOpts,
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
		  			
		  			$('.datepickerfield').datepicker({constrainInput: true, minDate: minOffset, defaultDate: defaultOffset, maxDate: maxOffset, dateFormat: 'dd MM yy'});
		  			
		  			$("#entityform textarea").first().focus();
		  			$('#entityform input').first().focus();
		  		}
		  }).dialog('open');
}

function popupConfirmationDialogAndPost(url, submitType, titleCode, width, height, tabIndex, redirectUrl) {
		    var dialogDiv = $("<div id='dialog-form'><div id='formerrors'></div>" + doI18N('text.confirmation.required') + "</div>");
		  
		  	var confirmButton = doI18N('dialog.button.confirm');
			var cancelButton = doI18N('dialog.button.cancel');
			
			var buttonsOpts = {};
			buttonsOpts[confirmButton] = function() {
				var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  			dialogDiv.dialog("close");
					  				//$newtabs.tabs('load', tabIndex);
									alert("should be reloaded this loan tab");
					  			}
				 
				executeAjaxRequest(url, submitType, "", saveSuccessFunction, formErrorFunction);

			};
			
			buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
		  
		  
		  dialogDiv.dialog({
		  		title: doI18N(titleCode), 
		  		width: width, 
		  		height: height, 
		  		modal: true,
		  		buttons: buttonsOpts,
		  		close: function() {
		  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
		  			$(this).remove();
				},
		  		open: function (event, ui) {}
		  	}).dialog('open');
}



//sign-out
function signOut(containerDivName) {
	base64 = "";
	$("#" + containerDivName).html("");
	alert("Close the Browser for a Complete Sign Out");
}



//utility functions

highlightMissingXlations = "Y";
function doI18N(xlateStr, params) { 
	if (highlightMissingXlations == "Y") return jQuery.i18n.prop(xlateStr, params)
	else
	{
		var xlated = jQuery.i18n.prop(xlateStr, params);
		if (xlated.substr(0,1) == "[" && xlated.substr(xlated.length - 1, 1) == "]") return xlated.substr(1, xlated.length - 2)
		else return xlated;
	}
}


function initialiseAndShowILLogon() {
	jQuery.support.cors = true;

	setInitialCulture();

	jsViewsRegisterHelpers();

	//baseApiUrl = "https://localhost:8443/mifosng-provider/api/v1/";
	baseApiUrl = "https://ec2-46-137-62-163.eu-west-1.compute.amazonaws.com:8443/mifosng-provider/api/v1/";
	if (QueryParameters["baseApiUrl"]) baseApiUrl = QueryParameters["baseApiUrl"];
	
	showILLogon("container");
}

function setInitialCulture() {

	baseCulture = 'en';
	if (QueryParameters["baseCulture"]) baseCulture = QueryParameters["baseCulture"];
	switch(baseCulture)
	{
			case "en":
  				break;
			case "fr":
  				break;
			case "es":
  				break;
			case "pt":
  				break;
			case "zh":
  				break;
			default:
  				alert("The culture/language you specified (" + baseCulture + ") isn't available so will default to 'en' (English).");
				baseCulture = 'en';
	}
	setCulture(baseCulture);	
}

function setCultureReshowListing(cultureVal) {
	setCulture(cultureVal);
	showMainContainer("container");
	showILClientListing();
}


function setCulture(cultureVal) {
	currentCulture = cultureVal;
    	Globalize.culture(currentCulture);
    	
    	$.datepicker.setDefaults( $.datepicker.regional[currentCulture]);
    	
    	jQuery.i18n.properties({
			name:'messages', 
			path: 'resources/global-translations/',
			mode:'map',
			cache: true,
			language: currentCulture,
			callback: function() {
			}
		});
}


QueryParameters = (function()
{
    var result = {};
    if (window.location.search)
    {
        // split up the query string and store in an associative array
        var params = window.location.search.slice(1).split("&");
        for (var i = 0; i < params.length; i++)
        {
            var tmp = params[i].split("=");
            result[tmp[0]] = unescape(tmp[1]);
        }
    }
    return result;
}());


function showNotAvailableDialog(titleCode) {
		var dialogDiv = $("<div id='notavailable-dialog-form'></div>");
		
		dialogDiv.append("<p>" + doI18N('dialog.messages.functionality.not.available') + "</p>");
		
		var okButton = doI18N('dialog.button.ok');
		
		var buttonsOpts = {};
		buttonsOpts[okButton] = function() {$(this).dialog("close");};
		
		dialogDiv.dialog({
	  		title: doI18N(titleCode), 
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
	    	if (this.name === 'notSelectedCurrencies' || this.name === 'notSelectedPermissions' || this.name === 'notSelectedRoles') {
	    		// do not serialize
	    	} else  {

	        if (o[this.name] !== undefined) {
	            if (!o[this.name].push) {
	                o[this.name] = [o[this.name]];
	            }
	            o[this.name].push(this.value || '');
	        } else {
	        	
	        	if (this.name === 'selectedItems' || this.name === 'notSelectedItems' || this.name === 'currencies' || this.name === 'permissions' || this.name === 'roles') {
	        		o[this.name] = new Array();
	        		o[this.name].push(this.value || '');
	        	} else {
	        		o[this.name] = this.value || '';	
	        	}
	        }
		}
	    });
	    return o;
	};
	




//Error functions		
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
		  	  errorObj.message = doI18N(this.userMessageGlobalisationCode, argArray[0], argArray[1], argArray[2], argArray[3], argArray[4], argArray[5]);
		  	  errorObj.value = this.value;
		  	  
		  	  errorArray[arrayIndex] = errorObj;
		  	  arrayIndex++
		  	});
		  	
		  	var templateArray = new Array();
		  	var templateErrorObj = new Object();
		  	templateErrorObj.title = doI18N('error.msg.header');
		  	templateErrorObj.errors = errorArray;
		  	
		  	templateArray[0] = templateErrorObj;
		  	
		  	var formErrorsHtml = $(templateSelector).render(templateArray);
		  	
		  	$(placeholderDiv).append(formErrorsHtml);
		}
}




// these helpers are registered for the jsViews and jsRender functionality to fix bug with display zero! 
// plus some utility functions are added also for doI18N and currentLocale
function jsViewsRegisterHelpers() {
	$.views.registerHelpers({
			
			money: function(monetaryObj) {
				
				Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
				
				var digits = monetaryObj.digitsAfterDecimal.toFixed(0);
				return Globalize.format(monetaryObj.amount, "n" + digits); 
			},
			moneyWithCurrency: function(monetaryObj) {
				
				Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
				
				var digits = monetaryObj.digitsAfterDecimal.toFixed(0);
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
			    	  if (undefined != dateParts)
				  {
			    	  	var year = dateParts[0];
			    	  	var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  	var day = dateParts[2];
			    	  
			    	  	var d = new Date();
			    	  	d.setFullYear(year,month,day);
			    	  
			    	  	return Globalize.format(d,"dd MMMM yyyy");
				  }
				  else return "";
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
			},
			currentLocale: function() {
			      try {
			    	  return Globalize.culture().name;
			      } catch(e) {
			        return "??";
			      }
			},
			doI18N: function(xlateStr, params) {
			      try {
			    	  return doI18N(xlateStr, params);
			      } catch(e) {
			        return xlateStr;
			      }
			}
	});
}

