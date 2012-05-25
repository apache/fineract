<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- templates below here -->
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

<script id="productFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
	<fieldset>
		<legend><spring:message code="form.legend.loan.product.information"/></legend>
		
		<label for="name"><spring:message code="form.label.loan.product.name"/></label>
		<input id="name" name="name" type="text" title="" value="{{=name}}"/>
		
		<label for="description"><spring:message code="form.label.loan.product.description"/></label>
		<textarea cols="73" rows="3" id="description" name="description" title="">{{=description}}</textarea>
	</fieldset>
	<fieldset>
		<legend><spring:message code="form.legend.loan.product.currency"/></legend>
		<label for="currencyCode"><spring:message code="form.label.loan.product.currency"/></label>
		<select id="currencyCode" name="currencyCode" title="" style="width: 300px;">
				{{#each possibleCurrencies}}
                	{{#if $parent.parent.data.principalMoney.currencyCode===code}}
						<option value="{{=code}}" selected="selected">{{=name}}</option>
					{{#else}}
						<option value="{{=code}}">{{=name}}</option>
					{{/if}}
          		{{/each}}
		</select>
		<label for="digitsAfterDecimal"><spring:message code="form.label.loan.product.decimalplaces"/></label>
		<input id="digitsAfterDecimal" name="digitsAfterDecimal" title="" style="width: 300px;" value="{{=$ctx.number(principalMoney.currencyDigitsAfterDecimal)}}" />
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
							<input id="principalFormatted" name="principalFormatted" title="" style="width: 125px;" value="{{=$ctx.money(principalMoney)}}" />
						</td>
					</tr>
					<tr>
						<td><spring:message code="form.label.loan.product.repaidevery"/></td>
						<td>
							<input id="repaymentEvery" name="repaymentEvery" title="" style="width: 50px;" value="{{=$ctx.number(repaidEvery)}}"/>
							
							<select id="repaymentFrequency" name="repaymentFrequency" title="" style="width: 121px;">
                                {{#each repaymentFrequencyOptions}}
                                 	{{#if $ctx.number($parent.parent.data.repaymentPeriodFrequency)===$ctx.number(id)}}
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
							<input id="numberOfRepayments" name="numberOfRepayments" title="" style="width: 50px;" value="{{=$ctx.number(numberOfRepayments)}}"/>
						</td>
					</tr>
				</table>
			</td>
			<td valign="top" style="padding-left: 5px; padding-right: 5px; border-right: 1px solid;">
				<table>
					<tr>
						<td><spring:message code="form.label.loan.product.nominal.interestrate"/></td>
						<td>
							<input id="interestRatePerPeriodFormatted" name="interestRatePerPeriodFormatted" title="" style="width: 70px;" value="{{=$ctx.decimal(interestRatePerPeriod, 4)}}" />

							<select id="interestRateFrequencyMethod" name="interestRateFrequencyMethod" title="" style="width: 139px;">
								{{#each interestFrequencyOptions}}
                                 	{{#if $ctx.number($parent.parent.data.interestRatePeriod)===$ctx.number(id)}}
										<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
									{{#else}}
										<option value="{{=$ctx.number(id)}}">{{=value}}</option>
									{{/if}}
                                {{/each}}
							</select>
						</td>
					</tr>
					<tr>
						<td><spring:message code="form.label.loan.product.amortization"/></td>
						<td>
							<select id="amortizationMethod" name="amortizationMethod" title="" style="width: 220px;">
                                {{#each possibleAmortizationOptions}}
                                 	{{#if $ctx.number($parent.parent.data.amortizationMethod)===$ctx.number(id)}}
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
							<select id="interestMethod" name="interestMethod" title="" style="width: 220px;">
								{{#each possibleInterestOptions}}
                                 	{{#if $ctx.number($parent.parent.data.interestMethod)===$ctx.number(id)}}
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
							<select id="interestCalculationPeriodMethod" name="interestCalculationPeriodMethod" title="" style="width: 220px;">
								{{#each possibleInterestRateCalculatedInPeriodOptions}}
                                 	{{#if $ctx.number($parent.parent.data.interestRateCalculatedInPeriod)===$ctx.number(id)}}
										<option value="{{=$ctx.number(id)}}" selected="selected">{{=value}}</option>
									{{#else}}
										<option value="{{=$ctx.number(id)}}">{{=value}}</option>
									{{/if}}
                                {{/each}}
							</select>
						</td>
					</tr>
					<tr>
						<td><spring:message code="form.label.loan.product.arrears.tolerance"/></td>
						<td>
							<input id="inArrearsToleranceAmountFormatted" name="inArrearsToleranceAmountFormatted" title="" style="width: 215px;" value="{{=$ctx.money(inArrearsTolerance)}}" />
						</td>
					</tr>
				</table>
			</td>
		</tr>
		</table>
	</fieldset>
</form>
</script>

<script id="productListTemplate" type="text/x-jquery-tmpl">
<table id='productstable' width='100%'>
	<thead>
		<tr>
			<th><spring:message code="table.heading.product.name"/></th>
			<th><spring:message code="table.heading.product.created"/></th>
			<th><spring:message code="table.heading.product.modified"/></th>
			<th><spring:message code="table.heading.product.active"/></th>
			<th>&nbsp;</th>
		</tr>
	</thead>
	<tbody>
{{#each items}}
<tr>
	<td title='{{=description}}'>{{=name}}</td>
	<td>{{=$ctx.globalDateTime(createdOn)}}</td>
	<td>{{=$ctx.globalDateTime(lastModifedOn)}}</td>
	<td>Yes</td>
	<td>
		<a id='deactivateproduct{{=id}}' class='deactivateproduct' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.deactivate"/></a>
		<a id='editproduct{{=id}}' class='editproduct' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.viewedit"/></a>
		<!--
		<a id='deleteproduct{{=id}}' class='deleteproduct' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.delete"/></a>
		-->
	</td>
</tr>
{{/each}}
	</tbody>
</table>
</script>

<script id="officeListTemplate" type="text/x-jquery-tmpl">
<table id='officestable' width='100%'>
	<thead>
		<tr>
			<th><spring:message code="table.heading.office.name"/></th>
			<th><spring:message code="table.heading.office.externalid"/></th>
			<th><spring:message code="table.heading.office.openedon"/></th>
			<th><spring:message code="table.heading.office.parent"/></th>
			<th>&nbsp;</th>
		</tr>
	</thead>
	<tbody>
{{#each items}}
<tr>
	<td>{{=name}}</td>
	<td>{{=externalId}}</td>
	<td>{{=$ctx.globalDate(openingDate)}}</td>
	<td>{{=parentName}}</td>
	<td>
		<a id='edit{{=id}}' class='edit' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.viewedit"/></a>
		<!--
		<a id='delete{{=id}}' class='delete' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.delete"/></a>
		-->
	</td>
</tr>
{{/each}}
	</tbody>
</table>
</script>

<script id="officeFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
	</div>
	<fieldset>
		<legend><spring:message code="form.legend.office.details"/></legend>
		<label for="name"><spring:message code="form.label.office.name"/></label>
		<input id="name" name="name" title="" type="text" value="{{=name}}" size="75"/>

        {{#if allowedParents.length > 0}}
		<label for="parentId"><spring:message code="form.label.office.parent"/></label>
		<select id="parentId" name="parentId" title="">
            <option value="-1"><spring:message code="option.office.first.choice"/></option>
			{{#each allowedParents}}
				{{#if $ctx.number($parent.parent.data.parentId)===$ctx.number(id)}}
					<option value="{{=$ctx.number(id)}}" selected="selected">{{=name}}</option>
				{{#else}}
					<option value="{{=$ctx.number(id)}}">{{=name}}</option>
				{{/if}}
            {{/each}}
		</select>
		{{#else}}
			<input type="hidden" id="rootOffice" name="rootOffice" value="true" />
		{{/if}}

		<input type="hidden" id="dateFormat" name="dateFormat" value="dd MMMM yyyy" />
		<label for="openingDateFormatted"><spring:message code="form.label.office.openedon"/></label>
		<input id="openingDateFormatted" name="openingDateFormatted" title="" type="text" value="{{=$ctx.globalDate(openingDate)}}" size="75" class="datepickerfield" />

		<label for="externalId"><spring:message code="form.label.office.externalid"/></label>
		<input id="externalId" name="externalId" title="" type="text" value="{{=externalId}}" size="75"/>
	</fieldset>
</form>
</script>

<script id="configurationFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
	<fieldset>
		<legend><spring:message code="form.legend.loan.product.currency"/></legend>

		<div class="multiselectwidget" style="width:400px;height:125px;">
			<label for="notSelectedItems"><spring:message code="form.label.configuration.available.currencies"/></label>
			<select id="notSelectedItems" name="notSelectedItems" title="" class="multiselectwidget" multiple="multiple" style="width:400px;height:125px;">
			{{#each currencyOptions}}
				{{#if $ctx.number($parent.parent.data.code)===code}}
					<option value="{{=code}}" selected="selected">{{=displayLabel}}</option>
				{{#else}}
					<option value="{{=code}}">{{=displayLabel}}</option>
				{{/if}}
            {{/each}}
			</select>
			<a href="#" id="add" class="multiselectwidget"><spring:message code="widget.multiselect.button.add"/> &gt;&gt;</a>
		</div>
				  
		<div class="multiselectwidget">
			<label for="selectedItems"><spring:message code="form.label.configuration.allowed.currencies"/></label>
			<select id="selectedItems" name="selectedItems" title="" class="multiselectwidget" multiple="multiple" style="width:400px;height:125px;">
			{{#each selectedCurrencyOptions}}
				{{#if $ctx.number($parent.parent.data.code)===code}}
					<option value="{{=code}}" selected="selected">{{=displayLabel}}</option>
				{{#else}}
					<option value="{{=code}}">{{=displayLabel}}</option>
				{{/if}}
            {{/each}}
			</select>
			<a href="#" id="remove" class="multiselectwidget">&lt;&lt; <spring:message code="widget.multiselect.button.remove"/></a>  
		</div>
	</fieldset>
</form>
</script>

<script id="changePasswordFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
        <label for="password"><spring:message code="form.label.new.password"/></label>
		<input id="password" name="password" title="" type="password" value="{{=password}}" size="75"/>

        <label for="repeatPassword"><spring:message code="form.label.repeat.new.password"/></label>
		<input id="repeatPassword" name="repeatPassword" title="" type="password" value="{{=passwordrepeat}}" size="75"/>
</form>
</script>

<script id="userSettingsFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
		<label for="username"><spring:message code="form.label.username"/></label>
		<input id="username" name="username" title="" type="text" value="{{=username}}" size="75"/>

        <label for="firstname"><spring:message code="form.label.firstname"/></label>
		<input id="firstname" name="firstname" title="The first name of user." type="text" value="{{=firstname}}" size="75"/>

        <label for="lastname"><spring:message code="form.label.lastname"/></label>
		<input id="lastname" name="lastname" title="The last name of user." type="text" value="{{=lastname}}" size="75"/>

		<label for="email"><spring:message code="form.label.email"/></label>
		<input id="email" name="email" title="" type="text" value="{{=email}}" size="75"/>
</form>
</script>

<script id="userSettingsTemplate" type="text/x-jquery-tmpl">
<h2><spring:message code="form.label.heading.office"/></h2>
<div class="row">
	<span class="rowlabel"><spring:message code="form.label.office"/></span>
	<span class="rowvalue">{{=officeName}}</span>
</div>
<br/>

<h2><spring:message code="form.label.heading.details"/></h2>
<div class="row">
	<span class="rowlabel"><spring:message code="form.label.name"/></span>
	<span class="rowvalue">{{=firstname}}&nbsp;{{=lastname}}</span>
</div>
<div class="row">
	<span class="rowlabel"><spring:message code="form.label.username"/></span>
	<span class="rowvalue">{{=username}}</span>
</div>
<div class="row">
	<span class="rowlabel"><spring:message code="form.label.email"/></span>
	<span class="rowvalue">{{=email}}</span>
</div>
<div class="row">
	<span class="rowlabel">&nbsp;</span>
	<span class="rowvalue"><a href="#" id="changedetails"><spring:message code="form.link.changedetails"/></a></span>
</div>
<br/>

<h2><spring:message code="form.label.heading.security"/></h2>
<div class="row">
	<span class="rowlabel"><spring:message code="form.label.password"/></span>
	<span class="rowvalue"><a href="#" id="changepassword"><spring:message code="form.link.changepassword"/></a></span>
</div>
<br/>

<h2><spring:message code="form.label.heading.roles"/></h2>
{{#each selectedRoles}}
<div class="row">
	<span class="rowlabel">&nbsp;</span>
	<span class="rowvalue"><b>{{=name}}</b> - <i>{{=description}}</i></span>
</div>
{{/each}}
</script>

<script id="permissionListTemplate" type="text/x-jquery-tmpl">
<table id='entitytable' width='100%'>
<thead>
	<tr>
        <th><spring:message code="table.heading.permission.type"/></th>
		<th><spring:message code="table.heading.permission.permission"/></th>
		<th><spring:message code="table.heading.permission.description"/></th>
	</tr>
</thead>
<tbody>
{{#each permissions}}
<tr>
    <td>
    {{#if $ctx.number(groupType)===$ctx.number(0)}}
		Unknown
	{{/if}}
	{{#if $ctx.number(groupType)===$ctx.number(1)}}
		User Administration
	{{/if}}
	{{#if $ctx.number(groupType)===$ctx.number(2)}}
		Organisation Administration
	{{/if}}
	{{#if $ctx.number(groupType)===$ctx.number(3)}}
		Portfolio Management
	{{/if}}
	{{#if $ctx.number(groupType)===$ctx.number(4)}}
		Reporting
	{{/if}}
    {{#if $ctx.number(groupType)===$ctx.number(5)}}
		Migration
	{{/if}}
	</td>
	<td>{{=name}}</td>
	<td>{{=description}}</td>
</tr>
{{/each}}
</tbody>
</table>
</script>

<script id="roleListTemplate" type="text/x-jquery-tmpl">
<table id='entitytable' width='100%'>
<thead>
	<tr>
		<th><spring:message code="table.heading.rolename"/></th>
		<th><spring:message code="table.heading.description"/></th>
		<th>&nbsp;</th>
	</tr>
</thead>
<tbody>
{{#each roles}}
<tr>
	<td>{{=name}}</td>
	<td>{{=description}}</td>
	<td>
		<a id='edit{{=id}}' class='edit' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.viewedit"/></a>
        <!--
		<a id='delete{{=id}}' class='delete' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.delete"/></a>
        -->
	</td>
</tr>
{{/each}}
</tbody>
</table>
</script>

<script id="usersListTemplate" type="text/x-jquery-tmpl">
<table id='entitytable' width='100%'>
<thead>
	<tr>
		<th><spring:message code="table.heading.officename"/></th>
		<th><spring:message code="table.heading.username"/></th>
        <th><spring:message code="table.heading.name"/></th>
		<th><spring:message code="table.heading.email"/></th>
		<th>&nbsp;</th>
	</tr>
</thead>
<tbody>
{{#each users}}
<tr>
	<td>{{=officeName}}</td>
	<td>{{=username}}</td>
    <td>{{=lastname}}, {{=firstname}}</td>
	<td>{{=email}}</td>
	<td>
		<a id='edit{{=id}}' class='edit' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.viewedit"/></a>
        <!--
		<a id='delete{{=id}}' class='delete' href='#' style='margin-right: 5px;'><spring:message code="link.action.product.delete"/></a>
        -->
	</td>
</tr>
{{/each}}
</tbody>
</table>
</script>

<script id="userFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
	<fieldset>
		<legend><spring:message code="form.legend.user.details"/></legend>

		<label for="username"><spring:message code="form.label.user.username"/></label>
		<input id="username" name="username" title="" type="text" value="{{=username}}" size="75"/>

        <label for="firstname"><spring:message code="form.label.user.firstname"/></label>
		<input id="firstname" name="firstname" title="" type="text" value="{{=firstname}}" size="75"/>

        <label for="lastname"><spring:message code="form.label.user.lastname"/></label>
		<input id="lastname" name="lastname" title="" type="text" value="{{=lastname}}" size="75"/>

		<label for="email"><spring:message code="form.label.user.email"/></label>
		<input id="email" name="email" title="" type="text" value="{{=email}}" size="75"/>

		<label for="officeId"><spring:message code="form.label.user.office"/></label>
		<select id="officeId" name="officeId" title="">
			<option value="-1"><spring:message code="option.office.first.choice"/></option>
			{{#each allowedOffices}}
				{{#if $ctx.number($parent.parent.data.officeId)===$ctx.number(id)}}
					<option value="{{=$ctx.number(id)}}" selected="selected">{{=name}}</option>
				{{#else}}
					<option value="{{=$ctx.number(id)}}">{{=name}}</option>
				{{/if}}
            {{/each}}
		</select>
		<br/>

		<div class="multiselectwidget">
			<label for="notSelectedRoles"><spring:message code="form.label.user.available.roles"/></label>
			<select id="notSelectedRoles" name="notSelectedRoles" title="Available application roles." class="multiselectwidget multiNotSelectedItems" multiple="multiple" style="width:450px;height:125px;">
			{{#each availableRoles}}
				<option value="{{=id}}">{{=name}}</option>
            {{/each}}
			</select>
			<a href="#" id="add" class="multiselectwidget multiadd"><spring:message code="widget.multiselect.button.add"/> &gt;&gt;</a>
		</div>
		
		<div class="multiselectwidget">
			<label for="roles"><spring:message code="form.label.user.selected.roles"/></label>
			<select id="roles" name="roles" title="User selected roles." class="multiselectwidget multiSelectedItems" multiple="multiple" style="width:450px;height:125px;">
			{{#each selectedRoles}}
				<option value="{{=id}}" selected="selected">{{=name}}</option>
            {{/each}}
			</select>
			<a href="#" id="remove" class="multiselectwidget multiremove">&lt;&lt; <spring:message code="widget.multiselect.button.remove"/></a>  
		</div>
	</fieldset>
</form>
</script>

<script id="roleFormTemplate" type="text/x-jquery-tmpl">
<form id="entityform">
    <div id="formerrors"></div>
	<fieldset>
		<legend><spring:message code="form.legend.role.details"/></legend>

		<label for="name"><spring:message code="form.label.role.name"/></label>
		<input id="name" name="name" title="" type="text" value="{{=name}}" size="75"/>

		<label for="description"><spring:message code="form.label.role.description"/></label>
		<textarea cols="73" rows="3" id="description" name="description" title="">{{=description}}</textarea>
		<br/>

		<div class="multiselectwidget">
			<label for="notSelectedPermissions"><spring:message code="form.label.role.available.permissions"/></label>
			<select id="notSelectedPermissions" name="notSelectedPermissions" title="" class="multiselectwidget multiNotSelectedItems" multiple="multiple" style="width:450px;height:125px;">
			{{#each availablePermissions}}
				<option value="{{=id}}" title="{{=description}}">{{=name}}</option>
            {{/each}}
			</select>
			<a href="#" id="add" class="multiselectwidget multiadd"><spring:message code="widget.multiselect.button.add"/> &gt;&gt;</a>
		</div>
		
		<div class="multiselectwidget">
			<label for="permissions"><spring:message code="form.label.role.selected.permissions"/></label>
			<select id="permissions" name="permissions" title="" class="multiselectwidget multiSelectedItems" multiple="multiple" style="width:450px;height:125px;">
			{{#each selectedPermissions}}
				<option value="{{=id}}" title="{{=description}}">{{=name}}</option>
            {{/each}}
			</select>
			<a href="#" id="remove" class="multiselectwidget multiremove">&lt;&lt; <spring:message code="widget.multiselect.button.remove"/></a>  
		</div>
	</fieldset>
</form>
</script>