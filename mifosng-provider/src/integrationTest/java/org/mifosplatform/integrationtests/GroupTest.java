package org.mifosplatform.integrationtests;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.GroupHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Group Test for checking Group: Creation, Activation, Client Association,
 * Updating & Deletion
 */
public class GroupTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void checkGroupFunctions() {
        Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Integer groupID = GroupHelper.createGroup(requestSpec, responseSpec);
        GroupHelper.verifyGroupCreatedOnServer(requestSpec, responseSpec, groupID);

        groupID = GroupHelper.activateGroup(requestSpec, responseSpec, groupID.toString());
        GroupHelper.verifyGroupActivatedOnServer(requestSpec, responseSpec, groupID, true);

        groupID = GroupHelper.associateClient(requestSpec, responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyGroupMembers(requestSpec, responseSpec, groupID, clientID);

        groupID = GroupHelper.disAssociateClient(requestSpec, responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyEmptyGroupMembers(requestSpec, responseSpec, groupID);

        String updatedGroupName = GroupHelper.randomNameGenerator("Group-", 5);
        groupID = GroupHelper.updateGroup(requestSpec, responseSpec, updatedGroupName, groupID.toString());
        GroupHelper.verifyGroupDetails(requestSpec, responseSpec, groupID, "name", updatedGroupName);

        groupID = GroupHelper.createGroup(requestSpec, responseSpec);
        GroupHelper.deleteGroup(requestSpec, responseSpec, groupID.toString());
        GroupHelper.verifyGroupDeleted(requestSpec, responseSpec, groupID);
    }
}