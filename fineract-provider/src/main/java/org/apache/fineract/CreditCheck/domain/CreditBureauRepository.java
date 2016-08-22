package org.apache.fineract.CreditCheck.domain;






//import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;



public interface CreditBureauRepository extends JpaRepository<CreditBureau, Long>, JpaSpecificationExecutor<CreditBureau>
{
    
    
   // CreditBureau findOne(long id);
    
    

}
