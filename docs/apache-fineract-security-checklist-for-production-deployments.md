# Apache Fineract Security Checklist for Production Deployments

## Introduction

Apache Fineract is a powerful core banking platform, but a secure production deployment is not created by running the default configuration.

The default configuration is useful for local development, demos, and test environments. Production is different:

- You expose real customer and financial data.
- You integrate with real identity providers, databases, object stores, SMS gateways, email servers, webhooks, Kafka, JMS, and monitoring tools.
- You must control who can authenticate, which tenant they can access, which permissions they have, and which endpoints are reachable from the public internet.

This article is a practical checklist based on the current Fineract branch. It focuses on deployment and configuration decisions that are easy to miss.

Important:

This is not an official security audit, legal advice, or a complete compliance checklist. These are useful things to review, but I do not take responsibility for missed steps, misconfiguration, insecure infrastructure, or production incidents. You should validate every item against your own architecture, threat model, and regulatory requirements.

## Source of Truth

The details below are based on the current branch implementation and configuration, especially:

| Area | Source in the codebase |
| --- | --- |
| Basic authentication security chain | `fineract-provider/src/main/java/org/apache/fineract/infrastructure/core/config/SecurityConfig.java` |
| OAuth2 authorization server mode | `fineract-provider/src/main/java/org/apache/fineract/infrastructure/security/config/AuthorizationServerConfig.java` |
| OIDC federation mode | `fineract-provider/src/main/java/org/apache/fineract/infrastructure/security/config/OidcFederationSecurityConfig.java` |
| Runtime defaults | `fineract-provider/src/main/resources/application.properties` |
| Tenant resolution | `fineract-security/src/main/java/org/apache/fineract/infrastructure/security/filter/TenantAwareBasicAuthenticationFilter.java` |
| Tenant DB password encryption | `fineract-core/src/main/java/org/apache/fineract/infrastructure/core/service/database/DatabasePasswordEncryptor.java` |
| Command idempotency | `fineract-core/src/main/java/org/apache/fineract/commands/service/SynchronousCommandProcessingService.java` |
| File upload policy | `fineract-document/src/main/java/org/apache/fineract/infrastructure/contentstore/policy` |

If you deploy another Fineract version, verify the properties and defaults again.

## Quick Checklist

Before going live, review at least these items:

| Area | Production expectation |
| --- | --- |
| Authentication mode | Use one clear authentication mode. Do not enable Basic and OAuth2 together. |
| TLS | Use real certificates. Do not use bundled or demo keystores. |
| HSTS | Enable only after HTTPS is correctly deployed for the relevant domains. |
| CORS | Replace wildcard defaults with explicit frontend origins and headers. |
| Public endpoints | Restrict Swagger, Actuator, instance-mode, password reset, and authentication endpoints at the network edge. |
| Tenant secrets | Replace default tenant DB credentials and master passwords. |
| Default users | Rotate or disable demo users and avoid broad `ALL_FUNCTIONS` access for normal users. |
| Roles and permissions | Use least privilege roles and maker-checker for sensitive actions. |
| Password controls | Enable strong password policy, login retry limits, password reuse checks, and first-login reset if appropriate. |
| 2FA | Enable and test 2FA for administrative and operational users. |
| Idempotency | Require `Idempotency-Key` for write commands from external clients. |
| Uploads | Keep filename, MIME, size, and storage policies restrictive. |
| Outbound HTTP | Disable insecure HTTP clients and validate external TLS certificates. |
| Logs and monitoring | Enable useful audit context, but avoid leaking secrets or sensitive payloads. |
| Backups | Encrypt, test, and protect tenant store and tenant database backups. |

## 1. Choose One Authentication Model

In the current branch, Basic authentication is enabled by default:

```properties
fineract.security.basicauth.enabled=${FINERACT_SECURITY_BASICAUTH_ENABLED:true}
fineract.security.oauth2.enabled=${FINERACT_SECURITY_OAUTH_ENABLED:false}
```

Fineract validates that Basic authentication and OAuth2 are not both enabled and not both disabled. In other words, decide which model you are using.

For production:

| Scenario | Suggested direction |
| --- | --- |
| Internal back-office deployment behind private network controls | Basic authentication can be acceptable if TLS, tenant controls, and strict roles are enforced. |
| Internet-facing API or modern frontend integration | Prefer OAuth2 or OIDC federation with a real identity provider. |
| Multi-tenant or partner-facing deployment | Avoid shared administrative users and use tenant-aware authentication carefully. |

Example Basic auth configuration:

```bash
FINERACT_SECURITY_BASICAUTH_ENABLED=true
FINERACT_SECURITY_OAUTH_ENABLED=false
```

Example OAuth2 configuration:

```bash
FINERACT_SECURITY_BASICAUTH_ENABLED=false
FINERACT_SECURITY_OAUTH_ENABLED=true
```

If you use OIDC federation, review:

```bash
FINERACT_SECURITY_OIDC_FEDERATION_ENABLED=true
FINERACT_SECURITY_OIDC_TENANT_CLAIM=fineract_tenant
FINERACT_SECURITY_OIDC_USERNAME_CLAIM=preferred_username
FINERACT_SECURITY_OIDC_AUTO_CREATE_USER=false
```

Important:

Do not enable automatic OIDC user creation without a clear default role strategy. If auto-created users receive broad roles, your identity provider becomes a direct path to privileged Fineract access.

## 2. Treat Tenant Resolution as a Security Boundary

Fineract is tenant-aware. In Basic authentication mode, the tenant is resolved from:

```text
Fineract-Platform-TenantId
```

or from the query parameter:

```text
tenantIdentifier
```

This means the tenant identifier is not just a convenience value. It decides which tenant database connection is loaded for the request.

Production recommendations:

- Prefer the `Fineract-Platform-TenantId` header over query parameters.
- Do not let users manually choose arbitrary tenant identifiers unless your frontend and authorization model are designed for that.
- Validate tenant routing at the API gateway or reverse proxy when possible.
- Do not expose tenant identifiers that reveal sensitive customer or environment information.
- Keep tenant database users and schemas isolated.

For OIDC federation, tenant resolution can come from issuer mapping, configured issuer fallback, or the tenant claim. Review the issuer-to-tenant mapping carefully.

## 3. Replace All Demo Secrets

The default configuration contains values that must not survive into production.

Examples:

```properties
fineract.tenant.username=${FINERACT_DEFAULT_TENANTDB_UID:root}
fineract.tenant.password=${FINERACT_DEFAULT_TENANTDB_PWD:postgres}
fineract.tenant.master-password=${FINERACT_DEFAULT_TENANTDB_MASTER_PASSWORD:fineract}
fineract.database.defaultMasterPassword=${FINERACT_DEFAULT_MASTER_PASSWORD:fineract}
spring.datasource.hikari.username=${FINERACT_HIKARI_USERNAME:root}
spring.datasource.hikari.password=${FINERACT_HIKARI_PASSWORD:postgres}
```

The tenant database password is encrypted in the tenant store. At runtime, Fineract validates the master password hash and decrypts the tenant DB password.

Production recommendations:

- Use unique database credentials per environment.
- Use unique database credentials per tenant where practical.
- Replace `FINERACT_DEFAULT_TENANTDB_MASTER_PASSWORD`.
- Replace `FINERACT_DEFAULT_MASTER_PASSWORD`.
- Store secrets in Kubernetes Secrets, a cloud secret manager, Vault, or equivalent.
- Never commit production secrets into `application.properties`, Docker Compose files, Helm values, or CI logs.
- Plan master password rotation carefully because encrypted tenant database passwords must remain decryptable.

Important:

Changing the master password after tenant passwords were encrypted can break tenant database startup unless you also re-encrypt stored tenant credentials and update the master password hash.

## 4. Use Real TLS and Enable HSTS Deliberately

The branch enables SSL by default:

```properties
server.ssl.enabled=${FINERACT_SERVER_SSL_ENABLED:true}
server.ssl.key-store=${FINERACT_SERVER_SSL_KEY_STORE:classpath:keystore.jks}
server.ssl.key-store-password=${FINERACT_SERVER_SSL_KEY_STORE_PASSWORD:openmf}
```

This is not enough for production. The bundled keystore and default password are development conveniences.

Production recommendations:

- Terminate TLS at a trusted ingress, load balancer, or reverse proxy, or configure Fineract with a real keystore.
- Replace `FINERACT_SERVER_SSL_KEY_STORE`.
- Replace `FINERACT_SERVER_SSL_KEY_STORE_PASSWORD`.
- Keep private keys outside the image.
- Use modern TLS settings from your infrastructure baseline.
- Test `X-Forwarded-*` handling because the application uses `server.forward-headers-strategy=framework`.

HSTS is disabled by default:

```properties
fineract.security.hsts.enabled=${FINERACT_SECURITY_HSTS_ENABLED:false}
```

Enable it only when HTTPS is stable:

```bash
FINERACT_SECURITY_HSTS_ENABLED=true
```

In the current security chain, HSTS includes subdomains and uses a max age of 31536000 seconds.

Important:

Do not enable HSTS casually on shared domains or test domains. Once browsers cache it, HTTP fallback will stop working for that domain and its subdomains.

## 5. Lock Down CORS

CORS is enabled by default with very broad values:

```properties
fineract.security.cors.enabled=${FINERACT_SECURITY_CORS_ENABLED:true}
fineract.security.cors.allowed-origin-patterns=${FINERACT_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS:*}
fineract.security.cors.allowed-methods=${FINERACT_SECURITY_CORS_ALLOWED_METHODS:*}
fineract.security.cors.allowed-headers=${FINERACT_SECURITY_CORS_ALLOWED_HEADERS:*}
fineract.security.cors.exposed-headers=${FINERACT_SECURITY_CORS_EXPOSED_HEADERS:*}
fineract.security.cors.allow-credentials=${FINERACT_SECURITY_CORS_ALLOW_CREDENTIALS:true}
```

This is convenient for development. It is too broad for production.

Example production direction:

```bash
FINERACT_SECURITY_CORS_ENABLED=true
FINERACT_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=https://app.example.com
FINERACT_SECURITY_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
FINERACT_SECURITY_CORS_ALLOWED_HEADERS=Authorization,Content-Type,Fineract-Platform-TenantId,Idempotency-Key,X-Correlation-ID
FINERACT_SECURITY_CORS_EXPOSED_HEADERS=Location,X-Notification-Refresh
FINERACT_SECURITY_CORS_ALLOW_CREDENTIALS=true
```

Production recommendations:

- Use exact frontend origins.
- Do not use `*` for origins in production.
- Do not expose all headers unless your frontend actually needs them.
- Keep tenant and idempotency headers explicit.
- Validate CORS behavior from the browser, not only with `curl`.

## 6. Restrict Public and Operational Endpoints

Some endpoints must be public enough to work, but that does not mean they should be reachable from everywhere.

In Basic authentication mode, the security chain permits:

| Endpoint pattern | Why it matters |
| --- | --- |
| `OPTIONS /api/**` | Required for browser preflight requests. |
| `POST /api/*/authentication` | Login endpoint. Needs rate limiting and monitoring. |
| `POST /api/*/password/forgot` | Password reset endpoint. Needs abuse protection. |
| `PUT /api/*/instance-mode` | Operationally sensitive. Restrict at the proxy unless intentionally exposed. |

Swagger and OpenAPI are enabled by default:

```properties
springdoc.api-docs.enabled=${SPRINGDOC_API_DOCS_ENABLED:true}
springdoc.swagger-ui.enabled=${SPRINGDOC_SWAGGER_UI_ENABLED:true}
```

Actuator exposure defaults to:

```properties
management.endpoints.web.exposure.include=${FINERACT_MANAGEMENT_ENDPOINT_WEB_EXPOSURE_INCLUDE:health,info,prometheus}
```

Production recommendations:

- Disable Swagger UI publicly, or restrict it to VPN/admin networks.
- Restrict `/actuator/**` at ingress or expose it only on a management network.
- Avoid exposing Prometheus metrics publicly.
- Add edge rate limiting for login and forgot-password endpoints.
- Explicitly block or restrict `PUT /api/*/instance-mode` if you do not use it operationally.

Example:

```bash
SPRINGDOC_API_DOCS_ENABLED=false
SPRINGDOC_SWAGGER_UI_ENABLED=false
FINERACT_MANAGEMENT_ENDPOINT_WEB_EXPOSURE_INCLUDE=health,info
```

## 7. Rotate or Disable Default Users

The tenant initial data includes users such as:

| User | Notes |
| --- | --- |
| `mifos` | Default application administrator user in demo data. |
| `system` | System user. |
| `interopUser` | Interoperability user. |

The initial data also creates a `Super user` role with broad application permissions and assigns it to `mifos` and `interopUser`.

Production recommendations:

- Rotate all default user passwords immediately.
- Disable users that are not required.
- Do not use shared human administrator accounts.
- Do not give normal users `ALL_FUNCTIONS`.
- Create separate roles for operations, support, finance, integration users, reporting, and administration.
- Review service accounts separately from human accounts.
- Monitor successful and failed login attempts for privileged accounts.

Important:

The easiest production mistake is to keep demo-style access because it makes testing easier. That shortcut is expensive later.

## 8. Use Roles, Permissions, and Maker-Checker

Fineract permissions are authority based. Some endpoints are protected directly in the security chain, and many commands are checked through command permissions such as `CREATE_*`, `UPDATE_*`, `DELETE_*`, and read permissions.

The `maker-checker` global configuration is disabled by default:

```xml
<column name="name" value="maker-checker"/>
<column name="enabled" valueBoolean="false"/>
```

Production recommendations:

- Enable maker-checker for high-risk commands.
- Use maker-checker for user administration, roles, permissions, product setup, loan approval, disbursement, write-off, charge-off, accounting, and configuration changes.
- Keep maker and checker duties separated.
- Review whether same-user maker-checker is allowed in your environment.
- Periodically export and review roles and permissions.

Maker-checker is not a replacement for least privilege. It is an additional control for sensitive operations.

## 9. Strengthen Password and Login Controls

The branch includes several password and login controls, but some are configuration driven.

Password policy:

- A simple policy exists in older initial data.
- A strong policy is added later and made active when the migration conditions apply.
- Password validation uses the active policy from `m_password_validation_policy`.

Login retry limit:

```xml
<column name="name" value="max-login-retry-attempts"/>
<column name="value" valueNumeric="5"/>
<column name="enabled" valueBoolean="false"/>
```

Password reuse check:

```xml
<column name="name" value="password-reuse-check-history-count"/>
<column name="value" valueNumeric="3"/>
<column name="enabled" valueBoolean="false"/>
```

Force password reset:

```xml
<column name="name" value="force-password-reset-days"/>
<column name="enabled" valueBoolean="false"/>
```

```xml
<column name="name" value="force-password-reset-on-first-login"/>
<column name="enabled" valueBoolean="false"/>
```

Production recommendations:

- Confirm that the active password policy is the strong policy.
- Enable max login retries.
- Enable password reuse prevention.
- Enable first-login reset for created or reset users.
- Decide whether periodic password reset is required by your policy.
- Disable `passwordNeverExpires` for normal users.
- Use unique service-account credentials and rotate them.

Important:

Password controls in the database only help if they are enabled and tested. Do not assume the presence of a configuration row means the control is active.

## 10. Enable and Tune Two-Factor Authentication

2FA is disabled by default:

```properties
fineract.security.2fa.enabled=${FINERACT_SECURITY_2FA_ENABLED:false}
```

When 2FA is enabled, the security chain requires the `TWOFACTOR_AUTHENTICATED` authority for normal API access.

The default two-factor configuration includes:

| Configuration | Default |
| --- | --- |
| `otp-delivery-email-enable` | `true` |
| `otp-delivery-sms-enable` | `false` |
| `otp-token-live-time` | `300` seconds |
| `otp-token-length` | `5` |
| `access-token-live-time` | `86400` seconds |
| `access-token-live-time-extended` | `604800` seconds |

Production recommendations:

- Enable 2FA for administrators and privileged users.
- Validate email or SMS delivery before enabling it globally.
- Review OTP length and token lifetime.
- Review extended access token lifetime.
- Make sure 2FA tokens are not logged.
- Test 2FA behavior with API clients, UI clients, and batch or integration users.

Example:

```bash
FINERACT_SECURITY_2FA_ENABLED=true
```

## 11. Require Idempotency for Write APIs

Fineract supports idempotent command processing through the `Idempotency-Key` header:

```properties
fineract.idempotency-key-header-name=${FINERACT_IDEMPOTENCY_KEY_HEADER_NAME:Idempotency-Key}
```

The command source table stores command state and has a unique constraint over:

```text
action_name, entity_name, idempotency_key
```

In this branch, command result persistence has been tightened so successful command execution and successful result persistence happen in one transactional flow, while final error results are persisted after retry handling when possible.

Why this matters:

- Clients can safely retry requests after timeouts.
- Duplicate disbursements, repayments, approvals, or adjustments are less likely when clients use stable idempotency keys.
- Support teams can inspect command-source records when investigating what happened.

Production recommendations:

- Require external clients to send `Idempotency-Key` for every write command.
- Generate a new key for each logical business action, not for each HTTP retry.
- Reuse the same key when retrying the same request.
- Do not reuse a key for a different command.
- Keep command-source retention long enough for audit and support needs.
- Configure and monitor the `Purge Processed Commands` job according to your retention policy.

Important:

Idempotency is not a permission control. It prevents duplicate processing patterns, but the user still needs correct authentication and authorization.

## 12. Lock Down File Uploads and Storage

Fineract document handling includes several protections:

- Path traversal checks.
- Filename extension whitelist.
- MIME whitelist.
- Post-upload MIME validation using detected content.

Defaults include:

```properties
fineract.content.regex-whitelist-enabled=true
fineract.content.mime-whitelist-enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

Default allowed file types include PDF, Word, Excel, JPEG, and PNG.

Production recommendations:

- Keep extension and MIME whitelists enabled.
- Avoid adding executable, script, archive, or HTML file types unless absolutely required.
- Keep upload size limits small.
- Store uploaded documents outside the application image.
- Use a private bucket if S3 storage is enabled.
- Restrict S3 bucket policies to the application role only.
- Encrypt stored documents at rest.
- Scan uploaded content if your threat model requires it.

If using filesystem storage, review:

```properties
fineract.content.filesystem.rootFolder=${FINERACT_CONTENT_FILESYSTEM_ROOT_FOLDER:${user.home}/.fineract}
```

If using S3, review:

```properties
fineract.content.s3.enabled=${FINERACT_CONTENT_S3_ENABLED:false}
fineract.content.s3.bucketName=${FINERACT_CONTENT_S3_BUCKET_NAME:}
fineract.content.s3.accessKey=${FINERACT_CONTENT_S3_ACCESS_KEY:}
fineract.content.s3.secretKey=${FINERACT_CONTENT_S3_SECRET_KEY:}
```

## 13. Disable Insecure Outbound HTTP

The application exposes an insecure HTTP client setting:

```properties
fineract.insecure-http-client=${FINERACT_INSECURE_HTTP_CLIENT:true}
```

When enabled in the shared OkHttp client, certificate and hostname verification can be bypassed.

Production recommendation:

```bash
FINERACT_INSECURE_HTTP_CLIENT=false
```

Also review webhook and integration clients. The hook processor contains similar logic controlled through the JVM system property:

```text
fineract.insecureHttpClient
```

Production recommendations:

- Do not bypass TLS validation for webhooks or integrations.
- Use valid certificates on downstream systems.
- Restrict webhook target domains.
- Avoid sending sensitive payloads to untrusted endpoints.
- Use integration-specific authentication secrets.
- Rotate webhook and external service credentials.

## 14. Review External Events, JMS, Kafka, and Jobs

External events are disabled by default:

```properties
fineract.events.external.enabled=${FINERACT_EXTERNAL_EVENTS_ENABLED:false}
```

JMS and Kafka producers are also configuration driven.

Production recommendations:

- Enable external events only when there is a real consumer and operational owner.
- Secure Kafka and JMS with TLS and authentication.
- Do not rely on localhost defaults in production.
- Review topic names, partition counts, retention, and access policies.
- Treat event payloads as sensitive.
- Monitor failed event delivery and retry queues.

For jobs:

- Separate API nodes, batch manager nodes, and batch worker nodes if your deployment scale requires it.
- Review `FINERACT_MODE_READ_ENABLED`, `FINERACT_MODE_WRITE_ENABLED`, `FINERACT_MODE_BATCH_WORKER_ENABLED`, and `FINERACT_MODE_BATCH_MANAGER_ENABLED`.
- Restrict operational job endpoints to administrators.
- Monitor stuck jobs and failed jobs.

## 15. Make Logging Useful, Not Dangerous

Useful production settings:

```properties
fineract.correlation.enabled=${FINERACT_LOGGING_HTTP_CORRELATION_ID_ENABLED:false}
fineract.ip-tracking.enabled=${FINERACT_CLIENT_IP_TRACKING_ENABLED:false}
fineract.logging.json.enabled=${FINERACT_LOGGING_JSON_ENABLED:false}
server.tomcat.accesslog.enabled=${FINERACT_SERVER_TOMCAT_ACCESSLOG_ENABLED:false}
```

Production recommendations:

- Enable correlation IDs so requests can be traced across services.
- Enable JSON logs if your log pipeline expects structured logs.
- Enable client IP tracking only if your proxy headers are trustworthy.
- Avoid SQL statement logging in production unless debugging a short-lived incident.
- Review `dumpQueriesOnException` because database exception logs can expose sensitive query context.
- Mask passwords, tokens, OTPs, authorization headers, tenant DB credentials, and external service secrets.
- Set log retention according to audit and privacy requirements.

Example direction:

```bash
FINERACT_LOGGING_HTTP_CORRELATION_ID_ENABLED=true
FINERACT_CLIENT_IP_TRACKING_ENABLED=true
FINERACT_LOGGING_JSON_ENABLED=true
```

## 16. Protect the Database Layer

Fineract has at least two important database layers:

| Database | Purpose |
| --- | --- |
| Tenant store database | Stores tenant metadata and tenant database connection information. |
| Tenant database | Stores business data for a tenant. |

Production recommendations:

- Use private network access only.
- Do not expose databases publicly.
- Use least-privilege database users.
- Use separate credentials for tenant store and tenant databases.
- Encrypt database storage.
- Encrypt backups.
- Test restore procedures.
- Restrict who can read tenant store connection information.
- Monitor schema migration failures.
- Decide whether Liquibase should run automatically on application startup or through a controlled migration process.

Important:

The tenant store is especially sensitive because it contains tenant routing and encrypted tenant DB connection data.

## 17. Validate Deployment with Security Smoke Tests

After configuration, run practical checks.

Suggested checks:

| Check | Expected result |
| --- | --- |
| Call API without authentication | Rejected. |
| Call API without tenant header in Basic mode | Rejected. |
| Call API with invalid tenant | Rejected. |
| Call write API without permission | Rejected. |
| Repeat write API with same `Idempotency-Key` | Does not create duplicate business result. |
| Access Swagger publicly | Blocked or intentionally restricted. |
| Access Actuator publicly | Blocked or intentionally restricted. |
| Use browser from unapproved origin | CORS blocks request. |
| Use default user credentials | Not valid in production. |
| Upload disallowed file type | Rejected. |
| Call downstream webhook with invalid certificate | Fails when insecure HTTP is disabled. |

## Example Production Baseline

This is not a complete deployment file. It is a starting point for review:

```bash
# Authentication
FINERACT_SECURITY_BASICAUTH_ENABLED=false
FINERACT_SECURITY_OAUTH_ENABLED=true
FINERACT_SECURITY_2FA_ENABLED=true

# TLS and headers
FINERACT_SERVER_SSL_ENABLED=true
FINERACT_SERVER_SSL_KEY_STORE=/run/secrets/fineract-keystore.p12
FINERACT_SERVER_SSL_KEY_STORE_PASSWORD=<secret>
FINERACT_SECURITY_HSTS_ENABLED=true

# CORS
FINERACT_SECURITY_CORS_ENABLED=true
FINERACT_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=https://app.example.com
FINERACT_SECURITY_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
FINERACT_SECURITY_CORS_ALLOWED_HEADERS=Authorization,Content-Type,Fineract-Platform-TenantId,Idempotency-Key,X-Correlation-ID
FINERACT_SECURITY_CORS_EXPOSED_HEADERS=Location,X-Notification-Refresh
FINERACT_SECURITY_CORS_ALLOW_CREDENTIALS=true

# Docs and management
SPRINGDOC_API_DOCS_ENABLED=false
SPRINGDOC_SWAGGER_UI_ENABLED=false
FINERACT_MANAGEMENT_ENDPOINT_WEB_EXPOSURE_INCLUDE=health,info

# Secrets
FINERACT_HIKARI_USERNAME=<tenant-store-user>
FINERACT_HIKARI_PASSWORD=<tenant-store-password>
FINERACT_DEFAULT_TENANTDB_MASTER_PASSWORD=<strong-master-password>
FINERACT_DEFAULT_MASTER_PASSWORD=<strong-master-password>

# Outbound HTTP
FINERACT_INSECURE_HTTP_CLIENT=false

# Observability
FINERACT_LOGGING_HTTP_CORRELATION_ID_ENABLED=true
FINERACT_CLIENT_IP_TRACKING_ENABLED=true
FINERACT_LOGGING_JSON_ENABLED=true
```

## Final Thoughts

A secure Fineract production deployment is not one switch. It is a set of decisions:

- How users authenticate.
- How tenants are selected.
- How secrets are stored.
- Which endpoints are public.
- Which roles can perform sensitive actions.
- How retries and idempotency are handled.
- How uploads, integrations, jobs, logs, and databases are controlled.

The safest approach is to treat the default configuration as a development baseline, then explicitly harden every boundary before going live.
