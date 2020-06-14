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

package org.apache.fineract.portfolio.client.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@SuppressWarnings("serial")
@Entity
@Table(name = "m_client_transfer_details")
public class ClientTransferDetails extends AbstractPersistableCustom {

    @Column(name = "client_id", length = 20, unique = true, nullable = false)
    private Long clientId;

    @Column(name = "from_office_id", nullable = false)
    private Long fromOfficeId;

    @Column(name = "to_office_id", nullable = false)
    private Long toOfficeId;

    @Column(name = "proposed_transfer_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date proposedTransferDate;

    @Column(name = "transfer_type", nullable = false)
    private Integer transferEventType;

    @Column(name = "submitted_on", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date submittedOn;

    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    protected ClientTransferDetails() {}

    private ClientTransferDetails(final Long clientId, final Long fromOfficeId, final Long toOfficeId, final Date proposedTransferDate,
            final Integer transferEventType, final Date submittedOn, final Long submittedBy) {
        this.clientId = clientId;
        this.fromOfficeId = fromOfficeId;
        this.toOfficeId = toOfficeId;
        this.proposedTransferDate = proposedTransferDate;
        this.transferEventType = transferEventType;
        this.submittedOn = submittedOn;
        this.submittedBy = submittedBy;
    }

    public static ClientTransferDetails instance(final Long clientId, final Long fromOfficeId, final Long toOfficeId,
            final Date proposedTransferDate, final Integer transferEventType, final Date submittedOn, final Long submittedBy) {
        return new ClientTransferDetails(clientId, fromOfficeId, toOfficeId, proposedTransferDate, transferEventType, submittedOn,
                submittedBy);

    }

}
