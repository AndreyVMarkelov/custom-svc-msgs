package com.cmpny.custom_svc_msgs;

import java.util.*;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTranslator;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.users.User;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Retrieve Custom Service Messages
 *
 * @author Chris Walquist
 */
public class CustomServiceMessages implements ServiceMessageTranslator {
    private final Logger LOG = Logger.getLogger(Loggers.SERVER_CATEGORY);

    public CustomServiceMessages() {
    }

    @NotNull
    public String getServiceMessageName() {
        //LOG.info("Registered to be notified of the message name 'stop_builds_in_project'");
        return "stop_builds_in_project";
    }

    // The notifier callback.
    @NotNull
    public List<BuildMessage1> translate(SRunningBuild build, BuildMessage1 originalMessage, ServiceMessage serviceMessage) {
        SProject project = build.getBuildType().getProject();
        long myId = build.getBuildId();
        User myUser = build.getTriggeredBy().getUser();
        LOG.info(myId + ": ServiceMessageTranslator received message: " + serviceMessage);

        List<SBuildType> buildTypes = project.getBuildTypes();

        List<BuildMessage1> buildMessages = new ArrayList<BuildMessage1>();

        // For all build configurations in the project...
        Iterator<SBuildType> buildTypeIterator = buildTypes.iterator();
        while (buildTypeIterator.hasNext()) {
            SBuildType buildInProject = buildTypeIterator.next();

            if (serviceMessage.getArgument().toLowerCase().contains("queued")) { // Dequeue all queued instances of this build config...
                List<SQueuedBuild> queuedBuilds = buildInProject.getQueuedBuilds(null);
                Iterator<SQueuedBuild> i = queuedBuilds.iterator();
                while (i.hasNext()) {
                    SQueuedBuild qb = i.next();
                    _log(buildMessages, originalMessage, myId + ": FOUND QUEUED BUILD, dequeueing... " + qb.toString());
                    qb.removeFromQueue(myUser, "Dequeued by successful instance of build " + build.getBuildType().getName() + "', BuildId=" + myId);
                }
            }

            if (serviceMessage.getArgument().toLowerCase().contains("running")) { // Stop all running instances of this build config...
                List<SRunningBuild> runningBuilds = buildInProject.getRunningBuilds(null);
                Iterator<SRunningBuild> j = runningBuilds.iterator();
                while (j.hasNext()) {
                    SRunningBuild rb = j.next();
                    if (rb.getBuildId() == myId) {
                        //LOG.info("(Not stopping build with BuildId=" + myId + ", That's me!)");
                        continue;
                    }
                    _log(buildMessages, originalMessage, myId + ": FOUND '" + rb.getBuildType().getName() + "' (" + rb.toString() + "), stopping...");
                    rb.stop(myUser, "Stopped by successful completion of build '" + build.getBuildType().getName() + "', BuildId=" + myId);
                }
            }
        }
        return buildMessages;
    }

    void _log(List<BuildMessage1> msgs, BuildMessage1 origMsg, String message) {
        LOG.info(message);
        msgs.add(new BuildMessage1(origMsg.getSourceId(), origMsg.getTypeId(), Status.NORMAL, new Date(), message));
    }
}
