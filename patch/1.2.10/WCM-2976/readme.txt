Summary

    * Status: Content's publication state changed to draft after being selected in a SCV
    * CCP Issue: CCP-778, Product Jira Issue: WCM-2976.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When we add a published document into an SCV,  its publication state changes to "draft". That is a wrong behavior.

Fix description

How is the problem fixed?

    *  We simple remove the code fragment that changes the publication state of node when node is added into an SCV.

Patch file: WCM-2976.patch

Tests to perform

Reproduction test
*Steps to repoduce :
    * go to Site Explorer
    * create a new content (webContent or article)
    * change its publication state to 'published'
    * go to the home page
    * edit the page
    * add an SCV portlet in the page
    * edit the portlet
    * choose 'select a content'
    * select the previously created content
    * save
      ---> OK, the content is displayed in the page
    * go to the Site Explorer
    * find your content
      ---> the content is in draft publication state.

Tests performed at DevLevel
* cf above

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
* No

Function or ClassName change:
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*

