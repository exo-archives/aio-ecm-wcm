Summary

    * Status: Links are displayed not underlined when visited
    * CCP Issue: CCP-810, Product Jira Issue: WCM-2992.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In FCKEditor, insert web links, portal links and "insert content" links. All links appear underlined. By clicking on these links, all the links change color. Web links and portal links remain underlined while "insert content" links become not underlined.

Fix description

How is the problem fixed?

    * remove style='text-decoration:none;' for the link stylesheet in PluginUtils.js

Patch file: WCM-2992.patch

Tests to perform

Reproduction test

    *  In FCKeditor, insert web links, portal links and "insert content" links
    *  Visit links one by one
    * => "insert content" links change to "have no underline"

Tests performed at DevLevel

    * Do the same tasks like Reproduction Test => all links have underline

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

    * Function or ClassName change: None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated by PM

Support Comment
* Support review: Patch validated

QA Feedbacks
*

