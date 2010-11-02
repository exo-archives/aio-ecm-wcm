Summary

    * Status: Problem with WCMInsertContent when browsing directories with illegal JCR character '
    * CCP Issue: CCP-533, Product Jira Issue: WCM-2882.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
   Problems with WCMInsertContent when browsing directories with illegal JCR character '
   * In Firefox, an exception is thrown when browsing that folder. 
   * In any browser, %27 is displayed inspite of '.

Fix description

How is the problem fixed?

    * Encode the folder name before sending the request.
    * Decode the folder name before displaying
    * Encode also the drive name.
    * Decode the drive name before displaying

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2882.patch

Tests to perform

Reproduction test

   To reproduce the problem you should follow these steps (using Firefox):

   1. In BO, choose sites content/acme/documents
   2. Add a folder "tes_'sss"
   3. Upload a picture on it
   4. Turn back to FO
   5. Add a new Content and choose Free Layout WebContent.
   6. In the Fckeditor of this newly added content select Insert Contents Icon
   7. Browse content:sites content->Acme->Documents->tes_'sss
      An error is observed:

      [ERROR] DriverConnector - Error when perform getFoldersAndFiles:  <javax.jcr.RepositoryException: Illegal path entry: "tes_'sss": Illegal path entry: "tes_'sss": Illeg
      al path entry: "tes_'sss">javax.jcr.RepositoryException: Illegal path entry: "tes_'sss": Illegal path entry: "tes_'sss": Illegal path entry: "tes_'sss"
              at org.exoplatform.services.jcr.impl.core.LocationFactory.parsePathEntry(LocationFactory.java:204)
              at org.exoplatform.services.jcr.impl.core.LocationFactory.parseNames(LocationFactory.java:237)
              at org.exoplatform.services.jcr.impl.core.LocationFactory.parseAbsPath(LocationFactory.java:86)
              at org.exoplatform.services.jcr.impl.core.SessionImpl.getItem(SessionImpl.java:516)
              at org.exoplatform.services.jcr.impl.core.SessionImpl.getItem(SessionImpl.java:96)
              at org.exoplatform.wcm.connector.fckeditor.DriverConnector.getFoldersAndFiles(DriverConnector.java:203)
      ...

Tests performed at DevLevel

    * Cf. above

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
    * No

Configuration changes

Configuration changes:

    * None

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Support team review: Proposed patch validated

QA Feedbacks
*

