Summary

    * Status: Impossible to show HTML link in a printed document
    * CCP Issue: CCP-692, Product Jira Issue: WCM-2946.
    * Complexity: low

The Proposal
Problem description

What is the problem to fix?

    *  HTML link is not shown in a printed document

Fix description

How is the problem fixed?

    * Correct display value of hyperlink in UIPCVContainer.gtmpl

Patch file: WCM-2946.patch

Tests to perform

Reproduction test
* Steps to reproduce:
1. Login
2. In BO: go to Site Management/acme/documents, create a document (e.g: Free layout webcontent)
3. Click on icon Insert/Edit link to add an HTML link in the document
4. Publish the document
5. Go to Site Editor/Add Content. Choose "Select a content"
6. Select the document newly created
7. Click Print icon to print this document
8. Choose to print to a file. The HTML link is not shown in the document (see attached images)

Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
* 

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: none

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* Patch validated by PM.

Support Comment
* Support review: patch validated

QA Feedbacks
*
