Summary

    * Status: Problem of using WCM newsletter
    * CCP Issue: N/A, Product Jira Issues: WCM-2893, WCM-3011.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Impossible to validate a subscription
* Error when sending a newsletter
* Subscriber doesn't receive newsletter.

Fix description

How is the problem fixed?
    * Register Newsletter form is reset after subscribe successfully:
          o update componentId of UINewsletterViewerPortlet to handle confirmation event.
          o fill registered data into the fields in Newsletter portlet.

    * Click Send button --> unknown error: Replace |ARCHIVE| by # in href property:
      ?
      <a href="*|ARCHIVE|*" style="font-size: 10px; color: rgb(153, 153, 153); line-height: 200%; font-family: verdana; text-decoration: none;">View it in your browser.</a>


Patch file: WCM-3011.patch

Tests to perform

Reproduction test
    * Case 1:

   1. Go to Newsletter page
   2. Input valid email
   3. Select a subscription
   4. Click Subscribe -> show message alert subscribe successfully -> OK
   5. Logout, Login into registered email -> there's mail requires to confirm. Click on the link -> jump to Newsletter page but the fields are blank -> error

    * Case 2:

   1. Create a category AA
   2. Create a Subscription(BB) for this category
   3. In the Newsletter page, subscribe to this new subscription. enter a valid email, as you will have to validate your subscription.
   4. Open Newsletter entry form : send date: current date, category : AA, Subscription : BB,
   5. Click Update Sending Parameters
   6. Input tittle for the letter
   7. Change content of letter
   8. Click Send button --> unknown error

Tests performed at DevLevel

    * Do the same tasks like Reproduction Test => No Exception's thrown, Newsletter page filled all registered data after clicking on confirm link.

Tests performed at QA/Support Level

    * cf above

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
