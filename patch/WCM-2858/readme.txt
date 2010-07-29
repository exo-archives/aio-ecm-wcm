Summary

    * Status: Error when moving document containing illegal jcr characters
    * CCP Issue: CCP-483, Product Jira Issue: WCM-2858
    * Complexity: LOW
    
The Proposal
Problem description

What is the problem to fix?

    * Exception when trying to drag and drop a binary file with name containing illegal JCR characters 

Fix description

How is the problem fixed?

    * Encode the node path (by URLEncoder) before moving files.  

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2858.patch 	
    * Properties

Tests to perform

Which test should have detect the issue?
*
To reproduce the problem, You should follow this steps:
1- In BO, upload document with name containing illegal jcr character like '
2- Drag and drop doccument to another content
3- A popup appears with message to confirm operation
4- click on OK.
An error appear with stack trace like the following one:

ERROR JCRExceptionManager - The following error occurs : javax.jcr.PathNotFoundException: Can't find path: /sites content/live/acme/categories/acme/chap1'.pdf
WARN UIJCRExplorer - The node cannot be found at /sites content/live/acme/categories/acme/chap1'.pdf into the workspace collaboration
ERROR UIJcrExplorerContainer - an unexpected error occurs while selecting the node <javax.jcr.PathNotFoundException: Can't find path: /sites content/live/acme/catego
ries/acme/chap1'.pdf>javax.jcr.PathNotFoundException: Can't find path: /sites content/live/acme/categories/acme/chap1'.pdf

This problem is due to the encoding of the character ' like it was the case in CCP-404
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
*
QA Feedbacks

Performed Tests
*

