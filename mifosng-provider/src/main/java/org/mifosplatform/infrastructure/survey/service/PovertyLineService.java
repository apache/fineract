package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PpiPovertyLineData;

import java.util.List;

/**
 * Created by Cieyou on 3/11/14.
 */
public interface PovertyLineService {

    PpiPovertyLineData retrieveAll(final String ppiName);

    LikeliHoodPovertyLineData retrieveForLikelihood(final String ppiName,final Long likelihood);

    //PovertyLineData retrieve();
}
