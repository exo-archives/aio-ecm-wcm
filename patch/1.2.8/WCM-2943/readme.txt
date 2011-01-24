Summary

    * Status: Error in displaying .rtf file
    * CCP Issue: CCP-677, Product Jira Issue: WCM-2943.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Error in displaying .rtf file

Fix description

How is the problem fixed?
*  RTF file has mime-type of "text/rtf" so program considers it as plain text file and displays the content directly on HTML page
   Check if mime-type of node is "text/rtf" then DO NOT display its content directly on HTML page.

Patch file: WCM-2943.patch

Tests to perform

Reproduction test
* Steps to reproduce:
1. Login
2. Go to acme/documents
3. Upload a .rtf file (e.g aaa.rtf)
4. View content of this file: see the source but not the visible content.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

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
* Validated by PM

Support Comment
* Patch validated

QA Feedbacks
*

