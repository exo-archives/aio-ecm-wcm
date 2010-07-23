Summary

    * Status: Download a binary file with name containing an illegal jcr char (using the right popup menu)
    * CCP Issue: CCP-404, Product Jira Issue: WCM-2832
    * Complexity: N/A
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    *  There is an exception in JCR when we trying to access a node with a special character.

Fix description

How the problem is fixed ?

    *  Escape the special character before accessing to JCR

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
There are currently no attachments on this page.
Tests to perform

Which test should have detect the issue ?
* Try to upload/create a binary/document which have a special character like "'" (a single quote)

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
* No, but we need to check as more as possible the special characters

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

