Summary

    * Status: Impossible to hide Admin bar for a user group
    * CCP Issue: CCP-665, Product Jira Issue: WCM-2933.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Once a registered user logs in, Site Administration bar is rendered immediately.
  The customer wants to hide this tool bar for platform/users group. 

Fix description

How is the problem fixed?
* Remove the access permission to the Admin toolbar portlet of regular users (who belong only to platform/users group)

Patch file: WCM-2933.patch

Tests to perform

Reproduction test
* See problem description

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
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Patch validated by the Support team

QA Feedbacks
*
