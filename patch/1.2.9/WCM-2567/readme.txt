Summary

    * Status: Exception when the page register is opened the 1st time portal classic
    * CCP Issue: N/A, Product Jira Issue: WCM-2567.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
When the user opens the 'register' page for the 1st time, the following exception is thrown:

INFO: Server startup in 61854 ms
[ERROR] portletcontainer - Error: <javax.jcr.RepositoryException: Illegal path entry: "": Illegal path entry: "": Illegal path entry: "">javax.jcr.RepositoryException: Illegal path entry: "": Illegal path entry: "": Illegal path entry: ""
at org.exoplatform.services.jcr.impl.core.LocationFactory.parsePathEntry(LocationFactory.java:204)
at org.exoplatform.services.jcr.impl.core.LocationFactory.parseNames(LocationFactory.java:237)
at org.exoplatform.services.jcr.impl.core.LocationFactory.parseAbsPath(LocationFactory.java:86)
at org.exoplatform.services.jcr.impl.core.SessionImpl.exportDocumentView(SessionImpl.java:277)
at org.exoplatform.services.jcr.ext.registry.RegistryService.getEntry(RegistryService.java:143)
at org.exoplatform.portal.config.jcr.DataStorageImpl.getPortletPreferences(DataStorageImpl.java:521)
at org.exoplatform.portal.application.jcr.PortletPreferencesPersisterImpl.getPortletPreferences(PortletPreferencesPersisterImpl.java:47)
at ...

The page is displayed correctly after that.

If the user logs-in, then logs-out, then opens this page again, the exception is thrown again.

Fix description

How is the problem fixed?

    * Replace

      <instance-id>protal#classic:/exoadmin/AccountPortlet/Account</instance-id>

by

<instance-id>portal#classic:/exoadmin/AccountPortlet/Account</instance-id>

Patch file: WCM-2567.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* c.f above

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

    * N/A

Is there a performance risk/cost?
*N/A

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

