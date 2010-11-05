Summary

    * Status: Empty backup path at Manage Publication form
    * CCP Issue: none, Product Jira Issue: WCM-2856
    * Complexity: LOW
    
The Proposal
Problem description

What is the problem to fix?

    *  [Workflow] Backup path is been empty at Manage Publication form

Fix description

How is the problem fixed?

    *  this issue is caused because of the missing configuration problem. There is no information of backup drive and its "/Expired Documents" node in configuration files of wcm. Adding this information fixes the issue.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2856.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue?
* Try to using workflow publication in Manage publication. You can see the backup path is empty

Is a test missing in the TestCase file?
* Yes in WCM, maybe it exist in DMS testcase

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
* Yes

Describe configuration changes:

* Add Backup drive and the system path to access to backup folder

Previous configuration will continue to work?
* Yes
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
* Patch validated by Support
QA Feedbacks

Performed Tests
*

