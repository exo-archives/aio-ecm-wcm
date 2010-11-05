Summary

    * Status: Problem in UIJCRExplorer when using SessionLeakDetector
    * CCP Issue: CCP-530, Product Jira Issue: WCM-2932.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When we enable session leak detector in WCM/AIO, we see the exceptions in the console. That is mean we have a memory leak here.

Fix description

How is the problem fixed?

    * Add try/catch block to logout sessions
    * Remove the unused method (isPreferenceNode() method. This method is useless when we switch to use link management system).
    * Modify the method getCurrentWorkspace() (return the workspace name of current drive instead of getting workspace from current node which doesn't make sense).

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test

   1. Enable session leak detector by adding

      JCR_SESSION_TRACK="-Dexo.jcr.session.tracking.active=true -Dexo.jcr.jcr.session.tracking.maxage=60" 
      JAVA_OPTS="$JCR_SESSION_TRACK $JAVA_OPTS $LOG_OPTS $SECURITY_OPTS $EXO_OPTS $EXO_CONFIG_OPTS $REMOTE_DEBUG"

      in eXo.sh
   2. Start WCM and access to Site management drive.
   3. Wait for 60 seconds. Or take some action such as Add document, Add folder.
      Observation: the exception appears in the server console.

Tests performed at DevLevel

    * As above, no exception appears.

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No
Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None.

Is there a performance risk/cost?
* No.
Validation (PM/Support/QA)

PM Comment
* VALIDATED BY PM

Support Comment
* Proposed patch validated

QA Feedbacks
*

