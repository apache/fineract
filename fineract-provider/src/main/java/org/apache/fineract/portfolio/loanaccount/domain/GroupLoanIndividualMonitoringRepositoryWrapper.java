package org.apache.fineract.portfolio.loanaccount.domain;

import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
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
        if (entity == null) { throw new GroupNotFoundException(id); }
        return entity;
    }
}
