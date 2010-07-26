Summary

    * Status: PCLV: display documents with name containing invalid JCR characters
    * CCP Issue: CCP-405, Product Jira Issue : WCM-2795
    * Complexity: LOW
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * Need to escape these invalid JCR characters before displaying.

Fix description

How is the problem fixed ?

    * In order to correctly display the title of the document, the use of unescapeIllegalJcrChars method (implemented in org.exoplatform.ecm.utils.text.Text) in DocumentsTemplate.gtmpl for displaying title should solve the problem.

Patch informations:
Patches files:
WCM-2795.patch 	

Tests to perform

Which test should have detect the issue ?
* To reproduce this issue:

   1. Go to File explorer
   2. Sites Managment->acme->Documents. Upload a binary with name containing character like ' and then publish it.
   3. Go to acme site. The uploaded document is displayed in CLV documents. A %27 replaces the '.

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

