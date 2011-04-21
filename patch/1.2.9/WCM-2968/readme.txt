Summary

    * Status: Labels aren't translated in French when managing publications
    * CCP Issue: CCP-737, Product Jira Issue: WCM-2968.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Labels aren't translated in French when managing publications

Fix description

How is the problem fixed?
* Update invocation of keys in UIPublicationHistory.gtmpl and ContentView.gtmpl

Patch file: WCM-2968.patch

Tests to perform

Reproduction test
* Steps to reproduce:
1. Login
2. Go to acme/documents
Case 1: Create a document
Labels aren't translated in French: "published", "draft" should be "publié", "brouillon" (image Manage_Publication1.png)
Case 2:
Create a document
Select this document
Manage Publications
Publication history
Labels aren't translated in French: "published", "draft", "enrolled" should be "publié", "brouillon", "créé" (image Manage_Publication2.png)

Tests performed at DevLevel
* No

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
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
