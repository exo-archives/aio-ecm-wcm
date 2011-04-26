Summary

    * Status: Quick edit icons of portlet in column container lost when editing page using IE7
    * CCP Issue: CCP-590, Product Jira Issue: WCM-2948.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Steps to reproduce:
    * Login as root
    * Go to Acme site; create new page AAA
    * Add 1 column container to page. Add 1 Content Detail portlet to each container.
    * Add 1 other Content Detail portlet outside the column container => then save.
    * Go to page AAA and switch to edit mode.


The problem happens from here:

   1. Resize windows => quick edit icon of the portlets in column container disappears.
   2. Pass mouse over 3 buttons Portlet Mode, Minimize, Maximize (in top-right corner) of the portlets in column container => the quick edit icon of these portlets appears again.
   3. Pass mouse over 3 buttons Portlet Mode, Minimize, Maximize of the portlets outside column container => the quick edit icon of portlets inside column container disappears.

This behavior occurs also with portlets in column container of mixed container.

Fix description

How is the problem fixed?

    * Add position:relative to the UIPortal div element

Patch file: WCM-2948.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * cf. above

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

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Validated

Support Comment

    * Validated

QA Feedbacks
*

