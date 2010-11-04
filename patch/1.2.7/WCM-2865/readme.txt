Summary

    * Status: Duplicated Id "UIPopupContainer"
    * CCP Issue: CCP-505, Product Jira Issue: WCM-2865.
    * Fixed by: WCM-2874.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The id "UIPopupContainer" is duplicated:

      <div id="UICLVPortlet" style="min-width:300px" class="UICLVPortlet"><span class="UIPopupContainer" id="UIPopupContainer"></span>

      <div id="UISingleContentViewerPortlet" style="min-width:300px" class="UISingleContentViewerPortlet"><span class="UIPopupContainer" id="UIPopupContainer"></span>

Fix description

How is the problem fixed?

    * Add time to the id of UIPopupContainer each time we create an instance of UIPopupContainer.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch file: WCM-2874.patch

    * Properties

Tests to perform

Reproduction test

    * Steps to reproduce:

   1. Go to ACME home page.
   2. Validate this page by using W3C validator => ERROR

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
*

Support Comment
* Validated

QA Feedbacks
*

