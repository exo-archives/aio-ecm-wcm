Summary

    * Status: W3C Validation: UICLVPortlet problems
    * CCP Issue: CCP-505, Product Jira Issue: WCM-2908.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
  In the page containing SCV with illustration image, W3C validation detects an error: 
* required attribute "alt" not specified: the alt attribute does not exist for HTML SRC tag of Illustration images

Fix description

How is the problem fixed?

    * Add alt property into SRC tag of Illustration images.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2908.patch

Tests to perform

Reproduction test 

How to reproduce:

   1. Add new page
   2. Add a new SCV portlet
   3. Select SCV Settings, select a content with image in caption section.
   4. Save
   5. Validate HTML using W3C validator

Tests performed at DevLevel
As above

Tests performed at QA/Support Level
As above
Documentation changes

Documentation changes:
Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * UIContentListPresentationBigImage.gtmpl
    * UIContentListPresentationDefault.gtmpl
    * UIContentListPresentationSmall.gtmpl
    * Templates are saved in JCR, Customer needs to update them to have this fix .

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Validated by Support

QA Feedbacks
*

