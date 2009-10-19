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
package org.exoplatform.wcm.webui.selector.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.wcm.webui.selector.UISelectPathPanel;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : maivanha1610@gmail.com
 */

@ComponentConfig(
                 template = "classpath:groovy/wcm/webui/selector/content/UIContentTreeBuilder.gtmpl",
                 events = @EventConfig(listeners = UIContentTreeBuilder.ChangeNodeActionListener.class)
)
public class UIContentTreeBuilder extends UIContainer {
  
  @SuppressWarnings("unused")
	private String path;

  /**
   * Instantiates a new uI web content tree builder.
   * 
   * @throws Exception the exception
   */
  public UIContentTreeBuilder() throws Exception {  }

  /**
   * Checks if is sym link.
   * 
   * @param node the node
   * 
   * @return true, if is sym link
   * 
   * @throws Exception the exception
   */
  public boolean isSymLink(Node node) throws Exception {
    LinkManager linkManager = getApplicationComponent(LinkManager.class);
    return linkManager.isLink(node);
  }

  /**
   * Gets the drives.
   * 
   * @return the drives
   * 
   * @throws Exception the exception
   */
  private List<DriveData> getDrives() throws Exception {    
    String repoName = getApplicationComponent(RepositoryService.class).getDefaultRepository().getConfiguration().getName();
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class);      
    List<DriveData> driveList = new ArrayList<DriveData>();    
    List<String> userRoles = Utils.getMemberships();    
    List<DriveData> allDrives = driveService.getAllDrives(repoName);
    Set<DriveData> temp = new HashSet<DriveData>();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if (userId != null) {
      // We will improve ManageDrive service to allow getAllDriveByUser
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        boolean flag = false;
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("${userId}")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (flag)
            continue;
          for (String rolse : userRoles) {
            if (driveData.hasPermission(allPermission, rolse)) {
              temp.add(driveData);
              break;
            }
          }
        }
      }
    } else {
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            break;
          }
        }
      }
    }

    for(Iterator<DriveData> iterator = temp.iterator();iterator.hasNext();) {
      driveList.add(iterator.next());
    }
    Collections.sort(driveList);
    return driveList; 
  }


  /**
   * Personal drives.
   * 
   * @param driveList the drive list
   * 
   * @return the list< drive data>
   */
  private List<DriveData> personalDrives(List<DriveData> driveList) {
    List<DriveData> personalDrives = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    String currentUser = Util.getPortalRequestContext().getRemoteUser();
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(userPath + "/${userId}/")) {
        drive.setHomePath(drive.getHomePath().replace("${userId}", currentUser));
        personalDrives.add(drive);
      }
    }
    Collections.sort(personalDrives);
    return personalDrives;
  }

  /**
   * Group drives.
   * 
   * @param driveList the drive list
   * 
   * @return the list< drive data>
   * 
   * @throws Exception the exception
   */
  private List<DriveData> groupDrives(List<DriveData> driveList) throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    List<DriveData> groupDrives = new ArrayList<DriveData>();
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    List<String> groups = Utils.getGroups();
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(groupPath)) {
        for(String group : groups) {
          if(drive.getHomePath().equals(groupPath + group)) {
            groupDrives.add(drive);
            break;
          }
        }
      } 
    }
    Collections.sort(groupDrives);
    return groupDrives;
  }

  /**
   * General drives.
   * 
   * @param driveList the drive list
   * 
   * @return the list< drive data>
   * 
   * @throws Exception the exception
   */
  private List<DriveData> generalDrives(List<DriveData> driveList) throws Exception {
    List<DriveData> generalDrives = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    for(DriveData drive : driveList) {
      if((!drive.getHomePath().startsWith(userPath) && !drive.getHomePath().startsWith(groupPath)) 
          || drive.getHomePath().equals(userPath)) {
        generalDrives.add(drive);
      }
    }
    return generalDrives;
  }

  /**
   * Gets the session.
   * 
   * @param workSpaceName the work space name
   * 
   * @return the session
   * 
   * @throws Exception the exception
   */
  private Session getSession(String workSpaceName) throws Exception {  
    return SessionProviderFactory.createSessionProvider().getSession(workSpaceName, 
                                                                     getApplicationComponent(RepositoryService.class).getDefaultRepository());
  }

  /**
   * Adds the tree from drives.
   * 
   * @param path the path
   * @param list the list
   * @param listDris the list dris
   * 
   * @throws Exception the exception
   */
  private void addTreeFromDrives(String path, List<UIContentTreeNode> list, List<DriveData> listDris) throws Exception{
    for(DriveData dri : listDris){
      try{
        Node node = (Node)getSession(dri.getWorkspace()).getItem(dri.getHomePath());
        list.add(new UIContentTreeNode(path, dri.getName(), dri.getWorkspace(), node, 1));
      }catch(Exception ex){ }
    }
  }

  /**
   * Adds the root drives.
   * 
   * @param list the list
   * 
   * @throws Exception the exception
   */
  private void addRootDrives(List<UIContentTreeNode> list) throws Exception{
    List<DriveData> listDris = getDrives();
    // Add Personal Drives into tree view
    list.add(new UIContentTreeNode("Personal Drives"));
    addTreeFromDrives("/Personal Drives", list, personalDrives(listDris));

    // Add Group Drives into tree view
    list.add(new UIContentTreeNode("Group Drives"));
    addTreeFromDrives("/Group Drives", list, groupDrives(listDris));

    // Add General Drives into tree view
    list.add(new UIContentTreeNode("General Drives"));
    addTreeFromDrives("/General Drives", list, generalDrives(listDris));
  }

  /**
   * Gets the tree node.
   * 
   * @return the tree node
   * 
   * @throws Exception the exception
   */
  public List<UIContentTreeNode> getTreeNode() throws Exception{
    List<UIContentTreeNode> list = new ArrayList<UIContentTreeNode>();
    addRootDrives(list);
    String workSpaceName = null;
    UIContentTreeNode treeNode = null;
    NodeIterator nodeIterator = null;
    Node node = null;
    int j = 0;
    int deep = 0;
    for(int i = 0; i < list.size(); i ++){
      treeNode = list.get(i);
      node = treeNode.getNode();
      if(node != null){
        j = i + 1;
        deep = treeNode.getDeep() + 1;
        if(deep == 2){
          workSpaceName = treeNode.getWorkSpaceName();
        }
        nodeIterator = node.getNodes();
        while(nodeIterator.hasNext()){
          try{
            node = nodeIterator.nextNode();
            if (!node.isNodeType(NodetypeConstant.EXO_WEBCONTENT) && !node.isNodeType(NodetypeConstant.EXO_HIDDENABLE) &&
            		(node.isNodeType(NodetypeConstant.EXO_TAXONOMY) || node.isNodeType(NodetypeConstant.NT_UNSTRUCTURED) || node.isNodeType(NodetypeConstant.NT_FOLDER)) ) {
              list.add(j, new UIContentTreeNode(treeNode.getTreePath(), workSpaceName, node, deep));
              j ++;
            }
          }catch(Exception ex){
            ex.printStackTrace();
          }
        }
      }
    }
    return list;
  }

  /**
   * The listener interface for receiving changeNodeAction events.
   * The class that is interested in processing a changeNodeAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addChangeNodeActionListener<code> method. When
   * the changeNodeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChangeNodeActionEvent
   */
  static public class ChangeNodeActionListener extends EventListener<UIContentTreeBuilder> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentTreeBuilder> event) throws Exception {
      UIContentTreeBuilder contentTreeBuilder = event.getSource();      
      String values = event.getRequestContext().getRequestParameter(OBJECTID);
      contentTreeBuilder.path = values.substring(values.lastIndexOf("/") + 1);
      values = values.substring(0, values.lastIndexOf("/"));
      String workSpaceName = values.substring(values.lastIndexOf("/") + 1);
      String nodePath = values.substring(0, values.lastIndexOf("/"));
      Node rootNode = (Node)contentTreeBuilder.getSession(workSpaceName).getItem(nodePath);
      UISelectPathPanel selectPathPanel = contentTreeBuilder.getAncestorOfType(UIContentBrowsePanel.class).getChild(UISelectPathPanel.class);
      selectPathPanel.setParentNode(rootNode);
      selectPathPanel.updateGrid();
      event.getRequestContext().addUIComponentToUpdateByAjax(selectPathPanel);
    }
  }
}
