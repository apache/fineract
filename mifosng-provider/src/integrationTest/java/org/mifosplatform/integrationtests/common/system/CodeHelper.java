package org.mifosplatform.integrationtests.common.system;

import static com.jayway.restassured.RestAssured.given;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class CodeHelper {

	public static final String CODE_ID_ATTRIBUTE_NAME = "id";
	public static final String RESPONSE_ID_ATTRIBUTE_NAME = "resourceId";
	public static final String SUBRESPONSE_ID_ATTRIBUTE_NAME = "subResourceId";
	public static final String CODE_NAME_ATTRIBUTE_NAME = "name";
	public static final String CODE_SYSTEM_DEFINED_ATTRIBUTE_NAME = "systemDefined";
	public static final String CODE_URL = "/mifosng-provider/api/v1/codes";

	public static final String CODE_VALUE_ID_ATTRIBUTE_NAME = "id";
	public static final String CODE_VALUE_NAME_ATTRIBUTE_NAME = "name";
	public static final String CODE_VALUE_POSITION_ATTRIBUTE_NAME = "position";
	public static final String CODE_VALUE_URL = "/mifosng-provider/api/v1/codes/[codeId]/codevalues";

	public static Object createCode(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final String codeName,
			final String jsonAttributeToGetback) {

		return Utils.performServerPost(requestSpec, responseSpec, CODE_URL
				+ "?" + Utils.TENANT_IDENTIFIER, getTestCodeAsJSON(codeName),
				jsonAttributeToGetback);
	}

	public static Object updateCode(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final String codeName, final String jsonAttributeToGetback) {

		return Utils.performServerPut(requestSpec, responseSpec, CODE_URL + "/"
				+ codeId + "?" + Utils.TENANT_IDENTIFIER,
				getTestCodeAsJSON(codeName), jsonAttributeToGetback);
	}

	public static Object getCodeById(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final String jsonAttributeToGetback) {

		return Utils.performServerGet(requestSpec, responseSpec, CODE_URL + "/"
				+ codeId + "?" + Utils.TENANT_IDENTIFIER,
				jsonAttributeToGetback);

	}

	public static Object getAllCodes(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec) {

		return Utils.performServerGet(requestSpec, responseSpec, CODE_URL + "?"
				+ Utils.TENANT_IDENTIFIER, "");

	}

	public static Object getSystemDefinedCodes(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec) {

		final String getResponse = given().spec(requestSpec).expect()
				.spec(responseSpec).when()
				.get(CodeHelper.CODE_URL + "?" + Utils.TENANT_IDENTIFIER)
				.asString();

		final JsonPath getResponseJsonPath = new JsonPath(getResponse);

		// get any systemDefined code
		return getResponseJsonPath.get("find { e -> e.systemDefined == true }");

	}

	public static String getTestCodeAsJSON(final String codeName) {
		final HashMap<String, String> map = new HashMap<String, String>();
		map.put(CODE_NAME_ATTRIBUTE_NAME, codeName);
		return new Gson().toJson(map);
	}

	public static String getTestCodeValueAsJSON(final String codeValueName,
			final Integer position) {
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CODE_VALUE_NAME_ATTRIBUTE_NAME, codeValueName);
		map.put(CODE_VALUE_POSITION_ATTRIBUTE_NAME, position);
		return new Gson().toJson(map);
	}

	public static Object deleteCodeById(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final String jsonAttributeToGetback) {

		return Utils.performServerDelete(requestSpec, responseSpec, CODE_URL
				+ "/" + codeId + "?" + Utils.TENANT_IDENTIFIER,
				jsonAttributeToGetback);

	}

	public static Object createCodeValue(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final String codeValueName, final Integer position,
			final String jsonAttributeToGetback) {

		return Utils.performServerPost(requestSpec, responseSpec,
				CODE_VALUE_URL.replace("[codeId]", codeId.toString()) + "?"
						+ Utils.TENANT_IDENTIFIER,
				getTestCodeValueAsJSON(codeValueName, position),
				jsonAttributeToGetback);
	}

	public static Object getCodeValuesForCode(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final String jsonAttributeToGetback) {

		return Utils.performServerGet(requestSpec, responseSpec,
				CODE_VALUE_URL.replace("[codeId]", codeId.toString()) + "?"
						+ Utils.TENANT_IDENTIFIER, jsonAttributeToGetback);
	}

	public static Object getCodeValueById(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final Integer codeValueId, final String jsonAttributeToGetback) {

	
		return Utils.performServerGet(requestSpec, responseSpec,
				CODE_VALUE_URL.replace("[codeId]", codeId.toString()) + "/"
						+ codeValueId.toString() + "?"
						+ Utils.TENANT_IDENTIFIER, jsonAttributeToGetback);
	}

	public static Object deleteCodeValueById(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final Integer codeValueId, final String jsonAttributeToGetback) {

		return Utils.performServerDelete(requestSpec, responseSpec,
				CODE_VALUE_URL.replace("[codeId]", codeId.toString()) + "/"
						+ codeValueId.toString() + "?"
						+ Utils.TENANT_IDENTIFIER, jsonAttributeToGetback);
	}

	public static Object updateCodeValue(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final Integer codeId,
			final Integer codeValueId, final String codeValueName,
			final Integer position, final String jsonAttributeToGetback) {

		return Utils.performServerPut(requestSpec, responseSpec,
				CODE_VALUE_URL.replace("[codeId]", codeId.toString()) + "/"
						+ codeValueId + "?" + Utils.TENANT_IDENTIFIER,
				getTestCodeValueAsJSON(codeValueName, position),
				jsonAttributeToGetback);
	}

}