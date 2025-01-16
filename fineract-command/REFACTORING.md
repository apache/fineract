Refactoring Instructions
========================

General
--------

1. POJOs

Please make sure that all POJOs (for request and response types) have a similar structure. They should:

- use Lombok to reduce boilerplate code
- make sure that all annotations are always in the same order (see example code)
- avoid `record`s for now (we might or might not migrate later from Lombok to `record`)
- each class should implement `java.io.Serializable`
- each class should contain a serialization version set to `1L`

Example:

```
package org.apache.fineract.command.sample.data;

...

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class DummyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    private String content;
}
```

2. Transformation between types

If you need to transform between two POJOs then use MapStruct wherever possible. Place the MapStruct interfaces under a sibling package `mapping` next to the `data` package.

3. Package structure (`org.apache.fineract.xxx.*`)

Please make sure to always follow this package structure pattern:

- `api`: contains all REST JAX-RS resource classes (later Spring Web MVC controllers)
- `command`: primarily used for command specific child class implementations of `org.apache.fineract.command.core.Command` (see section "Command Pipeline preserving Type Information")
- `data`: contains all DTOs (request and response types)
- `domain`: contains all entity/table mapping classes
- `handler`: contains all command handlers
- `service`: contains business logic services
- `mapping`: contains MapStruct interfaces
- `middleware`: contains middleware related to command processing; I am not expecting that we will need any of this during the refactoring process
- `security`: might contain later so called Spring Security "authorization managers" for more complex use cases
- `serialization`: technically we should not need this package anymore after we are done with the refactorings; in theory there could be very complex data structures that are not easily digestable by Jackson; for those case we could still use this package to add de-/serialization helpers (Jackson provides a proper API for this)
- `starter`: Spring Java configuration that allows us to make Fineract customizable (make parts of the system replaceable)
- `validation`: contains custom Jakarta Validation components/annotations

In general avoid too many nesting levels. Ideally we would have only one additional underneath the base package (`org.apache.fineract.*`) where in turn only the above package patterns are used.

REST API
--------

1. Read Requests

The **read requests** (HTTP **GET**) usually only require to refactor (if at all) the business logic services in case they don't return proper Java POJOs. Please name all return types for these read requests consistently. We propose to add always a suffix `Response`; this makes it immediately clear that we are dealing with a data transfer object (vs database mapping entities) and that it's something that we return to the clients (vs incoming requests). Historically we've used `Data` as a suffix, but this doesn't make it clear if it's used as input or output. Let's see an example.

This is how the legacy code looks like:

```
public class BusinessDateApiResource {
    ...
    @GET
    public String getBusinessDates(@Context final UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasReadPermission("BUSINESS_DATE");
        final List<BusinessDateData> foundBusinessDates = this.readPlatformService.findAll();
        ApiRequestJsonSerializationSettings settings = parameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, foundBusinessDates);
    }
    ...
}
```

This is how the refactored code should look like; there is absolutely no need to manually serialize:

```
public class BusinessDateApiResource {
    ...
    @GET
    public List<BusinessDateResponse> getBusinessDates() {
        securityContext.authenticatedUser().validateHasReadPermission(BUSINESS_DATE);
        return this.readPlatformService.findAll();
    }
    ...
}
```

[!IMPORTANT] We should make sure that all response DTOs should reside in a package `org.apache.fineract.xxx.data`; for above example `org.apache.fineract.infrastructure.businessdate.data.BusinessDateResponse`!

Bonus: very often you'll see that we are using our homegrown `PlatformSecurityContext` to validate read permissions. This is the **WRONG** way to do it. Fortunately, fixing this is a low hanging fruit. We just need to introduce an "Ant matcher" configuration in the web configuration. For above example we should add an entry in `org.apache.fineract.infrastructure.core.config.SecurityConfig`. The change should look something like this:

```
@Configuration
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@EnableMethodSecurity
public class SecurityConfig {
    ...
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(antMatcher("/api/**")).authorizeHttpRequests((auth) -> {
                auth.requestMatchers(antMatcher(HttpMethod.OPTIONS, "/api/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/echo")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/authentication")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/self/authentication")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/self/registration")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/self/registration/user")).permitAll()
                        // NOTE: enforce read permission for get business date by type
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/businessdate/*")).hasPermission("ALL_FUNCTIONS", "ALL_FUNCTIONS_READ", "READ_BUSINESS_DATE")
                        // ... insert more like above...
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/api/*/instance-mode")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/twofactor/validate")).fullyAuthenticated()
                        .requestMatchers(antMatcher("/api/*/twofactor")).fullyAuthenticated()
                        .requestMatchers(antMatcher("/api/**"))
                        .access(allOf(fullyAuthenticated(), hasAuthority("TWOFACTOR_AUTHENTICATED")));
            }).httpBasic((httpBasic) -> httpBasic.authenticationEntryPoint(basicAuthenticationEntryPoint()))
            .cors(Customizer.withDefaults()).csrf((csrf) -> csrf.disable())
            .sessionManagement((smc) -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(tenantAwareBasicAuthenticationFilter(), SecurityContextHolderFilter.class)
            .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class)
            .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class)
            .addFilterAfter(fineractInstanceModeApiFilter(), CorrelationHeaderFilter.class);
        if (!Objects.isNull(loanCOBFilterHelper)) {
            http.addFilterAfter(loanCOBApiFilter(), FineractInstanceModeApiFilter.class)
                    .addFilterAfter(idempotencyStoreFilter(), LoanCOBApiFilter.class);
        } else {
            http.addFilterAfter(idempotencyStoreFilter(), FineractInstanceModeApiFilter.class);
        }

        if (fineractProperties.getSecurity().getTwoFactor().isEnabled()) {
            http.addFilterAfter(twoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        } else {
            http.addFilterAfter(insecureTwoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        }

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers(antMatcher("/api/**")).requiresSecure());
        }
        return http.build();
    }
    ...
}
```

The nice side effect is that we'll have all security rules that we are enforcing in one place. This will enable more flexible customizations around security (will be handled in the modular security proposal).

The final refactored function should look like this:

```
public class BusinessDateApiResource {
    ...
    @GET
    public List<BusinessDateResponse> getBusinessDates() {
        return this.readPlatformService.findAll();
    }
    ...
}
```

That way we lose immediately dependencies on the security context, the GSON configuration and the handcrafted serialization helper for GSON.

[!IMPORTANT] We have to make sure that the Jackson parser configuration matches the one for GSON (especially dates etc.)!

2. Write Requests

The **write requests** (HTTP **PUT** and **POST**) are the ones that affect the command processing infrastructure. The JSON body in pretty much all of the legacy cases is probably represented as a simple string variable that gets passed to a command wrapper class. All these variables need to be replaced by proper POJO classes that represent the request body. You can get hints how these classes should look like by checking the OpenAPI dummy classes we created to re-introduce the type information for the OpenAPI descriptor (see Swagger annotations).

Usually you'll have 2 classes to take care of: **requests** (incoming input parameters) and **responses** (outgoing results). These classes are technically **DTO**s (data transfer objects) or **VO**s (value objects) and to make them more recognizable as such I would start standardizing the naming. E.g. if I have a use case aka REST endpoint that creates client data we would name that DTO class `CreateClientRequest`; and if any data needs to be sent back to the client then that class should be called `CreateClientResponse`. This is a very simple mechanic that helps identifying immediately what is what and doesn't require the developers to come up with any naming stunts. Do not try to re-use these DTOs on multiple endpoints, it's not worth it. Create a separate DTO for each endpoint. Nice side effect: all this becomes then nicer to read (both in Java code and in the OpenAPI descriptor and the resulting Asciidoc documentation) and this will make it less likely that names are clashing in the OpenAPI descriptor (and during code generation for the Fineract Java Client).

Only new thing that needs to be injected in those REST API resource classes (JAX-RS) or controllers (Spring Web MVC) is the `CommandPipeline` component that allows you to send requests to the **command pipeline** which in turn will be processed by **handlers** that eventually call one or more **business logic services**. Results are sent back to the pipeline and are received in the REST API aka returned by `CommandPipeline` as so called **supplier objects**. (Java) Supplier is a functional interface, i.e. there is only one function to be implemented (`get()`). This small abstraction helps us to standardize how the results are delivered (**synchronous**, **asynchronous**, **non-blocking**) and maintain the same internal API.

3. Jakarta Validation

TBD

Example POJO:

```
package org.apache.fineract.command.sample.data;

...

import jakarta.validation.constraints.NotEmpty;

...

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class DummyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    @NotEmpty
    private String content;
}
```

Create message resource bundles for the validation errors.

TBD

4. **Skip**: Spring Web MVC

We will not do this right now, but later we might move away from JAX-RS and introduce **Spring Web MVC** which is mostly just changing the annotations (e.g. `@POST` vs `@PostRequest`). This process should be pretty straight forward and could be even (semi-) automated by using **OpenRewrite** recipes. Once we have proper POJOs for requests and response we can also use **Jakarta Validation** annotations to validate the request content. You'll see that currently we have an explicit service injected everywhere that does those checks manually (that includes some JSON parsing); very tedious and work intensive and hard to refactor. The first setup with Jakarta Validation take a little longer, but once that is working for one use case it should be pretty much rinse and repeat for the rest. Occasionally for very exotic validations there might be a need to implement **custom validations**. Another advantage of this approach: we can finally add the proper **internationalized error messages** on the server side, not only the translation keys. This removes a ton of code on the client side and ensures that the clients always have the correct messages. Read the Jakarta Validation documentation on how that works and use **Hibernate Validation** to implement this (pretty much the defacto standard library).

5. Security Enforcement

very often you'll see that we are using our homegrown  to validate read permissions. This is the **WRONG** way to do it. Fortunately, fixing this is a low hanging fruit. We just need to introduce an "Ant matcher" configuration in the web configuration. For above example we should add an entry in `org.apache.fineract.infrastructure.core.config.SecurityConfig`. The change should look something like this:

Another relatively low hanging fruit that can be tackled here is the way we enforce security aka authorization with `org.apache.fineract.infrastructure.security.service.PlatformSecurityContext`. At the moment we use an explicitly injected security service that we call pretty much everywhere in the REST API layer and then decide in numerous if-then-else constructs if we should execute the commands (or read requests for that matter). This is the **WRONG** way to do it. What we should do is to use Spring Security's APIs and define the authorization requirements in the web configuration where you have then all the security related definitions in one place (instead of chasing them down in all the controller classes); in some more complex cases we might need to implement so called `AuthorizationManager` components to enforce a certain authorization; in any case this will all be visible in one place, the web configuration which makes adapting to specific custom situations also a **LOT** easier.

Because all JSON parsing was done up until now manually some developers got tired and decided to pass the JSON data structures (`CommandWrapper`, `JsonCommand`) directly to the business logic services. This is **WRONG**. The only parameter those business logic functions should receive is exactly ONE Java Pojo that contains all necessary input parameter for that function to execute. **DO NOT** define functions with primitive/base types. It is impossible to understand the order of those parameters once you have e.g. 17 (example to make a point) string variables. Ideally, you'd just pass the request POJOs to those functions. If you need to transform something then use **MapStruct** instead of manual code. The refactoring should not be too bad. Once proper Java POJOs are defined for the functions we just need to replace the manual JSON parsing boilerplate in the service classes and just call the getters/setters of the request POJOs (instead the manual JSON parsing). The results should also be sent back in proper POJOs (responses). I would go as far that even if you have a single string value (example) that you **should** wrap it in a response class (same for the incoming input parameters aka requests). This will make the service interfaces more stable if we add/remove parameters over time; in other words: this will avoid all those refactoring fests we have upstream when we have functions with 12 base type (example) parameters and need to add/remove something. Obviously you need to check where else in Fineract's code base those service functions are called and adapt accordingly, but usually those functions are called in just one place (handlers).

Command Pipeline preserving Type Information
--------------------------------------------

`CommandPipeline` needs to be injected where needed (usually only REST API controllers). By default everything is configured for `sync`(-hronous) processing. Other modes (`async`, `disruptor`) can be easily configured via application.properties, but need more testing and are out of scope for now. As you can see the command object just contains some of metadata (`createdAt`, `username` etc.) and the payload aka request object. Obviously we have different use cases so that **payload** attribute is defined as a generic **type**. Please check the unit test code how to create a command object with payload/request properly.

```
public class DummyApiResource {
    ...
    @GET
    DummyResponse dummy(@HeaderParam("x-fineract-request-id") UUID requestId, @HeaderParam("x-fineract-tenant-id") String tenantId, DummyRequest request) {
        var command = new DummyCommand();
        command.setId(requestId);
        command.setPayload(request);

        tenantService.set(tenantId);

        Supplier<DummyResponse> result = pipeline.send(command);

        return result.get();
    }
    ...
}
```

Make sure to create command specific child class of the generic (and abstract) `org.apache.fineract.command.core.Command` class. Example:

```
...

@Data
@EqualsAndHashCode(callSuper = true)
public class DummyCommand extends Command<DummyRequest> {}
```

If everything is done correctly then no type information will be lost and everything can be parsed without further help by the Jackson parser. Eventually all handcrafted boilerplate JSON parsing code can be dumped. In rare cases we might need to add de-/serialization helper classes (a concept provided by Jackson) to help the parser identify the types properly.

When we use **Spring Web MVC unit testing** (actually integration testing) gets very easy. Just a couple of annotations and you can execute them pretty much like simple unit tests in your IDE (because Spring Web MVC is a first class citizen obviously in the Spring Framework). NONE of that handcrafted client code like in our current integration tests is required; the tests should be easier to refactor and easier to understand. Writing those tests is **optional** for now, because we have already over **1000 integration tests**. After all those refactorings the REST API should be 100% compatible to upstream even if it uses a different technology stack. If everything passes those ""old"" integration tests then we can be pretty confident that we didn't mess something up. Migrating the integration tests to simpler Spring Web tests can be done later."

Command Handlers
----------------

In the current CQRS implementation we have already a concept that is called **handlers**. Those handlers are responsible to receive the command objects with their (request) payloads and transform the requests as needed an pass them to one or more business logic services. The refactoring of those handlers should not be too complicated, they just need to implement the Java interface `CommandHandler`. Look at my test samples to see how the implementation details look like.

The old handlers look somewhat like this:

```
@Service
@CommandType(entity = "PAYMENTTYPE", action = "CREATE")
public class CreatePaymentTypeCommandHandler implements NewCommandSourceHandler {

    private final PaymentTypeWriteService paymentTypeWriteService;

    @Autowired
    public CreatePaymentTypeCommandHandler(final PaymentTypeWriteService paymentTypeWriteService) {
        this.paymentTypeWriteService = paymentTypeWriteService;
    }

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.paymentTypeWriteService.createPaymentType(command);
    }
}
```

... and this is how the refactored handler could look like:

```
@Slf4j
@RequiredArgsConstructor
@Component
public class CreatePaymentTypeCommandHandler implements CommandHandler<CreatePaymentTypeRequest, CreatePaymentTypeResponse> {

    private final PaymentTypeWriteService paymentTypeWriteService;

    @Override
    public CreatePaymentTypeResponse handle(Command<CreatePaymentTypeRequest> command) {
        // TODO: refactor business logic service to accept properly typed request objects as input
        return paymentTypeWriteService.createPaymentType(command.getPayload());
    }
}
```


Execution Mode
--------------

1. Sync Execution

This is the default execution mode. Performance is to be expected on par with the current legacy implementation all tests need to work with this mode.

2. **Skip**: Async Execution

Already included in the current implementation. Just needs a proper **configuration** in **application.properties** (see unit tests). One thing that might need some additional coding: the use of **thread local variables in multi threaded environments** needs some special care to properly work (we use this to identify the current tenant). Also: we should upgrade to JDK 21 and make use of virtual threads (very easy in Spring Boot, simple configuration property). This allows for massive parallel execution that is not bound by physical CPU cores without (take this with a pinch of salt) performance penalties (read: use millions of threads).

3. **Skip**: Non-blocking Execution

Already included in the current implementation and configurable. I've tried a couple of combinations with LMAX disruptor, but this needs more testing to figure out optimal an configuration (see also https://lmax-exchange.github.io/disruptor/user-guide/). Would be worth to create more realistic JMH benchmarks. I have added a simple one to get a first idea how the mechanics are working.

Maker-Checker
-------------

This will be part of a separate proposal. The only related feature I've added here was command persistence so that you can save commands for deferred execution. Other than that I want to keep this concept (command processing) clean and avoid mixing to many concepts/concerns in one place. This ensures better maintainability. Maker-checker is actually a security related concept and should probably be handled with the proper Spring Security APIs (`AuthorizationManager` interface comes to mind). But again, different proposal and can be ignored here for now.

When we encounter the first need to take care of Maker-Checker then let's figure out a solution that has minimal impact and do a proper cleanup when the Maker-Checker proposal is available.

[!NOTE] The best guess is that Maker-Checker will probably implemented
