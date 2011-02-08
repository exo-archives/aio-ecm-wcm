Summary

    * Status: Problem when deleting a drive in Sites Management which was created in Manage Sites
    * CCP Issue: N/A, Product Jira Issue: WCM-2938.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. In Site Administrator
   2. Create site (e.g: AAA)
   3. Go to Site Explorer
   4. Choose Site Management
   5. Delete AAA folder
   6. Return to Sites Explorer, AAA drive exists in General Drives: not OK
   7. Return to Manage Sites, AAA site still exists: not OK
   8. Delete AAA site
      Exceptions:
      ERROR [portal:UIPortalApplication] Error during the processAction phase
      javax.jcr.PathNotFoundException: Node not found /sites content/live/aaa
      at org.exoplatform.services.jcr.impl.core.NodeImpl.getNode(NodeImpl.java:195)
      at org.exoplatform.services.wcm.portal.impl.LivePortalManagerServiceImpl.getLivePortal(LiveP
      ortalManagerServiceImpl.java:102)
      at org.exoplatform.services.wcm.portal.impl.LivePortalManagerServiceImpl.getLivePortal(LiveP
      ortalManagerServiceImpl.java:78)
      ...s

Fix description

How is the problem fixed?

    * The portal site is deleted in the wrong manner when all of its contents are removed in Site Management. 
    * The solution is to handle exceptions and clear out history of the deleted site.

Patch files: WCM-2938-DMSpart.patch, WCM-2938-WCMpart.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

