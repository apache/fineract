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
package org.apache.fineract.interoperation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "interop_transfer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_interop_transfer_codes", columnNames = { "transaction_code", "transfer_code" }) })
public class InteropTransfer extends AbstractPersistableCustom<Long> {

    @Column(name = "transaction_code", nullable = false, length = 128)
    private String transactionCode;

    @Column(name = "transfer_code", nullable = false, length = 128)
    private String transferCode;

    @Column(name = "state", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private InteropActionState state;

    @Column(name = "completed_timestamp")
    private LocalDateTime completedTimestamp;

    public InteropTransfer(@NotNull String transactionCode, @NotNull String transferCode, @NotNull InteropActionState state,
            LocalDateTime completedTimestamp) {
        this.transactionCode = transactionCode;
        this.transferCode = transferCode;
        update(state, completedTimestamp);
    }

    public void update(@NotNull InteropActionState state, LocalDateTime completedTimestamp) {
        this.state = state;
        this.completedTimestamp = completedTimestamp;
    }
}
