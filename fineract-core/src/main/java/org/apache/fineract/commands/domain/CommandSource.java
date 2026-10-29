/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.commands.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.IpAddressUtils;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_portfolio_command_source")
public class CommandSource extends AbstractPersistableCustom<Long> {

    @Column(name = "action_name", nullable = true, length = 100)
    private String actionName;

    @Column(name = "entity_name", nullable = true, length = 100)
    private String entityName;

    /*
     * The identifier columns below (office, group, client, loan, savings, product, transaction, resource, subresource
     * and their external ids) hold what the command was submitted with. On rows where resultIdsWritten is set, what the
     * command returned is in the matching result_* columns; older rows hold the returned values here instead.
     */
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
    private Long subResourceId;

    @Column(name = "command_as_json", length = 1000)
    private String commandAsJson;

    @ManyToOne
    @JoinColumn(name = "maker_id", nullable = false)
    private AppUser maker;

    /*
     * Deprecated: Columns and data left untouched to help migration.
     *
     * @Column(name = "made_on_date", nullable = false) private LocalDateTime madeOnDate;
     *
     * @Column(name = "checked_on_date", nullable = true) private LocalDateTime checkedOnDate;
     */

    @Column(name = "made_on_date_utc", nullable = false)
    private OffsetDateTime madeOnDate;

    @Column(name = "checked_on_date_utc")
    private OffsetDateTime checkedOnDate;

    @ManyToOne
    @JoinColumn(name = "checker_id", nullable = true)
    private AppUser checker;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "creditbureau_id")
    private Long creditBureauId;

    @Column(name = "organisation_creditbureau_id")
    private Long organisationCreditBureauId;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "idempotency_key", length = 50)
    private String idempotencyKey;

    @Column(name = "resource_external_id")
    private ExternalId resourceExternalId;

    @Column(name = "subresource_external_id")
    private ExternalId subResourceExternalId;

    @Column(name = "result")
    private String result;

    @Column(name = "result_status_code")
    private Integer resultStatusCode;

    @Column(name = "loan_external_id", length = 100)
    private ExternalId loanExternalId;

    @Column(name = "client_ip", nullable = true)
    private String clientIp;

    @Column(name = "is_sanitized", nullable = false)
    private boolean sanitized;

    /*
     * Identifiers returned by the command. The columns above keep the identifiers the command was submitted with.
     */
    @Column(name = "result_office_id")
    private Long resultOfficeId;

    @Column(name = "result_group_id")
    private Long resultGroupId;

    @Column(name = "result_client_id")
    private Long resultClientId;

    @Column(name = "result_loan_id")
    private Long resultLoanId;

    @Column(name = "result_savings_account_id")
    private Long resultSavingsId;

    @Column(name = "result_product_id")
    private Long resultProductId;

    @Column(name = "result_transaction_id", length = 100)
    private String resultTransactionId;

    @Column(name = "result_resource_id")
    private Long resultResourceId;

    @Column(name = "result_resource_external_id", length = 100)
    private ExternalId resultResourceExternalId;

    @Column(name = "result_subresource_id")
    private Long resultSubResourceId;

    @Column(name = "result_subresource_external_id", length = 100)
    private ExternalId resultSubResourceExternalId;

    @Column(name = "result_loan_external_id", length = 100)
    private ExternalId resultLoanExternalId;

    /*
     * Rows written before the result columns existed have the returned identifiers in the request columns instead.
     */
    @Column(name = "result_ids_written", nullable = false)
    private boolean resultIdsWritten;

    /*
     * Set on every row inserted since the request columns stopped being overwritten, so they hold the submitted
     * identifiers whatever happens to the command afterwards.
     */
    @Column(name = "submitted_ids_kept", nullable = false)
    private boolean submittedIdsKept;

    public static CommandSource fullEntryFrom(final CommandWrapper wrapper, final JsonCommand command, final AppUser maker,
            String idempotencyKey, Integer status, boolean sanitized) {
        return CommandSource.builder() //
                .actionName(wrapper.actionName()) //
                .entityName(wrapper.entityName()) //
                .resourceGetUrl(wrapper.getHref()) //
                .resourceId(command.entityId()) //
                .subResourceId(command.subentityId()) //
                .commandAsJson(command.json()) //
                .maker(maker) //
                .madeOnDate(DateUtils.getAuditOffsetDateTime()) //
                .status(status) //
                .idempotencyKey(idempotencyKey) //
                .officeId(wrapper.getOfficeId()) //
                .groupId(command.getGroupId()) //
                .clientId(command.getClientId()) //
                .loanId(command.getLoanId()) //
                .savingsId(command.getSavingsId()) //
                .productId(command.getProductId()) //
                .transactionId(command.getTransactionId()) //
                .creditBureauId(command.getCreditBureauId()) //
                .organisationCreditBureauId(command.getOrganisationCreditBureauId()) //
                .clientIp(IpAddressUtils.getClientIp()) //
                .loanExternalId(command.getLoanExternalId()).sanitized(sanitized).submittedIdsKept(true).build(); //
    }

    public String getPermissionCode() {
        return this.actionName + "_" + this.entityName;
    }

    @NotNull
    public CommandProcessingResultType getStatusEnum() {
        return CommandProcessingResultType.fromInt(status);
    }

    public void setStatus(@NotNull CommandProcessingResultType status) {
        this.status = status.getValue();
    }

    public void markAsAwaitingApproval() {
        setStatus(CommandProcessingResultType.AWAITING_APPROVAL);
    }

    public boolean isAwaitingApproval() {
        return getStatusEnum().isAwaitingApproval();
    }

    public boolean isProcessed() {
        return getStatusEnum().isProcessed();
    }

    public void markAsChecked(final AppUser checker) {
        this.checker = checker;
        this.checkedOnDate = DateUtils.getAuditOffsetDateTime();
        setStatus(CommandProcessingResultType.PROCESSED);
    }

    public boolean isChecked() {
        return checker != null && isProcessed();
    }

    public void markAsRejected(final AppUser checker) {
        this.checker = checker;
        this.checkedOnDate = DateUtils.getAuditOffsetDateTime();
        setStatus(CommandProcessingResultType.REJECTED);
    }

    public boolean isRejected() {
        return checker != null && isRejected();
    }

    /**
     * Stores the identifiers returned by the command in the result columns, leaving the request columns untouched so
     * the command stays searchable by what it was submitted with.
     */
    public void updateForAudit(final CommandProcessingResult result) {
        this.resultOfficeId = result.getOfficeId();
        this.resultGroupId = result.getGroupId();
        this.resultClientId = result.getClientId();
        this.resultLoanId = result.getLoanId();
        this.resultSavingsId = result.getSavingsId();
        this.resultProductId = result.getProductId();
        this.resultTransactionId = result.getTransactionId();
        this.resultResourceId = result.getResourceId();
        this.resultResourceExternalId = result.getResourceExternalId();
        this.resultSubResourceId = result.getSubResourceId();
        this.resultSubResourceExternalId = result.getSubResourceExternalId();
        this.resultLoanExternalId = result.getLoanExternalId();
        this.resultIdsWritten = true;
    }
}
