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
package org.apache.fineract.infrastructure.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Handle;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

/**
 * Checks that every JAX-RS route is authorized: either by a rule in {@link ApiAuthorizationRules}, by a permission
 * check that the resource method itself calls, or by an entry in {@link #ALLOWED} that explains where the route is
 * authorized instead.
 *
 * <p>
 * A route that matches none of these is reachable by any authenticated user, whatever permissions the user holds,
 * because the security filter chains let unmatched {@code /api/**} requests through once the user is authenticated.
 */
class ApiAuthorizationCoverageTest {

    /**
     * Methods that check a permission of the current user when a resource method calls them.
     */
    private static final Pattern PERMISSION_CHECK = Pattern
            .compile("validateHas\\w*|logCommandSource|checkUserPermissionForReport|hasNotPermission\\w*|validateMakerChecker\\w*");

    /**
     * Sample values for path parameters whose value selects the entity type; every other parameter becomes "1".
     */
    private static final Map<String, String> SAMPLE_PATH_PARAMETERS = Map.of("entityType", "clients", "resourceType", "clients", "type",
            "share");

    private static final String PUBLIC = "public: no authentication is needed to log in or to request a password reset (permitAll in SecurityConfig)";
    private static final String TWO_FACTOR = "two-factor authentication: has to be reachable before the second factor is validated";
    private static final String OWN_DATA = "only the data of the authenticated user";
    private static final String IMPORT_TEMPLATE = "bulk import template download: BulkImportWorkbookPopulatorServiceImpl checks the read permissions of the data in the workbook";
    private static final String IMPORT_UPLOAD = "bulk import upload: the import handler submits every row through logCommandSource, which checks the command permission";
    private static final String LOAN_POINT_IN_TIME = "LoansPointInTimeApiDelegate checks READ_LOAN";
    private static final String SAVINGS_TRANSACTION_SEARCH = "SavingsAccountTransactionsSearchServiceImpl checks READ_SAVINGSACCOUNT";
    private static final String DATATABLE_READ = "DatatableReadServiceImpl checks or filters by the READ_<datatable> permission";
    private static final String MAKER_CHECKER = "maker-checker: PortfolioCommandSourceWritePlatformServiceImpl checks the checker permission of the command; the list is filtered by the checker permissions of the user";
    private static final String INTEROP_COMMAND = "InteropServiceImpl submits the transaction through logCommandSource";
    private static final String BATCH = "batch: every sub-request runs the resource method of its URL, which checks its own permission";
    private static final String NO_TENANT_DATA = "no tenant data: liveness probe and a calculator that works only on its query parameters";
    private static final String OPEN_ON_PURPOSE = "available to every user on purpose: status and lookup data the user interface needs regardless of permissions; search is limited to the office hierarchy of the user";

    /**
     * Routes authorized somewhere the test cannot see, with the reason. Keep the reasons specific.
     */
    private static final Map<String, String> ALLOWED = Map.ofEntries(Map.entry("POST /api/v1/authentication", PUBLIC),
            Map.entry("POST /api/v1/password/forgot", PUBLIC), Map.entry("GET /api/v1/twofactor", TWO_FACTOR),
            Map.entry("POST /api/v1/twofactor", TWO_FACTOR), Map.entry("POST /api/v1/twofactor/validate", TWO_FACTOR),
            Map.entry("GET /api/v1/userdetails", OWN_DATA), Map.entry("GET /api/v1/notifications", OWN_DATA),
            Map.entry("PUT /api/v1/notifications", OWN_DATA), Map.entry("GET /api/v1/centers/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/clients/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/fixeddepositaccounts/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/fixeddepositaccounts/transaction/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/glaccounts/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/groups/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/loans/{loanId}/guarantors/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/journalentries/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/loans/repayments/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/loans/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/offices/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/recurringdepositaccounts/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/recurringdepositaccounts/transactions/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/savingsaccounts/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/savingsaccounts/transactions/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("GET /api/v1/users/downloadtemplate", IMPORT_TEMPLATE),
            Map.entry("POST /api/v1/accounts/{type}/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/centers/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/clients/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/fixeddepositaccounts/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/fixeddepositaccounts/transaction/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/glaccounts/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/groups/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/loans/{loanId}/guarantors/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/journalentries/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/loans/repayments/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/loans/uploadtemplate", IMPORT_UPLOAD), Map.entry("POST /api/v1/offices/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/recurringdepositaccounts/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/recurringdepositaccounts/transactions/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/savingsaccounts/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/savingsaccounts/transactions/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("POST /api/v1/users/uploadtemplate", IMPORT_UPLOAD),
            Map.entry("GET /api/v1/loans/at-date/{loanId}", LOAN_POINT_IN_TIME),
            Map.entry("GET /api/v1/loans/at-date/external-id/{loanExternalId}", LOAN_POINT_IN_TIME),
            Map.entry("POST /api/v1/loans/at-date/search", LOAN_POINT_IN_TIME),
            Map.entry("POST /api/v1/loans/at-date/search/external-id", LOAN_POINT_IN_TIME),
            Map.entry("GET /api/v1/savingsaccounts/{savingsId}/transactions/search", SAVINGS_TRANSACTION_SEARCH),
            Map.entry("GET /api/v1/savingsaccounts/external-id/{savingsExternalId}/transactions/search", SAVINGS_TRANSACTION_SEARCH),
            Map.entry("POST /api/v1/savingsaccounts/{savingsId}/transactions/query", SAVINGS_TRANSACTION_SEARCH),
            Map.entry("POST /api/v1/savingsaccounts/external-id/{savingsExternalId}/transactions/query", SAVINGS_TRANSACTION_SEARCH),
            Map.entry("GET /api/v1/datatables", DATATABLE_READ), Map.entry("GET /api/v1/datatables/{datatable}", DATATABLE_READ),
            Map.entry("POST /api/v1/datatables/{datatable}/query", DATATABLE_READ),
            Map.entry("POST /api/v1/makercheckers/{auditId}", MAKER_CHECKER),
            Map.entry("DELETE /api/v1/makercheckers/{auditId}", MAKER_CHECKER), Map.entry("GET /api/v1/makercheckers", MAKER_CHECKER),
            Map.entry("POST /api/v1/interoperation/transactions/{accountId}/disburse", INTEROP_COMMAND),
            Map.entry("POST /api/v1/interoperation/transactions/{accountId}/loanrepayment", INTEROP_COMMAND),
            Map.entry("POST /api/v1/batches", BATCH), Map.entry("GET /api/v1/interoperation/health", NO_TENANT_DATA),
            Map.entry("GET /api/v1/fixeddepositaccounts/calculate-fd-interest", NO_TENANT_DATA),
            Map.entry("GET /api/v1/loans/is-catch-up-running", OPEN_ON_PURPOSE),
            Map.entry("GET /api/v1/loans/oldest-cob-closed", OPEN_ON_PURPOSE),
            Map.entry("GET /api/v1/working-capital-loans/is-catch-up-running", OPEN_ON_PURPOSE),
            Map.entry("GET /api/v1/working-capital-loans/oldest-cob-closed", OPEN_ON_PURPOSE),
            Map.entry("GET /api/v1/makercheckers/searchtemplate", OPEN_ON_PURPOSE),
            Map.entry("GET /api/v1/runreports/availableExports/{reportName}", OPEN_ON_PURPOSE),
            Map.entry("GET /api/v1/search", OPEN_ON_PURPOSE));

    private static List<Route> routes;

    @BeforeAll
    static void findRoutes() {
        routes = findAllRoutes();
    }

    @Test
    void everyRouteIsAuthorized() {
        final Set<String> unauthorized = new TreeSet<>();
        for (Route route : routes) {
            if (!isCoveredByRule(route) && !checksPermissionInCode(route) && !ALLOWED.containsKey(route.key())) {
                unauthorized.add(route.key() + "  <- " + route.description());
            }
        }
        assertThat(unauthorized).as("""
                These routes are reachable by any authenticated user. Add a rule to ApiAuthorizationRules, check the \
                permission in the resource method, or, if the route is authorized elsewhere, add it to ALLOWED with the reason""")
                .isEmpty();
    }

    @Test
    void allowedRoutesExistAndNeedTheirEntry() {
        final Map<String, Route> byKey = new HashMap<>();
        routes.forEach(route -> byKey.put(route.key(), route));
        final Set<String> stale = new TreeSet<>();
        for (String key : ALLOWED.keySet()) {
            final Route route = byKey.get(key);
            if (route == null || isCoveredByRule(route) || checksPermissionInCode(route)) {
                stale.add(key);
            }
        }
        assertThat(stale).as("ALLOWED entries for routes that no longer exist or are authorized by a rule or in the resource").isEmpty();
    }

    @Test
    void findsTheResources() {
        assertThat(routes).hasSizeGreaterThan(500);
    }

    private static boolean isCoveredByRule(Route route) {
        for (ApiAuthorizationRules.Rule rule : ApiAuthorizationRules.RULES) {
            if (rule.matcher().matches(route.httpMethod(), route.samplePath())) {
                return true;
            }
        }
        return false;
    }

    private static boolean checksPermissionInCode(Route route) {
        final ClassCalls calls = ClassCalls.of(route.resource());
        final Deque<String> pending = new ArrayDeque<>();
        final Set<String> visited = new HashSet<>();
        pending.add(route.methodKey());
        while (!pending.isEmpty()) {
            final String method = pending.pop();
            if (!visited.add(method)) {
                continue;
            }
            for (String called : calls.invokedNames(method)) {
                if (PERMISSION_CHECK.matcher(called).matches()) {
                    return true;
                }
            }
            pending.addAll(calls.ownMethodsCalled(method));
        }
        return false;
    }

    private static List<Route> findAllRoutes() {
        // every class annotated with @Path, whatever its @Conditional says: conditions need the application properties
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {

            @Override
            protected boolean isCandidateComponent(MetadataReader metadataReader) {
                return metadataReader.getAnnotationMetadata().hasAnnotation(Path.class.getName());
            }

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isConcrete();
            }
        };
        final List<Route> found = new ArrayList<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents("org.apache.fineract")) {
            final Class<?> resource = ClassUtils.resolveClassName(candidate.getBeanClassName(), null);
            if (isTestOnly(resource) || isTestSource(resource)) {
                continue;
            }
            final String classPath = resource.getAnnotation(Path.class).value();
            for (Method method : resource.getDeclaredMethods()) {
                final String httpMethod = httpMethod(method);
                if (httpMethod == null || !Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                    continue;
                }
                final Path methodPath = method.getAnnotation(Path.class);
                final String path = "/api/" + trim(classPath) + (methodPath == null ? "" : "/" + trim(methodPath.value()));
                found.add(new Route(httpMethod, path, resource, method));
            }
        }
        return found;
    }

    private static boolean isTestSource(Class<?> resource) {
        return resource.getProtectionDomain().getCodeSource().getLocation().getPath().contains("/classes/java/test/");
    }

    private static boolean isTestOnly(Class<?> resource) {
        final Profile profile = resource.getAnnotation(Profile.class);
        return profile != null && Arrays.asList(profile.value()).contains(FineractProfiles.TEST);
    }

    private static String httpMethod(Method method) {
        for (Class<? extends Annotation> annotation : List.of(GET.class, POST.class, PUT.class, DELETE.class)) {
            if (method.isAnnotationPresent(annotation)) {
                return annotation.getSimpleName();
            }
        }
        return null;
    }

    private static String trim(String path) {
        String trimmed = path;
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private record Route(String httpMethod, String path, Class<?> resource, Method method) {

        private static final Pattern PATH_PARAMETER = Pattern.compile("\\{\\s*(\\w+)\\s*(?::[^}]*)?}");

        String key() {
            return httpMethod + " " + path.replaceAll("/+$", "");
        }

        String samplePath() {
            final Matcher matcher = PATH_PARAMETER.matcher(path);
            final StringBuilder sample = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sample, SAMPLE_PATH_PARAMETERS.getOrDefault(matcher.group(1), "1"));
            }
            matcher.appendTail(sample);
            return sample.toString();
        }

        String methodKey() {
            return method.getName() + Type.getMethodDescriptor(method);
        }

        String description() {
            return resource.getSimpleName() + "#" + method.getName();
        }
    }

    /**
     * The methods each method of a class invokes, read from its bytecode.
     */
    private static final class ClassCalls extends ClassVisitor {

        private static final Map<Class<?>, ClassCalls> CACHE = new HashMap<>();

        private final String internalName;
        private final Map<String, Set<String>> invokedNames = new HashMap<>();
        private final Map<String, Set<String>> ownMethodsCalled = new HashMap<>();

        private ClassCalls(String internalName) {
            super(Opcodes.ASM9);
            this.internalName = internalName;
        }

        static synchronized ClassCalls of(Class<?> type) {
            return CACHE.computeIfAbsent(type, ClassCalls::read);
        }

        private static ClassCalls read(Class<?> type) {
            final String internalName = Type.getInternalName(type);
            final ClassCalls calls = new ClassCalls(internalName);
            try (InputStream bytecode = type.getClassLoader().getResourceAsStream(internalName + ".class")) {
                new ClassReader(bytecode).accept(calls, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return calls;
        }

        Set<String> invokedNames(String method) {
            return invokedNames.getOrDefault(method, Set.of());
        }

        Set<String> ownMethodsCalled(String method) {
            return ownMethodsCalled.getOrDefault(method, Set.of());
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final String key = name + descriptor;
            final Set<String> names = invokedNames.computeIfAbsent(key, k -> new HashSet<>());
            final Set<String> own = ownMethodsCalled.computeIfAbsent(key, k -> new HashSet<>());
            return new MethodVisitor(Opcodes.ASM9) {

                @Override
                public void visitMethodInsn(int opcode, String owner, String calledName, String calledDescriptor, boolean isInterface) {
                    names.add(calledName);
                    if (owner.equals(internalName)) {
                        own.add(calledName + calledDescriptor);
                    }
                }

                @Override
                public void visitInvokeDynamicInsn(String dynamicName, String dynamicDescriptor, Handle bootstrap, Object... arguments) {
                    // lambdas and method references compile to methods of this class that the bootstrap arguments refer
                    // to
                    for (Object argument : arguments) {
                        if (argument instanceof Handle handle && handle.getOwner().equals(internalName)) {
                            own.add(handle.getName() + handle.getDesc());
                        }
                    }
                }
            };
        }
    }
}
