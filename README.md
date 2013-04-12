custom-svc-msgs
===============

TeamCity plugin to allow a build to stop and/or de-queue the remaining builds in its project.

Synopsis 
------
Given a project with several build configurations, and a collection of builds queued and running builds, the first build to succeed stops the remainder of the queued or running builds, with appropriate messages in its log.

Background
------
An FPGA fitting run may need to execute several times (with slightly differing parameters) before finding a solution.

 
Given that each attempt can chew up a lot of disk space and may take up to several hours to succeed, it makes sense to run several attempts at the same time, so that they may run unattended and in parallel.  Supposing a pool of several agents assigned to my FPGA project, and supposing a build configuration for each FPGA run I would like to attempt, I can configure and launch all the build configurations at once, and have the first successful attempt stop all the others (or remove them from the queue if they've not yet started). 


Overview
------
The **custom-svc-msgs** plugin listens via the [ServiceMessageTranslator](http://javadoc.jetbrains.net/teamcity/openapi/current/jetbrains/buildServer/messages/serviceMessages/ServiceMessageTranslator.html) interface for messages sent according to the [TeamCity Service Message API](http://confluence.jetbrains.com/display/TCD7/Build+Script+Interaction+with+TeamCity).  When a build finishes, it writes a message to STDOUT something like this: `##teamcity[stop_builds_in_project 'queued,running']`.  The ServiceMessageTranslator's `translate()` callback receives a handle to the running build; from there it figures out its project, then the project's builds that are queued and/or running, and then de-queues and stops those builds as necessary.

Features
------
* `translate()` returns a list of messages.  If this list is non-empty, those messages are handled by TeamCity.  **custom-svc-msgs** uses this behavior to display information in the build log about which builds have been stopped and de-queued. 
* The build that sends the 'stop_builds_in_project' message knows not to stop itself.
* If you want to only stop running builds, or only stop queued builds, remove the appropriate string from the message.
* If you want to launch another instance of the same configuration (but only one additional--see Limitations below), you can use the ellipsis beside the run button to customize the parameters for that build, and click "Run Build".  **custom-svc-msgs** will handle stopping/de-queueing this build in the same way as all the others.
* Builds associated with other projects are not touched by **custom-svc-msgs**.

Limitations
------
* For a given configuration, TeamCity only allows one running and one queued instance; you may not queue up multiple instances of the same build configuration.


Building the plugin and the .zip
------
Type "ant".  The zip will be created as dist/custom-svc-msgs.zip.


Debugging
------
* **custom-svc-msgs** writes output to the TeamCity server log by instantiating a log4j logger with category **Loggers.SERVER_CATEGORY**.


Installing/updating the plugin
------
1. Copy the plugin .zip to the <data-root>/plugins directory.
1. Stop TeamCity.
1. If you are not seeing the updated code between restarts, you may need to remove the web container's work directory.
1. Restart TeamCity.
