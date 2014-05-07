# Thundr Deferred

Deferred task execution for [Thundr](http://3wks.github.io/thundr/)

[![Build Status](https://travis-ci.org/kuhnza/thundr-contrib-deferred.png)](https://travis-ci.org/kuhnza/thundr-contrib-deferred)

## Why

Here are a few reasons why you might want to use Thundr Deferred:

1. Execute long running tasks asynchonously (e.g. send an email, run a report)
2. Distribute load across a pool of worker machines
3. Retry tasks that fail 

## Dependencies

Thundr Deferred depends on the [Thundr Quartz](https://github.com/kuhnza/thundr-contrib-quartz) module for its queue monitor implementation.

## Usage

In your `ApplicationModule`:
```java
@Override
public void requires(DependencyRegistry dependencyRegistry) {
    super.requires(dependencyRegistry);

    dependencyRegistry.addDependency(QuartzModule.class);
    dependencyRegistry.addDependency(DeferredModule.class);
}
```
Take care to add the DeferredModule entry after the Thundr Quartz entry however otherwise you'll get an error when starting your application complaining that QuartzScheduler isn't in your injection context. 

With the module added you'll now have access to the DeferredTaskService from your injection context which means it will
be injected into any object that declares it as a dependency in its constructor.

Imagine now we've got a controller which subscribes users to a newsletter. Once subscribed we want to immediately send 
the user a welcome email. This operation is a bit slow to do synchronously within the request so let's defer it.

Here's our controller:

```java
import com.threewks.thundr.deferred.DeferredTaskService;

public class MyController {
  private DeferredTaskService deferredTaskService;
  
  public MyController(DeferredTaskService deferredTaskService) {
    this.deferredTaskService = deferredTaskService;
  }
  
  public JspView subscribe(String firstName, String lastName, String email) {
    // Perhaps do some databasey stuff to create the user etc
    
    // Run our asynchronous email task
    deferredTaskService.defer(new SendWelcomeEmailTask(firstName, lastName, email));
    
    // Return a success page to the user
    return new JspView("success.html")
  }
}
```

As you can see it's as simple as creating a new task object and passing it to `.defer()`.

So what does our task object look like? Well tasks are pretty simple things to make. All that's required is that you
implement the [DeferredTask] interface like so:

```java
import com.threewks.thundr.deferred.task.DeferredTask;

public class SendWelcomeEmailTask implements DeferredTask {
  private String firstName;
  private String email;
  
  public SendWelcomeEmailTask(String firstName, String email) {
    this.firstName = firstName;
    this.email = email;
  }
  
  public void run() {
    sendEmail(email, "Welcome!", "Hi " + firstName + ",\nWelcome to our service :-)");  
  }
  
  private void sendEmail(String to, String subject, String body) {
    // Some nasty Java Mail API code here...
  }
}
```

Hopefully you can see that it's really easy to create new tasks and defer them.

## How it works

"What is this dark magic?" you ask? Behind the scenes Thundr Deferred makes use of message queues to serialize task 
requests and their attached data. A queue monitor (powered by Thundr Quartz) polls the queue at a defined interval
(defaults to 5 seconds) for new tasks to process. Since quartz is triggering processing of new tasks, tasks are 
run inside quartz' thread pool (i.e. outside of the current request).

Another upside of using a queue is that task processing can very easily be distributed amongst a cluster of nodes.
Provided that the queue is external (i.e. not in memory) nodes will compete to pull messages off the queue in a 
first come, first serve fashion.

### A note on serialization

Thundr Deferred makes use of the [GSON](https://code.google.com/p/google-gson/) library for serializing and 
deserializing tasks on and off the queue. Consequently you should check out the [GSON usage docs](https://sites.google.com/site/gson/gson-user-guide#TOC-Using-Gson) if you have any questions about what can and can't be serialized.

For convenience we have included date/time convertors for proper serialization of Joda DateTime objects.

## Queue Monitors

There is only one queue monitor supported: Thundr Quartz. Should you require something different raise a pull request
or a ticket. In the event that you choose to implement your own this can be configured via the `deferredQueueMonitor` 
property in your `application.properties` file like so:

```ini
deferredQueueMonitor=com.threewks.thundr.deferred.QuartzQueueMonitor
```

## Queue Providers

At present two queue providers are included:

* In memory
* Amazon Simple Queue Service

Support for additional queue providers is planned however this will be subject to demand. If you have a specific need
please feel free to send a pull request or raise a ticket.

The queue provider implementation can be configured via the `deferredQueueProvider` property in your 
`application.properties` file like so.

```ini
deferredQueueProvider=com.threewks.thundr.deferred.provider.InMemoryQueueProvider
```

### In memory queue provider

For simplicity sake this is the default queue provider. You probably shouldn't use this for all but the most trivial use
cases however as messages posted to this queue are only as reliable and persistent as the node its running on. Also you
lose any potential benefits relating to distributed task execution as memory tasks can only be read by the local node.

### Amazon Simple Queue Service

As the name suggests this queue provider integrates with Amazon's [Simple Queue Service](http://aws.amazon.com/sqs/) 
(SQS). 

#### Configuration

The SQS queue provider supports the following additional configuration items in your application.properties

* deferredSqsAccessKey - your AWS API access key
* deferredSqsSecretKey - your AWS API secret key
* deferredSqsRegion    - The region your queue is deployed in (e.g. us-east-1)
* deferredSqsQueueName - Your queue name (defaults to thundr-deferred-[env] where env is your environment name)

[DeferredTask]: https://github.com/kuhnza/thundr-contrib-deferred/blob/master/src/main/java/com/threewks/thundr/deferred/task/DeferredTask.java
