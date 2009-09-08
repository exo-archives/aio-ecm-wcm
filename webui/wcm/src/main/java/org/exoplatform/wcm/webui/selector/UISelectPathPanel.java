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
package org.exoplatform.wcm.webui.selector;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham
 * hoa.pham@exoplatform.com
 * Jun 23, 2008
 */
@ComponentConfig(
    template =  "classpath:groovy/ecm/webui/tree/selectone/UISelectPathPanel.gtmpl",
    events = {
        @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
    }
)
public class UISelectPathPanel extends UIContainer {
  
  /** The ui page iterator_. */
  private UIPageIterator uiPageIterator_;
  
  /** The accepted mime types. */
  public String[] acceptedMimeTypes = {};
  
  /** The parent node. */
  protected Node parentNode;
  
  /** The accepted node types. */
  private String[] acceptedNodeTypes = {};
  
  /** The excepted node types. */
  private String[] exceptedNodeTypes = {};
  
  /** The default excepted node types. */
  private String[] defaultExceptedNodeTypes = {};
  
  /** The allow publish. */
  private boolean allowPublish = false;
  
  /** The publication service_. */
  private PublicationService publicationService_ = null;
  
  /** The templates_. */
  private List<String> templates_ = null;
  
  /**
   * Instantiates a new uI select path panel.
   * 
   * @throws Exception the exception
   */
  public UISelectPathPanel() throws Exception { 
    uiPageIterator_ = addChild(UIPageIterator.class, null, "UISelectPathIterate");
  }
  
  /**
   * Gets the uI page iterator.
   * 
   * @return the uI page iterator
   */
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }
  
  /**
   * Checks if is allow publish.
   * 
   * @return true, if is allow publish
   */
  public boolean isAllowPublish() {
    return allowPublish;
  }

  /**
   * Sets the allow publish.
   * 
   * @param allowPublish the allow publish
   * @param publicationService the publication service
   * @param templates the templates
   */
  public void setAllowPublish(boolean allowPublish, PublicationService publicationService, List<String> templates) {
    this.allowPublish = allowPublish;
    publicationService_ = publicationService;
    templates_ = templates;
  }
  
  /**
   * Adds the node publish.
   * 
   * @param listNode the list node
   * @param node the node
   * @param publicationService the publication service
   * 
   * @throws Exception the exception
   */
  private void addNodePublish(List<Node> listNode, Node node, PublicationService publicationService) throws Exception {
    if (isAllowPublish()) {
      NodeType nt = node.getPrimaryNodeType();
      if (templates_.contains(nt.getName())) { 
        Node nodecheck = publicationService.getNodePublish(node, null);
        if (nodecheck != null) {
          listNode.add(nodecheck); 
        }
      } else {
        listNode.add(node);
      }
    } else {
      listNode.add(node);
    }
  }
  
  /**
   * Sets the parent node.
   * 
   * @param node the new parent node
   */
  public void setParentNode(Node node) { this.parentNode = node; }
  
  /**
   * Gets the parent node.
   * 
   * @return the parent node
   */
  public Node getParentNode() { return parentNode; }

  /**
   * Gets the accepted node types.
   * 
   * @return the accepted node types
   */
  public String[] getAcceptedNodeTypes() { return acceptedNodeTypes; }

  /**
   * Sets the accepted node types.
   * 
   * @param acceptedNodeTypes the new accepted node types
   */
  public void setAcceptedNodeTypes(String[] acceptedNodeTypes) { 
    this.acceptedNodeTypes = acceptedNodeTypes;
  }
  
  /**
   * Gets the excepted node types.
   * 
   * @return the excepted node types
   */
  public String[] getExceptedNodeTypes() { return exceptedNodeTypes; }

  /**
   * Sets the excepted node types.
   * 
   * @param exceptedNodeTypes the new excepted node types
   */
  public void setExceptedNodeTypes(String[] exceptedNodeTypes) { 
    this.exceptedNodeTypes = exceptedNodeTypes;
  }
  
  /**
   * Gets the default excepted node types.
   * 
   * @return the default excepted node types
   */
  public String[] getDefaultExceptedNodeTypes() { return defaultExceptedNodeTypes; }
  
  /**
   * Sets the default excepted node types.
   * 
   * @param defaultExceptedNodeTypes the new default excepted node types
   */
  public void setDefaultExceptedNodeTypes(String[] defaultExceptedNodeTypes) {
    this.defaultExceptedNodeTypes = defaultExceptedNodeTypes;
  }
  

  /**
   * Gets the accepted mime types.
   * 
   * @return the accepted mime types
   */
  public String[] getAcceptedMimeTypes() { return acceptedMimeTypes; }
  
  /**
   * Sets the accepted mime types.
   * 
   * @param acceptedMimeTypes the new accepted mime types
   */
  public void setAcceptedMimeTypes(String[] acceptedMimeTypes) { this.acceptedMimeTypes = acceptedMimeTypes; }  

  /**
   * Gets the selectable nodes.
   * 
   * @return the selectable nodes
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
	public List getSelectableNodes() throws Exception { 
	  return uiPageIterator_.getCurrentPageData(); 
	  }
  
  /**
   * Update grid.
   * 
   * @throws Exception the exception
   */
  public void updateGrid() throws Exception {
    ObjectPageList objPageList = new ObjectPageList(getListSelectableNodes(), 10);
    uiPageIterator_.setPageList(objPageList);
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
      if(matchMimeType(symChild) && matchNodeType(symChild) && isValidState(symChild)) {
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
   * Checks if is valid state.
   * 
   * @param node the node
   * 
   * @return true, if is valid state
   * 
   * @throws Exception the exception
   */
  private boolean isValidState(Node node) throws Exception {
	  WCMPublicationService publicationService = getApplicationComponent(WCMPublicationService.class);
	  String state = publicationService.getContentState(node);
	  if (state==null) return true;
	  WCMComposer composer = getApplicationComponent(WCMComposer.class);
	  List<String> states = composer.getAllowedStates(WCMComposer.MODE_EDIT);
	  return states.contains(state);
  }

  /**
   * Match node type.
   * 
   * @param node the node
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  protected boolean matchNodeType(Node node) throws Exception {
    if(acceptedNodeTypes == null || acceptedNodeTypes.length == 0) return true;
    for(String nodeType: acceptedNodeTypes) {
      if((node != null) && node.isNodeType(nodeType)) return true;
    }
    return false;
  }
  
  /**
   * Checks if is excepted node type.
   * 
   * @param node the node
   * 
   * @return true, if is excepted node type
   * 
   * @throws RepositoryException the repository exception
   */
  protected boolean isExceptedNodeType(Node node) throws RepositoryException {
    if(defaultExceptedNodeTypes.length > 0) {
      for(String nodeType: defaultExceptedNodeTypes) {
        if((node != null) && node.isNodeType(nodeType)) return true;
      }
    }
    if(exceptedNodeTypes == null || exceptedNodeTypes.length == 0) return false;
    for(String nodeType: exceptedNodeTypes) {
      if((node != null) && node.isNodeType(nodeType)) return true;
    }
    return false;
  }

  /**
   * Match mime type.
   * 
   * @param node the node
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  protected boolean matchMimeType(Node node) throws Exception {
    if(acceptedMimeTypes == null || acceptedMimeTypes.length == 0) return true;
    if(!node.isNodeType("nt:file")) return true;
    String mimeType = node.getNode("jcr:content").getProperty("jcr:mimeType").getString();
    for(String type: acceptedMimeTypes) {
      if(type.equalsIgnoreCase(mimeType))
        return true;
    }
    return false;
  }
  
  /**
   * Gets the path taxonomy.
   * 
   * @return the path taxonomy
   * 
   * @throws Exception the exception
   */
  public String getPathTaxonomy() throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    return nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
  }

  /**
   * The listener interface for receiving selectAction events.
   * The class that is interested in processing a selectAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectActionListener<code> method. When
   * the selectAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectActionEvent
   */
  static public class SelectActionListener extends EventListener<UISelectPathPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UISelectPathPanel> event) throws Exception {
      UISelectPathPanel uiSelectPathPanel = event.getSource();      
      UIContainer uiTreeSelector = uiSelectPathPanel.getParent();

      String value = event.getRequestContext().getRequestParameter(OBJECTID);
      
      if(uiTreeSelector instanceof UIOneNodePathSelector) {
        if(!((UIOneNodePathSelector)uiTreeSelector).isDisable()) {
          value = ((UIOneNodePathSelector)uiTreeSelector).getWorkspaceName() + ":" + value ;
        }
      } 
      String returnField = ((UIBaseNodeTreeSelector)uiTreeSelector).getReturnFieldName();
      ((UISelectable)((UIBaseNodeTreeSelector)uiTreeSelector).getSourceComponent()).doSelect(returnField, value) ;
      
      UIComponent uiOneNodePathSelector = uiSelectPathPanel.getParent();
      if (uiOneNodePathSelector instanceof UIOneNodePathSelector) {
        UIComponent uiComponent = uiOneNodePathSelector.getParent();
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent);
        }
        UIComponent component = ((UIOneNodePathSelector)uiOneNodePathSelector).getSourceComponent().getParent();
        if (component != null) {
          event.getRequestContext().addUIComponentToUpdateByAjax(component);
        }
      }
    }
  }
}
