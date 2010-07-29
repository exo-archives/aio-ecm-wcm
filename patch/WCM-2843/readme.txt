Summary

    * Status: Problem with FCK editor's insert link to a site page plugin
    * CCP Issue: CCP-454, Product Jira Issue: WCM-2843
    * Complexity: LOW
   
The Proposal
Problem description

What is the problem to fix?

    *  We didn't show the navigation if there is no reference page in that navigation

Fix description

How is the problem fixed?

    *  Show the navigation even it doesn't have the reference page.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2843.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue?
* Add a new navigation without the reference page. Add/edit a web content and select Insert portal link.

Is a test missing in the TestCase file?
* Yes

Added UnitTest?
* No

Recommended Performance test?
* Yes
Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*
Configuration changes

Is this bug changing the product configuration?
* No

Describe configuration changes:
* N/A

Previous configuration will continue to work?
* N/A
Risks and impacts

Is there a risk applying this bug fix?
* No

Can this bug fix have an impact on current client projects?
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

