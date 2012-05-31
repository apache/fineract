<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!-- templates below here -->

<script id="newLoanScheduleTemplate" type="text/x-jquery-tmpl">
<table id="repaymentschedule" class="pretty displayschedule">
	<caption><spring:message code="tab.client.account.loan.schedule.table.repaymentschedule.caption"/></caption>
	<thead>
		<tr>
			<th>#</th>
			<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.date"/></th>
			<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.principal"/></th>
			<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.interest"/></th>
			<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.total"/></th>
			<th><spring:message code="tab.client.account.loan.schedule.table.heading.outstanding"/></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>{{=$ctx.money(cumulativePrincipal)}}</td>
		</tr>
		{{#each scheduledLoanInstallments}}
		<tr>
			<td>{{=$ctx.number(installmentNumber)}}</td>
			<td>{{=$ctx.globalDateAsISOString(periodEnd)}}</td>
			<td>{{=$ctx.money(principalDue)}}</td>
			<td>{{=$ctx.money(interestDue)}}</td>
			<td>{{=$ctx.money(totalInstallmentDue)}}</td>
			<td>{{=$ctx.money(outStandingBalance)}}</td>
		</tr>
		{{/each}}
	</tbody>
	<tfoot class="ui-widget-header">
		<tr class="ui-widget-header">
			<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.total"/></th>
			<th>&nbsp;</th>
			<th>{{=$ctx.money(cumulativePrincipal)}}</th>
			<th>{{=$ctx.money(cumulativeInterest)}}</th>
			<th>{{=$ctx.money(cumulativeTotal)}}</th>
			<th>&nbsp;</th>
		</tr>
	</tfoot>
</table>
</script>

<script id="newLoanFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<input type="hidden" id="clientId" name="clientId" value="{{=clientId}}" />
	<input type="hidden" id="currencyCode" name="currencyCode" value="{{=selectedProduct.principal.currencyCode}}" />
	<input type="hidden" id="digitsAfterDecimal" name="digitsAfterDecimal" value="{{=$ctx.number(selectedProduct.principal.digitsAfterDecimal)}}" />
	<input type="hidden" id="dateFormat" name="dateFormat" value="dd MMMM yyyy" />
	<input type="hidden" id="locale" name="locale" value="${currentLocale}" />

	<fieldset>
		<legend><spring:message code="form.legend.loan.information"/></legend>
		<div>
			<table id="productdetails">
				<tr>
					<td valign="top">
						<label for="clientname"><spring:message code="form.label.loan.applicant"/></label>
						<input id="clientname" name="clientName" title="" type="text" value="{{=clientName}}" disabled="disabled" />

						<label for="productId"><spring:message code="form.label.product.dropdown"/></label>
						<select name="productId" id="productId">
							<option value="-1"><spring:message code="form.option.product.dropdown.first.choice"/></option>
						{{#each allowedProducts}}
							{{#if $ctx.number($parent.parent.data.productId)===$ctx.number(id)}}
									<option value="{{=$ctx.number(id)}}" selected="selected">{{=name}}</option>
							{{#else}}
									<option value="{{=$ctx.number(id)}}">{{=name}}</option>
							{{/if}}
        				{{/each}}
  						</select>
						<label for="description"><spring:message code="form.label.loan.product.description"/></label>
						<textarea id="description" rows="2" cols="50" draggable="false" disabled="disabled">{{=selectedProduct.description}}</textarea>
					</td>
					<td style="text-align:right;vertical-align:bottom;width:50px;">&nbsp;</td>
					<td valign="top">
						<label for="submittedOnDate"><spring:message code="form.label.loan.submitted.on"/></label>
						<input id="submittedOnDate" name="submittedOnDate" title="" type="text" class="datepickerfield" value="{{=$ctx.globalDate(submittedOnDate)}}" />

						<label for="submittedOnNote"><spring:message code="form.label.loan.submitted.on.note"/></label>
						<textarea id="submittedOnNote" name="submittedOnNote" rows="2" cols="50" draggable="false">{{=submittedOnNote}}</textarea>
					</td>
				</tr>
			</table>
		</div>
	</fieldset>
	<fieldset>
		<legend><spring:message code="form.legend.loan.product.loan.terms"/></legend>
		<table>
			<tr>
				<td valign="top" style="padding-right: 5px; border-right: 1px solid;">
					<table>
						<tr>
							<td><spring:message code="form.label.loan.product.loan.amount"/></td>
							<td>
								<input id="principal" name="principal" title="" type="text" value="{{=$ctx.money(selectedProduct.principal)}}" style="width: 125px;" />
								<input id="principalCurrencyCode" name="principalCurrencyCode" title="" type="text" value="{{=selectedProduct.principal.displaySymbol}}" style="width: 40px;" disabled="disabled" />
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.product.repaidevery"/></td>
							<td>
								<input id="repaymentEvery" name="repaymentEvery" title="" type="text" value="{{=$ctx.number(selectedProduct.repaymentEvery)}}" style="width: 50px;" />
								
								<select name="repaymentFrequencyType" id="repaymentFrequencyType"  title="" style="width: 121px;">
								{{#each selectedProduct.repaymentFrequencyTypeOptions}}
								{{#if $ctx.number($parent.parent.data.selectedProduct.repaymentFrequencyType.id)===$ctx.number(id)}}
									<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
								{{#else}}
									<option value="{{=$ctx.number(id)}}">{{=value}}</option>
								{{/if}}
        						{{/each}}
  								</select>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.product.numberof.repayments"/></td>
							<td>
								<input id="numberOfRepayments" name="numberOfRepayments" title="" type="text" value="{{=$ctx.number(selectedProduct.numberOfRepayments)}}" style="width: 50px;" />
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.expected.disbursement.date.on"/></td>
							<td>
							<input id="expectedDisbursementDate" name="expectedDisbursementDate" title="" type="text" class="datepickerfield" value="{{=$ctx.globalDate(expectedDisbursementDate)}}" style="width:172px;"/>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.first.repayment.on"/></td>
							<td>
							<input id="repaymentsStartingFromDate" name="repaymentsStartingFromDate" title="" type="text" class="datepickerfield" value="{{=$ctx.globalDate(repaymentsStartingFromDate)}}" style="width:172px;"/>
							</td>
						</tr>
					</table>
				</td>
				<!-- interest details cell -->
				<td valign="top" style="padding-left: 5px; padding-right: 5px; border-right: 1px solid;">
					<table>
						<tr>
							<td><spring:message code="form.label.loan.product.nominal.interestrate"/></td>
							<td>
								<input id="interestRatePerPeriod" name="interestRatePerPeriod" title="" type="text" value="{{=$ctx.decimal(selectedProduct.interestRatePerPeriod, 4)}}" style="width: 60px;" />
								
								<select name="interestRateFrequencyType" id="interestRateFrequencyType"  title="" style="width: 115px;">
								{{#each selectedProduct.interestRateFrequencyTypeOptions}}
								{{#if $ctx.number($parent.parent.data.selectedProduct.interestRateFrequencyType.id)===$ctx.number(id)}}
									<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
								{{#else}}
									<option value="{{=$ctx.number(id)}}">{{=value}}</option>
								{{/if}}
        						{{/each}}
  								</select>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.annual.nominal.interestrate"/></td>
							<td>
								<input id="interestRatePerYear" name="interestRatePerYear" title="" type="text" style="width: 60px;" disabled="disabled" />
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.product.amortization"/></td>
							<td>
								<select name="amortizationType" id="amortizationType"  title="" style="width: 178px;">
								{{#each selectedProduct.amortizationTypeOptions}}
								{{#if $ctx.number($parent.parent.data.selectedProduct.amortizationType.id)===$ctx.number(id)}}
									<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
								{{#else}}
									<option value="{{=$ctx.number(id)}}">{{=value}}</option>
								{{/if}}
        						{{/each}}
  								</select>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.product.interest.method"/></td>
							<td>
								<select name="interestType" id="interestType"  title="" style="width: 180px;">
								{{#each selectedProduct.interestTypeOptions}}
								{{#if $ctx.number($parent.parent.data.selectedProduct.interestType.id)===$ctx.number(id)}}
									<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
								{{#else}}
									<option value="{{=$ctx.number(id)}}">{{=value}}</option>
								{{/if}}
        						{{/each}}
  								</select>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.product.interest.rate.calcutated.in.period"/></td>
							<td>
							<select id="interestCalculationPeriodType" name="interestCalculationPeriodType" title="" style="width: 220px;">
								{{#each selectedProduct.interestCalculationPeriodTypeOptions}}
                                 	{{#if $ctx.number($parent.parent.data.selectedProduct.interestCalculationPeriodType.id)===$ctx.number(id)}}
										<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
									{{#else}}
										<option value="{{=$ctx.number(id)}}">{{=value}}</option>
									{{/if}}
                                {{/each}}
							</select>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.interest.charged.from"/></td>
							<td>
							<input id="interestChargedFromDate" name="interestChargedFromDate" title="" type="text" class="datepickerfield" value="{{=$ctx.globalDate(interestCalculatedFromDate)}}" style="width:172px;"/>
							</td>
						</tr>
						<tr>
							<td><spring:message code="form.label.loan.product.arrears.tolerance"/></td>
							<td>
								<input id="inArrearsTolerance" name="inArrearsTolerance" title="" type="text" value="{{=$ctx.money(selectedProduct.inArrearsTolerance)}}" style="width: 125px;" />
								<input id="inArrearsCurrencyCode" name="inArrearsCurrencyCode" title="" type="text" value="{{=selectedProduct.inArrearsTolerance.displaySymbol}}" style="width: 40px;" disabled="disabled" />
							</td>
						</tr>
					</table>
				</td>
				<td>
				<!-- Other options -->
					<div>
						<span id="toolbar" class="ui-widget-header ui-corner-all">
							<button type="submit" class="submitloanapp" id="submitloanapp" name="_eventId_submit"  title="Submit loan application.">Submit loan application</button>
							<button type="submit" class="cancelloanapp" id="cancelloanapp" name="_eventId_cancel" title="Cancel loan application.">Cancel</button>
						</span>
					</div>
				<!-- other options -->
				</td>
			</tr>
		</table>
	</fieldset>
</form>
</script>

<script id="newLoanFormTemplateMin" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<input type="hidden" id="clientId" name="clientId" value="{{=clientId}}" />
	
	<label for="clientname"><spring:message code="form.label.loan.applicant"/></label>
	<input id="clientname" name="clientName" title="" type="text" value="{{=clientName}}" disabled="disabled" />

	<label for="productId"><spring:message code="form.label.product.dropdown"/></label>
	<select name="productId" id="productId">
		<option value="-1"><spring:message code="form.option.product.dropdown.first.choice"/></option>
{{#each allowedProducts}}
	{{#if $ctx.number($parent.parent.data.productId)===$ctx.number(id)}}
		<option value="{{=$ctx.number(id)}}" selected="selected">{{=name}}</option>
	{{#else}}
		<option value="{{=$ctx.number(id)}}">{{=name}}</option>
	{{/if}}
{{/each}}
	</select>
</form>	
</script>

<script id="noteListViewTemplate" type="text/x-jquery-tmpl">
{{#if notes.length > 0}}
	<div class="ui-widget .ui-widget-content ui-corner-all notecontainer">
		<div class="ui-widget-header">
			<span class="ui-icon ui-icon-info" style="float: left; margin-right: 5px;" ></span>
			<span>{{=title}}</span>
		</div>
		<div class="ui-widget ui-widget-content ui-corner-all">
		
		{{#each notes}}
			<div>
				<div class="notespacer">
					<span class="ui-icon ui-icon-note" style="float: left; margin-right: 5px;"></span>
					{{#if noteTypeId === 100}}
					<span><spring:message code="widget.notes.label.client.note"/></span>
					{{/if}}
					{{#if noteTypeId === 200}}
					<span><spring:message code="widget.notes.label.loan.note"/></span>
					{{/if}}
					{{#if noteTypeId === 300}}
					<span><spring:message code="widget.notes.label.loan.transaction.note"/></span>
					{{/if}}
					<span><a href="#" class="editclientnote" id="editclientnotelink{{=id}}"><spring:message code="link.edit"/></a></span>
				</div>
				<div class="notecontent">{{=note}}</div>
				<div class="notespacer">
					<span class="ui-icon ui-icon-person" style="float: left; margin-right: 5px;" ></span>
					<span class="noteusername">{{=updatedByUsername}}</span>
					<span>&nbsp;{{=$ctx.globalDateTime(updatedOn)}}</span>
				</div>
			</div>
		{{/each}}
		</div>
	</div>
{{/if}}
</script>

<script id="noteFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<label for="note"><spring:message code="dialog.notes.form.label.note"/></label>
	<textarea rows="10" cols="75" id="note" name="note" cssClass="text ui-widget-content ui-corner-all">{{=note}}</textarea>
</form>
</script>

<script id="clientSearchTabTemplate" type="text/x-jquery-tmpl">
<form method="post" id="viewClient" name="viewClient" action="switchToClient">
	<label for="client"><spring:message code="label.client.search"/></label>
	<select name="client" id="client">
		<option value="0"><spring:message code="option.client.search.first.choice"/></option>
		{{#each clients}}
			<option value="{{=id}}">{{=displayName}}&nbsp;-&nbsp;({{=officeName}})</option>
        {{/each}}
  	</select>
</form>	
</script>

<script id="clientFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
	<label for="office"><spring:message code="label.office"/></label>
	<select id="officeId" name="officeId" title="">
		<option value=""><spring:message code="option.office.first.choice"/></option>
		{{#each allowedOffices}}
			{{#if $ctx.number($parent.parent.data.officeId)===$ctx.number(id)}}
				<option value="{{=$ctx.number(id)}}" selected="selected">{{=name}}</option>
			{{#else}}
				<option value="{{=$ctx.number(id)}}">{{=name}}</option>
			{{/if}}
        {{/each}}
	</select>

	<table>
		<tr>
			<td>
				<label for="firstname"><spring:message code="label.firstname"/></label>
				<input id="firstname" name="firstname" title="" type="text" value="{{=firstname}}"/>
			</td>
			<td>
				<label for="lastname"><spring:message code="label.lastname"/></label>
				<input id="lastname" name="lastname" title="" type="text" value="{{=lastname}}"/>
			</td>
		<tr>
		<tr>
			<td colspan="2">
				<label for="clientOrBusinessName"><spring:message code="label.longname"/></label>
				<input id="clientOrBusinessName" name="clientOrBusinessName" title="" type="text" value="{{=fullname}}" style="width:100%"/>
			</td>
		</tr>
	</table>

	<input type="hidden" id="dateFormat" name="dateFormat" value="dd MMMM yyyy" />
	<label for="joiningDate"><spring:message code="label.joiningdate"/></label>
	<input id="joiningDate" name="joiningDate" title="" type="text" class="datepickerfield" value="{{=$ctx.globalDate(joinedDate)}}"/>
</form>
</script>

<script id="clientAccountSummariesTemplate" type="text/x-jquery-tmpl">
{{#if $ctx.numberGreaterThanZero(anyLoanCount)}}

{{#if $ctx.numberGreaterThanZero(pendingApprovalLoanCount)}}
<div class="row">
	<span class="longrowlabel"><spring:message code="label.client.account.pending.approval.loans"/> ({{=$ctx.number(pendingApprovalLoanCount)}}):</span>
	<span class="rowvalue">
	{{#each pendingApprovalLoans}}
		<span class="loanaccount"><a href="${baseApiUrl}loans/{{=id}}" id="loan{{=id}}" class="openloanaccount" title="{{=loanProductName}}: &#35;{{=id}}">{{=loanProductName}}: &#35;{{=id}}</a></span>
	{{/each}}
	</span>
</div>
{{/if}}

{{#if $ctx.numberGreaterThanZero(awaitingDisbursalLoanCount)}}
<div class="row">
	<span class="longrowlabel"><spring:message code="label.client.account.pending.disbursal.loans"/> ({{=$ctx.number(awaitingDisbursalLoanCount)}}):</span>
	<span class="rowvalue">
	{{#each awaitingDisbursalLoans}}
		<span class="loanaccount"><a href="${baseApiUrl}loans/{{=id}}" id="loan{{=id}}" class="openloanaccount" title="{{=loanProductName}}: &#35;{{=id}}">{{=loanProductName}}: &#35;{{=id}}</a></span>
	{{/each}}
	</span>
</div>
{{/if}}

{{#if $ctx.numberGreaterThanZero(activeLoanCount)}}
<div class="row">
	<span class="longrowlabel"><spring:message code="label.client.account.active.loans"/> ({{=$ctx.number(activeLoanCount)}}):</span>
	<span class="rowvalue">
	{{#each openLoans}}
		<span class="loanaccount"><a href="${baseApiUrl}loans/{{=id}}" id="loan{{=id}}" class="openloanaccount" title="{{=loanProductName}}: &#35;{{=id}}">{{=loanProductName}}: &#35;{{=id}}</a></span>
	{{/each}}
	</span>
</div>
{{/if}}

{{#if $ctx.numberGreaterThanZero(closedLoanCount)}}
<div class="row">
	<div class="longrowlabel"><spring:message code="label.client.account.closed.loans"/> ({{=$ctx.number(closedLoanCount)}}):</div>
	<div class="rowvalue">
	{{#each closedLoans}}
		<span class="loanaccount"><a href="${baseApiUrl}loans/{{=id}}" id="loan{{=id}}" class="openloanaccount" title="{{=loanProductName}}: &#35;{{=id}}">{{=loanProductName}}: &#35;{{=id}}</a></span>
	{{/each}}
	</div>
</div>
{{/if}}

{{#else}}
<div>
	<spring:message code="label.client.account.no.loans.exist"/>
</div>
{{/if}}
</script>

<script id="clientDataTabTemplate" type="text/x-jquery-tmpl">
<span id="toolbar" class="ui-widget-header ui-corner-all">
    <!--
	<button id="cashflowbtn{{=id}}" class="casflowbtn">New cashflow analysis</button>
    -->
	<sec:authorize access="hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE', 'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE')">
	<button id="newloanbtn{{=id}}" class="newloanbtn">New loan application</button>
	</sec:authorize>

	<button id="addnotebtn{{=id}}" class="addnotebtn">Add note</button>
</span>
<hr/>

<div id="clienttabcontent">
	<div id="clienttableftpane" style="float:left; max-width: 700px; width: 700px;">
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.name"/></span>
			<span class="rowvalue">{{=displayName}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.branch"/></span>
			<span class="rowvalue">{{=officeName}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.joinedon"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(joinedDate)}}</span>
		</div>

		<p><b><spring:message code="heading.client.account.account.overview"/></b></p>
		<div id="clientaccountssummary"></div>

		<div id="clientadditionaldata"></div>
	</div>
	<!-- end of left pane content -->

	<!-- placeholder for notes content -->
	<div id="clienttabrightpane" style="float:right;"></div>
</div>
<div style="clear: both;"></div>
</script>

<script id="loanDataTabTemplate" type="text/x-jquery-tmpl">
<c:url value="/portfolio/loan/{{=$ctx.number(id)}}/undodisbursal" var="undoDisbursalUrl"/>
<c:url value="/portfolio/loan/{{=$ctx.number(id)}}/undoapproval" var="undoApprovalUrl"/>

<sec:authorize access="hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_APPROVE_LOAN_ROLE', 'CAN_APPROVE_LOAN_IN_THE_PAST_ROLE', 
		'CAN_REJECT_LOAN_ROLE', 'CAN_REJECT_LOAN_IN_THE_PAST_ROLE', 'CAN_WITHDRAW_LOAN_ROLE', 'CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE', 'CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED', 
		'CAN_UNDO_LOAN_APPROVAL_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE')">

{{#if anyActionOnLoanAllowed}}
<div id="loanactions">
	<span id="toolbar" class="ui-widget-header ui-corner-all">

		<sec:authorize access="hasAnyRole('CAN_REJECT_LOAN_ROLE', 'CAN_REJECT_LOAN_IN_THE_PAST_ROLE', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if rejectAllowed}}
		<button id="rejectbtn{{=id}}" class="rejectloan">Reject</button>
		{{/if}}
		</sec:authorize>

		<sec:authorize access="hasAnyRole('CAN_WITHDRAW_LOAN_ROLE', 'CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if withdrawnByApplicantAllowed}}
		<button id="withdrawnbyapplicantloanbtn{{=id}}" class="withdrawnbyapplicantloan">Withdrawn By Applicant</button>
		{{/if}}
		</sec:authorize>

		<sec:authorize access="hasAnyRole('CAN_APPROVE_LOAN_ROLE', 'CAN_APPROVE_LOAN_IN_THE_PAST_ROLE', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if pendingApproval}}
		<button id="approvebtn{{=id}}" class="approveloan">Approve</button>
		{{/if}}
		</sec:authorize>

		<sec:authorize access="hasAnyRole('CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if pendingApproval}}
		<button id="deletebtn{{=id}}" class="deleteloan">Delete</button>
		{{/if}}
		</sec:authorize>

		<sec:authorize access="hasAnyRole('CAN_UNDO_LOAN_APPROVAL_ROLE', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if undoApprovalAllowed}}
		<button id="undoapprovebtn{{=id}}" class="undoapproveloan">Undo Approval</button>
		{{/if}}
		</sec:authorize>
		
		<sec:authorize access="hasAnyRole('CAN_DISBURSE_LOAN_ROLE', 'CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE','PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if disbursalAllowed}}
		<button id="disbursebtn{{=id}}" class="disburseloan">Disburse</button>
		{{/if}}
		</sec:authorize>

		<sec:authorize access="hasAnyRole('CAN_UNDO_LOAN_DISBURSAL_ROLE', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
			{{#if undoDisbursalAllowed}}
			<button id="undodisbursalbtn{{=id}}" class="undodisbursalloan">Undo Disbursal</button>
			{{/if}}
		</sec:authorize>

		<sec:authorize access="hasAnyRole('CAN_MAKE_LOAN_REPAYMENT_ROLE', 'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE', 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')" >
		{{#if makeRepaymentAllowed}}
		<button id="repaymentbtn{{=id}}" class="repaymentloan">Make Repayment</button>
		{{/if}}
		</sec:authorize>
		
		{{#if waiveAllowed}}
		<button id="waivebtn{{=id}}" class="waiveloan">Waive</button>
		{{/if}}
	</span>
</div>
{{/if}}

<div id="loantabs{{=id}}" class="loantabs">
	<ul>
		<li><a href="#details{{=id}}" title="details"><spring:message code="tab.client.account.loan.details"/></a></li>
		<li><a href="#schedule{{=id}}" title="schedule"><spring:message code="tab.client.account.loan.schedule"/></a></li>
		{{#if loanData.loanRepayments}}
		<li><a href="#repayments{{=id}}" title="repayments"><spring:message code="tab.client.account.loan.repayments"/></a></li>
		{{/if}}
	</ul>
    <!-- first loan tab -->
	<div id="details{{=id}}" style="margin-top: 5px;">
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.status"/></span>
			<span class="rowvalue">{{=lifeCycleStatusText}} (<spring:message code="label.client.account.loan.status.since"/> {{=$ctx.globalDate(lifeCycleStatusDate)}}) 
			{{#if $ctx.numberGreaterThanZero(loanData.summary.totalInArrears.amount)}}
				{{#if open}}
			- <spring:message code="label.client.account.loan.status.arrears"/> {{=$ctx.moneyWithCurrency(loanData.summary.totalInArrears)}}
				{{/if}}
			{{/if}}
			</span>
		</div>

		{{#if submittedOnDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.submitted.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(submittedOnDate)}}</span>
		</div>
		{{/if}}

		{{#if approvedOnDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.approved.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(approvedOnDate)}}</span>
		</div>
		{{/if}}

		{{#if actualDisbursementDate === null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.expected.disbursement.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(expectedDisbursementDate)}}</span>
		</div>
		{{/if}}

		{{#if actualDisbursementDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.disbursed.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(actualDisbursementDate)}}</span>
		</div>
		{{/if}}

		{{#if expectedFirstRepaymentOnDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.first.repayment.due.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(expectedFirstRepaymentOnDate)}}</span>
		</div>
		{{/if}}

		{{#if interestCalculatedFromDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.interest.charged.from"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(interestCalculatedFromDate)}}</span>
		</div>
		{{/if}}

		{{#if open}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.expected.maturity.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(expectedMaturityDate)}}</span>
		</div>
		{{/if}}

		{{#if closedOnDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.closed.on"/></span>
			<span class="rowvalue">{{=$ctx.globalDate(closedOnDate)}}</span>
		</div>
		{{/if}}

		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.term"/></span>
			<span class="rowvalue">{{=loanTermInMonths}} <spring:message code="label.months"/> / ({{=loanTermInDays}} <spring:message code="label.days"/>)</span>
		</div>

		{{#if closedOnDate !== null}}
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.actual.term"/></span>
			<span class="rowvalue">{{=actualLoanTermInMonths}} <spring:message code="label.months"/> / ({{=actualLoanTermInDays}} <spring:message code="label.days"/>)</span>
		</div>
		{{/if}}

		<br/>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.product"/></span>
			<span class="rowvalue">{{=loanProductName}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.externalid"/></span>
			<span class="rowvalue">{{=externalId}}</span>
		</div>
		<br/>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.currency"/></span>
			<span class="rowvalue">{{=principal.defaultName}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.principal"/></span>
			<span class="rowvalue">{{=$ctx.moneyWithCurrency(principal)}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.repayments"/></span>
			<span class="rowvalue">{{=numberOfRepayments}} every {{=repaymentFrequencyNumber}}&nbsp;{{=repaymentFrequencyTypeText}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.amortization"/></span>
			<span class="rowvalue">{{=amortizationMethodText}}</span>
		</div>
		<div class="row">
			<span class="longrowlabel"><spring:message code="label.client.account.loan.interest"/></span>
			<span class="rowvalue">{{=$ctx.decimal(interestRatePerYear, 4)}}% per annum ({{=$ctx.decimal(interestRatePerPeriod, 4)}}%&nbsp; per {{=interestPeriodFrequencyText}}) - {{=interestMethodText}}</span>
		</div>

		<div id="loanadditionaldata{{=id}}"></div>
	</div>		

	<!-- second loan tab -->
	<div id="schedule{{=id}}" style="margin-top: 5px;">
		<table id="summarytable{{=id}}" class="pretty displayschedule">
			<caption><spring:message code="tab.client.account.loan.schedule.table.summary.caption"/></caption>
			<thead>
				<tr>
					<th class="empty"></th>
					<th><spring:message code="tab.client.account.loan.schedule.table.summary.heading.original"/></th>
					<th><spring:message code="tab.client.account.loan.schedule.table.summary.heading.paid"/></th>
					<th><spring:message code="tab.client.account.loan.schedule.table.summary.heading.waived"/></th>
					<th><spring:message code="tab.client.account.loan.schedule.table.heading.outstanding"/></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.principal"/></th>
					<td>{{=$ctx.money(loanData.summary.originalPrincipal)}}</td>
					<td>{{=$ctx.money(loanData.summary.principalPaid)}}</td>
					<td>&nbsp;</td>
					<td>{{=$ctx.money(loanData.summary.principalOutstanding)}}</td>
				</tr>
				<tr>
					<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.interest"/></th>
					<td>{{=$ctx.money(loanData.summary.originalInterest)}}</td>
					<td>{{=$ctx.money(loanData.summary.interestPaid)}}</td>
					<td>&nbsp;</td>
					<td>{{=$ctx.money(loanData.summary.interestOutstanding)}}</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.total"/></th>
					<th>{{=$ctx.money(loanData.summary.originalTotal)}}</th>
					<th>{{=$ctx.money(loanData.summary.totalPaid)}}</th>
					<th>{{=$ctx.money(loanData.summary.totalWaived)}}</th>
					<th>{{=$ctx.money(loanData.summary.totalOutstanding)}}</th>
				</tr>
			</tfoot>
		</table>

		<br/>

		<table id="repaymentschedule_todate{{=id}}" class="pretty displayschedule">
			<caption><spring:message code="tab.client.account.loan.schedule.table.repaymentschedule.caption"/></caption>
			<colgroup span="2"></colgroup>
			<colgroup span="3">
				<col class="lefthighlightcol">
				<col>
				<col class="righthighlightcol">
			</colgroup>
			<colgroup span="3">
				<col class="lefthighlightcol">
				<col>
				<col class="righthighlightcol">
			</colgroup>
			<colgroup span="3">
				<col class="lefthighlightcol">
				<col>
				<col class="righthighlightcol">
			</colgroup>
			<thead>
				<tr>
					<th colspan="2" scope="colgroup" class="empty">&nbsp;</th>
					<th colspan="3" scope="colgroup" class="highlightcol"><spring:message code="tab.client.account.loan.schedule.table.column.heading.principal"/></th>
					<th colspan="3" scope="colgroup" class="highlightcol"><spring:message code="tab.client.account.loan.schedule.table.column.heading.interest"/></th>
					<th colspan="3" scope="colgroup" class="highlightcol"><spring:message code="tab.client.account.loan.schedule.table.column.heading.total"/></th>
				</tr>
				<tr>
					<th scope="col">#</th>
					<th scope="col"><spring:message code="tab.client.account.loan.schedule.table.column.heading.date"/></th>
					<th scope="col" class="lefthighlightcolheader"><spring:message code="tab.client.account.loan.schedule.table.column.heading.expected"/></th>
					<th scope="col"><spring:message code="tab.client.account.loan.schedule.table.column.heading.paid"/></th>
					<th scope="col" class="righthighlightcolheader"><spring:message code="tab.client.account.loan.schedule.table.heading.outstanding"/></th>
					<th scope="col" class="lefthighlightcolheader"><spring:message code="tab.client.account.loan.schedule.table.column.heading.expected"/></th>
					<th scope="col"><spring:message code="tab.client.account.loan.schedule.table.column.heading.paid"/></th>
					<th scope="col" class="righthighlightcolheader"><spring:message code="tab.client.account.loan.schedule.table.heading.outstanding"/></th>
					<th scope="col" class="lefthighlightcolheader"><spring:message code="tab.client.account.loan.schedule.table.column.heading.expected"/></th>
					<th scope="col"><spring:message code="tab.client.account.loan.schedule.table.column.heading.paid"/></th>
					<th scope="col" class="righthighlightcolheader"><spring:message code="tab.client.account.loan.schedule.table.heading.outstanding"/></th>
				</tr>
			</thead>
			<tbody>
			{{#each loanData.repaymentSchedule.periods}}
			<tr>
				<td scope="row">{{=$ctx.number(period)}}</td>
				<td>{{=$ctx.globalDate(date)}}</td>
				<td class="lefthighlightcolheader">{{=$ctx.money(principal)}}</td>
				<td>{{=$ctx.money(principalPaid)}}</td>
				<td class="righthighlightcolheader">{{=$ctx.money(principalOutstanding)}}</td>
				<td class="lefthighlightcolheader">{{=$ctx.money(interest)}}</td>
				<td>{{=$ctx.money(interestPaid)}}</td>
				<td class="righthighlightcolheader">{{=$ctx.money(interestOutstanding)}}</td>
				<td class="lefthighlightcolheader">{{=$ctx.money(total)}}</td>
				<td>{{=$ctx.money(totalPaid)}}</td>
				<td class="righthighlightcolheader">{{=$ctx.money(totalOutstanding)}}</td>
			</tr>
			{{/each}}
			</tbody>
			<tfoot>
				<tr>
					<th colspan="2"><spring:message code="tab.client.account.loan.schedule.table.column.heading.total"/></th>
					<th class="lefthighlightcolheader">{{=$ctx.money(loanData.summary.originalPrincipal)}}</th>
					<th>{{=$ctx.money(loanData.summary.principalPaid)}}</th>
					<th class="righthighlightcolheader">{{=$ctx.money(loanData.summary.principalOutstanding)}}</th>
					<th class="lefthighlightcolheader">{{=$ctx.money(loanData.summary.originalInterest)}}</th>
					<th>{{=$ctx.money(loanData.summary.interestPaid)}}</th>
					<th class="righthighlightcolheader">{{=$ctx.money(loanData.summary.interestOutstanding)}}</th>
					<th class="lefthighlightcolheader">{{=$ctx.money(loanData.summary.originalTotal)}}</th>
					<th>{{=$ctx.money(loanData.summary.totalPaid)}}</th>
					<th class="righthighlightcolheader">{{=$ctx.money(loanData.summary.totalOutstanding)}}</th>
				</tr>
			</tfoot>
		</table>
	</div>
    {{#if loanData.loanRepayments}}
	<!-- Third loan tab -->
	<div id="repayments{{=id}}" style="margin-top: 5px;">
		<table id="newrepaymentsactivity{{=id}}" class="pretty displaypayments">
		<caption><spring:message code="tab.client.account.loan.schedule.table.repayments.caption"/></caption>
			<thead>
				<tr>
				<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.paymentdate"/></th>
				<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.amountpaid"/></th>
				<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.principal.portion"/></th>
				<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.interest.portion"/></th>
				{{#if open}}
				<th><spring:message code="tab.client.account.loan.schedule.table.column.heading.action"/></th>
				{{/if}}
				</tr>
			</thead>
			<tbody>
				{{#each loanData.loanRepayments}}
				<tr>
				<td>{{=$ctx.globalDate(date)}}</td>
				<td>{{=$ctx.money(total)}}</td>
				<td>{{=$ctx.money(principal)}}</td>
				<td>{{=$ctx.money(interest)}}</td>
				{{#if $parent.parent.data.open}}
				<td><button id="adjustrepaymentbtn{{=$parent.parent.parent.data.id}}_{{=id}}" class="adjustloanrepayment">Adjust</button></td>
				{{/if}}
				</tr>
				{{/each}}
			</tbody>
		</table>
	</div>
	{{/if}}

</div>

</sec:authorize>
</script>

<script id="stateTransitionLoanFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<input type="hidden" id="dateFormat" name="dateFormat" value="dd MMMM yyyy" />
	<label for="eventDate"><spring:message code="label.loan.on"/></label>
	<input id="eventDate" name="eventDate" title="" class="datepickerfield" />

	<label for="note"><spring:message code="label.loan.action.note"/></label>
	<textarea rows="3" cols="53" id="note" name="note"></textarea>
</form>
</script>

<script id="undoStateTransitionLoanFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<label for="note"><spring:message code="label.loan.action.note"/></label>
	<textarea rows="6" cols="53" id="note" name="note"></textarea>
</form>
</script>

<script id="transactionLoanFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>

	<input type="hidden" id="dateFormat" name="dateFormat" value="dd MMMM yyyy" />
	<input type="hidden" id="locale" name="locale" value="{{=$ctx.currentLocale()}}" />

	<label for="transactionDate"><spring:message code="label.loan.transaction.on"/></label>
	<input id="transactionDate" name="transactionDate" class="text ui-widget-content ui-corner-all datepickerfield" title=""d value="{{=$ctx.globalDate(date)}}"/>

	<label for="transactionAmount"><spring:message code="label.loan.transaction.amount"/></label>
	<input id="transactionAmount" name="transactionAmount" class="text ui-widget-content ui-corner-all" title="" value="{{=$ctx.money(total)}}" />

	<label for="note"><spring:message code="label.loan.action.note"/></label>
	<textarea rows="3" cols="53" id="note" name="note" class="text ui-widget-content ui-corner-all"></textarea>
</form>
</script>

<script id="formErrorsTemplate" type="text/x-jquery-tmpl">
<div class="ui-widget">
	<div class="ui-state-error ui-corner-all">
		<span class="ui-icon ui-icon-alert" style="float: left; margin-right: 5px;" ></span>
		<span>{{=title}}</span>
		<div style="margin-left: 5px;">
		{{#each errors}}
			<div>
				<em>{{=message}}</em>
			</div>
		{{/each}}
		</div>
	</div>
</div>
</script>