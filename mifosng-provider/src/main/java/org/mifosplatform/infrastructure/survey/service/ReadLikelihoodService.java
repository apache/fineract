package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.survey.data.LikelihoodData;

import java.util.List;

/**
 * Created by Cieyou on 3/12/14.
 */
public interface ReadLikelihoodService {

    public List<LikelihoodData> retrieveAll(final String ppiName);
    public LikelihoodData retrieve(final Long likelihoodId);
}
