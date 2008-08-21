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
package org.exoplatform.wcm.presentation.acp.config;

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
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
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

@ComponentConfig(lifecycle = UIContainerLifecycle.class, events = {
    @EventConfig(listeners = UIPermissionInfo.DeleteActionListener.class, confirm = "UIPermissionInfo.msg.confirm-delete-permission"),
    @EventConfig(listeners = UIPermissionInfo.EditActionListener.class) })
public class UIPermissionInfo extends UIContainer {

  public static String[]  PERMISSION_FIELD  = { "usersOrGroups", "accessible", "editable" };

  private static String[] PERMISSION_ACTION = { "Edit", "Delete" };

  public UIPermissionInfo() throws Exception {
    UIGrid uiGrid = createUIComponent(UIGrid.class, null, "PermissionInfo");
    uiGrid.getUIPageIterator().setId("PermissionInfoIterator");
    uiGrid.configure("usersOrGroups", PERMISSION_FIELD, PERMISSION_ACTION);
    addChild(uiGrid);       
  }

  public String getNodeOwner(Node node) throws Exception {
    return node.getProperty("exo:owner").getString();
  }

  private Node getCurrentWebContent() {
    UIQuickCreationWizard quickCreationWizard = getAncestorOfType(UIQuickCreationWizard.class);
    UIContentDialogForm contentDialogForm = quickCreationWizard.getChild(UIContentDialogForm.class);
    return contentDialogForm.getWebContent();
  }

  public void updateGrid() throws Exception {
    Node node = getCurrentWebContent();
    ExtendedNode webContent = (ExtendedNode) node;
    List<PermissionBean> permissionBeans = new ArrayList<PermissionBean>();
    List permissionList = webContent.getACL().getPermissionEntries();
    Map<String, List<String>> permissionMap = new HashMap<String, List<String>>();
    Iterator permissionIterator = permissionList.iterator();

    while (permissionIterator.hasNext()) {
      AccessControlEntry accessControlEntry = (AccessControlEntry) permissionIterator.next();
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
    Set keys = permissionMap.keySet();
    Iterator keysIter = keys.iterator();
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

  public static class DeleteActionListener extends EventListener<UIPermissionInfo> {
    public void execute(Event<UIPermissionInfo> event) throws Exception {
      UIPermissionInfo permissionInfo = event.getSource();
      RepositoryService repositoryService = permissionInfo
          .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      Session session = SessionProvider.createSystemProvider().getSession(workspace,
          manageableRepository);
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

  public static class EditActionListener extends EventListener<UIPermissionInfo> {
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

  public class PermissionBean {

    private String  usersOrGroups;

    private boolean accessible;

    private boolean editable;

    public String getUsersOrGroups() {
      return usersOrGroups;
    }

    public void setUsersOrGroups(String s) {
      usersOrGroups = s;
    }

    public boolean isEditable() {
      return editable;
    }

    public void setEditable(boolean bool) {
      editable = bool;
    }

    public boolean isAccessible() {
      return accessible;
    }

    public void setAccessible(boolean bool) {
      accessible = bool;
    }

  }

}
