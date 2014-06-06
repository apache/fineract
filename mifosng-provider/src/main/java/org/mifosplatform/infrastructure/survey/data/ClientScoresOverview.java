package org.mifosplatform.infrastructure.survey.data;

import org.joda.time.LocalDate;

/**
 * Created by Cieyou on 3/18/14.
 */
public class ClientScoresOverview {

    final private String surveyName;
    final private long id ;
    final private String likelihoodCode ;
    final private String likelihoodName;
    final private long score;
    final private Double povertyLine;
    final private LocalDate date;

    public ClientScoresOverview(final String likelihoodCode,
                                final String likelihoodName,
                                final long score,
                                final Double povertyLine,
                                final LocalDate date,
                                final long resourceId,
                                final String surveyName) {

        this.likelihoodCode = likelihoodCode;
        this.likelihoodName = likelihoodName;
        this.score = score;
        this.povertyLine = povertyLine;
        this.date = date;
        this.id = resourceId;
        this.surveyName = surveyName;

    }
}
