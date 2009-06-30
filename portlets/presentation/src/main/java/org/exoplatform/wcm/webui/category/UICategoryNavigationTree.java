/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.category;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 19, 2009  
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "app:/groovy/CategoryNavigation/UICategoryNavigationTree.gtmpl",
    events = {
      @EventConfig(listeners = UICategoryNavigationTree.ChangeNodeActionListener.class)
    }
)
public class UICategoryNavigationTree extends UIContainer {
  
  private boolean            allowPublish        = false;

  private PublicationService publicationService_ = null;

  private List<String>       templates_          = null;

  private String[]           acceptedNodeTypes   = {};

  /** The root tree node. */
  protected Node             rootTreeNode;

  /** The current node. */
  protected Node             currentNode;

  public boolean isAllowPublish() {
    return allowPublish;
  }

  public void setAllowPublish(boolean allowPublish, PublicationService publicationService, List<String> templates) {
    this.allowPublish = allowPublish;
    publicationService_ = publicationService;
    templates_ = templates;
  }

  /**
   * Instantiates a new uI node tree builder.
   * 
   * @throws Exception the exception
   */
  public UICategoryNavigationTree() throws Exception {
    
    PortletPreferences portletPreferences = UICategoryNavigationUtils.getPortletPreferences();
    String preferenceRepository = portletPreferences.getValue(UICategoryNavigationConstant.PREFERENCE_REPOSITORY, "");
    String preferenceTreeName = portletPreferences.getValue(UICategoryNavigationConstant.PREFERENCE_TREE_NAME, "");
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    Node rootTreeNode = taxonomyService.getTaxonomyTree(preferenceRepository, preferenceTreeName);
    setRootTreeNode(rootTreeNode);
    setAcceptedNodeTypes(new String[] {"nt:folder", "nt:unstructured", "nt:file", "exo:taxonomy"});
    
    UITree tree = addChild(UICategoryNavigationTreeBase.class, null, null);
    tree.setBeanLabelField("name");
    tree.setBeanIdField("path");
  }

  /**
   * Gets the root tree node.
   * 
   * @return the root tree node
   */
  public Node getRootTreeNode() {
    return rootTreeNode;
  }

  /**
   * Sets the root tree node.
   * 
   * @param node the new root tree node
   * @throws Exception the exception
   */
  public final void setRootTreeNode(Node node) throws Exception {
    this.rootTreeNode = node;
    this.currentNode = node;
  }

  /**
   * Gets the current node.
   * 
   * @return the current node
   */
  public Node getCurrentNode() {
    return currentNode;
  }

  /**
   * Sets the current node.
   * 
   * @param currentNode the new current node
   */
  public void setCurrentNode(Node currentNode) {
    this.currentNode = currentNode;
  }

  /**
   * Gets the accepted node types.
   * 
   * @return the accepted node types
   */
  public String[] getAcceptedNodeTypes() {
    return acceptedNodeTypes;
  }

  /**
   * Sets the accepted node types.
   * 
   * @param acceptedNodeTypes the new accepted node types
   */
  public void setAcceptedNodeTypes(String[] acceptedNodeTypes) {
    this.acceptedNodeTypes = acceptedNodeTypes;
  }

  public void buildTree() throws Exception {
    NodeIterator sibbling = null;
    NodeIterator children = null;
    UICategoryNavigationTreeBase tree = getChild(UICategoryNavigationTreeBase.class);
    Node selectedNode = currentNode;
    tree.setSelected(selectedNode);
    if (Utils.getNodeSymLink(selectedNode).getDepth() > 0) {
      tree.setParentSelected(selectedNode.getParent());
      sibbling = Utils.getNodeSymLink(selectedNode).getNodes();
      children = Utils.getNodeSymLink(selectedNode).getNodes();
    } else {
      tree.setParentSelected(selectedNode);
      sibbling = Utils.getNodeSymLink(selectedNode).getNodes();
      children = null;
    }
    if (sibbling != null) {
      tree.setSibbling(filter(sibbling));
    }
    if (children != null) {
      tree.setChildren(filter(children));
    }
  }

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

  private List<Node> filter(final NodeIterator iterator) throws Exception {
    List<Node> list = new ArrayList<Node>();
    if (acceptedNodeTypes.length > 0) {
      for (; iterator.hasNext();) {
        Node sibbling = iterator.nextNode();
        if (sibbling.isNodeType("exo:hiddenable"))
          continue;
        for (String nodetype : acceptedNodeTypes) {
          if (sibbling.isNodeType(nodetype)) {
            list.add(sibbling);
            break;
          }
        }
      }
      List<Node> listNodeCheck = new ArrayList<Node>();
      for (Node node : list) {
        addNodePublish(listNodeCheck, node, publicationService_);
      }
      return listNodeCheck;
    }
    for (; iterator.hasNext();) {
      Node sibbling = iterator.nextNode();
      if (sibbling.isNodeType("exo:hiddenable"))
        continue;
      list.add(sibbling);
    }
    List<Node> listNodeCheck = new ArrayList<Node>();
    for (Node node : list)
      addNodePublish(listNodeCheck, node, publicationService_);
    return listNodeCheck;
  }

  /**
   * When a node is change in tree. This method will be rerender the children & sibbling nodes of 
   * current node and broadcast change node event to other uicomponent
   * 
   * @param path the path
   * @param requestContext the request context
   * @throws Exception the exception
   */
  public void changeNode(String path, Object context) throws Exception {
    NodeFinder nodeFinder_ = getApplicationComponent(NodeFinder.class);
    String rootPath = rootTreeNode.getPath();
    if (rootPath.equals(path) || !path.startsWith(rootPath)) {
      currentNode = rootTreeNode;
    } else {
      if (path.startsWith(rootPath))
        path = path.substring(rootPath.length());
      if (path.startsWith("/"))
        path = path.substring(1);
      currentNode = nodeFinder_.getNode(rootTreeNode, path);
    }
  }

  /**
   * The listener interface for receiving changeNodeAction events. The class
   * that is interested in processing a changeNodeAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addChangeNodeActionListener<code> method. When
   * the changeNodeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChangeNodeActionEvent
   */
  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UICategoryNavigationTree categoryNavigationTree = event.getSource().getParent();
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      categoryNavigationTree.changeNode(uri, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(categoryNavigationTree.getParent());
    }
  }
}
