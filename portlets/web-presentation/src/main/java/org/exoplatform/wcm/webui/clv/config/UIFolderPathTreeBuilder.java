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
package org.exoplatform.wcm.webui.clv.config;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.webui.tree.UINodeTree;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 16, 2008
 */

@ComponentConfig(events = @EventConfig(listeners = UINodeTreeBuilder.ChangeNodeActionListener.class))
public class UIFolderPathTreeBuilder extends UINodeTreeBuilder {

  private Node currentPortal;

  private Node sharedPortal;

  public UIFolderPathTreeBuilder() throws Exception {
    super();
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    Writer writer = context.getWriter();
    String folderExplorerTitle = context.getApplicationResourceBundle().getString(
        "UIFolderExplorer.title");
    writer.write("<div class=\"FolderExplorer\">");
    writer.write("<div class=\"TitleBox\">");
    writer.write("<p>" + folderExplorerTitle + "</p>");
    writer.write("</div>");
    writer.write("<div class=\"FolderExplorerTree\">");
    writer.write("<div class=\"FolderInnerExplorerTree\">");
    buildTree();
    super.renderChildren();
    writer.write("</div>");
    writer.write("</div>");
    writer.write("</div>");
  }

  public Node getCurrentPortal() {
    return currentPortal;
  }

  public void setCurrentPortal(Node currentPortal) {
    this.currentPortal = currentPortal;
  }

  public Node getSharedPortal() {
    return sharedPortal;
  }

  public void setSharedPortal(Node sharedPortal) {
    this.sharedPortal = sharedPortal;
  }

  public void buildTree() throws Exception {
    UINodeTree tree = getChild(UINodeTree.class);
    tree.setSelected(currentNode);
    String currentPath = currentNode.getPath();
    String currentPortalPath = currentPortal.getPath();
    String sharedPortalPath = sharedPortal.getPath();
    Node webContentsFolder = null;
    Node documentsFolder = null;
    if (currentNode.getPath().equals(rootTreeNode.getPath())) {
      List<Node> portals = new ArrayList<Node>();
      portals.add(currentPortal);
      portals.add(sharedPortal);
      tree.setSibbling(portals);
      tree.setParentSelected(rootTreeNode);
    } else if (currentPath.equals(sharedPortalPath)) {
      webContentsFolder = getWebContentsFolder(sharedPortal);
      documentsFolder = getDocumentsFolder(sharedPortal);
      List<Node> children = new ArrayList<Node>();
      children.add(webContentsFolder);
      children.add(documentsFolder);
      tree.setChildren(children);
      tree.setSibbling(children);
      tree.setParentSelected(rootTreeNode);
    } else if (currentPath.equals(currentPortalPath)) {
      webContentsFolder = getWebContentsFolder(currentPortal);
      documentsFolder = getDocumentsFolder(currentPortal);
      List<Node> children = new ArrayList<Node>();
      children.add(webContentsFolder);
      children.add(documentsFolder);
      tree.setChildren(children);
      tree.setSibbling(children);
      tree.setParentSelected(rootTreeNode);
    } else {
      List<Node> children = filterSubFolder(currentNode);
      tree.setChildren(children);
      tree.setSibbling(children);
      tree.setParentSelected(currentNode.getParent());
    }

  }

  private List<Node> filterSubFolder(Node parent) throws Exception {
    List<Node> subFolderList = new ArrayList<Node>();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = repositoryService.getCurrentRepository().getConfiguration().getName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> listDocumentTypes = templateService.getDocumentTemplates(repository);
    for (NodeIterator iterator = parent.getNodes(); iterator.hasNext();) {
      Node child = iterator.nextNode();
      NodeType nodeType = child.getPrimaryNodeType();
      if ((nodeType.isNodeType("nt:folder") || nodeType.isNodeType("nt:unstructured"))
          && (! listDocumentTypes.contains(nodeType.getName()))) {
        subFolderList.add(child);
      }
    }
    return subFolderList;
  }

  public Node getWebContentsFolder(Node portal) throws Exception {
    WebSchemaConfigService configService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = configService
        .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return portalFolderSchemaHandler.getWebContentStorage(portal);
  }

  public Node getDocumentsFolder(Node portal) throws Exception {
    WebSchemaConfigService configService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = configService
        .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return portalFolderSchemaHandler.getDocumentStorage(portal);
  }
}
