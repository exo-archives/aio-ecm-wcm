Summary

    * Status: Search in FO: bug when the result number exceeds 250
    * CCP Issue: CCP-429, Product Jira Issue : WCM-2828
    * Complexity: HIGH
    
The Proposal
Problem description

What is the problem to fix?

    * When the result number exceeds 250, there won't be any result found.
    * Only the first page results are displayed. 

Fix description

How the problem is fixed?

    *  Change the way to create/get the iterator if the results is bigger than 250.

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2828.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue?
* Create more than 250 results and try to sarch

Is a test missing in the TestCase file?
* Yes

Added UnitTest?
* No

Recommended Performance test?
* Yes, for search only.
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
* Proposed patch validated
QA Feedbacks

Performed Tests
*

