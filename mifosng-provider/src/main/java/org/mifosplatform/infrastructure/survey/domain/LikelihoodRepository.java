package org.mifosplatform.infrastructure.survey.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by Cieyou on 3/12/14.
 */
public interface LikelihoodRepository extends JpaRepository<Likelihood, Long>, JpaSpecificationExecutor<Likelihood> {


}
