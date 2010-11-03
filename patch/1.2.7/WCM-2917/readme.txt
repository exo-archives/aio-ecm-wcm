Summary

    * Status: SCV: Error when adding content which extends from both exo:webContent and other node types
    * CCP Issue: CCP-601, Product Jira Issue: WCM-2917.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Add a content which extends from both exo:webcontent and other node types.
The following error stack trace is as follows.

[ERROR] portal:UIPortalApplication - Error during the processAction phase <javax.jcr.nodetype.ConstraintViolationException: Mandatory item exo:linkURL not found. Node [/sites content/live/classic/web contents/frfrff primary type: exo:pictureOnHeadWebcontent]>javax.jcr.nodetype.ConstraintViolationException: Mandatory item exo:linkURL not found. Node [/sites content/live/classic/web contents/frfrff primary type: exo:pictureOnHeadWebcontent] at org.exoplatform.services.jcr.impl.core.NodeImpl.validateMandatoryChildren(NodeImpl.java:2525) at org.exoplatform.services.jcr.impl.core.SessionDataManager.validateMandatoryItem(SessionDataManager.java:1188) at org.exoplatform.services.jcr.impl.core.SessionDataManager.validate(SessionDataManager.java:1090) at org.exoplatform.services.jcr.impl.core.SessionDataManager.commit(SessionDataManager.java:1002) at org.exoplatform.services.jcr.impl.core.ItemImpl.save(ItemImpl.java:571) at org.exoplatform.services.jcr.impl.core.SessionImpl.save(SessionImpl.java:908)
...

Problem analysis
This error is caused by the fact that mandatory properties of the added node type are not introduced when creating SCV. Content is created before being edited in the SCV.

    * One from the following behaviors is expected by the customer:

   1. In UINameWebContentForm, getListFileType() is selecting templates that extend from exo:webcontent to be displayed. This criterion should be changed in order to not display content which extends from other node types besides exo:webcontent when creating SCV.
   2. Insert values for mandatory fields when creating content (As it is the case for the property name for the existing contents)
   3. Create content after editing it.

Fix description

How is the problem fixed?

    * When creating content, insert values for mandatory fields.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2917.patch

Tests to perform

Reproduction test

   1. Create a template extending from both exo:webContent and other node types like exo:link or exo:linkurl. 
      For example, add <supertype>exo:link</supertype> to exo:pictureOnHeadWebcontent (in wcm-ext-nodetypes.xml of portal.war)
   2. Create an SCV. Add new content. Choose the previously defined template (picture on head layout webcontent)
   3. Save

Tests performed at DevLevel
* Cf. above

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

    * Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment

    * PATCH VALIDATED BY PM

Support Comment

    * Proposed patch validated by Support Team

QA Feedbacks
*

