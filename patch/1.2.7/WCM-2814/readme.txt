Summary

    * Status: Managing tables in FCKEditor
    * CCP Issue: CCP-415, Product Jira Issue: WCM-2814
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The creation of tables in FCKEditor does not work correctly.
With IE:

   1. The table's creation doesn't keep account of the properties defined in the popup creation.
   2. The table doesn't appear in the portal.

With FF:

   1. The access to the properties of the table already created opens a popup that is not correctly completed.
   2. The validation of properties in the table doesn't work because there isn't the button "OK".

In addition, open FCKeditor causes a JavaScript error.
Fix description

Problem analysis

    * Old JavaScript codes to set style for the table work incorrectly in IE browser. IE browser doesn't support for setting table style by calling table.setAttribute( "style", value).

How is the problem fixed?

    * Use table.style.cssText to set style for the table.
    * Add a function getColorFromMultiValueStyle() to get correctly the color value in different formats (name, RBG, hexa).

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2814.patch

Tests to perform

Reproduction test

   1. In File Explorer, edit a document (e.g. article)
   2. Add a table into document content. Set up properties of the table. Click OK.
   3. Check the table that has created (in FCKEditor, in File Explorer).
   4. Return to Edit document.
   5. Focus on the table. Right click and select Table Properties.

Tests performed at DevLevel

    * Create document, add a table, set its properties and click OK button
    * Check the table that has created -> it is showed correctly
    * Right-click into the table, choose "Table Properties" function, modify some properties of table, click OK button
    * Recheck the table, it's changed correctly

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
* PATCH VALIDATED BY PM

Support Comment
* Support review: patch validated

QA Feedbacks
*


