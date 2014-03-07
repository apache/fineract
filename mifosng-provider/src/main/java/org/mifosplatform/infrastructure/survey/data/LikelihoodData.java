package org.mifosplatform.infrastructure.survey.data;

import java.util.List;

/**
 * Created by Cieyou on 3/12/14.
 */
public class LikelihoodData {

    final long resourceId;
    final String likeliHoodName;
    final String likeliHoodCode;
    final long enabled;


    public LikelihoodData(final long resourceId,
                                     final String likeliHoodName,
                                     final String likeliHoodCode,
                                     final long enabled){
        this.resourceId = resourceId;
        this.likeliHoodName = likeliHoodName;
        this.likeliHoodCode = likeliHoodCode;
        this.enabled = enabled;

    }

}
