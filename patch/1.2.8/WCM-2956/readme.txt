Summary

    * Status: Error in research when writing characters *
    * CCP Issue: CCP-713, Product Jira Issue: WCM-2956.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * Login by root.
    * Go to Sites/acme
    * Switch edit mode.
    * Edit a CLV portlet.
    * Choose manual mode.
    * Click on the select folder path icon.
    * In the content search form, put a text containing *
    * Click on the search button.
      1. Exception in server:

      [ERROR] portal:UIPortalApplication - Error during the processAction phase <javax.jcr.query.InvalidQueryException: Illegal name: Document*: Illegal name: Document*: Illegal name: Document*>javax.jcr.query.InvalidQueryException: Illegal name: Document*: Illegal name: Document*: Illegal name: Document*
      	at org.exoplatform.services.jcr.impl.core.query.sql.JCRSQLQueryBuilder.createQuery(JCRSQLQueryBuilder.java:171)
      	at org.exoplatform.services.jcr.impl.core.query.sql.QueryBuilder.createQueryTree(QueryBuilder.java:38)
      	at org.exoplatform.services.jcr.impl.core.query.QueryParser.parse(QueryParser.java:58)
      	at org.exoplatform.services.jcr.impl.core.query.lucene.QueryImpl.<init>(QueryImpl.java:100)
      	at org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex.createExecutableQuery(SearchIndex.java:207)
      	at org.exoplatform.services.jcr.impl.core.query.QueryImpl.init(QueryImpl.java:123)
              ...
      2. In the content search form, "Unknown error" message is shown if putting a text containing "*"in "Search by name" field. 

Fix description

How is the problem fixed?

    * Add a filter of illegal JCR name characters 
    * Make property and type input forms ineditable to user so that value must be chosen from the radio box

Patch file: WCM-2956.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Test with all illegal JCR characters 

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

    * Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support Team Review: Patch Validated

QA Feedbacks
*
