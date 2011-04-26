Summary

    * Status: Bugs when search and download documents which names contain apostrophes.
    * CCP Issue: CCP-884, Product Jira Issue: WCM-2997.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The uploading of a document which the name contains apostrophes is not denied and when we search for this document in front office, for exemple "ap'str.doc", the document appears "ap%27str.doc" and we can not download it.
Fix description

How is the problem fixed?

    * unescape the illegal JCR characters and save it into the exo:title property of the document

Patch files: WCM-2997

Tests to perform

Reproduction test

   1. Upload a document which the name contains apostrophes
   2. Search for this document in front office, for exemple "ap'str.doc", the document appears "ap%27str.doc" and we can not download it.

Tests performed at DevLevel

   1. Do the same tasks like reproduction test => search OK
   2. click to a document => view detail OK
   3. click to download it => download OK

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No
Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

