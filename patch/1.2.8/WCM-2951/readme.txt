Summary

    * Status: Impossible selection to insert contents into a text content
    * CCP Issue: CCP-704, Product Jira Issue: WCM-2951.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * When adding a content by using the icon "Insert Contents" of the FCKEditor, a window appears to allow the selection of contents to be inserted into the text.
      But, when using a personalized IE7, this window doesn't contain the left part which shows the tree of accessible drives/directories: the block is empty.

Fix description

How is the problem fixed?
    * There is an error Javascript in the execution of if (ActiveXObject (" Microsoft. XMLDOM ")) in PluginUtils.js.
      This error is caused by the fact that ActiveX isn't instantiated by the same way between portal/fckeditor/exo/content/js/PluginUtil.js (the error appears when testing the ActiveXObject but not at it's instantiation) and eXoWCMResources/javascript/eXo/wcm/Service.js.

Patch file: WCM-2951.patch

Tests to perform

Reproduction test
* Cf. above

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
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: none

Is there a performance risk/cost?
* N/A.

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Support review: patch validated

QA Feedbacks
*

