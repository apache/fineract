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
package org.apache.fineract.portfolio.transfer.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when not supported loan
 * template type is sent.
 */
public class TransferNotSupportedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons for invalid Journal Entry **/
    public static enum TRANSFER_NOT_SUPPORTED_REASON {
        CLIENT_DESTINATION_GROUP_NOT_SPECIFIED, CLIENT_BELONGS_TO_MULTIPLE_GROUPS, SOURCE_AND_DESTINATION_GROUP_CANNOT_BE_SAME, ACTIVE_SAVINGS_ACCOUNT, BULK_CLIENT_TRANSFER_ACROSS_BRANCHES, DESTINATION_GROUP_MEETING_FREQUENCY_MISMATCH, DESTINATION_GROUP_HAS_NO_MEETING;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("ACTIVE_SAVINGS_ACCOUNT")) {
                return "Cannot transfer Clients/Groups having active Savings accounts";
            } else if (name().toString().equalsIgnoreCase("BULK_CLIENT_TRANSFER_ACROSS_BRANCHES")) {
                return "Bulk Transfers of clients between Groups in different branches not allowed ";
            } else if (name().toString().equalsIgnoreCase("CLIENT_DESTINATION_GROUP_NOT_SPECIFIED")) {
                return "Destination Group for transfer of clients originally linked to a group not selected ";
            } else if (name().toString().equalsIgnoreCase("CLIENT_BELONGS_TO_MULTIPLE_GROUPS")) {
                return "Transfer of clients linked to multiple groups is not supported  ";
            } else if (name().toString().equalsIgnoreCase("DESTINATION_GROUP_MEETING_FREQUENCY_MISMATCH")) {
                return "Cannot transfer Clients with active accounts between groups with different meeting frequency";
            } else if (name().toString().equalsIgnoreCase("SOURCE_AND_DESTINATION_GROUP_CANNOT_BE_SAME")) {
                return "Source and destination groups for bulk client transfers should be different";
            } else if (name().toString().equalsIgnoreCase("DESTINATION_GROUP_HAS_NO_MEETING")) { return "Cannot transfer Client with active accounts to a groups with no meeting frequency"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("ACTIVE_SAVINGS_ACCOUNT")) {
                return "error.msg.entity.transfers.with.active.savings.accounts";
            } else if (name().toString().equalsIgnoreCase("BULK_CLIENT_TRANSFER_ACROSS_BRANCHES")) {
                return "error.msg.groups.bulk.client.transfers.to.different.office";
            } else if (name().toString().equalsIgnoreCase("CLIENT_DESTINATION_GROUP_NOT_SPECIFIED")) {
                return "error.msg.client.transfers.destination.group.absent";
            } else if (name().toString().equalsIgnoreCase("CLIENT_BELONGS_TO_MULTIPLE_GROUPS")) {
                return "error.msg.client.transfers.with.multiple.group.linkages";
            } else if (name().toString().equalsIgnoreCase("DESTINATION_GROUP_MEETING_FREQUENCY_MISMATCH")) {
                return "error.msg.client.transfers.with.active.accounts.between.groups.with.different.meeting.frequency";
            } else if (name().toString().equalsIgnoreCase("SOURCE_AND_DESTINATION_GROUP_CANNOT_BE_SAME")) {
                return "error.msg.groups.bulk.client.transfers.to.same.group";
            } else if (name().toString().equalsIgnoreCase("DESTINATION_GROUP_HAS_NO_MEETING")) { return "error.msg.client.transfers.with.active.accounts.to.group.with.no.meeting.frequencys"; }
            return name().toString();
        }
    }

    public TransferNotSupportedException(final TRANSFER_NOT_SUPPORTED_REASON reason, final Object... defaultUserMessageArgs) {
        super(reason.errorCode(), reason.errorMessage(), defaultUserMessageArgs);
    }

}