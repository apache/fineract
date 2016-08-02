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
package org.apache.fineract.portfolio.loanaccount.domain;

import org.apache.fineract.portfolio.loanaccount.exception.GroupLoanIndividualMonitoringNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupLoanIndividualMonitoringRepositoryWrapper {
	
	public final GroupLoanIndividualMonitoringRepository groupLoanIndividualMonitoringRepository;

	@Autowired
	public GroupLoanIndividualMonitoringRepositoryWrapper(
			GroupLoanIndividualMonitoringRepository groupLoanIndividualMonitoringRepository) {
		this.groupLoanIndividualMonitoringRepository = groupLoanIndividualMonitoringRepository;
	}
	
	public void save(final GroupLoanIndividualMonitoring entity) {
        this.groupLoanIndividualMonitoringRepository.save(entity);
    }

    public void delete(final GroupLoanIndividualMonitoring entity) {
        this.groupLoanIndividualMonitoringRepository.delete(entity);
    }
    
    public GroupLoanIndividualMonitoring findOneWithNotFoundDetection(final Long id) {
        final GroupLoanIndividualMonitoring entity = this.groupLoanIndividualMonitoringRepository.findOne(id);
        if (entity == null) { throw new GroupLoanIndividualMonitoringNotFoundException(id); }
        return entity;
    }
}
