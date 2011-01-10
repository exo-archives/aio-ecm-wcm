Summary

    * Status: Labels in French
    * CCP Issue: CCP-470, Product Jira Issue: WCM-2879
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Some labels are not translated in French

Fix description

How is the problem fixed?
* Modify in resource bundles files:
  Sauver -> Enregistrer
  Reset -> Réinitialiser
  Config -> Configuration
  Translate English labels into French.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch file: WCM-2879.patch

Tests to perform

Reproduction test
Check labels in the following resource bundles files: 
- classic_fr.xml
- webui_fr.xml
- FastContentCreatorPortlet_fr.xml
- FormGeneratorPortlet_fr.xml
- WCM portlets: 
  + ParameterizedContentListViewer_fr.xml
  + ContentListViewer_fr.xml
  + ParameterizedContentViewer_fr.xml
  + CategoryNavigation_fr.xml
  + SingleContentViewer_fr.xml
- NewsletterManager_fr.xml, 
- publication_fr.xml

Tests performed at DevLevel
* No

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

Can this bug fix have an impact any side effects on current client projects?

    * Function or ClassName change: none.

Is there a performance risk/cost?
* N/A

Validation (PM/Support/QA)

PM Comment
* VALIDATED BY PM

Support Comment

    * Support review : patch validated

QA Feedbacks
*
