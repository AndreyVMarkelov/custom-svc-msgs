custom-svc-msgs
===============

TeamCity plugin to allow a build to stop and/or de-queue the remaining builds in its project.

Synopsis 
------
Given a project with several build configurations, and a collection of builds queued and running builds,
the first build to succeed stops the remainder of the queued or running builds, with appropriate messages in its log.

Background
------
An FPGA fitting run may need to execute several times (with slightly differing parameters) before finding a solution.

Given that each attempt can chew up a lot of disk space and may take up to several hours to succeed, it makes
sense to run several attempts at the same time, so that they may run unattended and in parallel.  Supposing a
pool of several agents assigned to my FPGA project, and supposing a build configuration for each FPGA run I
would like to attempt, I can configure and launch all the build configurations at once, and have the first
successful attempt stop all the others (or remove them from the queue if they've not yet started). 

Overview
------
The **custom-svc-msgs** plugin listens via the [ServiceMessageTranslator](http://javadoc.jetbrains.net/teamcity/openapi/current/jetbrains/buildServer/messages/serviceMessages/ServiceMessageTranslator.html)
interface for messages sent to the [TeamCity Service Message API](http://confluence.jetbrains.com/display/TCD7/Build+Script+Interaction+with+TeamCity).
When a build finishes, it writes a message to STDOUT something like this: `##teamcity[stop_builds_in_project 'queued,running']`.
The Service Message system intercepts it (it doesn't show up in the log) and routes to the `translate()`
callback of the ServiceMessageTranslator registered for `stop_builds_in_project` messages, passing a
handle to the running build, from which is retrieved the project, then all the project's
queued and/or running builds.  `translate()` de-queues and stops those builds as necessary (except for the build
that messaged it).

Features
------
* **custom-svc-msgs** displays information in the build log about which builds have been stopped and de-queued.
This is done via a list of messages returned from `translate()`, which are handled by TeamCity's Service Message API; the API
provides for writing messages to the build log.
* To only stop running builds, or only cancel queued builds, remove either 'running' or 'queued' from the message
(the delimiter doesn't matter, nor the case).
* To launch another instance of the same configuration (but only one additional--see Limitations below),
use the ellipsis next to the run button to customize the parameters for that build, then click "Run Build".
**custom-svc-msgs** will handle stopping/de-queueing this build in the same way as all the others.
* Builds associated with other projects are not touched by **custom-svc-msgs**.

Limitations
------
* For a given configuration, TeamCity only allows one queued instance; you may not queue up multiple instances of the same build configuration. (The maximum number of simultaneously running instances, however, can be set in the build config).


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
