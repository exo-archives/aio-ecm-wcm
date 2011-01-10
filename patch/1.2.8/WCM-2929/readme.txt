Summary

    * Status: Labels in French
    * CCP Issue: Product Jira Issue: WCM-2929.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
After WCM-2879 and PORTAL-3829, when changing language to French, some labels are still in English.
- Edit Content List Viewer
- Newsletter
- Acme->Contact Us
- Form generator

Fix description

How is the problem fixed?
* Add new resource bundle keys
* Correct label invocations in groovy templates

Patch file: WCM-2929.patch

Tests to perform

Reproduction test
* Review all labels in AIO 1.6.7

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

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
* Function or ClassName change: none

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated by PM

Support Comment
* Patch validated

QA Feedbacks
*

