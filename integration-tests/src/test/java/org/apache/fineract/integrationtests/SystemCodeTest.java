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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for creating, updating, deleting codes and code values
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SystemCodeTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification generalResponseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        this.generalResponseSpec = new ResponseSpecBuilder().build();

    }

    // @Ignore()
    @Test
    // scenario 57, 58, 59, 60
    public void testCreateCode() {
        final String codeName = "Client Marital Status";

        final Integer createResponseId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, codeName,
                CodeHelper.RESPONSE_ID_ATTRIBUTE_NAME);

        // verify code created

        final HashMap newCodeAttributes = (HashMap) CodeHelper.getCodeById(this.requestSpec, this.responseSpec, createResponseId, "");

        Assertions.assertNotNull(newCodeAttributes);
        assertEquals(createResponseId, newCodeAttributes.get(CodeHelper.CODE_ID_ATTRIBUTE_NAME), "Verify value of codeId");

        assertEquals(codeName, newCodeAttributes.get(CodeHelper.CODE_NAME_ATTRIBUTE_NAME), "Verify code name");
        assertEquals(false, newCodeAttributes.get(CodeHelper.CODE_SYSTEM_DEFINED_ATTRIBUTE_NAME), "Verify system defined is false");

        // update code
        final HashMap updateChangeResponse = (HashMap) CodeHelper.updateCode(this.requestSpec, this.responseSpec, createResponseId,
                codeName + "(CHANGE)", "changes");

        assertEquals(codeName + "(CHANGE)", updateChangeResponse.get(CodeHelper.CODE_NAME_ATTRIBUTE_NAME), "Verify code name updated");

        // delete code
        final Integer deleteResponseId = (Integer) CodeHelper.deleteCodeById(this.requestSpec, this.responseSpec, createResponseId,
                CodeHelper.RESPONSE_ID_ATTRIBUTE_NAME);
        assertEquals(createResponseId, deleteResponseId, "Verify code deleted");

        // verify code deleted
        final HashMap deletedCodeValues = (HashMap) CodeHelper.getCodeById(this.requestSpec, this.generalResponseSpec, deleteResponseId,
                "");

        Assertions.assertNotNull(deletedCodeValues);
        assertNull(deletedCodeValues.get(CodeHelper.CODE_ID_ATTRIBUTE_NAME), "Verify value of codeId");

        assertNull(deletedCodeValues.get(CodeHelper.CODE_NAME_ATTRIBUTE_NAME), "Verify code name");
        assertNull(deletedCodeValues.get(CodeHelper.CODE_SYSTEM_DEFINED_ATTRIBUTE_NAME), "Verify system defined is false");
    }

    // @Ignore()
    @Test
    // scenario 57, 60
    public void testPreventCreateDuplicateCode() {
        final String codeName = "Client Marital Status";

        // create code
        final Integer createResponseId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, codeName,
                CodeHelper.RESPONSE_ID_ATTRIBUTE_NAME);

        // verify code created
        final HashMap newCodeAttributes = (HashMap) CodeHelper.getCodeById(this.requestSpec, this.responseSpec, createResponseId, "");

        Assertions.assertNotNull(newCodeAttributes);
        assertEquals(createResponseId, newCodeAttributes.get(CodeHelper.CODE_ID_ATTRIBUTE_NAME), "Verify value of codeId");

        assertEquals(codeName, newCodeAttributes.get(CodeHelper.CODE_NAME_ATTRIBUTE_NAME), "Verify code name");
        assertEquals(false, newCodeAttributes.get(CodeHelper.CODE_SYSTEM_DEFINED_ATTRIBUTE_NAME), "Verify system defined is false");

        // try to create duplicate-- should fail
        final List<HashMap> error = (List) CodeHelper.createCode(this.requestSpec, this.generalResponseSpec, codeName,
                CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.code.duplicate.name", error.get(0).get("userMessageGlobalisationCode"), "Verify duplication error");

        // delete code that was just created

        final Integer deleteResponseId = (Integer) CodeHelper.deleteCodeById(this.requestSpec, this.responseSpec, createResponseId,
                CodeHelper.RESPONSE_ID_ATTRIBUTE_NAME);
        assertEquals(createResponseId, deleteResponseId, "Verify code deleted");

        // verify code deleted
        final HashMap deletedCodeAttributes = (HashMap) CodeHelper.getCodeById(this.requestSpec, this.generalResponseSpec, deleteResponseId,
                "");

        Assertions.assertNotNull(deletedCodeAttributes);
        assertNull(deletedCodeAttributes.get(CodeHelper.CODE_ID_ATTRIBUTE_NAME), "Verify value of codeId");

        assertNull(deletedCodeAttributes.get(CodeHelper.CODE_NAME_ATTRIBUTE_NAME), "Verify code name");
        assertNull(deletedCodeAttributes.get(CodeHelper.CODE_SYSTEM_DEFINED_ATTRIBUTE_NAME), "Verify system defined is false");

    }

    // @Ignore
    @Test
    public void testUpdateDeleteSystemDefinedCode() {

        // get any systemDefined code
        final HashMap systemDefinedCode = (HashMap) CodeHelper.getSystemDefinedCodes(this.requestSpec, this.responseSpec);

        // delete system-defined code should fail
        final List<HashMap> error = (List) CodeHelper.deleteCodeById(this.requestSpec, this.generalResponseSpec,
                (Integer) systemDefinedCode.get(CodeHelper.CODE_ID_ATTRIBUTE_NAME), CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.code.systemdefined", error.get(0).get("userMessageGlobalisationCode"), "Cannot delete system-defined code");

        // update system-defined code should fail

        final List<HashMap> updateError = (List) CodeHelper.updateCode(this.requestSpec, this.generalResponseSpec,
                (Integer) systemDefinedCode.get(CodeHelper.CODE_ID_ATTRIBUTE_NAME),
                systemDefinedCode.get(CodeHelper.CODE_NAME_ATTRIBUTE_NAME) + "CHANGE", CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.code.systemdefined", updateError.get(0).get("userMessageGlobalisationCode"),
                "Cannot update system-defined code");

    }

    // @Ignore
    @Test
    public void testCodeValuesNotAssignedToTable() {

        final String codeName = Utils.randomNameGenerator("Marital Status1", 10);

        final String codeValue1 = "Married1";
        final String codeValue2 = "Unmarried1";

        final int codeValue1Position = 1;
        final int codeValue2Position = 1;

        final String codeDescription1 = "Description11";
        final String codeDescription2 = "Description22";

        // create code
        final Integer createCodeResponseId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, codeName,
                CodeHelper.RESPONSE_ID_ATTRIBUTE_NAME);

        // create first code value
        final Integer createCodeValueResponseId1 = (Integer) CodeHelper.createCodeValue(this.requestSpec, this.responseSpec,
                createCodeResponseId, codeValue1, codeDescription1, codeValue1Position, CodeHelper.SUBRESPONSE_ID_ATTRIBUTE_NAME);

        // create second code value
        final Integer createCodeValueResponseId2 = (Integer) CodeHelper.createCodeValue(this.requestSpec, this.responseSpec,
                createCodeResponseId, codeValue2, codeDescription2, codeValue1Position, CodeHelper.SUBRESPONSE_ID_ATTRIBUTE_NAME);

        // verify two code values created

        final List<HashMap> codeValuesList = (List) CodeHelper.getCodeValuesForCode(this.requestSpec, this.responseSpec,
                createCodeResponseId, "");

        assertEquals(2, codeValuesList.size(), "Number of code values returned matches number created");

        // verify values of first code value
        final HashMap codeValuesAttributes1 = (HashMap) CodeHelper.getCodeValueById(this.requestSpec, this.responseSpec,
                createCodeResponseId, createCodeValueResponseId1, "");

        Assertions.assertNotNull(codeValuesAttributes1);
        assertEquals(createCodeValueResponseId1, codeValuesAttributes1.get(CodeHelper.CODE_VALUE_ID_ATTRIBUTE_NAME),
                "Verify value of codeValueId");

        assertEquals(codeValue1, codeValuesAttributes1.get(CodeHelper.CODE_VALUE_NAME_ATTRIBUTE_NAME), "Verify value of code name");

        assertEquals(codeDescription1, codeValuesAttributes1.get(CodeHelper.CODE_VALUE_DESCRIPTION_ATTRIBUTE_NAME),
                "Verify value of code description");

        assertEquals(codeValue1Position, codeValuesAttributes1.get(CodeHelper.CODE_VALUE_POSITION_ATTRIBUTE_NAME),
                "Verify position of code value");

        // verify values of second code value
        final HashMap codeValuesAttributes2 = (HashMap) CodeHelper.getCodeValueById(this.requestSpec, this.responseSpec,
                createCodeResponseId, createCodeValueResponseId2, "");

        Assertions.assertNotNull(codeValuesAttributes2);
        assertEquals(createCodeValueResponseId2, codeValuesAttributes2.get(CodeHelper.CODE_VALUE_ID_ATTRIBUTE_NAME),
                "Verify value of codeValueId");

        assertEquals(codeValue2, codeValuesAttributes2.get(CodeHelper.CODE_VALUE_NAME_ATTRIBUTE_NAME), "Verify value of code name");

        assertEquals(codeDescription2, codeValuesAttributes2.get(CodeHelper.CODE_VALUE_DESCRIPTION_ATTRIBUTE_NAME),
                "Verify value of code description");

        assertEquals(codeValue2Position, codeValuesAttributes2.get(CodeHelper.CODE_VALUE_POSITION_ATTRIBUTE_NAME),
                "Verify position of code value");

        // update code value 1
        final HashMap codeValueChanges = (HashMap) CodeHelper.updateCodeValue(this.requestSpec, this.responseSpec, createCodeResponseId,
                createCodeValueResponseId1, codeValue1 + "CHANGE", codeDescription1 + "CHANGE", 4, "changes");

        assertEquals(codeValue1 + "CHANGE", codeValueChanges.get("name"), "Verify changed code value name");

        assertEquals(codeDescription1 + "CHANGE", codeValueChanges.get("description"), "Verify changed code value description");

        // delete code value
        Integer deletedCodeValueResponseId1 = (Integer) CodeHelper.deleteCodeValueById(this.requestSpec, this.generalResponseSpec,
                createCodeResponseId, createCodeValueResponseId1, CodeHelper.SUBRESPONSE_ID_ATTRIBUTE_NAME);

        // Verify code value deleted

        final ArrayList<HashMap> deletedCodeValueAttributes1 = (ArrayList<HashMap>) CodeHelper.getCodeValueById(this.requestSpec,
                this.generalResponseSpec, createCodeResponseId, deletedCodeValueResponseId1, CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.codevalue.id.invalid", deletedCodeValueAttributes1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        final List<HashMap> deletedCodeValuesList = (List) CodeHelper.getCodeValuesForCode(this.requestSpec, this.responseSpec,
                createCodeResponseId, "");

        assertEquals(1, deletedCodeValuesList.size(), "Number of code values is 1");

        final Integer deletedCodeValueResponseId2 = (Integer) CodeHelper.deleteCodeValueById(this.requestSpec, this.generalResponseSpec,
                createCodeResponseId, createCodeValueResponseId2, CodeHelper.SUBRESPONSE_ID_ATTRIBUTE_NAME);

        final ArrayList<HashMap> deletedCodeValueAttributes2 = (ArrayList<HashMap>) CodeHelper.getCodeValueById(this.requestSpec,
                this.generalResponseSpec, createCodeResponseId, deletedCodeValueResponseId2, CommonConstants.RESPONSE_ERROR);

        assertEquals(deletedCodeValueAttributes2.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE), "error.msg.codevalue.id.invalid");

        final List<HashMap> deletedCodeValuesList1 = (List) CodeHelper.getCodeValuesForCode(this.requestSpec, this.responseSpec,
                createCodeResponseId, "");

        assertEquals(0, deletedCodeValuesList1.size(), "Number of code values is 0");

    }

    @Disabled
    @Test
    public void testCodeValuesAssignedToTable() {

    }

}
