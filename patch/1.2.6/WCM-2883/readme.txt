Summary

    * Status: Impossible to edit pages
    * CCP Issue: CCPID, Product Jira Issue: WCM-2883
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Impossible to insert containers to edit a page. The working area is blank.
Reproduction test:

    * Select a page
    * Click Edit page
    * Choose Show Container
    * Select container and Drag & drop container from list in left pane to page area in right pane
      => Blank working area. See the attached file in WCM-2883.

Fix description

How is the problem fixed?

    * The bug results from recent changes in UIContainer.gtmpl and UITableColumnContainer.gtmpl in Portal layer (PORTAL-3776). We need therefore to update the corresponding templates in WCM.

Fix also the remaining bug in WCM-2821:

   1. The default container in classic homepage doesn't have Edit and Delete icons.
   2. A unique order of the Delete and Edit icons in all containers.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch files:
 	 
File WCM-2883.patch 	  	

    * Properties

Tests to perform

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation Changes:
*
Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change?

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Support review : 20100728: tested on AIO 1.6.x built from branches: still got problems:

    * when editing site, the user workspace menu containing portlets/containers is not shown, we must refresh to view it.
    * when adding a container in the bottom, some javascript code is shown on the page.

* Support review : 20100729 : tested and validated, there is no problem with the new patch 2010-07-29-WCM-2883.patch

QA Feedbacks
*
Labels parameters

