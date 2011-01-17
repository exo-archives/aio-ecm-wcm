Summary

    * Status: Link can't be distinguished in fck editor edit mode
    * CCP Issue: CCP-427, Product Jira Issue: WCM-2823.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1. Edit a page -> insert on it a new scv
2. Edit the scv. Insert on it a new content, eg. a Free layout WebContent.
3. In this document, add a link using Insert/Edit link icon
4. The link couldn't be distinguished from the other text. 

Fix description

How is the problem fixed?

    * By default, FCKeditor uses stylesheets defined in fck_editorarea.css file. 
      In WCM project, FCKeditor was changed to use the stylesheets of PORTAL, WCM. 
      The links in FCKeditor applies those stylesheets (color:black, text-decoration:none) => can't be distinguished.
    * To fix this problem, put fck_editorarea.css file into the list of stylesheet files which are applied to FCKeditor.

Patch file: WCM-2823.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Do the steps like Reproduction test => link style is changed to blue & underlined => It is OK.

Tests performed at QA/Support Level
* No

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
* N/A

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Support Team Review: Patch validated
 

QA Feedbacks
*
Labels parameters

