Summary

    * Status: Problem of displaying an Article with comments in a SCV
    * CCP Issue: N/A, Product Jira Issue: WCM-3028.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Display problem of article with comments in FO (SCV, PCV).

Fix description

How is the problem fixed?

    * Create UIComponent to allow edit comment in SCV Portlet
    * Override methods in UIPresentation, UIPCVPresentation, UIContentViewer to return EditComment and RemoveComment components correctly (previously they returned null)
    * Add missing keys to translate tooltips

Patch file: WCM-3028.patch

Tests to perform

Reproduction test
* Steps to reproduce:
1. Login as administrator
2. Go to Sites Management/acme/documents
3. Create an article with comments and publish it
4. Go to acme/overview, add a new page (e.g AAA)
5. Add an SCV to this page, add the Article to this SCV: UI Error
6. Go to AAA page: Comments aren't displayed
Exception in server console:
?
[ERROR] portal:Lifecycle - template : /exo:ecm/templates/exo:article/views/view1 <java.lang.NullPointerException: Cannot invoke method event() on null object>java.lang.NullPointerException: Cannot invoke method event() on null object
          at org.codehaus.groovy.runtime.NullObject.invokeMethod(NullObject.java:77)
          at org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:784)
          at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:758)
          at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:170)
          at script1310959951365.run(script1310959951365.groovy:122)
      ...

Note: If opening AAA page by users who didn't comment the article, there isn't any problem

Tests performed at DevLevel
*cf above

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
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated.

Support Comment
* Patch validated.

QA Feedbacks
*

