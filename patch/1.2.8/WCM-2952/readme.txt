Summary

    * Status: [Newsletters] Wrong text of "Change subscription" button in French
    * CCP Issue: CCP-707, Product Jira Issue: WCM-2952.
    * Complexity: trivial

The Proposal
Problem description

What is the problem to fix?

    * Wrong text of "Change subscription" button in French (acme/Newsletters)

Fix description

How is the problem fixed?

    * Correct key in NewsletterViewer_fr.xml

Patch file: WCM-2952.patch

Tests to perform

Reproduction test
* Steps to reproduce:

1. Login
2. Go to ACME/Newsletters
3. Input email address and Check to subscribe
4. Click on Subscribe
5. Change language into French
Button: "ChangeSubscriptions" should be "Modifier mon abonnement"

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

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
* VALIDATED BY PM

Support Comment
* Support validated

QA Feedbacks
*
