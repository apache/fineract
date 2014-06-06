package org.mifosplatform.infrastructure.survey.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LikelihoodRepository extends JpaRepository<Likelihood, Long>, JpaSpecificationExecutor<Likelihood> {

   @Query("FROM Likelihood WHERE ppi_name =:ppiName AND id !=:id")
   List<Likelihood> findByPpiNameAndLikeliHoodId(@Param("ppiName") String ppiName,@Param("id") Long likeliHoodId);

}
