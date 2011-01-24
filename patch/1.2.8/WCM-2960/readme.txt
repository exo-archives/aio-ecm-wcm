Summary

    * Status: Impossible to select an invisible page when creating a portal page link into a text (FCK Editor)
    * CCP Issue: CCP-731, Product Jira Issue: WCM-2960.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

Create a page and set it as invisible one => This page is not displayed in navigation menu (that is correct), but can be accessed and displayed using a link (or typing matching URI in Web browser, for advanced users only).

In addition, create a text using FCK Editor (Free layout webcontent), and insert a page link:
* Click "Insert a page link" button,
* Click "Select a page" button,
  A window opens to display all available pages => This page selection window allows to select pages with visible attribute but not invisible ones.

Expectation: As invisible pages are created to be accessible using only web links, it should be possible to select an invisible page.

Fix description

How is the problem fixed?
* Ignore the condition check isDisplay property when displaying pages in PortalLinkConnector.java file. Then page selection window will display visible and invisible pages.

Patch file: WCM-2960.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* cf. above

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
* N/A

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Patch accepted

QA Feedbacks
*

