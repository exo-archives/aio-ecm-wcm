Summary

    * Status: No attribute "target" in "ACME Overview" when checking with W3C
    * CCP Issue: CCP-668, Product Jira Issue: WCM-2939.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * No attribute "target" in "ACME Overview" when checking with W3C

Fix description

How is the problem fixed?

    * Use onclick() function of JavaScript to open a linked document instead of using target attribute.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test

   1. Go to ACME Overview
   2. Validate this page by using W3C validator: ERROR
      There is no attribute X:

      Line 2110, column 312: there is no attribute "target"
      ...acme/documents/conditions.doc" target="_newdoc">Conditions and Requirements</a...

Tests performed at DevLevel

   1. Do the same steps like Reproduction test
   2. Validate this page => have no error with "target attribute"

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
* Validated by TL on behalf of PM.

Support Comment
* Support review: patch validated

QA Feedbacks
*

