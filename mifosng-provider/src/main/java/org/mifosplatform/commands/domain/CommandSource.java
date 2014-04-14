/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_portfolio_command_source")
public class CommandSource extends AbstractPersistable<Long> {

    @Column(name = "action_name", nullable = true, length = 100)
    private String actionName;

    @Column(name = "entity_name", nullable = true, length = 100)
    private String entityName;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "savings_account_id")
    private Long savingsId;

    @Column(name = "api_get_url", length = 100)
    private String resourceGetUrl;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "subresource_id")
    private Long subresourceId;

    @Column(name = "command_as_json", length = 1000)
    private String commandAsJson;

    @ManyToOne
    @JoinColumn(name = "maker_id", nullable = false)
    private AppUser maker;

    @Column(name = "made_on_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date madeOnDate;

    @ManyToOne
    @JoinColumn(name = "checker_id", nullable = true)
    private AppUser checker;

    @Column(name = "checked_on_date", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkedOnDate;

    @Column(name = "processing_result_enum", nullable = false)
    private Integer processingResult;

    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    public static CommandSource fullEntryFrom(final CommandWrapper wrapper, final JsonCommand command, final AppUser maker) {
        return new CommandSource(wrapper.actionName(), wrapper.entityName(), wrapper.getHref(), command.entityId(), command.subentityId(),
                command.json(), maker, DateTime.now());
    }

    protected CommandSource() {
        //
    }

    private CommandSource(final String actionName, final String entityName, final String href, final Long resourceId,
            final Long subresourceId, final String commandSerializedAsJson, final AppUser maker, final DateTime madeOnDateTime) {
        this.actionName = actionName;
        this.entityName = entityName;
        this.resourceGetUrl = href;
        this.resourceId = resourceId;
        this.subresourceId = subresourceId;
        this.commandAsJson = commandSerializedAsJson;
        this.maker = maker;
        this.madeOnDate = madeOnDateTime.toDate();
        this.processingResult = CommandProcessingResultType.PROCESSED.getValue();
    }

    public void markAsChecked(final AppUser checker, final DateTime checkedOnDate) {
        this.checker = checker;
        this.checkedOnDate = checkedOnDate.toDate();
        this.processingResult = CommandProcessingResultType.PROCESSED.getValue();
    }

    public void markAsRejected(final AppUser checker, final DateTime checkedOnDate){
        this.checker = checker;
        this.checkedOnDate = checkedOnDate.toDate();
        this.processingResult = CommandProcessingResultType.REJECTED.getValue();
    }

    public void updateResourceId(final Long resourceId) {
        this.resourceId = resourceId;
    }

    public void updateSubresourceId(final Long subresourceId) {
        this.subresourceId = subresourceId;
    }

    public void updateJsonTo(final String json) {
        this.commandAsJson = json;
    }

    public Long resourceId() {
        return this.resourceId;
    }

    public Long subresourceId() {
        return this.subresourceId;
    }

    public boolean hasJson() {
        return StringUtils.isNotBlank(this.commandAsJson);
    }

    public String json() {
        return this.commandAsJson;
    }

    public String getActionName() {
        return this.actionName;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getPermissionCode() {
        return this.actionName + "_" + this.entityName;
    }

    public Long getResourceId() {
        return this.resourceId;
    }

    public Long getSubresourceId() {
        return this.subresourceId;
    }

    public void markAsAwaitingApproval() {
        this.processingResult = CommandProcessingResultType.AWAITING_APPROVAL.getValue();
    }

    public boolean isMarkedAsAwaitingApproval() {
        if (this.processingResult.equals(CommandProcessingResultType.AWAITING_APPROVAL.getValue())) { return true; }

        return false;
    }

    public void updateForAudit(final Long officeId, final Long groupId, final Long clientId, final Long loanId, final Long savingsId,
            final Long productId, final String transactionId) {
        this.officeId = officeId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.productId = productId;
        this.transactionId = transactionId;
    }

    public String getResourceGetUrl() {
        return this.resourceGetUrl;
    }

    public Long getProductId() {
        return this.productId;
    }

    /**
     * @return the clientId
     */
    public Long getClientId() {
        return clientId;
    }

    /**
     * @return the groupId
     */
    public Long getGroupId() {
        return groupId;
    }

    /**
     * @return the loanId
     */
    public Long getLoanId() {
        return loanId;
    }

    /**
     * @return the officeId
     */
    public Long getOfficeId() {
        return officeId;
    }

    /**
     * @return the savingsId
     */
    public Long getSavingsId() {
        return savingsId;
    }

    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return this.transactionId;
    }

    public void updateTransaction(final String transactionId) {
        this.transactionId = transactionId;
    }

}