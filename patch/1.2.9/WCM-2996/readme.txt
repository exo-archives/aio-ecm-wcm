Summary

    * Status: Bugs when creating a page under many nodes
    * CCP Issue: CCP-824, Product Jira Issue: WCM-2996.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Bugs when creating a page under many nodes

Fix description

How is the problem fixed?

    *  Manage "Page Created/Update" and "Navigation Created/Update" events to save the correct data into the properties of node.

Patch files: WCM-2996.patch

Tests to perform

Reproduction test
*Steps to reproduce:
1) Login to the portal acme, choose "Site Editor" then "Add Page"
2) Insert a portlet "Content Detail" on the page, and create a new content in this page (Rapid publication)
3) Go into the file explorer and observe the properties of the content by "View node properties", it is well linked to the page you created earlier.
4) Choose "Site Editor" then "Edit Navigation"
5) Right click in the tree to add a node (you can add this node under the same navigation as the first node or under another navigation, I will explain the différence between these two cases in step 8), click Page selector and select the page you created in step 2 and save.
6) Back in the Files Explorer to look at the properties of the node. We observe that:
"Publication: ApplicationId": only one page
"Publication: navigationNodeURIs": only one node while the page is under two different navigation nodes.
7) Choose "Sites Edior" then "Edit Navigation", and remove the first node created.
8) Back in the Files Explorer to look at the properties of the node. We observe :
If both nodes were created under the same navigation, the values of "Publication: ApplicationId" and "Publication: navigationNodeURIs" remain those of the first node although it was deleted.
If both nodes were created under two different navigations, the value of "Publication: ApplicationId" and "Publication: navigationNodeURIs" become empty.

Tests performed at DevLevel

    * Do the same tasks like reproduction test

    * In case we have 1 PageNode pointing to a Page, when we delete a PageNode(in Edit Navigation function) => PageNode's URL, PageId and appId of all entities that relate to the content is removed. (It's the same when we delete the Page)
    * In case we have 2 PageNodes which point to a Page, when we remove a PageNode => only information about removed PageNode is removed (the PageNode's URL)
    * In case we have 2 Pages that point to a Content
      + when we remove a PageNode => only PageNode's URL, PageId and appId that relate to the removed PageNode is removed from the content
      + it's the same when we remove a Page.
    * so on...

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

