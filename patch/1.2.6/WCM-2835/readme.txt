Summary

    * Status: Content Selector does not work on IE7
    * CCP Issue: CCP-443, Product Jira Issue: WCM-2835
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Content Selector does not work on IE7

Fix description

How is the problem fixed?

    * Use onChange instead of onClick.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM-2835.patch

    * Properties

Tests to perform

Reproduction test
On Internet Explorer 7
In the CLV, to use a list of documents (this is the same for a Single Content Viewer).
Steps:
0- Put some contents in a folder /acme/webcontent/* (some docs, some images and some webcontent)
1- Drop a CLV on a page
2- Edit the CLV
3- In the Edit dialog (Configuration of Content list viewer), select "Mode: Manual"
4- Click on the Content Selector
5- Navigate to the folder you want to use, /acme/webcontent/ for example
5.1 - You see all the web content (OK)
5.2 - Now select "MEDIAS"
6- BUG: NOTHING HAPPENS

Tests performed at DevLevel?
*

Tests performed at QA/Support Level?
*
Documentation changes

Documentation Changes:
*
Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change?

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

