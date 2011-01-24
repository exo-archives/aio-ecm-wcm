/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wcm.webui.selector.folder;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.wcm.webui.selector.UISelectPathPanel;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham
 * hoa.pham@exoplatform.com
 * Jun 23, 2008
 */
@ComponentConfig(
                 template =  "classpath:groovy/wcm/webui/selector/UISelectPathPanel.gtmpl",
                 events = {
                     @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
                 }
)
public class UISelectPathPanelFolder extends UISelectPathPanel {
  private String repositoryName_;

  public UISelectPathPanelFolder() throws Exception {
	  super();
	  RepositoryService repoService = WCMCoreUtils.getService(RepositoryService.class);
	  this.repositoryName_ = repoService.getCurrentRepository().getConfiguration().getName();	  
  }
  public String getRepositoryName(){
	  return repositoryName_;
  }
  public void setRepositoryName(String repositoryName){
	  repositoryName_ = repositoryName;
  }

  /**
   * Gets the list selectable nodes.
   * 
   * @return the list selectable nodes
   * 
   * @throws Exception the exception
   */
  public List<Node> getListSelectableNodes() throws Exception {	
    List<Node> list = new ArrayList<Node>();
    if (parentNode == null) return list;
    Node realNode = Utils.getNodeSymLink(parentNode);
    for (NodeIterator iterator = realNode.getNodes();iterator.hasNext();) {
      Node child = iterator.nextNode();
      if(child.isNodeType("exo:hiddenable")) continue;
      Node symChild= Utils.getNodeSymLink(child);
      if(isFolder(symChild)) {
        list.add(child);
      }
    }
    List<Node> listNodeCheck = new ArrayList<Node>();
    for (Node node : list) {
      addNodePublish(listNodeCheck, node, publicationService_);
    }
    return listNodeCheck;
  }
  
  /**
   * Checks if is folder.
   * 
   * @param node the node
   * 
   * @return true, if is folder
   * 
   * @throws Exception the exception
   */
  private boolean isFolder(Node node) throws Exception{
	 
  	return 		!isDocumentType(node) && 
  						(node.isNodeType("nt:folder") || node.isNodeType("nt:unstructured") || node.isNodeType("exo:taxonomy"));
  }
  /**
   * Checks if is document type.
   * 
   * @param node the node
   * 
   * @return true, if is folder
   * 
   * @throws Exception the exception
   */
  private boolean isDocumentType(Node node) throws Exception {
	  if (repositoryName_.equals(null)){
		  RepositoryService repoService = WCMCoreUtils.getService(RepositoryService.class);
		  repositoryName_ = repoService.getCurrentRepository().getConfiguration().getName();
	  }
	  TemplateService templateService = WCMCoreUtils.getService(TemplateService.class);	  	
	  List<String> documentList = templateService.getAllDocumentNodeTypes(repositoryName_);	  	
	  for (String documentType : documentList) {		  
		  if (node.getPrimaryNodeType().isNodeType(documentType))	return true;		   
	  }
	  return false;
  }
}
