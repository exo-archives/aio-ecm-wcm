Summary

    * Status: A problem with WCMInsertContent when browsing directories with special characters (IE7)
    * CCP Issue: CCP-438, Product Jira Issue: WCM-2861
    * Complexity: low.
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * Environment: IE7.
      Context: edit a document using CK Editor.
      When click on WCMInsertContent, it's impossible to access to a folder with name containing some special characters such as é, è or à.

Fix description

How is the problem fixed?

    * Encode the folder's name before sending the request.

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File WCM2861.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue ?
To reproduce the problem (using IE7):
1- In BO, choose File Explorer: Managed Sites -> acme -> documents
2- Add a folder "testé"
3- Upload a picture on it
4- Add a new content (BO or FO), choose Free Layout WebContent as template.
6- In the Fckeditor of this newly added content, select WCInsertContent icon
7- Browse content: Managed Sites->acme->documents->testé

An error is observed:

[ERROR] DriverConnector - Error when perform getFoldersAndFiles:  <javax.jcr.PathNotFoundException: Item not found /sites content/live/acme/documents/test? in workspac
e collaboration>javax.jcr.PathNotFoundException: Item not found /sites content/live/acme/documents/test? in workspace collaboration
        at org.exoplatform.wcm.connector.fckeditor.DriverConnector.getFoldersAndFiles(DriverConnector.java:196)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
...

The path is not recognized since it contain a special character.

Is a test missing in the TestCase file ?
*

Added UnitTest ?
* No.

Recommended Performance test?
*
Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*
Configuration changes

Is this bug changing the product configuration ?
* No.

Describe configuration changes:
*

Previous configuration will continue to work?
* Yes.
Risks and impacts

Is there a risk applying this bug fix ?
* None.

Is this bug fix can have an impact on current client projects ?
*

Is there a performance risk/cost?
* No.
Validation By PM & Support

PM Comment
* VALIDATED BY PM

Support Comment
* Validated by Support Team
QA Feedbacks

Performed Tests
*

