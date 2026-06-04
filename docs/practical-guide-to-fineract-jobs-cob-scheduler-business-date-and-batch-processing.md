# A Practical Guide to Fineract Jobs: COB, Scheduler, Business Date, and Batch Processing

## Introduction

Apache Fineract has several concepts that are easy to mix together:

- Scheduler jobs
- Spring Batch jobs
- Close of Business
- Business date
- COB date
- Batch manager mode
- Batch worker mode
- Remote partitioning
- The `/v1/batches` HTTP API

They are related, but they are not the same thing.

This article explains how they fit together in the current Fineract implementation, with special focus on Loan COB because it is one of the most important background processes in a production system.

## Source of Truth

This article is based on the current branch implementation, especially these areas:

| Area | Main implementation |
| --- | --- |
| Job names | `fineract-core/.../infrastructure/jobs/service/JobName.java` |
| Scheduler startup | `fineract-provider/.../infrastructure/jobs/service/JobSchedulerServiceImpl.java` |
| Job launcher | `fineract-provider/.../infrastructure/jobs/service/JobStarter.java` |
| Quartz registration | `fineract-provider/.../infrastructure/jobs/service/JobRegisterServiceImpl.java` |
| Scheduler veto logic | `fineract-provider/.../infrastructure/jobs/service/SchedulerVetoer.java` |
| Scheduler write service | `fineract-provider/.../infrastructure/jobs/service/SchedularWritePlatformServiceJpaRepositoryImpl.java` |
| Scheduler API | `fineract-provider/.../infrastructure/jobs/api/SchedulerApiResource.java` |
| Jobs API | `fineract-provider/.../infrastructure/jobs/api/SchedulerJobApiResource.java` |
| Business date type | `fineract-core/.../infrastructure/businessdate/domain/BusinessDateType.java` |
| Business date read service | `fineract-provider/.../infrastructure/businessdate/service/BusinessDateReadPlatformServiceImpl.java` |
| Business date write service | `fineract-provider/.../infrastructure/businessdate/service/BusinessDateWritePlatformServiceImpl.java` |
| Loan COB constants | `fineract-cob/.../loan/LoanCOBConstant.java` |
| Loan COB manager | `fineract-provider/.../cob/loan/LoanCOBManagerConfiguration.java` |
| Loan COB worker | `fineract-provider/.../cob/loan/LoanCOBWorkerConfiguration.java` |
| Inline Loan COB | `fineract-provider/.../cob/loan/LoanInlineCOBConfig.java` |
| COB partitioning | `fineract-provider/.../cob/loan/LoanCOBPartitioner.java` |
| COB processing | `fineract-provider/.../cob/AbstractItemProcessor.java` |
| Loan COB processing | `fineract-provider/.../cob/loan/AbstractLoanItemProcessor.java` |
| COB business steps | `fineract-provider/.../cob/service/COBBusinessStepServiceImpl.java` |
| Business step API | `fineract-provider/.../infrastructure/jobs/api/ConfigureBusinessStepApiResource.java` |
| Runtime properties | `fineract-provider/src/main/resources/application.properties` |

## Quick Glossary

| Term | Meaning in Fineract |
| --- | --- |
| Scheduler job | A job configured in Fineract's scheduler metadata and triggered by Quartz. |
| Spring Batch job | The executable job implementation launched by Fineract, such as `LOAN_COB`. |
| COB | Close of Business processing. For loans, this runs configured business steps against eligible loans. |
| Business date | Fineract's operational date, used instead of always using the server date. |
| COB date | The date used to drive Close of Business processing. |
| Batch manager | The instance responsible for scheduling and partitioning batch work. |
| Batch worker | The instance responsible for executing batch work chunks. |
| Remote partitioning | Splitting batch work into partitions that workers execute. |
| Inline COB | A direct COB execution path for a selected set of loan ids. |
| `/v1/batches` | An HTTP request batching API. Do not confuse it with Spring Batch background jobs. |

## The High-Level Model

Fineract job execution has four layers:

```text
database scheduler metadata
  -> Quartz trigger
  -> Fineract JobStarter
  -> Spring Batch job
```

For Loan COB, the Spring Batch job then adds another layer:

```text
Loan COB manager
  -> partitions eligible loans
  -> sends partitions to workers
  -> workers execute configured COB business steps
```

That distinction matters operationally.

If a job does not start, look at scheduler metadata and Quartz behavior.

If a job starts but no loans are processed, look at COB dates, loan eligibility, partitioning, and business step configuration.

If partitions are created but workers do nothing, look at batch worker mode and remote partition messaging.

## Runtime Modes

Fineract supports separate runtime modes through properties:

| Property | Default | Meaning |
| --- | --- | --- |
| `FINERACT_MODE_READ_ENABLED` | `true` | Instance can serve read APIs. |
| `FINERACT_MODE_WRITE_ENABLED` | `true` | Instance can serve write APIs. |
| `FINERACT_MODE_BATCH_MANAGER_ENABLED` | `true` | Instance can manage scheduled jobs and partitions. |
| `FINERACT_MODE_BATCH_WORKER_ENABLED` | `true` | Instance can execute batch worker steps. |

The default is an all-in-one instance: read, write, batch manager, and batch worker are all enabled.

In larger deployments, you may split these roles.

For example:

| Instance | Read | Write | Batch manager | Batch worker |
| --- | --- | --- | --- | --- |
| API node | Yes | Yes | No | No |
| Batch manager | No or limited | No or limited | Yes | No |
| Batch worker | No or limited | No or limited | No | Yes |

The exact topology depends on your deployment, database capacity, and operational requirements.

## Scheduler Startup

On application startup, Fineract starts the scheduler only when batch manager mode is enabled.

The startup flow is:

1. Fineract receives the application context refreshed event.
2. If batch manager mode is disabled, scheduler startup is skipped.
3. For each tenant, Fineract sets tenant context.
4. Business dates are loaded into thread-local context.
5. Scheduler job metadata is read from the database.
6. Jobs are registered with Quartz.
7. Misfire and reset-on-boot behavior is handled.
8. Tenant context is cleared.

This means a node with batch manager disabled should not be expected to start or manage scheduled jobs.

## The Scheduler APIs

Fineract exposes scheduler and job APIs.

Common paths include:

| API | Purpose |
| --- | --- |
| `GET /v1/scheduler` | Read scheduler status. |
| `POST /v1/scheduler?command=start` | Start scheduler. |
| `POST /v1/scheduler?command=stop` | Stop scheduler. |
| `GET /v1/jobs` | List jobs. |
| `GET /v1/jobs/{jobId}` | Read one job. |
| `POST /v1/jobs/{jobId}?command=executeJob` | Execute one job manually. |
| `PUT /v1/jobs/{jobId}` | Update job metadata. |
| `GET /v1/jobs/{jobId}/runhistory` | Read job run history. |

Job execution APIs are batch-manager operations. If the instance is not a batch manager, do not expect it to behave like a scheduler control node.

## Quartz, Vetoing, and Run History

Fineract uses Quartz to trigger scheduled jobs.

Before a job actually runs, Fineract applies scheduler logic to avoid unsafe execution. The scheduler write service checks things such as:

- Is the job already running?
- Is the cron trigger actually due?
- Is the scheduler suspended?
- Did the job misfire?
- Is this node allowed to execute the job?

If the job is allowed, Fineract marks it as currently running and starts it.

After completion, scheduler listeners update run history:

- Success or failure status
- Error message and stack trace when relevant
- Next run time
- Currently-running flag
- Run history version

This is why run history is the first place to check after a failed scheduled job.

## JobStarter: The Bridge Into Spring Batch

The `JobStarter` is the bridge between Fineract scheduler metadata and Spring Batch.

When a job starts, it:

1. Ensures tenant context exists.
2. Loads tenant information if needed.
3. Sets system-user authentication.
4. Loads business dates.
5. Sets the default action context.
6. Builds Spring Batch job parameters.
7. Starts the Spring Batch job through the job launcher.
8. Treats failed, abandoned, stopped, stopping, or unknown statuses as execution failure.

This tenant and business date setup is important. Jobs should not run as anonymous, tenantless background threads.

## Business Date and COB Date

Fineract has two business date types:

| Type | Meaning |
| --- | --- |
| `BUSINESS_DATE` | The operational business date. |
| `COB_DATE` | The date used for Close of Business processing. |

If the business date feature is disabled, Fineract defaults both dates to the tenant local date.

If the feature is enabled, Fineract reads stored dates from `m_business_date`.

When business date adjustment is enabled, changing `BUSINESS_DATE` can automatically set:

```text
COB_DATE = BUSINESS_DATE - 1 day
```

That convention is important for Loan COB. COB often processes the day that just closed, while the business date has already moved forward.

## Business Date APIs

The business date API allows reading and updating business dates.

Common paths include:

| API | Purpose |
| --- | --- |
| `GET /v1/businessdate` | Read all business dates. |
| `GET /v1/businessdate/{type}` | Read one business date type. |
| `POST /v1/businessdate` | Update a business date. |

Before running COB manually, confirm the business date and COB date.

Many "COB processed nothing" problems are actually date problems.

## What Loan COB Does

Loan COB is the Close of Business process for loans.

The job name is:

```text
LOAN_COB
```

It is enabled by:

```text
FINERACT_JOB_LOAN_COB_ENABLED=true
```

That is the default.

Loan COB selects eligible loans, partitions the work, and runs configured business steps against each loan.

Representative business steps include:

- Update loan arrears aging
- Check due installments
- Check repayment due
- Check repayment overdue
- Apply charges to overdue loans
- Recalculate loan interest
- Set loan delinquency tags
- Add periodic accrual entries
- Post accrual activity
- Process capitalized income amortization
- Process buy-down fee amortization

The exact active steps and order are configurable.

Do not assume every possible step runs in your environment.

## Loan COB Manager Flow

The Loan COB manager job is configured when batch manager mode is enabled.

Its flow is:

1. Resolve custom job parameters.
2. Partition Loan COB work.
3. Handle stayed locked loan accounts.
4. Unlock processed loan accounts.

The partitioning step identifies eligible loans and splits them into partitions. Those partitions are then sent to workers.

## Loan COB Worker Flow

The Loan COB worker is configured when batch worker mode is enabled.

For each loan item, the worker flow is approximately:

1. Read the loan.
2. Rebuild the progressive loan model if needed.
3. Load the configured business steps.
4. Run each business step in configured order.
5. Set the loan's `lastClosedBusinessDate`.
6. Write the processed result.

During business step execution, Fineract sets the action context to COB. That context matters because some domain logic behaves differently depending on whether it is running as normal API activity or as COB activity.

## Which Loans Are Eligible for Loan COB?

Loan COB does not process every loan every time.

The current implementation selects non-closed loans with statuses such as:

- Submitted and pending approval
- Approved
- Active
- Transfer in progress
- Transfer on hold

It also uses the loan's `lastClosedBusinessDate`.

The normal selection is based around:

```text
lastClosedBusinessDate = COB_DATE - 1 day
```

or loans where the last closed business date is missing.

Catch-up mode has stricter date behavior and is intended for catching up loans that are behind.

If Loan COB processes no loans, check the COB date and `lastClosedBusinessDate` before assuming the job is broken.

## Business Steps Configuration

Fineract exposes APIs to inspect and configure business steps.

Common paths include:

| API | Purpose |
| --- | --- |
| `GET /v1/jobs/names` | List job names that support step configuration. |
| `GET /v1/jobs/{jobName}/steps` | Read configured steps for a job. |
| `PUT /v1/jobs/{jobName}/steps` | Update configured steps for a job. |
| `GET /v1/jobs/{jobName}/available-steps` | List available steps for a job. |

For Loan COB, this is operationally important.

If no business steps are configured, the partitioner can stop the job because there is no business work to execute.

## Accruals and COB

Loan COB can create accounting-relevant activity.

For example, the COB business steps include:

- Add periodic accrual entries
- Post accrual activity

These steps can create loan transactions such as accrual or accrual activity transactions. Those transactions then flow through the accounting engine and create journal entries if product accounting is configured correctly.

This is the correct mental model:

```text
Loan COB business step
  -> loan transaction
  -> accounting processor
  -> journal entries
```

COB itself is not the accounting ledger. COB is one producer of accounting-relevant loan events.

## Inline Loan COB

Fineract also has an inline Loan COB path.

Inline COB is useful when you need to run COB for a specific set of loan ids rather than relying on the normal scheduled partition selection.

The inline job uses parameters such as loan ids, business date, and business steps.

This is a powerful operational tool, but it should be used carefully:

- Confirm the loan ids.
- Confirm business date and COB date.
- Confirm the selected business steps.
- Confirm the loan has not already been processed for that date.
- Review output and run history after execution.

## Remote Partitioning

Loan COB uses partitioned batch processing.

In an all-in-one setup, the manager and worker can live in the same application process.

In a distributed setup, the manager sends partition requests to workers. The current codebase includes support around:

- Spring application events
- JMS
- Kafka-related manager and worker configuration

The default local setup uses Spring events. That is appropriate when the manager and worker are in the same process.

For separated manager and worker deployments, use a real remote transport and validate the configuration carefully.

## Important Runtime Properties

The most important job and COB properties include:

| Property | Default | Meaning |
| --- | --- | --- |
| `FINERACT_JOB_LOAN_COB_ENABLED` | `true` | Enables Loan COB job beans. |
| `FINERACT_JOB_STUCK_RETRY_THRESHOLD` | `5` | Threshold for stuck job retry behavior. |
| `LOAN_COB_CHUNK_SIZE` | `100` | Number of loan items per worker chunk. |
| `LOAN_COB_PARTITION_SIZE` | `100` | Number of loan ids per partition range. |
| `LOAN_COB_THREAD_POOL_CORE_POOL_SIZE` | `5` | Core worker thread count. |
| `LOAN_COB_THREAD_POOL_MAX_POOL_SIZE` | `5` | Max worker thread count. |
| `LOAN_COB_THREAD_POOL_QUEUE_CAPACITY` | `20` | Worker queue capacity. |
| `LOAN_COB_RETRY_LIMIT` | `5` | Retry limit for worker step failures. |
| `LOAN_COB_POLL_INTERVAL` | `500` | Manager polling interval for partition completion. |

There are also message transport properties for Spring events, JMS, and Kafka depending on the deployment model.

## Tuning Loan COB

Start with the defaults unless you have measured a bottleneck.

Tune one dimension at a time:

| Tuning option | What it affects |
| --- | --- |
| Chunk size | Transaction size and memory per worker commit. |
| Partition size | How work is divided across workers. |
| Worker thread count | Parallelism inside a worker instance. |
| Number of workers | Horizontal processing capacity. |
| Database pool size | Whether workers can actually obtain DB connections. |
| Message transport | Partition delivery reliability and throughput. |

The common mistake is increasing worker threads without increasing database capacity.

That usually makes the system less stable, not faster.

## Scheduler Job Versus `/v1/batches`

Fineract has an endpoint family named `/v1/batches`.

That API is for batching HTTP API requests.

It is not the same thing as:

- Spring Batch
- Loan COB
- Scheduled jobs
- Batch manager mode
- Batch worker mode

This distinction matters because `/v1/batches` can be available in contexts where job execution is restricted differently.

When discussing background jobs, use precise language:

| Say this | When you mean |
| --- | --- |
| Scheduler job | A configured Quartz-triggered Fineract job. |
| Spring Batch job | The executable batch job implementation. |
| Loan COB | The loan Close of Business job. |
| HTTP batch API | `/v1/batches`, batching API requests. |

## Operational Checklist Before Running Loan COB

Before running Loan COB manually, check:

| Check | Why |
| --- | --- |
| Batch manager mode is enabled | Manual job execution is a manager operation. |
| Batch worker mode is enabled somewhere | Partitions need workers. |
| Scheduler status is known | Avoid fighting a running scheduler. |
| Business date is correct | Jobs load business date context. |
| COB date is correct | Loan eligibility depends on COB date. |
| Loan COB job is enabled | Disabled job beans cannot execute. |
| Job is not already running | Prevent overlapping processing. |
| Node id matches job metadata | Jobs can be node-specific. |
| Business steps are configured | No steps means no useful COB work. |
| Worker capacity is sufficient | Partitions require DB and thread capacity. |
| Run history is monitored | Failures are recorded there. |

## Troubleshooting Guide

| Symptom | Likely area to check |
| --- | --- |
| Job does not appear | Job bean disabled, batch manager disabled, or scheduler metadata missing. |
| Job appears but does not run | Scheduler stopped, suspended, node mismatch, cron not due, or currently-running flag. |
| Manual execution fails immediately | Batch manager mode, permissions, job parameters, or tenant context. |
| Loan COB runs but processes no loans | COB date, loan status, `lastClosedBusinessDate`, catch-up mode, or empty partitions. |
| Loan COB stops because there is no work | Missing or empty business step configuration. |
| Workers do not receive partitions | Batch worker mode or remote partition transport configuration. |
| COB is slow | Chunk size, partition size, worker count, DB connection pool, or expensive business steps. |
| COB creates no accounting | Product accounting rule, accrual step configuration, or product-to-GL mappings. |
| Date behavior looks wrong | Business date feature, tenant timezone, or COB date adjustment. |
| Loans remain locked | Stayed locked loan handling, failed worker step, or interrupted execution. |

## Practical Debugging Flow

Use this sequence when a scheduled job or Loan COB issue appears:

1. Confirm which tenant is affected.
2. Confirm whether you are on a batch manager instance.
3. Read scheduler status.
4. Read the job metadata.
5. Check whether the job is currently marked running.
6. Check the last run history record.
7. Confirm business date and COB date.
8. For Loan COB, inspect configured business steps.
9. For Loan COB, check eligible loan dates and statuses.
10. Confirm workers are enabled and receiving partitions.
11. Review application logs around the job execution id.
12. Only then tune chunk, partition, or thread settings.

## Production Notes

### Keep scheduler control narrow

Only batch manager instances should control jobs.

Avoid allowing every API node to behave like a scheduler node unless that is an intentional all-in-one deployment.

### Treat business date changes as operational events

Changing business date changes how jobs and domain logic behave.

It should be part of a controlled operating procedure, not an ad hoc database update.

### Do not tune blindly

COB performance is usually constrained by one of these:

- Database throughput
- Lock contention
- Worker concurrency
- Expensive business steps
- Message transport
- Loan data shape

Increasing thread count does not fix all of those.

### Separate "job failed" from "job did no work"

A successful job can process zero loans if no loans are eligible.

That is different from a failed job.

Use run history, logs, partition output, and loan eligibility checks to distinguish them.

## Summary

Fineract jobs are easiest to understand as a layered system:

```text
Scheduler metadata
  -> Quartz trigger
  -> JobStarter
  -> Spring Batch job
  -> COB manager
  -> COB worker
  -> COB business steps
```

Business date and COB date are central to that flow.

For Loan COB specifically, the most important operational questions are:

- Is the instance a batch manager?
- Is there at least one batch worker?
- Are business date and COB date correct?
- Is Loan COB enabled?
- Are business steps configured?
- Are loans eligible based on status and `lastClosedBusinessDate`?
- Are partitions reaching workers?
- Did run history record a failure?

Once you separate scheduler behavior, batch execution, business date handling, and COB business logic, Fineract jobs become much easier to operate and troubleshoot.
