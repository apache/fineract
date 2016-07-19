package org.apache.fineract.integrationtests.common;


import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class NotificationHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String NOTIFICATION_API_URL = "/fineract-provider/api/v1/notifications?" + Utils.TENANT_IDENTIFIER;

    public NotificationHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static Object getNotifications(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                          final String jsonReturn) {
        final String GET_NOTIFICATIONS_URL = NOTIFICATION_API_URL;
        System.out.println("-----------------------------GET NOTIFICATIONS-----------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_NOTIFICATIONS_URL, "");
    }
}
