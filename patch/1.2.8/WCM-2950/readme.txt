Summary

    * Status: Align problem in acme/News page
    * CCP Issue: N/A, Product Jira Issue: WCM-2950.
    * Complexity: trivial

The Proposal
Problem description

What is the problem to fix?

    * Go to acme/News page
    * In WCM 1.2.6/AIO 1.6.6: Category Content portlet displays in the right of category tree. 
    * In WCM 1.2.7/AIO-1.6.7: Category Content portlet displays under the category tree. 

Fix description

How is the problem fixed?

    * Add some style sheet to set float and margin.

Patch file: WCM-2950.patch

Tests to perform

Tests performed at DevLevel
* The same steps as in the problem description

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* None

Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No side effect.

Is there a performance risk/cost?
* No, there is no risk.

Validation (PM/Support/QA)

PM Comment
* Patch validated by PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

