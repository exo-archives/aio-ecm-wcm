Summary

    * Status: Impossible to add image from JCR in SCV
    * CCP Issue: CCP-416, Product Jira Issue: WCM-2815
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The image is uploaded from the local folder. The customer wants to upload images from JCR instead.

Fix description

How is the problem fixed?

    * Add a new listener for UIContentDialogForm
    * Fix displaying popup regression (IE).

Patch information:
Patch files:
WCM-2815.patch	  	

Tests to perform

Which test should have detected the issue ?
* This test could be complex and not complete from our side.

Is a test missing in the TestCase file ?
* Yes

Added UnitTest ?
* No

Recommended Performance test?
* No


Documentation changes

Documentation Changes:
*


Configuration changes

Is this bug changing the product configuration ?
* No

Will previous configuration continue to work?
* N/A


Risks and impacts

Is there a risk applying this bug fix ?
* No

Can this bug fix have an impact on current client projects?
No.

    * Function or ClassName change? Nothing.

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

