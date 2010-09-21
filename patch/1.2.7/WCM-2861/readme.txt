Summary

    * Status: A problem with WCMInsertContent when browsing directories with special characters (IE7)
    * CCP Issue: CCP-438, Product Jira Issue: WCM-2861
    * Complexity: low.

The Proposal
Problem description

What is the problem to fix?

    * Environment: IE7.
      Context: edit a document using CK Editor.
      When click on WCMInsertContent, it's impossible to access to a folder with name containing some special characters such as é, è or à.

Fix description

How is the problem fixed?

    * Encode the folder's name before sending the request.
    * Encode also the driver name.

Patch information:
Patches files:
WCM-2861.patch

Tests to perform

Which test should have detected the issue?
To reproduce the problem (using IE7):

Case 1: already fixed in AIO 1.6.6

   1. In BO, choose File Explorer: Managed Sites -> acme -> documents
   2. Add a folder "testé"
   3. Upload a picture on it
   4. Add a new content (BO or FO), choose Free Layout WebContent as template.
   5. In the Fckeditor of this newly added content, select WCInsertContent icon
   6. Browse content: Managed Sites->acme->documents->testé

An error is observed:

[ERROR] DriverConnector - Error when perform getFoldersAndFiles:  <javax.jcr.PathNotFoundException: Item not found /sites content/live/acme/documents/test? in workspac
e collaboration>javax.jcr.PathNotFoundException: Item not found /sites content/live/acme/documents/test? in workspace collaboration
        at org.exoplatform.wcm.connector.fckeditor.DriverConnector.getFoldersAndFiles(DriverConnector.java:196)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
...

The path is not recognized since it contain a special character.

Case 2: to fix in AIO 1.6.7

   1. Create a new drive "testéDrive" with home path shared for example. Add all permissions to it
   2. Create a new content (Free Layout webcontent)
   3. Click on WCMInsertContent
      In the tree explorer select the "testéDrive"

This exception occurs:

[ERROR] DriverConnector - Error when perform getFoldersAndFiles:  <java.lang.NullPointerException>java.lang.NullPointerException
        at org.exoplatform.wcm.connector.fckeditor.DriverConnector.getFoldersAndFiles(DriverConnector.java:198)
...

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*


Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration?
* No.

Describe configuration changes:
*

Previous configuration will continue to work?
* Yes.


Risks and impacts

Is there a risk applying this bug fix?
* None.

Can this bug fix have an impact on current client projects?
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

