Summary

    * Status: Patch validated: Can not order content in clv manual browse
    * CCP Issue: CCP-424, Product Jira Issue: WCM-2818
    * Complexity: LOW
    * Impacted Client(s): CG95
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * An exception is raised if the current document is a symlink

Fix description

How is the problem fixed ?

    * Check if the current document is a symlink so we will interact with the real node

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
	 
File WCM-2818.patch 	 	  	

    * Properties

Tests to perform

Which test should have detected the issue ?
* Change a CLV's edit mode to manual, add some content and remove them after that.

Is a test missing in the TestCase file ?
* Yes

Added UnitTest ?
* No

Recommended Performance test?
* No
Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*
Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
* N/A

will previous configuration continue to work?
*N/A
Risks and impacts

Is there a risk applying this bug fix ?
* No

Can this bug fix have an impact on current client projects ?
* No

Is there a performance risk/cost?
* No
Validation By PM & Support

PM Comment
*

Support Comment
*
QA Feedbacks

Performed Tests
*

