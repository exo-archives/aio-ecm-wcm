Summary

    * Status: Add page wizards: some labels are not translated into French
    * CCP Issue: CCP-720, Product Jira Issue: WCM-2957.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Go to Site Editor -> Add/Edit Page Wizard
In the second step of page creation, labels of "Select a Page Layout Template" are not translated.

Fix description

How is the problem fixed?
    * Replace hard-coded labels by resource keys in PageConfigOptions.groovy
    * Add the missing resource bundles

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2957.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no

Is there a performance risk/cost?
* no

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Proposed patch validated

QA Feedbacks
*

