Summary

    * Status: Portlet Category Navigation
    * CCP Issue: CCP-465, Product Jira Issue: WCM-2845
    * Complexity: LOW
    * Impacted Client(s): Bull-Sevices, CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * The category is deleted but portlet Category Navigation still trying to access.
    * Incorrect translation

Fix description

How the problem is fixed ?

    * Check the category is existed or not before trying to access.
    * Show the message for user

Patch informations:
Patches files:
WCM-2845.patch


Tests to perform

Which test should have detect the issue ?
* Delete the category and try select it in Category Navigation portlet.

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

Previous configuration will continue to work?
* N/A


Risks and impacts

Is there a risk applying this bug fix ?
* This patch should be applied with the patch in the issue WCM-2753

Is this bug fix can have an impact on current client projects ?
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

