Summary

    * Status: Patch validated: Problem of display when edit CLV
    * CCP Issue: CCP-425, Product Jira Issue: WCM-2819
    * Complexity: LOW
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * Missing a tr and a td tag inside a table

Fix description

How the problem is fixed ?

    * Insert missing tr and td tag in the template.

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch files:
 	 
File WCM2819.patch 	 	  	

    * Properties

Tests to perform

Which test should have detected the issue ?
* Try to edit page and see the CLV

Is a test missing in the TestCase file?
* Yes

Added UnitTest ?
* No

Recommended Performance test?
* No
Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*
Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
* N/A

Will previous configuration continue to work?
* N/A
Risks and impacts

Is there a risk applying this bug fix ?
* No

Can this bug have an impact on current client projects ?
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

