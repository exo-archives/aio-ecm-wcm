Summary

    * Status: PCLV display the original document's title
    * CCP Issue: N/A, Product Jira Issue: WCM-2905.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Go to ACME drive then create a new document (any type) and publish it, you now can see it in the News page
    * Back to edit content, edit the title and go to News page again, you will see the title is changed but the summary still fine.

Fix description

How is the problem fixed?

    *  Do not try getting the frozen node if the "nt:frozenNode" property is available.
    * Get the title normally, try getting the frozen node's title if the publish node's title is not available

Patch information:
Patch files: WCM-2905.patch


Tests to perform

Reproduction test
*

    * Go to ACME drive then create a new document (any type) and publish it, you now can see it in the News page
    * Back to edit content, edit the title and go to News page again, you will see the title is changed but the summary still fine.

Tests performed at DevLevel
*No

Tests performed at QA/Support Level
*No


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

    * Function or ClassName change

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment
* Support review : patch validated

QA Feedbacks
*

