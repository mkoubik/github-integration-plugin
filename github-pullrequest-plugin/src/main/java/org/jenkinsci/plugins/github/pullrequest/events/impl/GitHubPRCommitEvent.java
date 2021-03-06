package org.jenkinsci.plugins.github.pullrequest.events.impl;

import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.github.pullrequest.GitHubPRCause;
import org.jenkinsci.plugins.github.pullrequest.GitHubPRPullRequest;
import org.jenkinsci.plugins.github.pullrequest.GitHubPRTrigger;
import org.jenkinsci.plugins.github.pullrequest.events.GitHubPREvent;
import org.jenkinsci.plugins.github.pullrequest.events.GitHubPREventDescriptor;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jenkinsci.plugins.github.pullrequest.utils.ObjectsUtil.isNull;

/**
 * Triggers build when commit hash changed

 * @author Kanstantsin Shautsou
 */
public class GitHubPRCommitEvent extends GitHubPREvent {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubPROpenEvent.class);
    private static final String DISPLAY_NAME = "Commit changed";

    @DataBoundConstructor
    public GitHubPRCommitEvent() {
    }

    @Override
    public GitHubPRCause check(GitHubPRTrigger gitHubPRTrigger, GHPullRequest remotePR,
                               GitHubPRPullRequest localPR, TaskListener listener) throws IOException {
        if (remotePR.getState().equals(GHIssueState.CLOSED)) {
            //TODO check whether push to closed allowed?
            return null; // already closed, nothing to check
        }

        if (isNull(localPR)) { // new
            return null; // not interesting for this event
        }

        GitHubPRCause cause = null;

        GHCommitPointer head = remotePR.getHead();
        if (!localPR.getHeadSha().equals(head.getSha())) {
            LOGGER.debug("New commit. Sha: {} => {}", localPR.getHeadSha(), head.getSha());
            final PrintStream logger = listener.getLogger();
            logger.println(this.getClass().getSimpleName() + ": new commit found, sha " + head.getSha());
//            GHUser user = head.getUser();
            cause = new GitHubPRCause(remotePR, DISPLAY_NAME, false);
        }

        return cause;
    }

    @Extension
    public static class DescriptorImpl extends GitHubPREventDescriptor {
        @Override
        public final String getDisplayName() {
            return DISPLAY_NAME;
        }
    }
}
