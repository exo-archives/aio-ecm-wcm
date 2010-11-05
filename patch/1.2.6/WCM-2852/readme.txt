Summary

    * Status: Can't Edit or delete a comment on a document
    * CCP Issue: CCPID, Product Jira Issue : WCM-2852
    * Complexity: LOW
    
The Proposal
Problem description

What is the problem to fix?

    * There's no css config for exo:comments configuration.

Fix description

How is the problem fixed?
* Add css skin configuration.  

Patch informations:

Patches files:
File WCM-2852.patch

Tests to perform

Which test should have detect the issue?
*
Steps:

    * Create a document, comment for this document
    * Delete and Edit icon of this comment are lost
    -> Can't delete or edit this comment

Is a test missing in the TestCase file?
*

Added UnitTest?
*

Recommended Performance test?
*


Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration?
*

Describe configuration changes:
*

Previous configuration will continue to work?
*


Risks and impacts

Is there a risk applying this bug fix?
*

Can this bug fix have an impact on current client projects?
*

Is there a performance risk/cost?
*


Validation By PM & Support

PM Comment
*

Support Comment
* Patch tested and validated by Support


QA Feedbacks

Performed Tests
*

