ummary

    * Status: Error while setting a page with PCLV portlet
    * CCP Issue: CCP-368, Product Jira Issue : WCM-2753
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix ?

    *  PCLV didn't check the taxonomy node exists before access it.

Fix description

How is the problem fixed ?

    *  Should to check the taxonomy node before access.

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
File WCM-2753.patch 	 	  	

    * Properties

Tests to perform

Tests performed at DevLevel ?
*1- Go to Sites Administration -> Ontologies -> Manage Taxonomy trees
2- Remove acme TaxonomyTree 
3- Go to site Acme -> News

Tests performed at QA/Support Level ?

   1. Go to Sites Administration -> Ontologies -> Manage Taxonomy trees
   2. Remove acme TaxonomyTree
   3. Go to site Acme -> News

Documentation changes

Documentation Changes:
* N/A
Configuration changes

Configuration changes:
* No

Previous configuration will continue to work?
* N/A
Risks and impacts

Is this bug fix can have an impact on current client projects ?

    * No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Validated by Support team

QA Feedbacks
*

