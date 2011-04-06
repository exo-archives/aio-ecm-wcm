Summary

    * Status: Impossible to change to another page of CLV portlets
    * CCP Issue: CCP-877, Product Jira Issue: WCM-2944.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Impossible to change to another page of CLV portlets

Fix description

How is the problem fixed?

    * Set id for UI Components when calling addChild() function instead of concatenating strings in Groovy template file.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2944.patch

Tests to perform

Reproduction test
* Steps to reproduce:
1. Create and publish some documents in Site Management/acme/documents. 
2. In FO, impossible to change to another page of Documents, a message is shown: "The blockId to update is not found: UICLVPresentation". 
   When you click OK in the first alert, a second alert is displayed with the following message:
   "The blockId to update is not found: UICLVPresentation"
  The same problem with any CLV portlets.

Tests performed at DevLevel
* Do the same tasks like Reproduce Test => popup is disappeared, CLV is turned over => OK

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated by PM

Support Comment
* Proposed patch validated

QA Feedbacks
*

