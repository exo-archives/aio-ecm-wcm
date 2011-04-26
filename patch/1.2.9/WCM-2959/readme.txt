Summary

    * Status: User workspace panel remains visible after editing the current page
    * CCP Issue: CCP-711, Product Jira Issue: WCM-2959.
    * Fixed also: WCM-2963
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * After editing a page with the Edit Page Wizard, the user workspace panel remains visible.

Fix description

How is the problem fixed?

    * Add a onClick event to Abort and Save buttons of Edit Page Wizard. When user click on these buttons, User workspace will close automatically and returning to Front Office.

Patch information:

Patch files: WCM-2959
 

Tests to perform

Reproduction test

   1. Go to "Site editor"->"Edit page wizard"
   2. The Edit page wizard panel and the user workspace panel are displayed.
   3. Press "Abort" or press "Next" then "Save"
   4. The user workspace panel is not closed.

Tests performed at DevLevel
* The same as in description.

Tests performed at QA/Support Level
* The same as in description.
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

    * N/A.

Function or ClassName change

    * web/wcmportal/src/main/webapp/groovy/webui/core/UIWizard.gtmpl

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

