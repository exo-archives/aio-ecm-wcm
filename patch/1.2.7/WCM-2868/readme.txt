Summary

    * Status: Cannot view nor select content when selecting a content (text) for an SCV using search
    * CCP Issue: CCP-507, Product Jira Issue: WCM-2868.
    * Complexity: N/A

What is the problem to fix?
  When using search to select content for an SCV, there are some problems: 
  * Neither select nor view icon works
  * No content displayed
  * When saving the SCV, an error was thrown

Fix description

How is the problem fixed?
    * Allow SCV search to view or select content

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:

Tests to perform

Reproduction test
    * To reproduce this issue: all images are at WCM-2868
         1. Create a page.
         2. Add a Single Content Viewer in it
         3. Configure SCV and choose "select a content"
         4. Do not browse, but use search feature to select content for SCV (see imprim.jpg)

Neither select (error1.txt) nor view icon works (error2.txt), and no content displayed (imprim1.jpg).
When saving the SCV, an error was thrown (imprim2.jpg).

    * But if we follow this scenario:
         1. Create a page
         2. Select the "page configs" in the page layout template
         3. Drag and drop the portlet "Content Detail"
         4. Steps 3 and 4 of the first scenario

=>The view icon works (imprim3.jpg) but the select icon is still not working.

If we repeat the first scenario, we found that the view icon works well like imprim3.jpg

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * NO

Configuration changes

Configuration changes:

    * NO

Will previous configuration continue to work?

    * YES

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * NO

Is there a performance risk/cost?

    * NO

Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Support review: patch validated

QA Feedbacks
*

