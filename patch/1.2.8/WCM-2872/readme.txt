Summary

    * Status: Link still be referenced after being inserted
    * CCP Issue: CCP-524, Product Jira Issue: WCM-2872.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* To reproduce the problem you should follow these steps:
   1. Add a new content (Free Web Layout)
   2. In the FCKEditor, insert a link to page.
   3. Turn back to the beginning of the current line to edit text related to the link.
   4. Click on enter 2 times.
      After that text added still refer to the inserted link (Click on the Insert/edit link to check it).

Fix description

How is the problem fixed?
1. Change the value of EnterMode and ShiftEnterMode configuration in fckconfig.js
   from   
    FCKConfig.EnterMode = 'br';
    FCKConfig.ShiftEnterMode = 'p';
  to   
    FCKConfig.EnterMode = 'p';
    FCKConfig.ShiftEnterMode = 'br';
2. Removes the unnecessary <p> tag by switching the value of IgnoreEmptyParagraphValue configuration in fckconfig.js from false to true.

Patch file: WCM-2872.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Do the same steps like Reproduction Test => the added text does not refer to the inserted link.

Tests performed at QA/Support Level
* No
Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* Only change on a configuration file of FCKeditor (fckconfig.js), not on WCM configuration files.

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Patch validated

QA Feedbacks
*

