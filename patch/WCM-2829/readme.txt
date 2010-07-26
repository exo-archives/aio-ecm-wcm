Summary

    * Status: Externalize the use of Fuzzy search
    * CCP Issue: CCP-399, Product Jira Issue : WCM-2829
    * Complexity: LOW
    * Impacted Client(s): Les Douanes Françaises and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    *  Customer asking about an improvement allow them to make a fuzzy search

Fix description

How is the problem fixed ?

    * Add some configuration and some default fuzzy search index

Patch information:
Patch files:
WCM-2829.patch 	

Tests to perform

Which test should have detected the issue ?
* Try to use WCM search with some keywords like "test~"

Is a test missing in the TestCase file ?
* Yes

Added UnitTest ?
* No

Recommended Performance test?
* Yes, for search page only


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration ?
* Yes

Describe configuration changes:
* Add new parameters to allow user to choose fuzzy search or not.

Previous configuration will continue to work?
* Yes, the default value of fuzzy search index is 0.5.


Risks and impacts

Is there a risk applying this bug fix ?
* No

Can this bug fix have an impact on current client projects ?
* N/A

Is there a performance risk/cost?
* N/A


Validation By PM & Support

PM Comment
*

Support Comment
*


QA Feedbacks

Performed Tests
*

