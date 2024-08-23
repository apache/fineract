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
package org.apache.fineract.infrastructure.dataqueries.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntityDatatableChecksRepository
        extends JpaRepository<EntityDatatableChecks, Long>, JpaSpecificationExecutor<EntityDatatableChecks> {

    List<EntityDatatableChecks> findByEntityAndStatus(String entityName, Integer status);

    @Query("""
                SELECT dt
                FROM EntityDatatableChecks dt
                INNER JOIN RegisteredDatatable rdt ON rdt.datatableName = dt.datatableName
                WHERE dt.entity = :entity
                AND dt.status = :status
                AND rdt.subtype = :subtype
            """)
    List<EntityDatatableChecks> findByEntityAndStatusAndSubtype(@Param("entity") String entity, @Param("status") Integer status,
            @Param("subtype") String subtype);

    @Query("select t from  EntityDatatableChecks t WHERE t.status =:status and t.entity=:entity and t.productId = :productId ")
    List<EntityDatatableChecks> findByEntityStatusAndProduct(@Param("entity") String entity, @Param("status") Integer status,
            @Param("productId") Long productId);

    @Query("select t from  EntityDatatableChecks t WHERE t.status =:status and t.entity=:entity and t.productId IS NULL ")
    List<EntityDatatableChecks> findByEntityStatusAndNoProduct(@Param("entity") String entity, @Param("status") Integer status);

    @Query("select t from  EntityDatatableChecks t WHERE t.status =:status "
            + "and t.entity=:entity and t.datatableName = :datatableName AND t.productId = :productId")
    List<EntityDatatableChecks> findByEntityStatusAndDatatableIdAndProductId(@Param("entity") String entityName,
            @Param("status") Integer status, @Param("datatableName") String datatableName, @Param("productId") Long productId);

    @Query("select t from  EntityDatatableChecks t WHERE t.status =:status and t.entity=:entity "
            + " and t.datatableName = :datatableName AND t.productId IS NULL")
    List<EntityDatatableChecks> findByEntityStatusAndDatatableIdAndNoProduct(@Param("entity") String entityName,
            @Param("status") Integer status, @Param("datatableName") String datatableName);

}
