Summary

    * Status: Problem when using the NewPortalConfigListener in WCM
    * CCP Issue: CCP-406, Product Jira Issue: WCM-2796
    * Complexity: HIGH
    
The Proposal
Problem description

What is the problem to fix?

    * When we use the portal NewPortalConfigListener in WCM, to allow to override portal navigation metadata from the XML configuration files each time the server restarts without deleting the database, all the content of portals (acme and classic) is removed

Fix description

How is the problem fixed?

    * I changed the way to add the content initializer plugins.
    * Add the priority to make these plugins work as expected.
    * Remove an unnessecary startable class.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2796.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue?
* Try to start portal with the parameter overwrite = true in the portal-configuration.xml

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
* Yes

Describe configuration changes:
* Changing the service's name

* Add the new property priority for the plugins

* Remove the configuration for removed service.

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
* Patch tested and validated by Support team
QA Feedbacks

Performed Tests
*

