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
package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.webui.tree.UINodeTree;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com Jun 23, 2008
 */
@ComponentConfig(
    events = @EventConfig(listeners = UINodeTreeBuilder.ChangeNodeActionListener.class)
)
public class UIWebContentTreeBuilder extends UINodeTreeBuilder {

  /** The current portal. */
  private Node currentPortal;
  
  /** The shared portal. */
  private Node sharedPortal;

  /**
   * Instantiates a new uI web content tree builder.
   * 
   * @throws Exception the exception
   */
  public UIWebContentTreeBuilder() throws Exception {
    super();
  }

  /**
   * Gets the current portal.
   * 
   * @return the current portal
   */
  public Node getCurrentPortal() { return currentPortal; }

  /**
   * Sets the current portal.
   * 
   * @param currentPortal the new current portal
   */
  public void setCurrentPortal(Node currentPortal) {
    this.currentPortal = currentPortal;
  }

  /**
   * Gets the shared portal.
   * 
   * @return the shared portal
   */
  public Node getSharedPortal() { return sharedPortal; }

  /**
   * Sets the shared portal.
   * 
   * @param sharedPortal the new shared portal
   */
  public void setSharedPortal(Node sharedPortal) {
    this.sharedPortal = sharedPortal;
  }  

  /* 
   * build web content tree for web content path selector
   *
   */  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UINodeTreeBuilder#buildTree()
   */
  public void buildTree() throws Exception {       
    UINodeTree tree = getChild(UINodeTree.class) ;   
    tree.setSelected(currentNode);
    String currentPath = currentNode.getPath();
    String currentPortalPath = currentPortal.getPath();
    String sharedPortalPath = sharedPortal.getPath();    
    if(currentNode.getPath().equals(rootTreeNode.getPath())) {      
      List<Node> portals = new ArrayList<Node>();
      portals.add(currentPortal);
      portals.add(sharedPortal);
      tree.setChildren(portals);
      tree.setSibbling(portals);
      tree.setParentSelected(rootTreeNode);
    }else if(currentPath.equals(sharedPortalPath)) {
      Node webStorage = getWebContentStorage(sharedPortal);
      List<Node> children = filterWebContentFolder(webStorage);
      tree.setChildren(children);
      tree.setSibbling(children);
      tree.setParentSelected(rootTreeNode);
    }else if(currentPath.equals(currentPortalPath)) {
      Node webStorage = getWebContentStorage(currentPortal);
      List<Node> children = filterWebContentFolder(webStorage);      
      tree.setChildren(children);      
      tree.setSibbling(children);
      tree.setParentSelected(rootTreeNode);
    }else if(currentPath.startsWith(currentPortalPath) || currentPath.startsWith(sharedPortalPath)) {
      if(currentNode.getParent().getPath().equals(currentPortal.getPath())) {
        Node webStorage = getWebContentStorage(currentPortal);
        List<Node> children = filterWebContentFolder(webStorage);
        tree.setChildren(children);
        tree.setSibbling(children);
        tree.setParentSelected(rootTreeNode);
      } else if(currentNode.getParent().getPath().equals(sharedPortal.getPath())) {
        Node webStorage = getWebContentStorage(sharedPortal);
        List<Node> children = filterWebContentFolder(webStorage);
        tree.setChildren(children);
        tree.setSibbling(children);
        tree.setParentSelected(rootTreeNode);
      } else {
        List<Node> sibbling = filterWebContentFolder(currentNode.getParent());
        List<Node> children = filterWebContentFolder(currentNode);
        tree.setChildren(children);
        tree.setSibbling(sibbling);
        tree.setParentSelected(currentNode.getParent());
      }
    }    
  }

  /**
   * Filter web content folder.
   * 
   * @param parent the parent
   * 
   * @return the list< node>
   * 
   * @throws Exception the exception
   */
  private List<Node> filterWebContentFolder(Node parent) throws Exception {
    List<Node> webContentList = new ArrayList<Node>();  
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String repository = ((ManageableRepository)parent.getSession().getRepository()).getConfiguration().getName();    
    for(NodeIterator iterator = parent.getNodes();iterator.hasNext();) {
      Node child = iterator.nextNode();
      NodeType nodeType = child.getPrimaryNodeType();
      if(nodeType.isNodeType("nt:unstructured") && !templateService.isManagedNodeType(nodeType.getName(),repository)) {
        webContentList.add(child);
      }
    }
    return webContentList;
  }

  /**
   * Gets the web content storage.
   * 
   * @param portal the portal
   * 
   * @return the web content storage
   * 
   * @throws Exception the exception
   */
  private Node getWebContentStorage(Node portal) throws Exception {
    WebSchemaConfigService configService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = configService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return portalFolderSchemaHandler.getWebContentStorage(portal);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UINodeTreeBuilder#changeNode(java.lang.String, org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void changeNode(String path, Object requestContext) throws Exception {
    if(path == null) return ;
    String rootPath = rootTreeNode.getPath();
    Node node = null;
    if(rootPath.equals(path) || !path.startsWith(rootPath)) {
      node = rootTreeNode;
      currentNode = rootTreeNode;
    } else if(path.equals(currentPortal.getPath())) {
      node = getWebContentStorage(currentPortal);
      currentNode = currentPortal;
    } else if(path.equals(sharedPortal.getPath())) {
      node = getWebContentStorage(sharedPortal);
      currentNode = sharedPortal;
    }
    else {
      node = (Node)rootTreeNode.getSession().getItem(path);
      currentNode = node;
    }
    broadcastOnChange(node,requestContext);
  }
}
