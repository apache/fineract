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
package org.apache.fineract.accounting.reconciliation.domain.repository;

import java.util.List;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReconciliationAdjustmentRepository
        extends JpaRepository<ReconciliationAdjustment, Long>, JpaSpecificationExecutor<ReconciliationAdjustment> {

    @Query("SELECT a FROM ReconciliationAdjustment a WHERE a.statementImport.id = :importId ORDER BY a.createdDate DESC")
    List<ReconciliationAdjustment> findByImportId(@Param("importId") Long importId);

    @Query("SELECT a FROM ReconciliationAdjustment a WHERE a.statementImport.id = :importId AND a.approved = :approved")
    List<ReconciliationAdjustment> findByImportIdAndApprovalStatus(@Param("importId") Long importId, @Param("approved") boolean approved);

    @Query("SELECT a FROM ReconciliationAdjustment a WHERE a.adjustmentType = :adjustmentType")
    List<ReconciliationAdjustment> findByAdjustmentType(@Param("adjustmentType") String adjustmentType);

    @Query("DELETE FROM ReconciliationAdjustment a WHERE a.statementImport.id = :importId")
    void deleteByImportId(@Param("importId") Long importId);
}
