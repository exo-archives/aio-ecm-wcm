Summary

    * Status: Problem with navigation in the WCM site admin toolbar
    * CCP Issue: CCP-453, Product Jira Issue: WCM-2842
    * Complexity: N/A
    
The Proposal
Problem description

What is the problem to fix?

    *  In the admin bar, user cannot open a sub navigation if the current navigation doesn't have the reference page.

Fix description

How is the problem fixed?

    *  Add event onlick for all the navigations

Patch information:
Patches files:
WCM-2842.patch 	  	

Tests to perform

Which test should have detect the issue?
* Add new navigation and a new sub navigation inside it. And try to select it from admin bar.

Is a test missing in the TestCase file?
* Yes

Added UnitTest?
* No

Recommended Performance test?
* No


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

