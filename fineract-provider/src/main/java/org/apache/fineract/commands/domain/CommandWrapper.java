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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.useradministration.api.PasswordPreferencesApiConstants;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CommandWrapper {

    private Long commandId;
    @SuppressWarnings("unused")
    private Long officeId;
    private Long groupId;
    private Long clientId;
    private Long loanId;
    private Long savingsId;
    private String actionName;
    private String entityName;
    private String taskPermissionName;
    private Long entityId;
    private Long subentityId;
    private String href;
    private String json;
    private String transactionId;
    private Long productId;
    private Long creditBureauId;
    private Long organisationCreditBureauId;

    @SuppressWarnings("unused")
    private Long templateId;

    public static CommandWrapper wrap(final String actionName, final String entityName, final Long resourceId, final Long subresourceId) {
        return new CommandWrapper().setActionName(actionName).setEntityName(entityName).setEntityId(resourceId)
                .setSubentityId(subresourceId);
    }

    public static CommandWrapper fromExistingCommand(final Long commandId, final String actionName, final String entityName,
            final Long resourceId, final Long subresourceId, final String resourceGetUrl, final Long productId) {
        return new CommandWrapper().setCommandId(commandId).setActionName(actionName).setEntityName(entityName).setEntityId(resourceId)
                .setSubentityId(subresourceId).setHref(resourceGetUrl).setProductId(productId);
    }

    public static CommandWrapper fromExistingCommand(final Long commandId, final String actionName, final String entityName,
            final Long resourceId, final Long subresourceId, final String resourceGetUrl, final Long productId, final Long officeId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId,
            final Long creditBureauId, final Long organisationCreditBureauId) {
        return new CommandWrapper().setCommandId(commandId).setActionName(actionName).setEntityName(entityName).setEntityId(resourceId)
                .setSubentityId(subresourceId).setHref(resourceGetUrl).setProductId(productId).setOfficeId(officeId).setGroupId(groupId)
                .setClientId(clientId).setLoanId(loanId).setSavingsId(savingsId).setTransactionId(transactionId)
                .setCreditBureauId(creditBureauId).setOrganisationCreditBureauId(organisationCreditBureauId);
    }

    public boolean isCreateDatatable() {
        return this.actionName.equalsIgnoreCase("CREATE") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isDeleteDatatable() {
        return this.actionName.equalsIgnoreCase("DELETE") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isUpdateDatatable() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isDatatableResource() {
        return this.href.startsWith("/datatables/");
    }

    public boolean isDelete() {
        return actionName.equalsIgnoreCase("DELETE") && this.entityId != null;
    }

    public boolean isDeleteOneToOne() {
        /* also covers case of deleting all of a one to many */
        return isDatatableResource() && actionName.equalsIgnoreCase("DELETE") && this.subentityId == null;
    }

    public boolean isDeleteMultiple() {
        return isDatatableResource() && actionName.equalsIgnoreCase("DELETE") && this.subentityId != null;
    }

    public boolean isUpdateOneToOne() {
        return isDatatableResource() && actionName.equalsIgnoreCase("UPDATE") && this.subentityId == null;
    }

    public boolean isUpdateMultiple() {
        return isDatatableResource() && actionName.equalsIgnoreCase("UPDATE") && this.subentityId != null;
    }

    public boolean isRegisterDatatable() {
        return this.actionName.equalsIgnoreCase("REGISTER") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isNoteResource() {
        boolean isnoteResource = false;
        if (this.entityName.equalsIgnoreCase("CLIENTNOTE") || this.entityName.equalsIgnoreCase("LOANNOTE")
                || this.entityName.equalsIgnoreCase("LOANTRANSACTIONNOTE") || this.entityName.equalsIgnoreCase("SAVINGNOTE")
                || this.entityName.equalsIgnoreCase("GROUPNOTE")) {
            isnoteResource = true;
        }
        return isnoteResource;
    }

    public boolean isUpdateOfOwnUserDetails(final Long loggedInUserId) {
        return entityName.equalsIgnoreCase("USER") && isUpdate() && loggedInUserId.equals(this.entityId);
    }

    public boolean isUpdate() {
        // permissions resource has special update which involves no resource.
        return (entityName.equalsIgnoreCase("PERMISSION") && actionName.equalsIgnoreCase("UPDATE"))
                || (entityName.equalsIgnoreCase("CURRENCY") && actionName.equalsIgnoreCase("UPDATE"))
                || (actionName.equalsIgnoreCase("CACHE") && actionName.equalsIgnoreCase("UPDATE"))
                || (actionName.equalsIgnoreCase("WORKINGDAYS") && actionName.equalsIgnoreCase("UPDATE"))
                || (isPasswordPreferencesResource() && actionName.equalsIgnoreCase("UPDATE"))
                || (actionName.equalsIgnoreCase("UPDATE") && (this.entityId != null));
    }

    public boolean isSurveyResource() {
        return this.href.startsWith("/survey/");
    }

    public boolean isPasswordPreferencesResource() {
        return this.entityName.equalsIgnoreCase(PasswordPreferencesApiConstants.ENTITY_NAME);
    }

    public boolean isUpdateDisbursementDate() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("DISBURSEMENTDETAIL")
                && this.entityId != null;
    }

    public boolean addAndDeleteDisbursementDetails() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("DISBURSEMENTDETAIL")
                && this.entityId == null;
    }
}
