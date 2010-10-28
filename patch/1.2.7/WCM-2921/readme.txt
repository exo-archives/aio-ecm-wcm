Summary

    * Status: Duplicated elements at the creation of a CLV
    * CCP Issue: CCP-2921, Product Jira Issue: WCM-2921.
    * Fixes also: CCP-531/WCM-2880, WCM-2913
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    There are some anomalies when editing CLV in manual mode:
    * Duplicate items in reverse order.
    * Item disappearance at disconnection if that is the only item in the previous session.

Fix description

How is the problem fixed?

    * Let the publication process do what it will do. After all, save the list of selected items on the portlet's preferences.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2921.patch

Tests to perform

Reproduction test

    * Case 1:

   1. Upload three documents AAA, BBB, CCC.
   2. Create a page containing a CLV.
   3. Edit the CLV in manual mode.
   4. Select three documents AAA, BBB, CCC.
   5. Validate the selection and the edition of CLV.
      Observation: the items displayed in the CLV (in the page) are in the right order: AAA, BBB, CCC.
   6. Disconnect, then reconnect (with the same account or another account).
   7. View the page with the CLV.
      Observation: the items displayed in the CLV are duplicated in this order: CCC, BBB, AAA, AAA, BBB, CCC.
   8. Select to edit the CLV: the elements are duplicated in the list (list for changing the order of elements).
      Edit items (click icon +): the elements are not duplicated. Validate the list and the edition of CLV.
      => The CLV displays in this order: CCC, BBB, AAA
   9. Disconnect then reconnect: items displayed are in this order: AAA, BBB, CCC.
      From this change, elements will not be duplicated, but at each edition of the validation of CLV (with disconnection / reconnection), the display order is reversed (WCM-2880)

    * Case 2:

   1. Create a page containing 2 CLVs (CLV1, CLV2).
   2. Edit these CLVs in manual mode:
          * In CLV1: select two content files (documents) AAA, BBB
          * In CLV2: select a content file (document) AAA
   3. Validate the selection and the edition of these CLVs.
   4. Disconnect, then reconnect (with the same account or another account). View the page with these CLVs. Observations (Case 1):
          * The items in CLV1 are duplicated in this order: BBB, AAA, AAA, BBB.
          * The items in CLV2 are duplicated : AAA, AAA.
   5. Select to edit the CLV1: the elements are duplicated in the list (list for changing the order of elements)
          * Edit items (click icon +): the elements are not duplicated.
          * Validate the list and the edition of CLV1.
   6. Observations:
          * In CLV1: BBB, AAA
          * In CLV2, there is nothing.
   7. Edit CLV2: there is nothing in the list (of element order).

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*
Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Support review : patch validated

QA Feedbacks
*
