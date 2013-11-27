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
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void checkGroupFunctions() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec);
        GroupHelper.verifyGroupCreatedOnServer(this.requestSpec, this.responseSpec, groupID);

        groupID = GroupHelper.activateGroup(this.requestSpec, this.responseSpec, groupID.toString());
        GroupHelper.verifyGroupActivatedOnServer(this.requestSpec, this.responseSpec, groupID, true);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyGroupMembers(this.requestSpec, this.responseSpec, groupID, clientID);

        groupID = GroupHelper.disAssociateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyEmptyGroupMembers(this.requestSpec, this.responseSpec, groupID);

        final String updatedGroupName = GroupHelper.randomNameGenerator("Group-", 5);
        groupID = GroupHelper.updateGroup(this.requestSpec, this.responseSpec, updatedGroupName, groupID.toString());
        GroupHelper.verifyGroupDetails(this.requestSpec, this.responseSpec, groupID, "name", updatedGroupName);

        // NOTE: removed as consistently provides false positive result on cloudbees server.
//        groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec);
//        GroupHelper.deleteGroup(this.requestSpec, this.responseSpec, groupID.toString());
//        GroupHelper.verifyGroupDeleted(this.requestSpec, this.responseSpec, groupID);
    }
}