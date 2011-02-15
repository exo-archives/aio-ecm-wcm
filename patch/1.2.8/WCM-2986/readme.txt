Summary

    * Status: Nothing happen when clicking to subscribe to newsletter
    * CCP Issue: N/A, Product Jira Issue: WCM-2986.
    * Complexity: low

The Proposal
Problem description

What is the problem to fix?

    * Nothing happen when subscribe newsletter

Fix description

Problem analysis
id of UINewsletterViewerForm was added inside div tag. That raises 2 problems:

   1. W3C validation is failed due to duplicate id of UINewsletterViewerForm of div and form tags.
   2. JavaScript function eXo.webui.UIForm.submitForm() can't take the right action when the user clicks Subscribe button.

How is the problem fixed?

    * Delete the id attribute in div tag.

Patch information:
Patch files: WCM-2986.patch


Tests to perform

Reproduction test
* Steps to reproduce:

    * Go to Newsletter page
    * Put email address
    * Tick on some subscription
    * Click on Subscribe button --> nothing happens

Tests performed at DevLevel
*

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
* Patch Validated

Support Comment
* Patch validated

QA Feedbacks
*

