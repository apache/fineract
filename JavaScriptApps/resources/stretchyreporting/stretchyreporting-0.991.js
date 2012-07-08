(function($) {

$.stretchyReporting = {};
    
$.stretchyReporting.initialise  = function(params) {

					initialiseReporting(params)
				};

$.stretchyReporting.newReportSelected = function(selectedRpt) {
					newReportSelected(selectedRpt);
				};

$.stretchyReporting.showTypeChanged = function() {
					showTypeChanged();
				};

$.stretchyReporting.clearLoadingImg = function() {
					clearLoadingImg();
				};

$.stretchyReporting.nothingCallback = function() {
					// nothing
				};

$.stretchyReporting.changeLanguage  = function(newLanguage) {
					changeLanguage(newLanguage)
				};

$.stretchyReporting.changeDecimals = function(newDecimals) {
					changeDecimals(newDecimals)
				};

$.stretchyReporting.changeSeparator= function(sepChar, decChar, indFormat) {
					changeSeparator(sepChar, decChar, indFormat)
				};

function fnRecordsDisplay(){
// oSettings isn't defined when table first created but coding around to not
// call this method until after it is.
	// if (typeof(oSettings) != "undefined") return oSettings.fnRecordsDisplay()
	// else return 0;
	return oSettings.fnRecordsDisplay();
}


function initialiseReporting(params) {

	tableSizeLimit = setTableSizeLimit();

	showMsg("Table size limit is: " + tableSizeLimit );

	nullTitleValue = "-99999999.11";

	resValue = '';
	if (params.resValue) resValue  = params.resValue;
	
	I18N_Needed = 'Y';
	if (params.I18N_Needed) I18N_Needed = params.I18N_Needed;

	bundleDir = 'bundle/';
	if (params.bundleDir) bundleDir = params.bundleDir;

	highlightMissingXlations = "Y";
 	if (params.highlightMissingXlations) highlightMissingXlations = params.highlightMissingXlations;

	if (params.initialLanguage) initI18N(params.initialLanguage);
	else initI18N("");

	if (params.reportOutputDiv) $('#' + params.reportOutputDiv).html('<div id="dt_example"><div id=StretchyReportOutput></div></div>');
	else
	{
		alert(doI18N("reportingInitError", ['reportOutputDiv']));
		return;
	}

	rptDB = ""
 	if (params.rptDB) rptDB = params.rptDB;

 	if (params.RESTUrl) RESTUrl = params.RESTUrl
	else RESTUrl = "";
 	
 	if (params.basicAuthKey) basicAuthKey = params.basicAuthKey
	else basicAuthKey = "";

 	if (params.pentahoUrl) pentahoUrl = params.pentahoUrl
	else pentahoUrl = "";

	loadingImg = "dots64.gif";
	if (params.loadingImg) loadingImg = params.loadingImg;

 	if (params.reportsListDiv) reportsListDiv = '#' + params.reportsListDiv;	
	else
	{
		alert(doI18N("reportingInitError", ['reportsListDiv']));
		return;
	}
	$(reportsListDiv).html('<img id=reportLoadingImage src="' + loadingImg + '" />');
	reportsListDivLabel = 'Report';

 	if (params.inputParametersDiv) inputParametersDiv = '#' + params.inputParametersDiv;	
	else
	{
		alert(doI18N("reportingInitError", ['inputParametersDiv']));
		return;
	}

 	if (params.runReportDiv) runReportDiv = '#' + params.runReportDiv;	
	else
	{
		alert(doI18N("reportingInitError", ['runReportDiv']));
		return;
	}

 	if (params.clearReportDiv) clearReportDiv = '#' + params.clearReportDiv;	
	else
	{
		alert(doI18N("reportingInitError", ['clearReportDiv']));
		return;
	}

 	if (params.apiKey || params.sharedSecret || params.accessToken || params.tokenSecret)
 	{
 		showMsg("OAuth params passed");
 		isAuthRequest = true;
 	 	if ((params.apiKey == null) || (params.sharedSecret == null) || (params.accessToken == null) || (params.tokenSecret == null))
 	 	{
 			alert("Each of apiKey, sharedSecret, accessToken and tokenSecret must be passed as parameters");
 			return;
 	 	}
 	 	apiKey = params.apiKey;
 	 	sharedSecret = params.sharedSecret;
 	 	accessToken = params.accessToken;
 	 	tokenSecret = params.tokenSecret;
 	}
 	else 
	{
		isAuthRequest = false;
		showMsg("OAuth params were not passed");
	}
 	
	reportListing = [];
	listOfParameters = [];

	setupButtons();
	initialiseSuccessFunctionVariables();
	initialiseDataTableDef();

	decimalsNo = 4;
	if (params.decimals) decimalsNo = params.decimals;

	separatorChar = ',';
	if (params.separatorChar) separatorChar = params.separatorChar;

	dbDecimalChar = ".";
	decimalChar = ".";
	if (params.decimalChar) decimalChar = params.decimalChar;

	indianFormat = false;
	if (params.indianFormat) indianFormat = params.indianFormat;


	reportQuery = 'FullReportList';
	if (params.reportQuery) reportQuery = params.reportQuery;

	getReportData(reportQuery, {}, setupReportListSuccess, true) 
}

function setCurrentDate() {
    var xxDate = new Date();
    return xxDate.getFullYear() + "-" + pad2(xxDate.getMonth() + 1) + "-" + pad2(xxDate.getDate());
}

function pad2(number) {
    return (number < 10 ? '0' : '') + number
}

function formatNumber(nStr)
{ 
// format no. of decimal places
	var mainNum;
	if (decimalsNo < 0)
	{
		mainNum = (nStr / Math.pow(10, Math.abs(decimalsNo))).toFixed(0);
	}
	else mainNum = nStr.toFixed(decimalsNo);

	if (indianFormat == true) return indianFormatNumber(mainNum)
	else return generalFormatNumber(mainNum);
}

// function for converting string into indian currency format
function indianFormatNumber(inNum) {

	var mainNum = inNum + '';
	var signStr = "";
	if (inNum < 0)
	{
		signStr = "-";
		mainNum = mainNum.substr(1);
	}


	var dpos = mainNum.indexOf(dbDecimalChar);
	var nStrEnd = '';
	if (dpos != -1) {
		nStrEnd = decimalChar + mainNum.substring(dpos + 1, mainNum.length);
		mainNum = mainNum.substring(0, dpos);
	}

	
	var remLength = mainNum.length;
	if (remLength > 3)
	{
		var strArray = [];
		var biteSize = 3;

		strArray.push(mainNum.substr(remLength - biteSize , biteSize));
		mainNum = mainNum.substr(0, remLength - biteSize);

		biteSize = 2;
		do
  		{
			remLength = mainNum.length;
			if (remLength < 2) biteSize = 1;
			strArray.push(mainNum.substr(remLength - biteSize , biteSize));
			if (remLength > biteSize) mainNum = mainNum.substr(0, remLength - biteSize);
 		}
		while (remLength > biteSize);

		mainNum = "";
		var arrLen = strArray.length - 1;
		for (i = arrLen; i >= 0; i -= 1)
		{
			if (i < arrLen) mainNum += separatorChar;
			mainNum += strArray[i];
		}
 	}
	return signStr + mainNum + nStrEnd;
}

function generalFormatNumber(inNum) {

	var mainNum = inNum + '';
	if (separatorChar != "")
	{
		var dpos = mainNum.indexOf(dbDecimalChar);
		var nStrEnd = '';
		if (dpos != -1) {
			nStrEnd = decimalChar + mainNum.substring(dpos + 1, mainNum.length);
			mainNum = mainNum.substring(0, dpos);
		}
		var rgx = /(\d+)(\d{3})/;
		while (rgx.test(mainNum)) {
			mainNum = mainNum.replace(rgx, '$1' + separatorChar + '$2');
		}
		return mainNum + nStrEnd;
	}
	else return mainNum; // no separator format

}

function getOrigTitleNumericValue(titleNumericValue) {
	var returnVal = titleNumericValue.substr(13);
	dblQuotePos = returnVal.indexOf('"');
	return Number(returnVal.substr(0, dblQuotePos));
}

function invalidDate(checkDate) {
// validates for yyyy-mm-dd returns true if invalid, false is valid
var dateformat = /^\d{4}(\-|\/|\.)\d{1,2}\1\d{1,2}$/;

    	if(!(dateformat.test(checkDate))) {
      	return true;
    	}
	else
	{
		var dyear = checkDate.substring(0,4);
		var dmonth = checkDate.substring(5,7) - 1;
		var dday = checkDate.substring(8);

		var newDate=new Date(dyear,dmonth ,dday);
		return !((dday==newDate.getDate()) && (dmonth==newDate.getMonth()) && (dyear==newDate.getFullYear()));
	}
}

function setupButtons() {
	$(runReportDiv).button(); 
	$(runReportDiv).button({ label: doI18N("rpt_run") });
	$(runReportDiv).click(function() {
  				runTheReport();
						});

	$(clearReportDiv).button(); 
	$(clearReportDiv).button({ label: doI18N("rpt_clear") });
	$(clearReportDiv).click(function() {
  				clearTheReport();
						});

	disableRunButton();
	disableClearButton();
}

function clearTheReport() {
	$('#StretchyReportOutput').html("");
	$('.DTTT_collection').remove();
	disableClearButton();
}

function enableClearButton() {
	$(clearReportDiv).button( "option", "disabled", false );
}

function disableClearButton() {
	$(clearReportDiv).button( "option", "disabled", true );
}

function enableRunButton() {
	$(runReportDiv).button( "option", "disabled", false );
}

function disableRunButton() {
	$(runReportDiv).button( "option", "disabled", true );
}


function getReportListingIndexSelectedReport() {

	var selectedRpt = $(reportsListDiv + ' select option:selected').val();
	return getReportListingIndex(selectedRpt);
}


function getReportListingIndex(reportOption) {

	for (var i in reportListing)
	{
		if (reportListing[i].name == reportOption)
		{
			return i;
		}
	}

	var returnMessage = "Report Option not Found: " + reportOption;
	alert(returnMessage);
	return "returnMessage";
}


function hideAllParameters() {

	clearTheReport();

	for (var i in listOfParameters)
	{
		$("#" + listOfParameters[i].name).css("display", "none");
	}
	$("#rptOutputType").css("display", "none");
	$("#rptShowType").css("display", "none");
}

function makeSelectTag(selectTagParams, selectOne, selectAll, xlateText) {

var selectHtml = '<div class=reportLabel>' + doI18N(selectTagParams.label) + '</div>';
	selectHtml = selectHtml + '<select onChange="' + selectTagParams.selectOnClick + '">';

	if (selectOne == true)
	{
		selectHtml = selectHtml + '<option value="0">' + doI18N('rpt_select_one') + '</option>';
	}
	if (selectAll == true)
	{
		selectHtml = selectHtml + '<option value="-1">' + doI18N('rpt_select_all') + '</option>';
	}

	var textVal;
	for (var i in selectTagParams.selectData)
	{
		if (xlateText == true) textVal = doI18N(selectTagParams.selectData[i].name)
		else textVal = selectTagParams.selectData[i].name;

		selectHtml = selectHtml + '<option value="' + selectTagParams.selectData[i].id + '">' + textVal + '</option>';
	}
	selectHtml = selectHtml + '</select>';
	$(selectTagParams.divName).html(selectHtml );
	$(selectTagParams.divName + ' select option:eq(0)').attr('selected', 'selected');
}


function newReportSelected(selectedRpt) {

	hideAllParameters();

	if (!(selectedRpt == "0"))
	{
		enableRunButton();
		var reportListingIndex = getReportListingIndex(selectedRpt);
		var isParam = false;
		for (var i in reportListing[reportListingIndex].parameters)
		{
			isParam = true;
			$('#' + reportListing[reportListingIndex].parameters[i][1]).css("display", "inline");
		}

		if (reportListing[reportListingIndex].type == 'Pentaho') $("#rptOutputType").css("display", "inline");
		if (reportListing[reportListingIndex].type == 'Table') $("#rptShowType").css("display", "inline");

		if (isParam == false) runTheReport();
	}
	else disableRunButton();
	
}

function showTypeChanged() {

	if ($(reportsListDiv + ' select option:selected').val() != "0") runTheReport()
	else showMsg("was select one");

}

function runTheReport()
{
	var selectedRpt = $(reportsListDiv + ' select option:selected').val();

	clearTheReport();

	var reportListingIndex = getReportListingIndexSelectedReport();

	if (parameterValidationErrors(reportListing[reportListingIndex].parameters) == 1)
	{
		return;
	}

	var theParams = {};

	var pValue;
	var reportParameterName;
	for (var i in reportListing[reportListingIndex].parameters)
	{
		var paramDetails = getParameterDetailsUsingName(reportListing[reportListingIndex].parameters[i][1]);
		switch(paramDetails.displayType)
		{
			case "select":
				pValue = $('#' + paramDetails.name+ ' select option:selected').val();
  				break;
			case "date":
				pValue = $('#' + paramDetails.variable).val();
  				break;
			default:
  				alert("System Error: Unknown Display Type: " + paramDetails.displayType);
		}

		if (reportListing[reportListingIndex].type == 'Pentaho') reportParameterName = reportListing[reportListingIndex].parameters[i][0]
		else reportParameterName = paramDetails.variable;

		// alert("Variable: " + reportParameterName + " Value: " + pValue);
		theParams[reportParameterName] = pValue ;
	}

	$('#StretchyReportOutput').html('<br><br><img id=reportLoadingImage src="' + loadingImg + '" />');



	var showOption = $('#rptShowType option:selected').val();
	switch(reportListing[reportListingIndex].type)
	{
		case "Table":
			if (showOption == "XLS") getExportCSV(selectedRpt, theParams, false)
			else
			{
				reportDataSuccess = function(data, textStatus, jqXHR){
					createTable(data);
					showTableReport();
				};
				getReportData(selectedRpt, theParams, reportDataSuccess, false);
			}
  			break;
		case "Chart":
			reportDataSuccess = function(data, textStatus, jqXHR){
			    				createTable(data);
			    				createChart(data);
							showChartReport(reportListing[reportListingIndex].subtype);
							};
			getReportData(selectedRpt, theParams, reportDataSuccess, false);
  			break;
		case "Pentaho":
			getPentahoReport(selectedRpt, theParams);
  			break;

		default:
  			alert("System Error: Unknown Report Type: " + reportListing[reportListingIndex].type);
	}

	enableClearButton();
}




function initialiseAllParameters() {

var parameterTableHtml = '<table><tr>';

	for (var i in listOfParameters)
	{
		switch(listOfParameters[i].displayType)
		{
			case "select":
				parameterTableHtml += '<td><div id=' + listOfParameters[i].name + '></div></td>';
  				break;
			case "date":
				parameterTableHtml += '<td><div id=' + listOfParameters[i].name + '><div class=reportLabel>' + doI18N(listOfParameters[i].label) + '</div>';
				parameterTableHtml += '<input id=' + listOfParameters[i].variable + ' type="text" size="12"/></div></td>';
  				break;
			default:
  				alert("System Error: Unknown Display Type: " + listOfParameters[i].displayType);
				return 1;
		}
	}

	parameterTableHtml += '<td valign="bottom"><select id=rptOutputType><option value="PDF" selected="selected" >PDF</option><option value="HTML">HTML</option><option value="XLS">EXCEL</option></select></td>';
	parameterTableHtml += '<td valign="bottom"><select id=rptShowType onChange="jQuery.stretchyReporting.showTypeChanged()"><option value="HTML" selected="selected" >' + doI18N("Show Table") + '</option><option value="XLS">' + doI18N("Full CSV Export") + '</option></select></td>';
	parameterTableHtml += '</tr></table>';

	$(inputParametersDiv).html(parameterTableHtml);

// set the initial values for parameters
	for (var i in listOfParameters)
	{
		parameterTableHtml = parameterTableHtml + '<td width="20">&nbsp;</td>';

		switch(listOfParameters[i].displayType)
		{
			case "select":
  				break;
			case "date":

				$('#' + listOfParameters[i].variable).datepicker({
								changeMonth: true,
								changeYear: true,
								dateFormat: 'yy-mm-dd'
								});
				var tmpDate;
				switch(listOfParameters[i].defaultVal)
				{
					case "today":
						tmpDate = setCurrentDate();
  						break;
					case "fromstart":
						tmpDate = "1900-01-01";
  						break;
					default:
						tmpDate = listOfParameters[i].defaultVal;
				}
				$('#' + listOfParameters[i].variable).val(tmpDate);
  				break;
			default:
  				alert("System Error: Unknown Display Type: " + listOfParameters[i].displayType);
				return 1;
		}
	}

	hideAllParameters();


// Populate all select (drop down list box) parameters
	for (var i in listOfParameters)
	{
		if (listOfParameters[i].displayType == 'select')
		{
			var selectOne = 'false';
			var selectAll = 'false';
			if (listOfParameters[i].selectOne == 'Y') selectOne = 'true';
			if (listOfParameters[i].selectAll == 'Y') selectAll = 'true';

			eval(generateSelectSuccessVariable(listOfParameters[i].name, listOfParameters[i].label, selectOne, selectAll));

			getReportData(listOfParameters[i].name, {}, selectSuccess, true) 
		}
	}

}



function initialiseSuccessFunctionVariables(){
var tmpRow;
setupParameterListSuccess = function(data, textStatus, jqXHR){
 					showMsgE("In setupParameterListSuccess ");

					for (var i in data.data )
					{
						tmpRow = {
  								name: data.data[i].row[0],
  								variable: data.data[i].row[1],
  								label: data.data[i].row[2],
  								displayType: data.data[i].row[3],
  								formatType: data.data[i].row[4],
  								defaultVal: data.data[i].row[5],
  								selectOne: data.data[i].row[6],
  								selectAll: data.data[i].row[7]
						}
						listOfParameters.push(tmpRow); 
					}
					initialiseAllParameters();
				};

setupReportListSuccess = function(data, textStatus, jqXHR){
 					showMsgE("In setupReportListSuccess");
					getReportData('FullParameterList', {}, setupParameterListSuccess, true);

					var prevId = -1;
					var currId;
					var tmpParameters;
					for (var i in data.data ) // create reportListing array
					{
						currId = data.data[i].row[0]
						if (currId != prevId)
						{
							tmpParameters = [];
							if (!(data.data[i].row[5] == null))
							{
								tmpParam = [];
								tmpParam.push(data.data[i].row[6]);
								tmpParam.push(data.data[i].row[7]);
								tmpParameters.push(tmpParam);
							}
							tmpRow = {
  									id: data.data[i].row[0],
  									name: data.data[i].row[1],
  									type: data.data[i].row[2],
  									subtype: data.data[i].row[3],
  									category: data.data[i].row[4],
  									parameters: tmpParameters
							}
							reportListing.push(tmpRow); 
							prevId = currId;

						}
						else
						{ 
							tmpParam = [];
							tmpParam.push(data.data[i].row[6]);
							tmpParam.push(data.data[i].row[7]);
							reportListing[(reportListing.length - 1)].parameters.push(tmpParam);
						}
					}


					var reportSelectData = [];
					for (var i in reportListing)
					{
						reportSelectData.push({id: reportListing[i].name, name: reportListing[i].name});
					}
					var listOfReportsTag = {divName: reportsListDiv,
									label: reportsListDivLabel,
									selectOnClick: 'jQuery.stretchyReporting.newReportSelected(options[selectedIndex].value)',
									selectData: reportSelectData
									}
								
					makeSelectTag(listOfReportsTag, true, false, true);
			};
}

function copyXLSon() {
	showMsg("Turning them on");
	$('#ToolTables_RshowTable_0').css('display', 'inline');
	$('#ToolTables_RshowTable_1').css('display', 'inline');
	ttInstances = TableTools.fnGetMasters();
	for (i in ttInstances) {
		ttInstances[i].that.fnResizeButtons();
	}
	filteredCopyXLSon = true;

}

function copyXLSoff() {
	showMsg("Turning them off");
	$('#ToolTables_RshowTable_0').css('display', 'none');
	$('#ToolTables_RshowTable_1').css('display', 'none');
	filteredCopyXLSon = false;
}

function applyFilterRules() {

		if (maxRowsForCopyandXLS >= fnRecordsDisplay())
		{
			showMsg("Max: " + maxRowsForCopyandXLS + " >= " + fnRecordsDisplay());
			if (filteredCopyXLSon == true) showMsg("copy xls on - so no need to do anything")
			else
			{
				showMsg("copy xls off - so can turn back on");
				copyXLSon();
				oTable.fnDraw(); 
			}
		}
		else
		{
			showMsg("Max: " + maxRowsForCopyandXLS + " < " + fnRecordsDisplay());
			if (filteredCopyXLSon == true)
			{
				showMsg("copy xls on - so have to turn off");
				copyXLSoff();
				oTable.fnDraw(); 
			}
			else showMsg("copy xls off - so no need to do anything");
		}

}

function initialiseDataTableDef() {
dataTableDef = {
		// "sDom": 'lfTip<"top"<"clear">>rtlfTip<"bottom"<"clear">>',
		"sDom": 'lfTip<"top"<"clear">>rt',
		"oTableTools": {
				"aButtons": [{	"sExtends": "copy",
							"sButtonText": doI18N("Copy to Clipboard")
									}, 
						{	"sExtends": "xls",
							"sButtonText": doI18N("Save to CSV")
						}
						// {
						// "sExtends": "print",
						// "sButtonText": doI18N("Print")
						// }
						],
				"sSwfPath": resValue + "DataTables-1.8.2/extras/TableTools/media/swf/copy_cvs_xls.swf"
			        },
			        
		"aaData": [],
		"aoColumns": [],
		"sPaginationType": "full_numbers",
		"bDeferRender": true,
		"bProcessing": true,
		"aLengthMenu": [[5, 10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
		// "bJQueryUI": true
	}

}


function generateSelectSuccessVariable(paramName, paramLabel, selectOne, selectAll) {

return 'var selectSuccess = function(data, textStatus,jqXHR){' +
					' var selectData = [];' +
					' for (var i in data.data )' +
					' {' +
						'selectData.push({id: data.data[i].row[0], name: data.data[i].row[1]});' + 
					' }' +
			    		" selectTagParams = {divName: '#" + paramName + "'," +
									"label: '" + paramLabel + "'," +
									"selectOnClick: '$.stretchyReporting.nothingCallback()'," +
									"selectData: selectData " +
									'};' +
					' makeSelectTag(selectTagParams, ' + selectOne + ', ' + selectAll + ', false);' +
			'};';

}


function createChart(theData) {

	RData = new google.visualization.DataTable();
      RData.addColumn('string', dataTableDef.aoColumns[0].sTitle);
      RData.addColumn('number', dataTableDef.aoColumns[1].sTitle);
      RData.addRows(dataTableDef.aaData.length);

	var ii = 0;
	var numberData;
	var dblQuotePos;
	for (var i in dataTableDef.aaData)
	{
		RData.setValue(ii, 0, dataTableDef.aaData[i][0]);
		
		if (dataTableDef.aoColumns[1].sType == "title-numeric" && dataTableDef.aaData[i][1].substr(0, 11) == '<span title') numberData = getOrigTitleNumericValue(dataTableDef.aaData[i][1])
		else numberData = dataTableDef.aaData[i][1];

		RData.setValue(ii, 1, numberData);
		ii = ii + 1;
  	};
}


function createTable(theData) {

	rowsDisplayable = 0;
	var tableColumns = [];
	for (var i in theData.columnHeaders)
	{
		var tmpSType;
		var tmpSClass = "";
		switch(theData.columnHeaders[i].columnType)
		{
			case "VARCHAR":
  				tmpSType = 'string';
  				break;
			case "DECIMAL":
  				tmpSType = 'title-numeric';
				tmpSClass = "rptAlignRight";
				break;
			case "DOUBLE":
  				tmpSType = 'title-numeric';
				tmpSClass = "rptAlignRight";
				break;
			case "BIGINT":
  				tmpSType = 'numeric';
				tmpSClass = "rptAlignRight";
  				break;
			case "SMALLINT":
  				tmpSType = 'numeric';
				tmpSClass = "rptAlignRight";
  				break;
			case "INT":
  				tmpSType = 'numeric';
				tmpSClass = "rptAlignRight";
  				break;
			default:
  				tmpSType = 'string';
		}
		tableColumns.push({ "sTitle": doI18N(theData.columnHeaders[i].columnName), 
					"sOriginalHeading": theData.columnHeaders[i].columnName,
					// "dataType": tmpSType,
					"sType": tmpSType,
					"sClass": tmpSClass
					});
	}

	var convNum = "";
	var tmpVal;
	var tableData = [];
	for (var i in theData.data )
	{
		var tmpArr = [];
		for (var j in theData.data[i].row)
		{
			tmpVal = theData.data[i].row[j];
			switch(tableColumns[j].sType)
			{
			case "string":
				if (tmpVal == null) tmpVal = "";
				tmpVal = convertCRtoBR(tmpVal);
  				break;
			case "numeric":
				if (tmpVal == null) tmpVal = ""
				else tmpVal = parseInt(tmpVal);
  				break;
			case "title-numeric":
				if (tmpVal == null) tmpVal = '<span title="' + nullTitleValue  + '"></span>' + "";
				else
				{
					convNum = formatNumber(parseFloat(tmpVal));
					tmpVal = '<span title="' + tmpVal + '"></span>' + convNum;
				}
  				break;
			default:
  				alert("System Error - Type not Found: " + tableColumns[j].sType);
			}
			tmpArr.push(tmpVal);
		}
		tableData.push(tmpArr);
	}

	maxRowsForCopyandXLS = Math.round(tableSizeLimit / (tableColumns.length));
	showMsg("Setting at Start maxRowsForCopyandXLS: " + maxRowsForCopyandXLS );
	filteredCopyXLSon = true;
	
	dataTableDef.aaData = tableData;
	dataTableDef.aoColumns= tableColumns;
	dataTableDef.aaSorting = [];
	dataTableDef.fnDrawCallback = function() {
      						showMsg( 'DataTables has redrawn the table' );
							if (isNewTable == false) applyFilterRules()
							else isNewTable = false;
    						};
								
}

function convertCRtoBR(str) {
    return str.replace(/(\r\n|[\r\n])/g, "<br />");
}

function showTableReport() {
	isNewTable = true;
	$('#StretchyReportOutput').html( '<table cellpadding="0" cellspacing="1" border="0" class="display" id="RshowTable" width=100%></table>' );
	oTable = $('#RshowTable').dataTable(dataTableDef);	
	oSettings = oTable.fnSettings();

	showMsg("1st recs displayed is: " + fnRecordsDisplay());
	applyFilterRules();
}


function showChartReport(rptSubType) {
/*
 * $('#StretchyReportOutput').html( '<table><tr><td width="25%" valign="top"><table
 * cellpadding="0" cellspacing="1" border="0" class="display" id="RshowTable"
 * width=100%></table></td><td width="75%" align="right"><div
 * id=RshowChart></div></td></tr></table>' );
 * $('#RshowTable').dataTable(dataTableDef);
 */

	$('#StretchyReportOutput').html( '<table><tr></td><td width="100%" align="center"><div id=RshowChart></div></td></tr></table>' );


var options;
var Rchart;

		switch(rptSubType)
		{
			case "Pie":
      			options = {
						legend: 'right', is3D: true, 
						width: 1000, height: 600
						// chartArea: {left:0,top:0, width:"100%",height:"100%"}
						};

      			Rchart = new google.visualization.PieChart(document.getElementById('RshowChart'));
        			Rchart.draw(RData, options);
  				break;
			case "Bar":
        			options = {
          					width: 1000, height: 600
         					// vAxis: {title: 'xxxxxxx', titleTextStyle: {color:
							// 'red'}}
						// chartArea: {left:0,top:0, width:"100%",height:"100%"}
       					};

        					Rchart = new google.visualization.BarChart(document.getElementById('RshowChart'));
        					Rchart.draw(RData, options);
  				break;
			default:
  				alert("System Error: Unknown Chart Type: " + rptSubType);
		}





/*
 * var Rchart = new
 * google.visualization.PieChart(document.getElementById('RshowChart'));
 * Rchart.draw(RData, {legend: 'right', is3D: true, width: 400, height: 500,
 * chartArea: {left:0,top:0, width:"100%",height:"100%"}});
 */
/*
 * var chartCategories = []; for (var i in dataTableDef.aaData) {
 * chartCategories.push(dataTableDef.aaData[i][0]); }
 * 
 * var chartSeries = [];
 * 
 * for (var i in dataTableDef.aaData) { chartSeries.push({data:
 * dataTableDef.aaData[i][1]}); }
 * 
 * 
 * var Rchart = new Highcharts.Chart({ chart: { renderTo: 'RshowChart',
 * defaultSeriesType: 'bar' }, title: { text: 'Historic World Population by
 * Region' }, xAxis: { categories: chartCategories, title: { text: null } },
 * yAxis: { min: -5000000, title: { text: 'Population (millions)', align: 'high' } },
 * tooltip: { formatter: function() { return ''+ this.series.name +': '+ this.y +'
 * millions'; } }, plotOptions: { bar: { dataLabels: { enabled: true } } },
 * legend: { layout: 'vertical', align: 'right', verticalAlign: 'top', x: -100,
 * y: 100, floating: true, borderWidth: 1, backgroundColor: '#FFFFFF', shadow:
 * true }, credits: { enabled: false }, series: chartSeries });
 */


}


function getReportData(rptName, inParams, successFunction, isParameterType) {
	if (isAuthRequest == true) getReportDataAuth(rptName, inParams, successFunction, isParameterType)
	else getReportDataNoAuth(rptName, inParams, successFunction, isParameterType);
}


function getReportDataAuth(rptName, inParams, successFunction, isParameterType) {
alert("needs fixing up, dont rely on data")
return
	var inQueryParameters =  {};
	for (var i in inParams ) inQueryParameters["R_" + i] = inParams[i];
	if (rptDB > "") inQueryParameters["R_rptDB"] = rptDB;
	
//	OAuthSimple().reset();
//	var OAuthProcess = (new OAuthSimple()).sign({
//		path : RESTUrl,
//		parameters : inQueryParameters,
//		signatures : {
//			'consumer_key' : apiKey,
//			'shared_secret' : sharedSecret,
//			'access_token' : accessToken,
//			'access_secret' : tokenSecret
//		}
//	});

	showMsgE("getReportData IS Auth: " + inQueryParameters);
	$.ajax({
		url: RESTUrl,
		type:'GET',
		dataType: 'json',
		contentType: "application/json; charset=utf-8",
		crossDomain: false,
		cache: false,
		beforeSend: function( xhr ) {
			xhr.setRequestHeader("Authorization", "Basic " + basicAuthKey);
		},
		success: successFunction,
		error:function(jqXHR, textStatus, errorThrown){
			showMsgE("getReportData IS Auth Error: ");
	    	var jsonValue = jQuery.parseJSON(jqXHR.responseText);
	    	alert("Response Text: " + jqXHR.responseText + "    Text Status: " + textStatus);
		}
	});


}
	

function buildReportParms(inParams) {

	var paramCount = 1;
	var reportParams = "";
	for (var i in inParams )
	{
		if (paramCount > 1) reportParams += "&"
		reportParams += encodeURIComponent("R_" + i) + "=" + encodeURIComponent(inParams[i]);
		paramCount = paramCount + 1;
	}
	if (rptDB > "") 
	{
		if (paramCount > 1) reportParams += "&"
		reportParams =  encodeURIComponent("R_rptDB") + "=" + encodeURIComponent(rptDB);
	}
	
	return reportParams
}

function getReportDataNoAuth(rptName, inParams, successFunction, isParameterType) {
	
	var inQueryParameters = buildReportParms(inParams);
	if (isParameterType == true)
	{
		if (inQueryParameters > "") inQueryParameters += "&parameterType=true"
		else inQueryParameters = "parameterType=true"
	}
	
	showMsgE("getReportDataNoAuth: " + inQueryParameters);
	$.ajax({
			url: RESTUrl + "/" + rptName,
			type:'GET',
			dataType: 'json',
			data: inQueryParameters,
			contentType: "application/json; charset=utf-8",
			crossDomain: false,
			cache: false,
			beforeSend : function(xhr) {
				xhr.setRequestHeader("Authorization", "Basic " + basicAuthKey);
			},
			success: successFunction,
			error:function(jqXHR, textStatus, errorThrown){
				showMsgE("getReportDataNoAuth: ");
		    	alert("Response Text: " + jqXHR.responseText + "    Text Status: " + textStatus + "     Error Thrown: " + errorThrown);
			    	var jsonValue = jQuery.parseJSON(jqXHR.responseText);
				alert("Last Alert - " + jsonValue.value + "for "  + jsonValue.field + " : " + jsonValue.code);
			}
	});

}

	
function getExportCSV(rptName, inParams, isParameterType) {

	var inQueryParameters = buildReportParms(inParams);
	if (inQueryParameters > "") inQueryParameters = "?" + inQueryParameters + "&exportCSV=true"
	else inQueryParameters = "?exportCSV=true";
	if (isParameterType == true) inQueryParameters += "&parameterType=true";
	
	var fullExportUrl = RESTUrl + "/" + rptName + inQueryParameters;
	showMsg("full export url: " + fullExportUrl);
	var loadHTML = '<iframe id=rptLoadingFrame src="' + fullExportUrl + '" frameborder="0" onload="jQuery.stretchyReporting.clearLoadingImg();" width="100%" height="600px" style="background:url(';
		loadHTML += "'" + loadingImg + "'" + ') no-repeat scroll 50% 100px;"><p>Your browser does not support iframes.</p></iframe>';

	$('#StretchyReportOutput').html(loadHTML);

}


function getPentahoReport(rptName, inParams) {
//todo 
	var currentReportName = inparams.name; //remember to include this when doing pentaho example
	var inQueryParameters =  "?output-type=" + $('#rptOutputType option:selected').val();
	// var paramCount = 1;
	for (var i in inParams )
	{
		// if (paramCount > 1) inQueryParameters += "&"
		// else inQueryParameters += "?";
		inQueryParameters += "&" + encodeURIComponent(i) + "=" + encodeURIComponent(inParams[i]);
		// paramCount = paramCount + 1;
	}

	var fullReportUrl = pentahoUrl + inQueryParameters;
	showMsg("full pentaho url: " + fullReportUrl);

	var loadHTML = '<iframe src="' + fullReportUrl + '" frameborder="1" width="100%" height="600px" style="background:url(';
		loadHTML += "'" + loadingImg + "'" + ') no-repeat scroll 50% 100px;"><p>Your browser does not support iframes.</p></iframe>';

	$('#StretchyReportOutput').html(loadHTML);

}

function clearLoadingImg() {
	$('#rptLoadingFrame').css("background", "");
}

function parameterValidationErrors(reportParams)
{
var tmpStartDate = "";
var tmpEndDate = "";
	for (var i in reportParams)
	{
		// alert("rpt param:" + reportParams[i][1]);
		var paramDetails = getParameterDetailsUsingName(reportParams[i][1]);
		
		switch(paramDetails.displayType)
		{
			case "select":
				var selectedVal = $('#' + paramDetails.name + ' select option:selected').val();
				if (selectedVal == 0)
				{
        				alert(doI18N("appropriateValues"));
					return 1;
				}
  				break;
			case "date":
				var tmpDate = $('#' + paramDetails.variable).val();
				if (!(tmpDate > ""))
				{
        				alert(doI18N("appropriateDateValues"));
					return 1;
				}
				if (invalidDate(tmpDate) == true)
				{
        				alert(doI18N("invalidDate") + " " + tmpDate);
					return 1;
				}

				if (paramDetails.variable == "startDate") tmpStartDate = tmpDate;
				if (paramDetails.variable == "endDate") tmpEndDate = tmpDate;
  				break;
			default:
  				alert("System Error: Unknown Display Type: " + paramDetails.displayType);
				return 1;
		}
	}

	if (tmpStartDate > "" && tmpEndDate > "")
	{
		if (tmpStartDate > tmpEndDate)
		{
        		alert(doI18N("startAfterEndDate"));
			return 1;
		}
	}

	return 0;
}


function getParameterDetailsUsingName(inputParam) {

	for (var i in listOfParameters )
	{
		if (listOfParameters[i].name == inputParam)
		{
			return listOfParameters[i];
		}
	}

	alert("System Error - Parameter not Found: " + inputParam);
	return {name: "unknownInputField"};
}


function initI18N(lang) {

	if (I18N_Needed == "Y")
	{
		var Ilang = lang;
		if (Ilang == "") Ilang = $.i18n.browserLang();
		jQuery.i18n.properties({
    			name:'Messages', 
    			path: bundleDir, 
    			mode:'map',
    			cache: true,
    			language: Ilang, 
    			callback: function() { 
    						}
		});
	}
}


function changeLanguage(newLanguage) {
	initI18N(newLanguage);

// run and clear buttons
	$(runReportDiv).button({ label: doI18N("rpt_run") });
	$(clearReportDiv).button({ label: doI18N("rpt_clear") });


// report listing
	$(reportsListDiv + ' div').text(doI18N(reportsListDivLabel));
	$(reportsListDiv + ' option').each(function(index) {
				if ($(this).val() == "0") $(this).text(doI18N('rpt_select_one'))
    				else
				{
					if ($(this).val() == "-1") $(this).text(doI18N('rpt_select_all'))
					else
					{
						$(this).text(doI18N($(this).val()))
					}
				}
			});

// parameter labels
	for (var i in listOfParameters)
	{
		$('#' + listOfParameters[i].name + ' div').text(doI18N(listOfParameters[i].label));

// parameter options select one/all
		if (listOfParameters[i].displayType == 'select')
		{
			$('#' + listOfParameters[i].name + ' option').each(function(index) {
				if ($(this).val() == "0") $(this).text(doI18N('rpt_select_one'))
    				else
				{
					if ($(this).val() == "-1") $(this).text(doI18N('rpt_select_all'))
				}
			});
		}
	}

// Data output (if any)
	if ($('#StretchyReportOutput').html() > "" )
	{
		for (var i in dataTableDef.aoColumns)
		{
			var tmpval = dataTableDef.aoColumns[i].sTitle ;
			dataTableDef.aoColumns[i].sTitle = doI18N(dataTableDef.aoColumns[i].sOriginalHeading);
		}
		redrawOutput();
	}

}

function doI18N(xlateStr, params) { 

	if (I18N_Needed == "Y")
	{
		if (highlightMissingXlations == "Y") return jQuery.i18n.prop(xlateStr, params)
		else
		{
			var xlated = jQuery.i18n.prop(xlateStr, params);
			if (xlated.substr(0,1) == "[" && xlated.substr(xlated.length - 1, 1) == "]") return xlated.substr(1, xlated.length - 2)
			else return xlated;
		}
	}
	else 
	{
		var retStr = xlateStr;
		if (retStr == 'rpt_select_one') retStr = 'Select One';
		if (retStr == 'rpt_select_all') retStr = 'Select All';
		return retStr;
	}
}
              

function changeDecimals(newDecimals) {

	decimalsNo = newDecimals;
	updateNumberData();
}

function changeSeparator(sepChar, decChar, indFormat) {

	separatorChar = sepChar;
	decimalChar = decChar;
	indianFormat = indFormat;
	updateNumberData();
}


function updateNumberData() {
// Data output (if any)
	if ($('#StretchyReportOutput').html() > "" )
	{
		var tmpVal;
		var origVal;
		var convNum;
		for (var i in dataTableDef.aaData )
		{
			for (var j in dataTableDef.aaData[i])
			{
				if (dataTableDef.aoColumns[j].sType == "title-numeric")
				{
					origVal = getOrigTitleNumericValue(dataTableDef.aaData[i][j]);
					if (origVal == nullTitleValue) convNum = ""
					else convNum = formatNumber(parseFloat(origVal));
					dataTableDef.aaData[i][j] = '<span title="' + origVal + '"></span>' + convNum;
				}
			}
		}
		redrawOutput();
	}

}

function redrawOutput() {

		clearTheReport();
		var reportListingIndex = getReportListingIndexSelectedReport();
		switch(reportListing[reportListingIndex].type)
		{
			case "Table":
				showTableReport();
  				break;
			case "Chart":
				showChartReport();
			case "Pentaho":
  				break;
			default:
  				alert("System Error: Unknown Report Type: " + reportListing[reportListingIndex].type);
		}
		enableClearButton();

}

function setTableSizeLimit() {
	if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){ // test for MSIE x.x;
 		var ieversion=new Number(RegExp.$1) // capture x.x portion and store as
											// a number
 		if (ieversion>=9) return 50000
 		else return 3000;
	}
	else return 50000;

}

function showMsg(msg) {
// remove except for testing console.log(msg);
}

function showMsgE(msg) {
	// console.log(msg);
}


})(jQuery);



