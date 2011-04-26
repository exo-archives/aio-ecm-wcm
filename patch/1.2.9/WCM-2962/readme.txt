Summary

    * Status: The user workspace panel remains visible after editing the current site
    * CCP Issue: CCP-711, Product Jira Issue: WCM-2962.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The customer wants that after editing a site, when returning to Front Office, User workspace will close automatically.

Fix description

How is the problem fixed?

    * Add a onClick event to Discard and Save and Finish buttons of User Workspace Panel. When user click on these buttons, User workspace will close automatically and returning to Front Office.


Patch files: WCM-2962

Tests to perform

Reproduction test

   1. Go to "Site editor"->"Edit Site" or "Edit Navigation"
   2. The Edit Site panel and the User Workspace panel are displayed.
   3. Press "Discard" button or press "Save and Finish" button
   4. The User Workspace panel is not closed.

Tests performed at DevLevel

    * cf above

Tests performed at QA/Support Level

    * cf above

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

    * web/wcmportal/src/main/webapp/groovy/webui/core/UIToolbar.gtmpl

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*
