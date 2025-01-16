Background and Motivation
-------------------------

Fineract accumulated some technical debt over the years. One area that is implicated is type-safety of internal and external facing APIs, the most prominent of which is Fineract's REST API. In general the package layout of the project reflects a more or less classic layered architecture (REST API, data transfer/value objects, business logic services, storage/repositories). The project predates some of the more modern frameworks and best practices that are available today and on occasions the data structures that are exchanged offer some challenges (e.g. generic types). Fineract's code base reflects that, especially where JSON de-/serialization is involved. Nowadays, this task would be simply delegated to the Jackson framework, but when Fineract (Mifos) started the decision was made to use Google's GSON library and create handcrafted helper classes to deal with JSON parsing. While this provided a lot of flexibility this approach had some downsides:

- the lowest common denominator is the string type (aka JSON blob); this is where we lose the type information
- the strings are transformed into JSONObjects; a little bit better than raw strings, but barely more than a hash map
- a ton of "magic" strings are needed to get/set values
- this approach makes refactoring unnecessarily more difficult
- to be able to serve an OpenAPI descriptor (as JSON and YAML) we had to re-introduce the type information at the REST API level with dummy classes that contain only the specified attributes; those classes are only used with the Swagger annotations and no were else
- some developers skipped the layered architecture and found it too tedious to maintain DTOs and JSON helper classes, and as a result just passed JSONObjects right to the business logic layer
- now the business logic is unnecessarily aware of how Fineract communicates to the outside world and makes replacing/enhancing the communication protocol (e.g. with GRPC) pretty much impossible

The list doesn't end here, but in the end things boil down to two main points:

- poor developer experience: boilerplate code and missing type safety cost more time
- bugs: the more code the more likely errors get introduced, especially when type safety is missing and we have to rely on runtime errors (vs. compile time).

There has been already some preparatory work done concerning type safety, but until now we avoided dealing with the real source of this issue. Fineract's architectures devises read from write requests ("CQRS", https://martinfowler.com/bliki/CQRS.html) for improved scalability.

The read requests are not that problematic, but all write requests pass through a component/service that is called "SynchronousCommandProcessingService. As the name suggests the execution of business logic is synchronous (mostly) due to this part of the architecture. This is not necessarily a problem (not immediately at least), but it's nevertheless a central bottleneck in the system. Even more important:  this service is responsible to route incoming commands to their respective handler classes which in turn execute functions on one or more business logic services. The payload of these commands are obviously not always the same... which is the main reason why we decided to use the lowest common denominator to be able to handle these various types and rendered all payloads as strings. This compromise bubbles now up in the REST API and the business logic layers (and actually everything in between).

Over the years we've also added additional features (e.g. idempotency guarantees for incoming write requests) that make it now very hard to reason about the execution flow. Testing the performance impact of such additions to the critical execution path even can't be properly measured. Note: the current implementation of idempotency relies on database lookups (quite often, for each incoming request) and none of those queries are cached. If we wanted to store already processed requests (IDs) in a faster system (let's Redis) then this can't be done without major refactoring.

In conclusion, if we really want to fix those issues that are not only cosmetic and affect the performance and the developer experience equally then we urgently need to fix the way how we process write requests aka commands.

Target Personas
---------------

- developers
- integrators
- end users
- BaaS

Goals
-----

- new command processing will run independently next to the legacy mechanics
- self contained
- fully tested
- ensure that the REST API is 100% backward compatible
- try to contain the migration and make it as easy as possible for the community to integrate those changes
- introduce types where needed and migrate the (old) JAX-RS REST resource classes to Spring Web MVC (better performance and better testability)
- introduce DTOs if not already available and make sure if they exist that they are not outdated
- assemble one DTO as command payload from all incoming REST API parameters (headers, query/path paramters, request bodies)
- annotate attributes in the DTOs with Jakarta Validation annotations to enforce constraints on their values
- wired REST API to the new command processing, one service at a time/pull request
- take a non-critical service (like document management) and migrate it to the new command processing mechanics from top (REST API) to bottom (business logic service)
- refactor command handlers to new internal API
- make sure that the business service logic classes/functions take only one DTO request input parameter (aka don't let a function have 12 input parameters of type string...)
- when all integration tests run  successfully then remove all legacy boilerplate code that is not used anymore
- make an ordered list of modules/features (easiest, lowest hanging fruit first)
- maintain at least the same performance as the current implementation
- optional: improve performance if it can be done in a reasonable time frame
- optional: improve resilience if it can be done in a reasonable time frame

Non-Goals
---------

- current command processing will stay untouched, will run independently of new infrastructure
- don't try cleaning up the storage layer; that's a separate effort for later (type safe queries, query peformance, clean entity classes)
- doesn't need to be optimized for speed immediately
- no changes in the integration tests

Proposed API Changes
--------------------

1. Command Wrapper

TBD

Class contains some generic atttributes like:

- username
- tenant ID
- timestamp

The actual payload (aka command input parameters) are defined as a generic parameter. It is expected that the modules implement classes that introduce the payload types and inherit from the abstract command class.

2. Command Processing Service

TBD

- synchronously (required): this is pretty much as we do right now (use virtual threads optionally)
- asynchronously (optional): with executor service and completable futures (use virtual threads optionally)
- non-blocking (optional): high perfomance LMAX Disruptor non-blocking implementation

These different perfromance level implementations need to be absolute drop-in replacements (for each other). It is expected that more performant implementations need more testing due to increased complexity and possible unforseen side effects. In case any problems show up we can always roll back to the required default implementation (synchronous).

NOTE: we should consider providing a command processing implementation based on Apache Camel once this concept is approved and we migrated already a couple of services. They are specialized for exactly this kind of use cases and have more dedicated people working on it's implementation. Could give more flexibility without us needing to maintain code.

3. Middlewares

TBD

4. Command Handlers

TBD

5. References to users (aka AppUser)

Keep things lightweight and only reference users by their user names.f

Risks
-----

TBD

- feature creep

ETA
---

A first prototype of the a new command processing component is ready for evaluation. There is also an initial smoke test (JMH) available.

You can try it out with the following instructions (it's still in a private repository, but will be published soon as an official PR):

```
git clone git@github.com:vidakovic/fineract.git
cd fineract
git checkout feature/FINERACT-2169
./gradlew :fineract-command:build
./gradlew :fineract-command:jmh
```

Diagrams
--------

TBD

Related Jira Tickets
--------------------

- https://issues.apache.org/jira/browse/FINERACT-2169
- https://issues.apache.org/jira/browse/FINERACT-2021
- https://issues.apache.org/jira/browse/FINERACT-1744
- https://issues.apache.org/jira/browse/FINERACT-1909
