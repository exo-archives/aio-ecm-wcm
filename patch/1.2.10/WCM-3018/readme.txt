Summary

    * Status: Problem when selecting contents with path contains apostrophe character (CLV, SCV)
    * CCP Issue: N/A, Product Jira Issue: WCM-3018.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Problem when selecting contents with path contains apostrophe character (CLV, SCV)

Fix description

How is the problem fixed?

    * Escape illegal JCR characters in the node path before getting node data from the path.
    * Unescape illegal JCR characters in the node path & node name before writing it to the HTML page.

Patch file: WCM-3018.patch

Tests to perform

Reproduction test

   1. Go to Sites Explorer/Sites management/acme/
   2. Create folder with name includes apostrophes (eg: exo'qa) and a sub-folder (eg: test)
   3. Add a new content inside, eg: content1
   4. Add a page
   5. Input valid values then go to step 3: Drag & drop CLV portlet or content detail porlet into page
   6. Click icon to edit CLV portlet and Click Select folder path icon
   7. Browse and select added folder (exo'qa) then browse and select sub-folder (test) -> Error on display category name
   8. Select content1. "Unknown error" popup and throw exception in console

Tests performed at DevLevel
* Do the same tasks like reproduction test => have no exception's thrown, node name is displayed well.

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

    * Function or ClassName change: No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
