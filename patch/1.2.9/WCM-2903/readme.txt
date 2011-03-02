Summary

    * Status: Update WCM's UIContainers
    * CCP Issue: CCP-790 Product Jira Issue: WCM-2903.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * After fixing PORTAL-2983, PORTAL-3855, there are some issues with WCM's UIContainers:
         1. Impossible to move a container (WCM-2945)
         2. Gray area appears each time the mouse passes the border of column container (WCM-2964)
         3. Possible to add a portlet to the right or left side of a column container (WCM-2965)

Fix description

How is the problem fixed?

    * Update UITableColumnContainer.gtmpl, UITableColumnContainer2.gtmpl, UIContainer2.gtmpl:
          o change some stylesheets to be suitable with Portal code.
          o add onmousedown() javascript function to be possible to move the containers (drag & drop).
    * Update page.xml & portal.xml of some sites (classic, acme...) to use new design (use UIColumnContainer in UITableColumnContainer2 instead of UIContainer2 to show column layouts)

Patch information:
Patch files: WCM-2903.patch

Tests to perform

Reproduction test
I. Impossible to move a container

    * Go to Edit Site
    * Choose Container tab
    * Select container in list and drag & drop it into Layout portal
      => Can't move this container anymore.

II. Gray area appears each time the mouse passes the border of column container

    * Login as root.
    * Go to Site Editor/Edit Site
    * Add a 3-column container to this portal (Container1).
    * Pick 1 portlet from User Workspace.
    * Move this portlet to the border of Container1. Move in to (or out of) this container (still hold left mouse button). 1 new gray area appears each time the mouse passes the higher (or lower) border of Container1.

III. Possible to add a portlet to the right or left side of a column container

    * Login as root.
    * Go to Site Editor/Edit Site
    * Insert a 3-column container to this page (Container1)
    * Add at least 1 portlet from User Workspace to any column container of Container1.
    * Drop a new portlet to the blank area below the other columns of Container1. The new porlet is added automatically to a new column at the right of existing columns.
    * After that, we can move the porlet at the new column from the right to the left side, place it on the right side of the most left column container of Container1.

Tests performed at DevLevel

    * Go to Edit Site
    * Insert a 3-column container to this page
    * Choose a portlet, drag it to a container (hold left mouse button & move the portlet into and out of the border of another container) => only one Gray Area appears => OK
    * Drop portlet into a column
    * Try to drop another portlet into the blank area below the other columns (inside the 3-column container) => can not do this, the dropped portlet will be add to outside of the 3-column container => OK
    * Choose Container tab
    * Try to drag a Container => possible to drag & drop it => OK

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

    * Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* VALIDATED BY PM

Support Comment
* Support Team Review: Patch validated

QA Feedbacks
*

