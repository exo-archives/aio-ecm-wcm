Summary

    * Status: Error while setting a page with PCLV portlet
    * CCP Issue: CCP-368, Product Jira Issue : WCM-2753
    * Fixes also CCP-465/WCM-2845
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix ?
*  PCLV doesn't check whether the taxonomy node exists or not before accessing it.

Fix description

How is the problem fixed ?
*  Check the taxonomy node before access.

Patches file: WCM-2753.patch

Tests to perform

Reproduction test case: ALL-540
   1.  Login as admin
   2. Go to ACME site -> Select News page.
      Go to Site Editor ->Edit Page Wizard.
   3. Click Next to move to step 3
   4. Click x icon at PCLV portlet (Parameterized) to delete it from this page
   5. Click Save to complete editing page
   6. Go to Site Explorer -> Sites Management under /acme/categories delete the acme category
   7. Select News page in acme site
      -> Exception in the server console (see below)
   8. Select News page -> Site Editor -> Edit Page Wizard.
   9. Drag & drop PCLV portlet into page
  10. Click Edit portlet icon --> show popup: The target blocked to update is not found: UIMaskWorkspace
Exception in console:

ERROR [portal:Lifecycle] template : app:/groovy/CategoryNavigation/UICategoryNavigationTree.gtmpl
java.lang.NullPointerException
at org.exoplatform.wcm.webui.category.UICategoryNavigationTree.buildTree(UICategoryNavigationTree.java:234)
at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
at java.lang.reflect.Method.invoke(Method.java:597)
at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)
at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:230)
at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:912)
at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:756)
at org.codehaus.groovy.runtime.InvokerHelper.invokePojoMethod(InvokerHelper.java:766)
...
at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:286)
at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:844)
at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:583)
at org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:447)
at java.lang.Thread.run(Thread.java:595)

Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
* 

Documentation changes

Documentation Changes:
* N/A

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have an impact on current client projects?
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

