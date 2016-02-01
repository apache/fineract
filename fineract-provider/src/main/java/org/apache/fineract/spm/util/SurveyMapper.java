/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.util;

import org.mifosplatform.spm.data.ComponentData;
import org.mifosplatform.spm.data.QuestionData;
import org.mifosplatform.spm.data.ResponseData;
import org.mifosplatform.spm.data.SurveyData;
import org.mifosplatform.spm.domain.Component;
import org.mifosplatform.spm.domain.Question;
import org.mifosplatform.spm.domain.Response;
import org.mifosplatform.spm.domain.Survey;

import java.util.ArrayList;
import java.util.List;

public class SurveyMapper {

    private SurveyMapper() {
        super();
    }

    public static SurveyData map(final Survey survey) {
        final SurveyData surveyData = new SurveyData(
                survey.getId(), SurveyMapper.mapComponents(survey.getComponents()),
                SurveyMapper.mapQuestions(survey.getQuestions()), survey.getKey(), survey.getName(),
                survey.getDescription(), survey.getCountryCode(), survey.getValidFrom(), survey.getValidTo()
        );
        return surveyData;
    }

    public static Survey map(final SurveyData surveyData) {
        final Survey survey = new Survey();
        survey.setComponents(SurveyMapper.mapComponentDatas(surveyData.getComponentDatas(), survey));
        survey.setQuestions(SurveyMapper.mapQuestionDatas(surveyData.getQuestionDatas(), survey));
        survey.setKey(surveyData.getKey());
        survey.setName(surveyData.getName());
        survey.setDescription(surveyData.getDescription());
        survey.setCountryCode(surveyData.getCountryCode());
        return survey;
    }

    private static List<ComponentData> mapComponents(final List<Component> components) {
        final List<ComponentData> componentDatas = new ArrayList<>();
        if (components != null) {
            for (final Component component : components) {
                componentDatas.add(new ComponentData(
                        component.getId(), component.getKey(), component.getText(), component.getDescription(),
                        component.getSequenceNo()
                ));
            }
        }
        return componentDatas;
    }

    private static List<Component> mapComponentDatas(final List<ComponentData> componentDatas, final Survey survey) {
        final List<Component> components = new ArrayList<>();
        if (componentDatas != null) {
            for (final ComponentData componentData : componentDatas) {
                final Component component = new Component();
                component.setSurvey(survey);
                component.setKey(componentData.getKey());
                component.setText(componentData.getText());
                component.setDescription(componentData.getDescription());
                component.setSequenceNo(componentData.getSequenceNo());
                components.add(component);
            }
        }
        return components;
    }

    private static List<QuestionData> mapQuestions(final List<Question> questions) {
        final List<QuestionData> questionDatas = new ArrayList<>();
        if (questions != null) {
            for (final Question question : questions) {
                questionDatas.add(new QuestionData(question.getId(),
                        SurveyMapper.mapResponses(question.getResponses()), question.getComponentKey(), question.getKey(),
                        question.getText(), question.getDescription(), question.getSequenceNo()
                ));
            }
        }
        return questionDatas;
    }

    private static List<Question> mapQuestionDatas(final List<QuestionData> questionDatas, final Survey survey) {
        final List<Question> questions = new ArrayList<>();
        if (questionDatas != null) {
            for (final QuestionData questionData : questionDatas) {
                final Question question = new Question();
                question.setSurvey(survey);
                question.setComponentKey(questionData.getComponentKey());
                question.setResponses(SurveyMapper.mapResponseDatas(questionData.getResponseDatas(), question));
                question.setKey(questionData.getKey());
                question.setText(questionData.getText());
                question.setDescription(question.getDescription());
                question.setSequenceNo(questionData.getSequenceNo());
                questions.add(question);
            }
        }
        return questions;
    }

    private static List<ResponseData> mapResponses(final List<Response> responses) {
        final List<ResponseData> responseDatas = new ArrayList<>();
        if (responses != null) {
            for (final Response response : responses) {
                responseDatas.add(new ResponseData(
                    response.getId(), response.getText(), response.getValue(), response.getSequenceNo()
                ));
            }
        }
        return responseDatas;
    }

    private static List<Response> mapResponseDatas(final List<ResponseData> responseDatas, final Question question) {
        final List<Response> responses = new ArrayList<>();
        if (responseDatas != null) {
            for (final ResponseData responseData : responseDatas) {
                final Response response = new Response();
                response.setQuestion(question);
                response.setText(responseData.getText());
                response.setValue(responseData.getValue());
                response.setSequenceNo(responseData.getSequenceNo());
                responses.add(response);
            }
        }
        return responses;
    }
}
