Summary

    * Status: CLV manual search content: Error when previewing content with image loaded from JCR
    * CCP Issue: CCP-586, Product Jira Issue: WCM-2911.
    * Complexity: easy

The Proposal
Problem description

What is the problem to fix?
When editing CLV in manual mode, there is an error when previewing content with image loaded from JCR.  

Fix description

How is the problem fixed?
* In class UIContentViewer, the function getRepositoryName() returns null and it raises the Exception. This function needs to be rewritten, it should return the correct current repository name.

Patch file: WCM-2911.patch

Tests to perform

Reproduction test
* Steps to reproduce:
   1. Create a document template where images are loaded from JCR.
   2. Add a document corresponding to the new document type. In this document, insert an image from JCR.
      Make sure that the image is displayed on the view.
   3. Publish the document
   4. Go to the FO. Edit a CLV, choose manual mode.
   5. Search for content.
      In the search result, the document appears.
      When you preview it, you will get an Exception and the doc is not displayed.


Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* None

Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* Patch validated by PM.

Support Comment
* Proposed patch validated

QA Feedbacks
* 
