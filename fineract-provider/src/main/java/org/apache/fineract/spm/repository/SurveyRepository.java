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
package org.apache.fineract.spm.repository;

import org.apache.fineract.spm.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("select s from Survey s where :pointInTime between s.validFrom and s.validTo")
    List<Survey> fetchActiveSurveys(@Param("pointInTime") final Date pointInTime);
    
    @Query("select s from Survey s ")
    List<Survey> fetchAllSurveys();

    @Query("select s from Survey s where s.key = :key and :pointInTime between s.validFrom and s.validTo")
    Survey findByKey(@Param("key") final String key, @Param("pointInTime") final Date pointInTime);
}
