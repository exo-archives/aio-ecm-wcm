Summary

    * Status: Problem for displaying documents with names containing illegal JCR characters using nt:file view
    * CCP Issue: CCP-403, Product Jira Issue: WCM-2794
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In nt:file view (left panel of File Explorer): When a file name contains an illegal JCR character such as ', this character isn't displayed well in publication window (publication history, publication status).

Problem analysis
When displaying document title, the unescapeIllegalJcrChars method of org.exoplatform.ecm.utils.text.Text should be applied in order to revert changes done to save node in the JCR. The changes should be done in the view1.gtmpl related to the nt:file
Fix description

How is the problem fixed ?

    * Escape these characters before displaying them.

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch files:
 	 
File WCM-2794.patch 	 	

    * Properties

Tests to perform

To reproduce the problem:
Scenario 1: already fixed in AIO 1.6.5
1- Choose a drive in the File Explorer
2- Upload a binary document containing illegal JCR characters like '
3- Double click on the uploaded document
4- The nt:file view is shown and the illegal JCR characters are changed in %number(%27 for ')
(ntFileView.JPG)

Scenario 2:
1- Upload a document containing the character '
2- Select the document-> manage publication
3-The problem of display appears in publication status and publication history.

Tests performed at DevLevel ?
*

Tests performed at QA/Support Level ?
*
Documentation changes

Documentation Changes:
*
Configuration changes

Configuration changes:
*

Previous configuration will continue to work?
*
Risks and impacts

Is this bug fix can have an impact on current client projects ?

    * Function or ClassName change ?

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

