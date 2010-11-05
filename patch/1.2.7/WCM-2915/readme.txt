Summary

    * Status: Quick edit icons lost when editing page using IE7
    * CCP Issue: CCP-590, Product Jira Issue: WCM-2915.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Steps to reproduce:

   1. Choose acme site.
   2. Change to edit mode
   3. Quick edit icons in the body of the page are displayed
   4. Go to the main menu. Edit icons in the body of the page are no more displayed.

Fix description

Problem analysis

    * The current template contains the table that is very difficult for positioning inside IE. The properties which are used to set position to the "Edit" segment cannot be displayed correct. And when some size/position event is raised on IE, all the <div> elements that show the edit button disappear.

How is the problem fixed?

    * Edit button is maintained in some administration portion in the pages, for example, Search Box, Login, etc, but the body is not right. We let the body portion use the same template layout with the admin portion. The template makes use of the DIV element.
    * Due to template change, we must reset some css for repositioning the page's items.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files: WCM-2915.patch, WCM-2915-style.txt

Tests to perform

Reproduction test
* Cf. above

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

    * Any customer who does not use ACME template must apply the patch and correct CSS according to the instruction in WCM-2915-style.txt.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* VALIDATED BY PM

Support Comment
*

QA Feedbacks
*

