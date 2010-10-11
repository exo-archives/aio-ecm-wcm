Summary

    * Status: Problem when displaying nt:file view in DMS Administration drive
    * CCP Issue: CCP-510, Product Jira Issue: WCM-2870.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Problem when displaying nt:file view in DMS Administration drive

Fix description

How is the problem fixed?

    * Replace a textbox to textarea, because there are 2 nested textareas.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:

Tests to perform

Reproduction test
* Go to DMS Administration->exo:ecm->templates->nt:file->views->view1
  When source is displayed, a problem appears since a text area is used for that.

Tests performed at DevLevel
* Functional test

Tests performed at QA/Support Level
*

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
    * VALIDATED BY PM

Support Comment
    * Support review: validated

QA Feedbacks
*

