Summary

    * Status: Menus of Administration bar hidden behind Calendar tables and chat bar
    * CCP Issue: CCP-600, Product Jira Issue: WCM-2931.
    * Fixes also: CS-4610
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?
    * In IE7, drop-down menus of Administration bar are hidden behind chat bar and some Calendar tables. 
      This bug does not occur in IE8 or Firefox. 

Fix description

How is the problem fixed?

    * Set a positive value for z-index of UISiteAdministrationPortlet. Since the chat bar and Calendar tables in CS haven't this property, drop-down menus of Administration bar will appear ahead.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2931.patch

Tests to perform

Reproduction tests

    * Case 1: WCM-2931: Sites menu in the administration bar lists all the existing portals.
      In IE7, when there is the chat bar at the bottom (classic portal) and the portal list is long, the last item in that portal list is hidden behind the chat bar.
      There is therefore no way to access that site and all of its children pages.

    * Case 2: CS-4610: Menu of the administration bar hidden behind Calendar tables.

   1. Login
   2. Go to Agenda
   3. Click on Day (or Week, Month, Workweek)
   4. In Custom Layout, select Toggle Left Pane
   5. Long menus in Administration bar are behind Calendar table. It is impossible to access these hidden portlets if they are not in the navigation bar (classic portal).
      This bug does not occur with Year or List table.

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

    * Function or ClassName change: no

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PM review: patch validated.

Support Comment

    * Support Team review: patch validated

QA Feedbacks
*

