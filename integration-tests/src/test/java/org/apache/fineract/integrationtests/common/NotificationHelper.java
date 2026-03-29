package org.apache.fineract.integrationtests.common;

import static org.awaitility.Awaitility.await;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetNotificationsResponse;
import org.apache.fineract.client.util.Calls;

@Slf4j
public final class NotificationHelper {

    private NotificationHelper() {}

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static GetNotificationsResponse getNotifications(final RequestSpecification requestSpec,
                                                            final ResponseSpecification responseSpec) {
        log.info("-----------------------------GET NOTIFICATIONS-----------------------------------");
        return Calls.ok(FineractClientHelper.getFineractClient().notifications.getAllNotifications(null, null, null, null, null));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static boolean areNotificationsAvailable(final RequestSpecification requestSpec,
                                                    final ResponseSpecification responseSpec) {
        return getNotifications(requestSpec, responseSpec).getPageItems().size() > 0;
    }

    // Waiting for notifications to be available is needed due to the asynchronous event processing
    public static void waitUntilNotificationsAreAvailable(final RequestSpecification requestSpec,
                                                          final ResponseSpecification responseSpec) {
        await().atMost(Duration.ofSeconds(30)) //
                .pollInterval(Duration.ofSeconds(5)) //
                .pollDelay(Duration.ofSeconds(5)) //
                .until(() -> NotificationHelper.areNotificationsAvailable(requestSpec, responseSpec));
    }
}