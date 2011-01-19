Summary

    * Status: FCKEditor: Exception when creating page link after adding new page for the user navigation
    * CCP Issue: CCP-588, Product Jira Issue: WCM-2914.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1. Login as root
2. Add a page to user:root's navigation
3. Add a new document (eg. Free layout Web Content)
4. Insert a link to the document
   Exception occurs when spread File Explorer to select page:
   [ERROR] PortalLinkConnector - Error when perform getPageURI:  <java.lang.NullPointerException>java.lang.NullPointerException
              at org.exoplatform.wcm.connector.fckeditor.PortalLinkConnector.getPageURI(PortalLinkConnector.java:125)

Fix description

Problem analysis
This exception occurs when processing the newly added page node to build the xml response: In processPageNode method of PortalLinkConnector class, we have this part of code: 

for (String role : page.getAccessPermissions()) {
      if (EVERYONE_PERMISSION.equalsIgnoreCase(role)) {
        accessMode = PUBLIC_ACCESS;
        break;
      }

page.getAccessPermissions() returns null for the newly added node and causes the already mentioned exception.

How is the problem fixed?
* Check if the list of permission is null.
* In case where the permission list is null, leave the page as private access

Patch file: WCM-2914.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Cf. above

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
* Patch validated on behalf of PM

Support Comment
* Patch Validated

QA Feedbacks
*
