Summary

    * Status: Unjustified use of JavaScript code
    * CCP Issue: CCP-482, Product Jira Issue: WCM-2857
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Found in some templates like : portal.war/WEB-INF/conf/wcm/artifacts/nodetypes-templates/webContent/dialogs/dialog1.gtmpl

<script>var yyy = 0; while (yyy < 10000) {yyy++;}</script>

Why do we use this javascript after each WYSIWYG component?

Fix description

How is the problem fixed?

    * Remove the unjustified code:

      <script>var yyy = 0; while (yyy < 10000) {yyy++;}</script>

Patch file: WCM-2857.patch

Tests to perform

Reproduction test

   1. Go to Sites Explorer, open dialog to create new Free layout webcontent document
   2. Compare the loading & displaying before and after removing the code => There is no difference.

Tests performed at DevLevel

   1. Remove the unjustified code:
   2. Do Reproduction Test => There is no difference.

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
* Validated by PM

Support Comment
* Patch validated

QA Feedbacks
*
