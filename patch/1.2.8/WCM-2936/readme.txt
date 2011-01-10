Summary

    * Status: Duplicate portlet ID in WCM portlets
    * CCP Issue: CCP-668, Product Jira Issue: WCM-2936.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When validating pages containing WCM portlets (Content List, Category Navigation, Category Content, Content by URL), an error is reported about duplicate portlet ID.

Fix description

How is the problem fixed?

    * Add time value (in millisecond ) after portlet ID

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: WCM-2936.patch

Tests to perform

Reproduction test

    * Case 1: CLV portlet: 
      Validate page acme/Overview by using W3C Validator => ERROR:
            Line 2006, column 18: ID "UICLVPortlet" already defined 
            <div id="UICLVPortlet" class="UICLVPortlet"><span class="UIPopupContain... 
            Line 2236, column 18: ID "UICLVPortlet" already defined 
            <div id="UICLVPortlet" class="UICLVPortlet"><span class="UIPopupContain... 
            Line 2445, column 18: ID "UICLVPortlet" already defined 
            <div id="UICLVPortlet" class="UICLVPortlet"><span class="UIPopupContain... 

    * Case 2: Category Navigation portlet
         1. Add new page AAA
         2. Add at least 2 Category Navigation portlet to AAA
         3. Validate AAA by using W3C Validator => ERROR:
             
            Line 1870, column 23: ID "UICategoryNavigationPortlet" already defined 
            <div id="UICategoryNavigationPortlet" class="UICategoryNavigationP... 
            Line 1872, column 10: ID "UICategoryNavigationTree" already defined 
            <div id="UICategoryNavigationTree" class="UICategoryNavigationTree"> 
            Line 1875, column 10: ID "UICategoryNavigationTreeBase" already defined 
            <div id="UICategoryNavigationTreeBase" class="SimpleVerticalHierachy"> 

    * Case 3: Category Content (PCLV) portlet 
         1. Add new page AAA
         2. Add at least 2 Category Content portlet to AAA
         3. Validate AAA by using W3C Validator => ERROR:
            
            Line 1955, column 23: ID "UIPCLVPortlet" already defined 
            <div id="UIPCLVPortlet" class="UIPCLVPortlet"><span class="UIPopup... 
            Line 1956, column 34: ID "UIPCLVContainer" already defined 
            <div class="UIPCLVContainer" id="UIPCLVContainer"> 
            Line 1960, column 28: ID "UIPCLVForm" already defined 
             	 <form class="UIForm" id="UIPCLVForm" action="/portal/private/acme/overview/Ca...

    * Case 4: Content by URL
         1. Add new page AAA
         2. Add at least 2 Content by URL portlet to AAA
         3. Validate AAA by using W3C Validator => ERROR:
            
            Line 1891, column 23: ID "UIPCVPortlet" already defined 
            <div id="UIPCVPortlet" class="UIPCVPortlet"><span class="UIPopupCo... 
            Line 1892, column 10: ID "UIPCVContainer" already defined 
            <div id="UIPCVContainer" class="UIPCVContainer"> 

Tests performed at DevLevel

    * cf above

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

    * No

Is there a performance risk/cost?

    * Probably yes

Validation (PM/Support/QA)

PM Comment

    * Validated by PM

Support Comment

    * Patch validated

QA Feedbacks
*
