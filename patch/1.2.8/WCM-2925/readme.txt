Summary

    * Status: The selection of repository brings up some content
    * CCP Issue: CCP-639, Product Jira Issue: WCM-2925.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Select the automatic mode in a CLV Portlet, then press the + button to set the repository.

The window to select repositories appears.

In a directory, all sub-directories can be selected, but it is also possible to select some types of content (like the node type exo:link). These nodes type have the supertype nt:unstructured.
The problem is that any node type which has the supertype nt: unstructured is considered as a folder according to the method isFolder of the classe UISelectPathPanelFolder:

private boolean isFolder(Node node) throws Exception{
  	return 		!node.isNodeType("exo:webContent") &&
  						(node.isNodeType("nt:folder") || node.isNodeType("nt:unstructured") || node.isNodeType("exo:taxonomy"));
  }

Fix description

How is the problem fixed?
    * Nodes are not well filtered when the purpose is only to show folder path to choose hence some node types are shown unwantedly and cause the problem.
    * Create function filters (isDocumentType) in UpdateTreeNodeConnecter.java and UISelectPathPanelFolder.java to filter out the unwanted nodes.

Patch file: WCM-2925.patch

Tests to perform

Reproduction test

   1. Create a content of type exo:link (Web Link template), named weblinkdoc
   2. Edit CLV in automatic mode
   3. Open the folder containing that new content. weblinkdoc appears as a folder.

Tests performed at DevLevel
* repeat the steps of reproduction test and other related test

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* no

Configuration changes

Configuration changes:
* no

Will previous configuration continue to work?
* yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
* no

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

