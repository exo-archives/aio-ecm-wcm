Summary

    * Status: Add the ability in FCKEditor to assign icons to specific contents
    * CCP Issue: CCP-751, Product Jira Issue: WCM-2972.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  In the FCKeditor, there are icons for documents and articles but there are no icons for specific contents.
      To overcome this problem, we tried to add an icon to a specific content. We went through 2 stages, creating a file under webapps that contains the CSS code and the image of the icon and adding in content.html, located under "fckeditor \ exo \ content \ ", a link to the CSS created. This solution requires the modification of "content.html", which can pose a problem during a migration to a maintenance release AIO.
      We require a solution that allows us to add icons without migration issues

Fix description

How is the problem fixed?

    * Add default16x16Icon in the PluginUtils.js

Tests to perform

Reproduction test

    * Create new node type
          o Group/Sites Administration/Types Of Content/Manage Node type
          o Click to Add button: In the Node Type table
                + Name space: exo
                + Node type name: customNodes
                + Super type: exo:article
    * Create new template with the new created node type
          o Groupe -> Sites Administration -> Manage Templates
          o Click to the Edit icon of the exo:article template
          o Copy Dialog and View
          o Close and return to Manage Templates screen
          o Click Add button
          o In the Template table:
                + Name: exo:customNodes
                + Label: CustomNodes
                + Permission: Any
          o In the Dialog table: Copy the contents of Dialog's article
          o In the View table: Copy the contents of View's article
          o Save this created template.
    * Create document
          o Go to File Explorer/Site Management/acme/document
          o Create new webcontent document
          o Create new document and choose the new created template
          o Save this document
          o In the FCKEditor, click to the Insert Content icon
          o Choose the document has just been created in the Site Management/acme/document

Actual result: It doesn't exist the icon for the selected content

Tests performed at DevLevel
* cf. above

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
* N/A

Support Comment
* Support review: Patch validated

QA Feedbacks
*

