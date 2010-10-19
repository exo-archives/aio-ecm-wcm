Summary

    * Status: Can't sort category content in a CLV
    * CCP Issue: CCP-438, Product Jira Issue: WCM-2838.
    * Complexity: HIGH

The Proposal
Problem description

What is the problem to fix?
    * It is impossible to sort category content in a CLV.
      WCM searches the symlink instead of the real node. However, symlinks don't contain properties (title, name) to order the results.

Fix description

How is the problem fixed?

    * Add some information to the link
    * Change the way to order contents/documents in Live mode and Edit mode

Patch file: WCM-2838.patch

Tests to perform

Reproduction test

   1. Connect as ROOT and go to ACME home page
   2. Go to the "acme" drive
   3. Create multiple contents in Business folder (that is in fact a category). Assume these documents are "001-Doc", "002-Doc", "003-Doc".
   4. Publish the content.
   5. Go to a page and put a CLV on this page
   6. In the CLV preferences choose "/sites content/live/acme/categories/acme/Business"
   7. The sorting feature is simply NOT WORKING.

Tests performed at DevLevel

    * No

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * Ordering management in Symlinks

Configuration changes

Configuration changes:

    * NO

Will previous configuration continue to work?

    * YES

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Yes. Maybe.

Is there a performance risk/cost?

    * NO

Validation (PM/Support/QA)

PM Comment

    * PATCH VALIDATE BY PM

Support Comment

    * Support validated this patch

QA Feedbacks
*

