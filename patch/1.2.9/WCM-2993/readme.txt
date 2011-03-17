Summary

    * Status: Errors when importing CSS files in fckeditor
    * CCP Issue: CCP-811, Product Jira Issue: WCM-2993.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * In fckeditor, using firebug, in xEditingArea, the <head> contains links to css and javascript files . By clicking on multiple CSS links, an error message is displayed. This message indicates a failure when importing the CSS file, this error is due to scores added in "a href" links.
    * Among the non-imported css files, the file "stylesheet.css" under eXoRessources / skin. This is the file which imposes the color black and the non underline for the visited "web links" and "portal links" inserted into FCKeditor in AIO-1.6.7.
      In AIO-1.6.8, the web and portal links appear violet and underlined because the CSS file "stylesheet.css"under eXoRessources / skin encountered an error when imported.
      We want that these visited links appear violet (according to web standards) and underlined, this is requested by our customers, but we noticed that the color and the underlined text decoration were corrected in AIO-1.6.8 haphazard by the error of importing CSS files.

Fix description

How is the problem fixed?

    * correct the file path of some stylesheets before importing it into FCKeditor stylesheet: remove the apostrophe from the path
    * update style for links in FCK by adding some declarations into FCKConfig.EditorAreaStyles variable.

Patch file: WCM-2993.patch

Tests to perform

Reproduction test

    * Open Fckeditor (add a document, add a comment). Add links in the content.
    * Open Firebug. 
      In xEditingArea, the <head> contains links to css and javascript files. 
      Click on multiple CSS links, an error message is displayed. This message indicates a failure when importing the CSS file, this error is due to scores added in "a href" links.

Tests performed at DevLevel

    * Do the same steps like Reproduction test
    * focus into firebug => all errors disappear
    * make a link in FCKeditor => it appears in blue and underline => OK
    * click into that link => its color is changed to visited color (purple) => OK

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
* Validated

Support Comment
* Validated

QA Feedbacks
*

