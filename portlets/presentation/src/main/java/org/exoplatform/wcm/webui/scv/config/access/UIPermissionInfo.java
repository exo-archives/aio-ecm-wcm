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
package org.exoplatform.wcm.webui.scv.config.access;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.scv.config.UIContentDialogForm;
import org.exoplatform.wcm.webui.scv.config.UIQuickCreationWizard;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Aug 15, 2008
 */

/**
 * The Class UIPermissionInfo.
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class, 
    events = {
      @EventConfig(
          listeners = UIPermissionInfo.DeleteActionListener.class, 
          confirm = "UIPermissionInfo.msg.confirm-delete-permission"),
          @EventConfig(listeners = UIPermissionInfo.EditActionListener.class) 
    }
)
public class UIPermissionInfo extends UIContainer {

  /** The PERMISSIO n_ field. */
  public static String[]  PERMISSION_FIELD  = { "usersOrGroups", "accessible", "editable" };

  /** The PERMISSIO n_ action. */
  private static String[] PERMISSION_ACTION = { "Edit", "Delete" };

  /**
   * Instantiates a new uI permission info.
   * 
   * @throws Exception the exception
   */
  public UIPermissionInfo() throws Exception {
    UIGrid uiGrid = createUIComponent(UIGrid.class, null, "PermissionInfo");
    uiGrid.getUIPageIterator().setId("PermissionInfoIterator");
    uiGrid.configure("usersOrGroups", PERMISSION_FIELD, PERMISSION_ACTION);
    addChild(uiGrid);
  }

  /**
   * Gets the node owner.
   * 
   * @param node the node
   * 
   * @return the node owner
   * 
   * @throws Exception the exception
   */
  public String getNodeOwner(Node node) throws Exception {
    return node.getProperty("exo:owner").getString();
  }

  /**
   * Gets the current web content.
   * 
   * @return the current web content
   */
  private Node getCurrentWebContent() {
    UIQuickCreationWizard quickCreationWizard = getAncestorOfType(UIQuickCreationWizard.class);
    UIContentDialogForm contentDialogForm = quickCreationWizard.getChild(UIContentDialogForm.class);
    return contentDialogForm.getWebContent();
  }

  /**
   * Checks for change permission right.
   * 
   * @param node the node
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  private boolean hasChangePermissionRight(ExtendedNode node) throws Exception {
    try {
      node.checkPermission(PermissionType.ADD_NODE);
      node.checkPermission(PermissionType.REMOVE);
      node.checkPermission(PermissionType.SET_PROPERTY);
      return true;
    } catch (AccessControlException e) {
      return false;
    }
  }

  /**
   * Update grid.
   * 
   * @throws Exception the exception
   */
  public void updateGrid() throws Exception {
    Node node = getCurrentWebContent();
    ExtendedNode webContent = (ExtendedNode) node;
    List<PermissionBean> permissionBeans = new ArrayList<PermissionBean>();
    List<AccessControlEntry> permissionList = webContent.getACL().getPermissionEntries();
    Map<String, List<String>> permissionMap = new HashMap<String, List<String>>();
    Iterator<AccessControlEntry> permissionIterator = permissionList.iterator();

    while (permissionIterator.hasNext()) {
      AccessControlEntry accessControlEntry = permissionIterator.next();
      String currentIdentity = accessControlEntry.getIdentity();
      String currentPermission = accessControlEntry.getPermission();
      List<String> currentPermissionsList = permissionMap.get(currentIdentity);
      if (!permissionMap.containsKey(currentIdentity)) {
        permissionMap.put(currentIdentity, null);
      }
      if (currentPermissionsList == null)
        currentPermissionsList = new ArrayList<String>();
      if (!currentPermissionsList.contains(currentPermission)) {
        currentPermissionsList.add(currentPermission);
      }
      permissionMap.put(currentIdentity, currentPermissionsList);
    }
    Set<String> keys = permissionMap.keySet();
    Iterator<String> keysIter = keys.iterator();
    String owner = SystemIdentity.SYSTEM;
    if (getNodeOwner(node) != null)
      owner = getNodeOwner(node);
    PermissionBean permOwnerBean = new PermissionBean();
    if (!permissionMap.containsKey(owner)) {
      permOwnerBean.setUsersOrGroups(owner);
      permOwnerBean.setAccessible(true);
      permOwnerBean.setEditable(true);
      permissionBeans.add(permOwnerBean);
    }

    while (keysIter.hasNext()) {
      String userOrGroup = (String) keysIter.next();
      List<String> permissions = permissionMap.get(userOrGroup);
      PermissionBean permBean = new PermissionBean();
      permBean.setUsersOrGroups(userOrGroup);
      int numberPermission = 0;
      for (String p : PermissionType.ALL) {
        if (!permissions.contains(p))
          break;
        numberPermission++;
      }
      if (numberPermission == PermissionType.ALL.length) {
        permBean.setEditable(true);
        permBean.setAccessible(true);
      } else {
        permBean.setAccessible(true);
      }
      permissionBeans.add(permBean);
    }
    UIGrid uiGrid = findFirstComponentOfType(UIGrid.class);
    ObjectPageList objPageList = new ObjectPageList(permissionBeans, 10);
    uiGrid.getUIPageIterator().setPageList(objPageList);
  }

  /**
   * The listener interface for receiving deleteAction events.
   * The class that is interested in processing a deleteAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addDeleteActionListener<code> method. When
   * the deleteAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DeleteActionEvent
   */
  public static class DeleteActionListener extends EventListener<UIPermissionInfo> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionInfo> event) throws Exception {
      UIPermissionInfo permissionInfo = event.getSource();
      RepositoryService repositoryService = permissionInfo
      .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      Session session = Utils.getSessionProvider(permissionInfo).getSession(workspace, manageableRepository);
      UIApplication uiApp = permissionInfo.getAncestorOfType(UIApplication.class);
      String name = event.getRequestContext().getRequestParameter(OBJECTID);
      Node webContent = permissionInfo.getCurrentWebContent();
      ExtendedNode node = (ExtendedNode) webContent;
      String nodeOwner = permissionInfo.getNodeOwner(node);
      if (name.equals(nodeOwner)) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionInfo.msg.no-permission-remove", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (permissionInfo.hasChangePermissionRight(node)) {
        if (node.canAddMixin("exo:privilegeable")) {
          node.addMixin("exo:privilegeable");
          node.setPermission(nodeOwner, PermissionType.ALL);
        }
        try {
          node.removePermission(name);
          node.save();
          session.save();
          permissionInfo.updateGrid();
          event.getRequestContext().addUIComponentToUpdateByAjax(permissionInfo.getParent());
        } catch (AccessDeniedException ace) {
          session.refresh(false);
          uiApp.addMessage(new ApplicationMessage("UIPermissionInfo.msg.access-denied", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }      
    }
  }

  /**
   * The listener interface for receiving editAction events.
   * The class that is interested in processing a editAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addEditActionListener<code> method. When
   * the editAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see EditActionEvent
   */
  public static class EditActionListener extends EventListener<UIPermissionInfo> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionInfo> event) throws Exception {
      UIPermissionInfo permissionInfo = event.getSource();
      String name = event.getRequestContext().getRequestParameter(OBJECTID);
      Node webContent = permissionInfo.getCurrentWebContent();
      ExtendedNode node = (ExtendedNode) webContent;
      UIPermissionSetting permissionSetting = permissionInfo.getAncestorOfType(
          UIPermissionManager.class).getChild(UIPermissionSetting.class);
      permissionSetting.fillForm(name, node);
      permissionSetting.lockForm(name.equals(permissionInfo.getNodeOwner(node)));
    }
  }

  /**
   * The Class PermissionBean.
   */
  public class PermissionBean {

    /** The users or groups. */
    private String  usersOrGroups;

    /** The accessible. */
    private boolean accessible;

    /** The editable. */
    private boolean editable;

    /**
     * Gets the users or groups.
     * 
     * @return the users or groups
     */
    public String getUsersOrGroups() {
      return usersOrGroups;
    }

    /**
     * Sets the users or groups.
     * 
     * @param s the new users or groups
     */
    public void setUsersOrGroups(String s) {
      usersOrGroups = s;
    }

    /**
     * Checks if is editable.
     * 
     * @return true, if is editable
     */
    public boolean isEditable() {
      return editable;
    }

    /**
     * Sets the editable.
     * 
     * @param bool the new editable
     */
    public void setEditable(boolean bool) {
      editable = bool;
    }

    /**
     * Checks if is accessible.
     * 
     * @return true, if is accessible
     */
    public boolean isAccessible() {
      return accessible;
    }

    /**
     * Sets the accessible.
     * 
     * @param bool the new accessible
     */
    public void setAccessible(boolean bool) {
      accessible = bool;
    }

  }

}
