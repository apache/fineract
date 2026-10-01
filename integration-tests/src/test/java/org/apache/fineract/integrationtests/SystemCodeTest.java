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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostCodesRequest;
import org.apache.fineract.client.models.PutCodeValueDataResponse;
import org.apache.fineract.client.models.PutCodeValuesDataRequest;
import org.apache.fineract.client.models.PutCodesRequest;
import org.apache.fineract.client.models.PutCodesResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for creating, updating, deleting codes and code values
 *
 */
public class SystemCodeTest extends FeignIntegrationTest {

    // @Ignore()
    @Test
    // scenario 57, 58, 59, 60
    public void testCreateCode() {
        final String codeName = "Client Marital Status";

        final Long createResponseId = createCode(codeName);

        // verify code created

        final GetCodesResponse newCodeAttributes = ok(() -> fineractClient().codes().retrieveOneCode(createResponseId));

        Assertions.assertNotNull(newCodeAttributes);
        assertEquals(createResponseId, newCodeAttributes.getId(), "Verify value of codeId");

        assertEquals(codeName, newCodeAttributes.getName(), "Verify code name");
        assertEquals(false, newCodeAttributes.getSystemDefined(), "Verify system defined is false");

        // update code
        final PutCodesResponse updateChangeResponse = ok(
                () -> fineractClient().codes().updateCode(createResponseId, new PutCodesRequest().name(codeName + "(CHANGE)")));

        assertEquals(codeName + "(CHANGE)", updateChangeResponse.getChanges().getName(), "Verify code name updated");

        // delete code
        final Long deleteResponseId = ok(() -> fineractClient().codes().deleteCode(createResponseId)).getResourceId();
        assertEquals(createResponseId, deleteResponseId, "Verify code deleted");

        // verify code deleted
        final CallFailedRuntimeException deletedCode = fail(() -> fineractClient().codes().retrieveOneCode(deleteResponseId));
        assertEquals(404, deletedCode.getStatus(), "Verify code no longer exists");
    }

    // @Ignore()
    @Test
    // scenario 57, 60
    public void testPreventCreateDuplicateCode() {
        final String codeName = "Client Marital Status";

        // create code
        final Long createResponseId = createCode(codeName);

        // verify code created
        final GetCodesResponse newCodeAttributes = ok(() -> fineractClient().codes().retrieveOneCode(createResponseId));

        Assertions.assertNotNull(newCodeAttributes);
        assertEquals(createResponseId, newCodeAttributes.getId(), "Verify value of codeId");

        assertEquals(codeName, newCodeAttributes.getName(), "Verify code name");
        assertEquals(false, newCodeAttributes.getSystemDefined(), "Verify system defined is false");

        // try to create duplicate-- should fail
        final CallFailedRuntimeException error = fail(() -> fineractClient().codes().createCode(new PostCodesRequest().name(codeName)));

        assertEquals("error.msg.code.duplicate.name", FeignErrors.firstError(error).userMessageGlobalisationCode(),
                "Verify duplication error");

        // delete code that was just created

        final Long deleteResponseId = ok(() -> fineractClient().codes().deleteCode(createResponseId)).getResourceId();
        assertEquals(createResponseId, deleteResponseId, "Verify code deleted");

        // verify code deleted
        final CallFailedRuntimeException deletedCode = fail(() -> fineractClient().codes().retrieveOneCode(deleteResponseId));
        assertEquals(404, deletedCode.getStatus(), "Verify code no longer exists");

    }

    // @Ignore
    @Test
    public void testUpdateDeleteSystemDefinedCode() {

        // get any systemDefined code
        final GetCodesResponse systemDefinedCode = ok(() -> fineractClient().codes().retrieveAllCodes()).stream()
                .filter(code -> Boolean.TRUE.equals(code.getSystemDefined())).findFirst().orElseThrow();

        // delete system-defined code should fail
        final CallFailedRuntimeException error = fail(() -> fineractClient().codes().deleteCode(systemDefinedCode.getId()));

        assertEquals("error.msg.code.systemdefined", FeignErrors.firstError(error).userMessageGlobalisationCode(),
                "Cannot delete system-defined code");

        // update system-defined code should fail

        final CallFailedRuntimeException updateError = fail(() -> fineractClient().codes().updateCode(systemDefinedCode.getId(),
                new PutCodesRequest().name(systemDefinedCode.getName() + "CHANGE")));

        assertEquals("error.msg.code.systemdefined", FeignErrors.firstError(updateError).userMessageGlobalisationCode(),
                "Cannot update system-defined code");

    }

    // @Ignore
    @Test
    public void testCodeValuesNotAssignedToTable() {

        final String codeName = Utils.uniqueRandomStringGenerator("Marital Status1", 10);

        final String codeValue1 = "Married1";
        final String codeValue2 = "Unmarried1";

        final int codeValue1Position = 1;
        final int codeValue2Position = 1;

        final String codeDescription1 = "Description11";
        final String codeDescription2 = "Description22";

        // create code
        final Long createCodeResponseId = createCode(codeName);

        // create first code value
        final Long createCodeValueResponseId1 = createCodeValue(createCodeResponseId, codeValue1, codeDescription1, codeValue1Position);

        // create second code value
        final Long createCodeValueResponseId2 = createCodeValue(createCodeResponseId, codeValue2, codeDescription2, codeValue1Position);

        // verify two code values created

        final List<GetCodeValuesDataResponse> codeValuesList = ok(
                () -> fineractClient().codeValues().retrieveAllCodeValues(createCodeResponseId));

        assertEquals(2, codeValuesList.size(), "Number of code values returned matches number created");

        // verify values of first code value
        final GetCodeValuesDataResponse codeValuesAttributes1 = ok(
                () -> fineractClient().codeValues().retrieveCodeValue(createCodeValueResponseId1, createCodeResponseId));

        Assertions.assertNotNull(codeValuesAttributes1);
        assertEquals(createCodeValueResponseId1, codeValuesAttributes1.getId(), "Verify value of codeValueId");

        assertEquals(codeValue1, codeValuesAttributes1.getName(), "Verify value of code name");

        assertEquals(codeDescription1, codeValuesAttributes1.getDescription(), "Verify value of code description");

        assertEquals(codeValue1Position, codeValuesAttributes1.getPosition(), "Verify position of code value");

        // verify values of second code value
        final GetCodeValuesDataResponse codeValuesAttributes2 = ok(
                () -> fineractClient().codeValues().retrieveCodeValue(createCodeValueResponseId2, createCodeResponseId));

        Assertions.assertNotNull(codeValuesAttributes2);
        assertEquals(createCodeValueResponseId2, codeValuesAttributes2.getId(), "Verify value of codeValueId");

        assertEquals(codeValue2, codeValuesAttributes2.getName(), "Verify value of code name");

        assertEquals(codeDescription2, codeValuesAttributes2.getDescription(), "Verify value of code description");

        assertEquals(codeValue2Position, codeValuesAttributes2.getPosition(), "Verify position of code value");

        // update code value 1
        final PutCodeValueDataResponse codeValueChanges = ok(
                () -> fineractClient().codeValues().updateCodeValue(createCodeResponseId, createCodeValueResponseId1,
                        new PutCodeValuesDataRequest().name(codeValue1 + "CHANGE").description(codeDescription1 + "CHANGE").position(4)));

        assertEquals(codeValue1 + "CHANGE", codeValueChanges.getChanges().getName(), "Verify changed code value name");

        assertEquals(codeDescription1 + "CHANGE", codeValueChanges.getChanges().getDescription(), "Verify changed code value description");

        // delete code value
        final Long deletedCodeValueResponseId1 = ok(
                () -> fineractClient().codeValues().deleteCodeValue(createCodeResponseId, createCodeValueResponseId1)).getSubResourceId();

        // Verify code value deleted

        final CallFailedRuntimeException deletedCodeValueAttributes1 = fail(
                () -> fineractClient().codeValues().retrieveCodeValue(deletedCodeValueResponseId1, createCodeResponseId));

        assertEquals("error.msg.codevalue.id.invalid", FeignErrors.firstError(deletedCodeValueAttributes1).userMessageGlobalisationCode());

        final List<GetCodeValuesDataResponse> deletedCodeValuesList = ok(
                () -> fineractClient().codeValues().retrieveAllCodeValues(createCodeResponseId));

        assertEquals(1, deletedCodeValuesList.size(), "Number of code values is 1");

        final Long deletedCodeValueResponseId2 = ok(
                () -> fineractClient().codeValues().deleteCodeValue(createCodeResponseId, createCodeValueResponseId2)).getSubResourceId();

        final CallFailedRuntimeException deletedCodeValueAttributes2 = fail(
                () -> fineractClient().codeValues().retrieveCodeValue(deletedCodeValueResponseId2, createCodeResponseId));

        assertEquals("error.msg.codevalue.id.invalid", FeignErrors.firstError(deletedCodeValueAttributes2).userMessageGlobalisationCode());

        final List<GetCodeValuesDataResponse> deletedCodeValuesList1 = ok(
                () -> fineractClient().codeValues().retrieveAllCodeValues(createCodeResponseId));

        assertEquals(0, deletedCodeValuesList1.size(), "Number of code values is 0");

    }

    @Disabled
    @Test
    public void testCodeValuesAssignedToTable() {

    }

    private Long createCode(String codeName) {
        return ok(() -> fineractClient().codes().createCode(new PostCodesRequest().name(codeName))).getResourceId();
    }

    private Long createCodeValue(Long codeId, String name, String description, int position) {
        return ok(() -> fineractClient().codeValues().createCodeValue(codeId,
                new PostCodeValuesDataRequest().name(name).description(description).position(position))).getSubResourceId();
    }
}
