ummary

    * Status: Category labels are not translated in WCMInsertContent functionality
    * CCP Issue: CCP-474, Product Jira Issue: WCM-2853
    * Complexity: HIGH
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    *  The labels of drives in Content selector (FKCEditor plugin) doesn't change when the language is changed

Fix description

How is the problem fixed ?

    * Add a new attribute called label to show the translated label of drives.

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2853.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue ?
* Change the language, add/edit a web content and select the Content selector plugin.

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
* No

Previous configuration will continue to work?
* No
Risks and impacts

Is there a risk applying this bug fix ?
* No

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

