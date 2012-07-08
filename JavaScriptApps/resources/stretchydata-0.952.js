(function($) {

	$.stretchyData = {};

	$.stretchyData.displayAllExtraData = function(params) {

		if (!(params.url)) {
			alert(doI18N("reportingInitError - url parameter"));
			return;
		}
		if (!(params.basicAuthKey)) {
			alert(doI18N("reportingInitError - basicAuthKey parameter"));
			return;
		}
		if (!(params.datasetType)) {
			alert(doI18N("reportingInitError - datasetType parameter"));
			return;
		}
		if (!(params.datasetPKValue)) {
			alert(doI18N("reportingInitError - datasetPKValue parameter"));
			return;
		}
		if (!(params.datasetTypeDiv)) {
			alert(doI18N("reportingInitError - datasetTypeDiv parameter"));
			return;
		}


		editLabel = "Edit";	
		if (params.editLabel) editLabel = params.editLabel;

		saveLabel = "Save";
		if (params.saveLabel) saveLabel = params.saveLabel;

		cancelLabel = "Cancel";		
		if (params.cancelLabel) cancelLabel = params.cancelLabel;


		var headingPrefix = "";
		if (params.headingPrefix)
			headingPrefix = params.headingPrefix;
		var headingClass = "";
		if (params.headingClass)
			headingClass = params.headingClass;
		var labelClass = "";
		if (params.labelClass)
			labelClass = params.labelClass;
		var valueClass = "";
		if (params.valueClass)
			valueClass = params.valueClass;
		displayAllExtraData(params.url, params.basicAuthKey, params.datasetType,
				params.datasetPKValue, params.datasetTypeDiv, headingPrefix,
				headingClass, labelClass, valueClass);
	};

	$.stretchyData.popupEditDialog = function(url, basicAuthKey, datasetType, datasetName, datasetPKValue, dsnDivName, title, width, height) {
		popupEditDialog(url, basicAuthKey, datasetType, datasetName, datasetPKValue, dsnDivName, title, width, height);
	};

	displayAllSuccessFunction = function(data, textStatus, jqXHR) {

		var headingClassStr = "";
		if (displayAllVars.headingClass > "")
			headingClassStr = ' class="' + displayAllVars.headingClass + '" ';

		var extraDataNamesVar = "";
		for ( var i in data) {
			var dsnDivName = generateDsnDivName(displayAllVars.datasetType, i,
					displayAllVars.datasetTypeDiv);
			extraDataNamesVar += '<br><span ' + headingClassStr + '><b>'
					+ doI18N(displayAllVars.headingPrefix)
					+ doI18N(data[i].name)
					+ ' - </span></b> ';

			extraDataNamesVar += editExtraDataLink(displayAllVars.url, displayAllVars.basicAuthKey,
					displayAllVars.datasetType,
					data[i].name,
					displayAllVars.datasetPKValue, dsnDivName);

			extraDataNamesVar += '<div id="' + dsnDivName + '">';
			extraDataNamesVar += '</div>';
		}
		$('#' + displayAllVars.datasetTypeDiv).html(extraDataNamesVar);
		for ( var i in data) {
			var dsnDivName = generateDsnDivName(displayAllVars.datasetType, i,
					displayAllVars.datasetTypeDiv);
			viewExtraData(displayAllVars.url, displayAllVars.basicAuthKey, displayAllVars.datasetType,
					data[i].name,
					displayAllVars.datasetPKValue, dsnDivName);
		}

	};

	displayAllErrorFunction = function(jqXHR, textStatus, errorThrown) {
		alert(jqXHR.responseText);
	};

	function displayAllExtraData(url, basicAuthKey, datasetType, datasetPKValue,
			datasetTypeDiv, headingPrefix, headingClass, labelClass, valueClass) {

		displayAllVars = {
			url : url,
			basicAuthKey : basicAuthKey,
			datasetType : datasetType,
			datasetPKValue : datasetPKValue,
			datasetTypeDiv : datasetTypeDiv,
			headingPrefix : headingPrefix,
			headingClass : headingClass,
			labelClass : labelClass,
			valueClass : valueClass
		};

		displayAllUrl = url + "additionalfields?type=" + encodeURIComponent(datasetType);
		getData(displayAllUrl, basicAuthKey, displayAllSuccessFunction, displayAllErrorFunction);
	}

	function editExtraDataLink(url, basicAuthKey, datasetType, datasetName, datasetPKValue,
			dsnDivName) {

		var popupVar = "jQuery.stretchyData.popupEditDialog('" + url + "', '" + basicAuthKey + "', '"
				+ datasetType + "', '" + datasetName + "', " + datasetPKValue
				+ ", '" + dsnDivName + "', '" + editLabel + " "
				+ doI18N(datasetName) + "', 900, 500);return false;";
		var editLink = '<A HREF="unknown.html" onClick="' + popupVar + '">'
				+ editLabel + '</A><br>';
		return editLink;
	}

	function viewExtraDataset(data, dsnDivName) {

		var dataLength = data.data.length;
		var extraDataViewVar = '<table width="100%"><tr>';

		var colsPerRow = 2;
		var colsPerRowCount = 0;
		var labelClassStr = "";
		var valueClassStr = "";
		if (displayAllVars.labelClass > "")
			labelClassStr = ' class="' + displayAllVars.labelClass + '" ';
		if (displayAllVars.valueClass > "")
			valueClassStr = ' class="' + displayAllVars.valueClass + '" ';

		for ( var i in data.columnHeaders) {
			if (!(data.columnHeaders[i].columnName == "id")) {
				colsPerRowCount += 1;
				if (colsPerRowCount > colsPerRow) {
					extraDataViewVar += '</tr><tr>';
					colsPerRowCount = 1;
				}
				var colVal = "";
				if (dataLength > 0)
					colVal = data.data[0].row[i];
				if (colVal == null)
					colVal = "";

				if (colVal > "" && data.columnHeaders[i].columnType == "Text")
					colVal = '<textarea rows="3" cols="40" readonly="readonly">'
							+ colVal + '</textarea>';
				extraDataViewVar += '<td valign="top"><span ' + labelClassStr
						+ '>' + doI18N(data.columnHeaders[i].columnName)
						+ ':</span></td><td valign="top"><span '
						+ valueClassStr + '>' + colVal + '</span></td>';
			}
		}
		extraDataViewVar += '</tr></table>';
		$('#' + dsnDivName).html(extraDataViewVar);
		// alert(dsnDivName + ": " + dsnDivName + extraDataViewVar);
	}
	;
	viewExtraDataErrorFunction = function(jqXHR, textStatus, errorThrown) {
		alert("err: " + jqXHR.responseText);
	};

	function viewExtraData(url, basicAuthKey, datasetType, datasetName, datasetPKValue, dsnDivName) {
		var viewExtraDataUrl = url + "additionalfields/"
				+ encodeURIComponent(datasetType) + "/"
				+ encodeURIComponent(datasetName) + "/"
				+ encodeURIComponent(datasetPKValue);

		var evalViewExtraDataSuccessFunction = "var viewExtraDataSuccessFunction = function(data, textStatus, jqXHR){  viewExtraDataset(data, '"
				+ dsnDivName + "'); };"
		eval(evalViewExtraDataSuccessFunction);
		getData(viewExtraDataUrl, basicAuthKey, viewExtraDataSuccessFunction, viewExtraDataErrorFunction);
	}

	popupEditErrorFunction = function(jqXHR, textStatus, errorThrown) {
		handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	};
	popupEditSuccessFunction = function(data, textStatus, jqXHR) {
		currentEditPopup.dialogDiv.append(extraDataBuildTemplate(data));
		extraDataOpenDialog(currentEditPopup.url, currentEditPopup.basicAuthKey);
		extraDataFormatDates(data.columnHeaders);
	};
	
	function popupEditDialog(url, basicAuthKey, datasetType, datasetName, datasetPKValue, dsnDivName, title, width, height) {

		currentEditPopup = {
			dialogDiv : $("<div id='dialog-form'></div>"),
			dsnDivName : dsnDivName,
			title : title,
			width : width,
			height : height,
			datasetType : datasetType,
			datasetName : datasetName,
			datasetPKValue : datasetPKValue,
			baseUrl : url,
			basicAuthKey : basicAuthKey,
			url : url + "additionalfields/"
					+ encodeURIComponent(datasetType) + "/"
					+ encodeURIComponent(datasetName) + "/"
					+ encodeURIComponent(datasetPKValue)
		};
		getData(currentEditPopup.url, currentEditPopup.basicAuthKey, popupEditSuccessFunction, popupEditErrorFunction);
	}

	function generateDsnDivName(datasetType, i, uniqueDivid) {
		return extraDataUnderscore(datasetType) + "_" + i + "_"
				+ extraDataUnderscore(uniqueDivid);
	}

	/* start of code to fill data in edit form */

	function extraDataUnderscore(colName) {
		return colName.replace(/ /g, "_")
	}

	function extraDataColDisplayHTML(colName, colType, colLength,
			colDisplayType, colAllowedValues, colVal) {
		var displayHTML = '<td valign="top"><label>' + colName
				+ ':</label></td><td valign="top">';
		var valueAttr = ""
		if (colVal != null)
			valueAttr = 'value="' + colVal.replace(/"/g, "&quot;") + '"';
		var colNameUnderscore = extraDataUnderscore(colName);

		switch (colType) {
		case "String":
			var colAllowedValuesLength = colAllowedValues.length;
			if (colDisplayType != "List") {
				var colSize = 40;
				if (colLength < colSize)
					colSize = colLength;
				displayHTML += '<input id="' + colNameUnderscore + '" name="'
						+ colNameUnderscore + '" size="' + colSize + '" '
						+ valueAttr + ' type="text"/>'
			} else
				displayHTML += extraDataSelectTag(colNameUnderscore,
						colAllowedValues, colVal);
			break;
		case "Text":
			var textVal = "";
			if (colVal != null)
				textVal = colVal;
			displayHTML += '<textarea id="' + colNameUnderscore + '" name="'
					+ colNameUnderscore + '" rows="4" cols="40">' + textVal
					+ '</textarea>';
			break;
		case "Date":
			displayHTML += '<input id="' + colNameUnderscore + '" name="'
					+ colNameUnderscore + '" size="12" ' + valueAttr
					+ ' type="text"/>';
			break;
		case "Decimal":
			displayHTML += '<input id="' + colNameUnderscore + '" name="'
					+ colNameUnderscore + '" size="12" ' + valueAttr
					+ ' type="text"/>';
			break;
		case "Integer":
			displayHTML += '<input id="' + colNameUnderscore + '" name="'
					+ colNameUnderscore + '" size="12" ' + valueAttr
					+ ' type="text"/>';
			break;
		default:
			displayHTML += "'" + colType + "'";
		}
		displayHTML += '</td>';
		return displayHTML;
	}

	function extraDataSelectTag(colName, colAllowedValues, colVal) {

		var selectedVal = "";
		var selectHtml = '<select id="' + colName + '" name="' + colName + '">';

		if ((colVal == null) || (colVal == ""))
			selectedVal = ' selected="selected" '
		else
			selectedVal = "";
		selectHtml += '<option value=""' + selectedVal + '></option>';

		for ( var i in colAllowedValues) {
			if (colVal == colAllowedValues[i])
				selectedVal = ' selected="selected" '
			else
				selectedVal = "";
			selectHtml += '<option value="' + colAllowedValues[i] + '"'
					+ selectedVal + '>' + colAllowedValues[i] + '</option>';
		}

		selectHtml += '</select>';
		// alert(selectHtml);
		return selectHtml;
	}

	function extraDataBuildTemplate(data) {

		var dataLength = data.data.length;

		var extraDataTemplateVar = '<form id="entityform">    <div id="formerrors"></div>';
		extraDataTemplateVar += '<table width="100%"><tr>';

		var colsPerRow = 2;
		var colsPerRowCount = 0;

		for ( var i in data.columnHeaders) {

			if (!(data.columnHeaders[i].columnName == "id")) {
				colsPerRowCount += 1;
				if (colsPerRowCount > colsPerRow) {
					extraDataTemplateVar += '</tr><tr>';
					colsPerRowCount = 1;
				}
				var colVal = "";
				if (dataLength > 0)
					colVal = data.data[0].row[i];
				extraDataTemplateVar += extraDataColDisplayHTML(
						data.columnHeaders[i].columnName,
						data.columnHeaders[i].columnType,
						data.columnHeaders[i].columnLength,
						data.columnHeaders[i].columnDisplayType,
						data.columnHeaders[i].columnValues, colVal);
			}
		}
		extraDataTemplateVar += '</tr>';
		return extraDataTemplateVar += '</table></form>';
	}

	function extraDataFormatDates(columnHeaders) {

		for ( var i in columnHeaders) {
			// alert("in loop: " +
			// extraDataUnderscore(columnHeaders[i].columnName) + " Type: " +
			// columnHeaders[i].columnType);
			switch (columnHeaders[i].columnType) {
			case "Date":
				$('#' + extraDataUnderscore(columnHeaders[i].columnName))
						.datepicker({

							changeMonth : true,
							changeYear : true,
							dateFormat : 'yy-mm-dd'
						});

				break;
			default:
			}
		}
	}

	$.fn.serializeObject = function() {
		var o = {};
		var a = this.serializeArray();
		$.each(a, function() {
			if (o[this.name] !== undefined) {
				if (!o[this.name].push) {
					o[this.name] = [ o[this.name] ];
				}
				o[this.name].push(this.value || '');
			} else {
				o[this.name] = this.value || '';
			}
		});
		return o;
	};

	function extraDataOpenDialog(url, basicAuthKey) {

		var saveButton = saveLabel;
		var cancelButton = cancelLabel;

		var buttonsOpts = {};
		buttonsOpts[saveButton] = function() {
			$('.multiSelectedItems option').each(function(i) {
				$(this).attr("selected", "selected");
			});

			var form_data = JSON.stringify($('#entityform').serializeObject());

			var jqxhr = $.ajax({
				url : url,
				type : 'POST',
				contentType : "application/json; charset=utf-8",
				dataType : 'json',
				data : form_data,
				cache : false,
				beforeSend : function(xhr) {
					xhr.setRequestHeader("Authorization", "Basic " + basicAuthKey);
				},
				success : function(data, textStatus, jqXHR) {
					currentEditPopup.dialogDiv.dialog("close");
					viewExtraData(currentEditPopup.baseUrl, basicAuthKey, 
							currentEditPopup.datasetType,
							currentEditPopup.datasetName,
							currentEditPopup.datasetPKValue,
							currentEditPopup.dsnDivName)
				},
				error : function(jqXHR, textStatus, errorThrown) {
					handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
				}
			});
		};

		buttonsOpts[cancelButton] = function() {
			$(this).dialog("close");
		};

		currentEditPopup.dialogDiv
				.dialog(
						{
							// title: jQuery.i18n.prop(titleCode),
							title : currentEditPopup.title,
							width : currentEditPopup.width,
							height : currentEditPopup.height,
							modal : true,
							buttons : buttonsOpts,
							close : function() {
								// if i dont do this, theres a problem with
								// errors being appended to dialog view second
								// time round
								$(this).remove();
							},
							open : function(event, ui) {
								$('.multiadd')
										.click(
												function() {
													return !$(
															'.multiNotSelectedItems option:selected')
															.remove()
															.appendTo(
																	'#selectedItems');
												});

								$('.multiremove')
										.click(
												function() {
													return !$(
															'.multiSelectedItems option:selected')
															.remove()
															.appendTo(
																	'#notSelectedItems');
												});
							}
						}).dialog('open');
	}

	/* end of code to fill data in edit form and display it */

	function getData(url, basicAuthKey, successFunction, errorFunction) {

		$.ajax({
			url : url,
			type : 'GET',
			contentType : "application/json; charset=utf-8",
			dataType : 'json',
			cache : false,
			beforeSend : function(xhr) {
				xhr.setRequestHeader("Authorization", "Basic " + basicAuthKey);
			},
			success : successFunction,
			error : errorFunction
		});
	}

	function handleXhrError(jqXHR, textStatus, errorThrown, templateSelector,
			placeholderDiv) {
		
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

	function doI18N(xlateStr, params) {
		/*
		 * if (I18N_Needed == "Y") { if (highlightMissingXlations == "Y") return
		 * jQuery.i18n.prop(xlateStr, params) else { var xlated =
		 * jQuery.i18n.prop(xlateStr, params); if (xlated.substr(0,1) == "[" &&
		 * xlated.substr(xlated.length - 1, 1) == "]") return xlated.substr(1,
		 * xlated.length - 2) else return xlated; } } else { var retStr =
		 * xlateStr; if (retStr == 'rpt_select_one') retStr = 'Select One'; if
		 * (retStr == 'rpt_select_all') retStr = 'Select All'; return retStr; }
		 */
		return xlateStr;
	}

})(jQuery);
