Summary

    * Status: Search some things from the version storage
    * CCP Issue: N/A Product Jira Issue: WCM-2826.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In the result of WCM search, there are documents stored belows jcr:system in the version storage.

   1. the URL is not meaningfull for an end user.
   2. We should not include version storage in the searched items.
      It should search only in published contents that are accessible to the front end.
      In other words, only the last published version should be available for search.

Fix description

How is the problem fixed?

    * The problem is the URL shows the unwanted version storage. This due to the node is at frozen node type. Hence, the problem is fixed by finding the original node for the relevant frozen node by UUID. 

Patch information:
Patch files: WCM-2826.patch

Tests to perform

Reproduction test
* Doing searches with results returned

Tests performed at DevLevel
* Doing a vary of different searches with results returned

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
* no


Configuration changes

Configuration changes:
* no

Will previous configuration continue to work?
* yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
* no 


Validation (PM/Support/QA)

PM Comment
*VALIDATED BY PM

Support Comment
*patch validated

QA Feedbacks
*

