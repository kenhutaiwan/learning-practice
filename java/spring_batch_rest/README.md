# spring-batch-rest

[![Build Status](https://travis-ci.org/chrisgleissner/spring-batch-rest.svg?branch=master)](https://travis-ci.org/chrisgleissner/spring-batch-rest)
[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/chrisgleissner/spring-batch-rest-api/maven-metadata.xml.svg)](https://search.maven.org/artifact/com.github.chrisgleissner/spring-batch-rest-api)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/spring-batch-rest/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/spring-batch-rest?branch=master)

REST API for <a href="https://spring.io/projects/spring-batch">Spring Batch</a>, based on Spring MVC and Spring Boot 2.

## Features
- Get information on jobs, job executions, and Quartz schedules
- Start job execution (synchronous or asynchronous) with optional job property overrides. The job properties can
either be obtained via a custom API or via standard Spring Batch job parameters, accessible from <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/scope/StepScope.html">step-scoped beans</a>.



## Live Demo

Check out the <a href="https://spring-batch-rest.herokuapp.com/swagger-ui.html">live demo</a> of this project's Swagger UI. This demo is automatically updated whenever the repo's master branch changes. Please note that it may take up to 30s for this Heroku app to perform a cold start after it has not been used for a while.

Here's how to run a <a href="https://github.com/chrisgleissner/spring-batch-rest/tree/master/example/src/main/java/com/github/chrisgleissner/springbatchrest/example/PersonJobConfig.java">sample job<a>:
* Click on `Spring Batch Job Executions`, then on `POST`. Now click on `Try it Out` on the right-hand side. Replace the contents of the `Example Value` input field with `{"name": "personJob"}` and press `Execute`. 
* The job has now been triggered. When it completes, you'll get a response body similar to:
```
{
  "jobExecution": {
    "id": 1,
    "jobId": 1,
    "jobName": "personJob",
    "startTime": "2018-12-23T18:19:13.185",
    "endTime": "2018-12-23T18:19:13.223",
    "exitCode": "COMPLETED",
    "exitDescription": "",
    "status": "COMPLETED",
    "exceptions": []
  },
  "_links": {
    "self": {
      "href": "https://spring-batch-rest.herokuapp.com/jobExecutions/1"
    }
  }
}
```
* You can now view this and other recently completed jobs via the <a href="https://spring-batch-rest.herokuapp.com/jobExecutions?jobName=personJob&limitPerJob=3">job execution overview<a>:
```
{
  "_embedded": {
  "jobExecutionResourceList": [
    {
    "jobExecution": {
      "id": 1,
      "jobId": 1,
      "jobName": "personJob",
...
```

## Getting Started

To integrate the REST API in your Spring Boot project, first add a dependency for Maven:

```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>spring-batch-rest-api</artifactId>
    <version>1.2.7</version>
</dependency>
```

or Gradle:
```
implementation 'com.github.chrisgleissner:spring-batch-rest-api:1.2.7'
```

Then add `@EnableSpringBatchRest` to your Spring Boot application class, for <a href="https://github.com/chrisgleissner/spring-batch-rest/blob/master/example/src/main/java/com/github/chrisgleissner/springbatchrest/example/SpringBatchRestSampleApplication.java">example</a>:
```java
@SpringBootApplication
@EnableSpringBatchRest
public class SpringBatchRestSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestSampleApplication.class, args);
    }
}
```

To see this example in action, run
```text
mvn clean install
java -jar example/target/*.jar
```
and then check the Swagger REST API docs at 
<a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a>.


## REST Endpoints

The following REST endpoints are available:

### Jobs

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobs                  | All jobs  |
| GET          | /jobs/{jobName}        | Single job  |

### Job Executions

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobExecutions         | Latest 3 executions for each job, sorted by descending end time (or start time if still running) |
| GET          | /jobExecutions/{id}    | Single job execution |
| POST         | /jobExecutions         | Start job execution with optional property overrides |

#### Request Parameters for GET `/jobExecutions` 

| Parameter | Default Value | Description |
|-----------|---------------|-------------|
| jobName | empty | <a href="https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html">Regular expression</a> of the job names to consider. If empty, all job names are used. |
| exitCode | empty | Exit code of the job execution. Can be `COMPLETED`, `EXECUTING`, `FAILED`, `NOOP`, `STOPPED` or `UNKNOWN` as per <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/ExitStatus.html">ExitStatus</a>. If empty, all exit codes are used. |
| limitPerJob | 3 | Maximum number of job executions to return for each job. |

#### Examples

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobExecutions?limitPerJob=50  | Latest 50 executions for each job |
| GET          | /jobExecutions?jobName=foo&exitCode=FAILED | Latest 3 failed executions for 'foo' job |
| GET          | /jobExecutions?jobName=foo.*&exitCode=FAILED&limitPerJob=10 | Latest 10 failed executions for jobs with a name starting with 'foo' |

### Quartz Schedules

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobDetails            | All Quartz schedules   |
| GET          | /jobsDetails/{quartzGroupName}/{quartzJobName}  | Single Quartz schedule |

## Error Handling

Where possible, subclasses of the Spring Batch <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/JobExecutionException.html">JobExecutionException</a>
are mapped to an appropriate HTTP status code and the response body contains further details. 

For example, trying to start a nonexistent job results in a response with a 404 status code and the following response body:
```
{
  "status": "404 NOT_FOUND",
  "message": "No job configuration with the name [foo] was registered",
  "exception": "NoSuchJobException",
  "detail": "Failed to start job 'foo' with JobConfig(name=foo, properties={foo=baz10}, asynchronous=false). Reason: No job configuration with the name [foo] was registered"
}
```

## Configuration

The default behaviour of the REST API can be tweaked via several Spring properties which can be placed in `application.properties`.

### Job Execution Caching

`com.github.chrisgleissner.springbatchrest.jobExecutionCacheSize` (default: 100)

For performance reasons, `/jobExecutions` queries are performed against an in-memory cache of recent 
job executions. If the `limitPerJob` request parameter is larger than the value of this property, the cache is bypassed and the
Spring Batch <a href="https://docs.spring.io/spring-batch/4.0.x/api/index.html?org/springframework/batch/core/explore/JobExplorer.html">JobExplorer</a> is used instead.

Large `jobExecutionCacheSize` values will result in increased heap use, but small values combined with large `limitSize` request parameters
will cause increased REST query latencies. Thus, if you increase this property value, you may also want to increase your heap size. 

The cache only contains job executions since the Spring context creation, ie. it is not warmed up from the JobExplorer and the DB this may rely on. If you want to be able to query job executions that were performed earlier, eg. during a prior JVM execution, you may want to disable caching. To do so, simply set the property to 0.


### Repeated Job Execution

`com.github.chrisgleissner.springbatchrest.addUniqueJobParameter` (default: true)

Spring Batch prevents repeated invocations of a job unless you use different properties (aka job parameters) each time. To bypass this, a unique property (ie. a random UUID) is added to each job invocation. You can disable this by setting the property to false. 



## Job Property Overrides

Properties can be overridden when starting a job via REST. These overrides can then be accessed from a job either via:
```java
@Bean
ItemWriter<Object> writer() {
    return new ItemWriter<Object>() {
        @Override
        public void write(List<?> items) throws Exception {
           String prop = JobPropertyResolvers.JobProperties.of("jobName").getProperty("propName");
           // ...
        }
    }
}
```
or alternatively by using `@StepScope`-annotated beans:
```java
@StepScope
@Bean 
ItemWriter<Object> writer(@Value("#{jobParameters['propName']}") String prop) {
    // ... 
}
```

If a property is not overridden, it is resolved against the Spring environment. All overrides are reverted on job completion.

## Utilities

The <a href="https://github.com/chrisgleissner/spring-batch-rest/tree/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util">util module</a> contains code for registering, starting and scheduling jobs:

[JobBuilder](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/JobBuilder.java) builds a simple job based on a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Runnable.html">Runnable</a>:

```java
Job job = jobBuilder.createJob("jobName", () -> System.out.println("Running job"));
```

[AdHocScheduler](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/AdHocScheduler.java) registers and triggers a job using a Quartz CRON trigger. This can be performed at 
run-time rather than Spring wiring time which allows for simplified set-up of a large number of jobs that only 
differ slightly:

```java
adHocScheduler.schedule("jobName", job, "0/30 * * * * ?");
```

[AdHocStarter](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/AdHocStarter.java) is similar to AdHocScheduler, but used for immediately starting a job:

```java
adHocStarter.start("jobName", job);

```
