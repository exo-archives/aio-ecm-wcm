Summary

    * Status: CLV, SCV when browsing directories with special characters (using IE7)
    * CCP Issue: CCP-422, Product Jira Issue : WCM-2816
    * Complexity: LOW
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * in IE7, an exception occurs when browsing directories with name containing special characters such as é, à in a CLV or an SCV  

Fix description

How the problem is fixed ?

    * Encode the special characters before sending the request  

Patch informations:
Patches files:
WCM-2816.patch 	

Tests to perform

Which test should have detect the issue ?
*

ie7 should be used to reproduce the problem:

1- In file explorer, choose Acme drive then add folder and sub-folders with names containing characters like the "é"
2- In FO, create a page, add an scv to it.
3- Edit the clv. Browse to select content. Select the priviously added directory. It would be not spread and an error appear:

ERROR portal:UIPortalApplication - Error during the processAction phase <javax.jcr.PathNotFoundException: Item not found /sites content/live/acme/categories/acme/tes
t?rep1 in workspace collaboration>javax.jcr.PathNotFoundException: Item not found /sites content/live/acme/categories/acme/test?rep1 in workspace collaboration

testérep1 was the directory's name

Is a test missing in the TestCase file ?
*

Added UnitTest ?
*

Recommended Performance test?
*


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration ?
*

Describe configuration changes:
*

Previous configuration will continue to work?
*


Risks and impacts

Is there a risk applying this bug fix ?
*

Is this bug fix can have an impact on current client projects ?
*

Is there a performance risk/cost?
*


Validation By PM & Support

PM Comment
*

Support Comment
*


QA Feedbacks

Performed Tests
*

