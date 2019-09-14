## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.


*Running through docker*

Install docker for your platform. Start the server via

```
./docker-start.sh
```


### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the "rest api" models used throughout the application.
|
├── pleo-antaeus-rest
|        Entry point for REST API. This is where the routes are defined.
└──
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!

## Solution

I enjoyed diving into Kotlin for this project! It has been several years since working in Java, and it was a delight to work in Kotlin, which feels like a snappy Java shorthand. My work has been scattered over the past 2 weeks, but I'd estimate I spent about 20 hours working on the project and bootstrapping my knowledge of Kotlin.

I completed this project in various pull requests, each centered around a core building block or feature:
1. fixed docker-start and README (#1)
2. updated Dockerfile for simple cron (#2)
3. added new DB tables and mappings (#4)
4. setup async handling of cron webhook (#6)
5. Updated core services (#7)
6. Added JobRunner framework to BillingService (#8)
7. Implemented monthly billing job (#9)
8. Implemented weekly billing job (#10)

(The madness behind the method is in #3 - in which I experimented with different language features)

### Approach

Antaeus is prepared to create and charge invoices on the first day of each month, and ensure that payment is made by the end of the month. 
*   A monthly job runs to create and charge invoices
*   A weekly job looks for outstanding invoices and attempts to fulfill them

To accomplish this, Antaeus implements a naive job-scheduling framework to create and track the status of long-running jobs. A job is run via the following:
1. A cron makes a POST request to the `/rest/v1/webooks/crons/` endpoint, specifying the type of job (`jobType`) and billing period (`period`)
2. The BillingService attempts to retrieve a previously created job with that `jobType` and `period` and returns its status. When there is no existing job, it starts a new one.
3. For new jobs, the BillingService initiates the asynchronous completion of the job in a separate thread and immediately responds to the endpoint with the newly created job name and status.

**Considerations**

In practice, I don’t always think it is a good idea to reinvent the wheel and build a scheduler from scratch! Before starting the project, I researched the popular third-party job scheduler [Quartz](http://www.quartz-scheduler.org/), which would have met the requirements I was looking for. At a larger scale and smaller tasks, I use [RabbitMQ](https://www.rabbitmq.com/) and [Celery](http://www.celeryproject.org/) in my day-to-day for scheduling tens of thousands of tasks at Rover. Rather than set up a distributed ecosystem or integrate with a third-party, I enjoyed building a small framework as a demonstration of how I think about software architecture. I hope you enjoy it!


### Architecture

#### Idempotent Cron Jobs

I added a `CronJob` table that enables the `/webhooks/crons/` endpoint to be idempotent; only one job is run for a given `jobType` and `period`. The database is kept in sync with the job’s status as it runs.

Example Request (`POST /rest/v1/webhooks/cron`)
```json
{
    "jobType": "MONTHLY_BILLING",
    "period": "2019-09-01"
}
```

Response
```json
{
    "id": 1,
    "name": "MONTHLY_BILLING_2019_09",
    "status": "CREATED",
    "started": "2019-09-14T15:27:21.316-07:00"
}
```

The `Dockerfile` is updated to setup the cron jobs defined in the `subscription-cron` file. Currently there are two crons for monthly-billing and weekly-settlement, respectively. The crons execute scripts located in the `bin` folder, and output is captured in `/var/log/cron.log`.

**Considerations**

I chose this approach as a simple way of using the web server to initiate billing jobs. With more time, I would make the jobs themselves more resilient to interruptions or failures by adding a “FAILED” status to CronJobs, and allow the re-running of a failed job. 

Alternatively, I considered is using a time-based approach that is not web-driven (e.g. the BillingService itself schedules jobs at regular intervals). A major drawback to this is that there is the risk of dropping a job scheduled for the future if the server restarts, or the scheduled job gets killed by some other means. I also like the added visibility into job statuses that the webhook provides to potential clients.


#### JobRunner Framework

Individual jobs are defined by implementing the `JobRunner` interface (located in the `io.pleo.antaeus.core.jobs` package) and updating some minimal configuration. This enables the `BillingService` and webhook to handle all billing jobs in a generic way. To add a new `JobRunner` to Antaeus,
1. Add a new value to the `JobType` enum that represents your job
2. Implement the `JobRunner` interface `run` method with your business logic
3. Update the `JobRunnerFactory` so that the enum maps to your `JobRunner`
4. Add a cron job and accompanying script that calls the webhook with your new `JobType`

That’s it!


### Billing Jobs

#### MonthlyBillingJobRunner

The `MonthlyBillingJobRunner` iterates over all customers and creates invoices for each one, attempting to charge. For a successful charge, the Invoice status is set to “PAID”. When a charge fails, the invoice is left as “PENDING” and a notification is sent to the customer. To facilitate this, the following core components were modified:
*   **`ChargeTable`**: This is a new table that records charge attempts made on each invoice, since it may take multiple charges to fulfill. It is useful to keep a record of previous charge attempts in order to address customer disputes. This table has a corresponding model and service for interacting with the DAL.
*   **Subscription Cost**: The `Currency` enum was expanded to serve as a map from Currency to the cost of a subscription. I did this for a few reasons:
    *   It captures the relationship between cost and currency, which would otherwise be kept in a database of product offerings (out of scope).
    *   Invoices are created with a currency and amount that comes directly from the customer, mitigating the risk of a `CurrencyMismatchException`

Relevant sources: 
*   /bin/cron_monthly_billing
*   `io.pleo.antaeus.core.jobs.MonthlyBillingJobRunner`
*   `package io.pleo.antaeus.models.Currency`


#### WeeklySettlementJobRunner

The `WeeklySettlementJobRunner` iterates over invoices created in the current month that have a “PENDING” status, and attempts to charge them. If successful, the Invoice is set to “PAID”. When a charge fails, the invoice is left as “PENDING” and a notification is sent to the customer. In addition, if it is the last charge attempt of the month, the Invoice status is set to “FAILED”.

To facilitate this, the following assumptions were made:
*   **Error Handling**: This job was implemented as a simple retry strategy for failed charges.
*   **Failed Invoices**: The “FAILED” Invoice status may be used in the future to send additional messaging the customer or potentially suspend their account (out of scope)
*   **Timing**: The cron is configured to run only on the 7th, 14th, 21st, and 28th of the month

Relevant sources: 
*   /bin/cron_weekly_settlement
*   `io.pleo.antaeus.core.jobs.WeeklySettlementJobRunner`

**Considerations for Both JobRunners**
*   **Runtime**: Both jobs take a simple iterative approach to performing charges one at a time. For a large-scale system with 100K+ users, these jobs would take a considerable amount of time. In this case, the jobs could be run in parallel batches, either locally via an Executor of threads/coroutines, or on a distributed worker force. 
*   **Threading**: The BillingService makes use of the `thread()` function, which I think is pretty cute, but dangerous. I looked at how to specify a threadpool or executor to this function, but I didn't figure out if it's possible. In order to tune the perfomance of concurrent jobs and prevent an unbounded number of threads from spawning, I would refactor the BillingService to spawn threads via an Executor (e.g. ThreadPoolExector). 
*   **Errors**: When performing charges, all errors are handled in the same manner. Usually, Network errors would be retried with a more aggressive strategy that assumes the error is transient.
*   **Data Denormalization**: I built services and data retrieval without diving deeply into the “Exposed” framework. I avoided using database “JOIN”s so that the services retain single-responsibility and stay decoupled from a relational datastore.
*   **Sharing Code**: There may be an opportunity to share more logic between the job runners. With more time, I would explore making the `ChargeService` a dependency of the `InvoiceService`, so that the existence of failed charges is abstracted away behind invoices.
*   **Testing**: Only the services and JobRunners are unit tested. I would also normally write unit tests for the DAL in order to verify query correctness, but I decided to skip due to time constraints


### Production Readiness

Antaeus in its current state is not prepared for production. In general, I like to use the [“Twelve-Factor App”](https://12factor.net/) as a guide. Some preparations for production would include:
*   **Logging**: I did not take the time to setup a robust logging configuration. I have used log4j in the past, but did not explore setting it up for this project. It is good practice to emit logs and regularly archive them in a searchable, scalable datastore such as S3/Logstash.
*   **Metrics**: for this project, I think it would be beneficial to emit the following metrics:
    *   Webhook request latency and http status codes, for understanding the normal operating profile, performance tuning, and detecting outages.
    *   JobRunner runtime and success/error, for the same reason as above
    *   Third-party `PaymentProvider` request latency and status codes, for understanding the normal operating profile and detecting third-party outages.
*   **Integration Testing and CI**: The unit tests make heavy use of mocking, which is not sufficient for catching bugs that occur during the [interactions between modules](https://twitter.com/yogthos/status/951905438727057408?lang=en)). With more time, I would write an integration test suite that understands what the state of the system should look like before and after the jobs run. I would integrate with Stripe payment provider (`StripePaymentProvider`) and try out the `stripe-mock` library, a mock HTTP server that responds like the real Stripe API ([https://github.com/stripe/stripe-mock](https://github.com/stripe/stripe-mock)). I manage a Stripe integration at my current job, and have been wanting to use it for our integration tests.
