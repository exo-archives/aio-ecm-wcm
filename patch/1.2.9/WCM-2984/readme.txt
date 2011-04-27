Summary

    * Status: Content in some CLV disappears after change position of documents listed in DOCUMENTS in Overview page
    * CCP Issue: N/A, Product Jira Issue: WCM-2984.
    * Complexity: Normal

The Proposal
Problem description

What is the problem to fix?

    *  Content in some CLV disappears after change position of documents listed in DOCUMENTS in Overview page

Fix description

How is the problem fixed?

    *  Remove the code fragment which unpublishes unchosen content in CLV list. That code fragment was added by wrong understanding of one developer for https://jira.exoplatform.org/browse/WCM-2356WCM-2356.

Patch files: WCM-2984.patch

Tests to perform

Reproduction test
* Steps to reproduce:

    * Login by root
    * Select acme site
    * Switch to edit mode
    * Click Quick edit icon at a CLV (e.g: Document)
    * Choose the manual browse and select a multiple contents for example:
      /sites content/live/acme/categories/acme/News1
      /sites content/live/acme/categories/acme/News2
      /sites content/live/acme/categories/acme/News3
      /sites content/live/acme/categories/acme/News4
    * In CLV config form
    * Use the Up/Down arrows to change their position (3 down) --> Save --> View OK
    * Use the Up/Down arrows to change their position (2 down) --> Save --> View OK
    * Logout --> content in some CLV disappears even after login again or switch to another site

Tests performed at DevLevel

* Steps to reproduce:

    * Login by root
    * Select acme site
    * Switch to edit mode
    * Click Quick edit icon at a CLV (e.g: Document)
    * Choose the manual browse and select a multiple contents for example:
      /sites content/live/acme/categories/acme/News1
      /sites content/live/acme/categories/acme/News2
      /sites content/live/acme/categories/acme/News3
      /sites content/live/acme/categories/acme/News4
    * In CLV config form
    * Use the Up/Down arrows to change their position (3 down) --> Save --> View OK
    * Use the Up/Down arrows to change their position (2 down) --> Save --> View OK
    * Logout --> content in some CLV disappears even after login again or switch to another site

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
No
Configuration changes

Configuration changes:
No

Will previous configuration continue to work?
Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
*No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

