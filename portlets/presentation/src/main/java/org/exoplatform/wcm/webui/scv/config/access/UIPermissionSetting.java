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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UIGroupMemberSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.scv.config.UIContentDialogForm;
import org.exoplatform.wcm.webui.scv.config.UIQuickCreationWizard;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Aug 13, 2008
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIPermissionSetting.SaveActionListener.class),
      @EventConfig(listeners = UIPermissionSetting.ResetActionListener.class),
      @EventConfig(listeners = UIPermissionSetting.SelectUserActionListener.class),
      @EventConfig(listeners = UIPermissionSetting.SelectMemberActionListener.class),
      @EventConfig(listeners = UIPermissionSetting.AddAnyActionListener.class) 
    }
)
public class UIPermissionSetting extends UIForm implements UISelectable {

  /** The Constant USERS_INPUTSET. */
  final static public String USERS_INPUTSET    = "usersInputSet";

  /** The Constant USERS_STRINGINPUT. */
  final static public String USERS_STRINGINPUT = "usersStringInput";

  /** The Constant PERMISSION. */
  final static public String PERMISSION        = "permission";

  /** The Constant POPUP_SELECT. */
  final static public String POPUP_SELECT      = "SelectUserOrGroup";

  /** The Constant ACCESSIBLE. */
  final static public String ACCESSIBLE        = "accessible";

  /** The Constant EDITABLE. */
  final static public String EDITABLE          = "editable";

  /**
   * Instantiates a new uI permission setting.
   * 
   * @throws Exception the exception
   */
  public UIPermissionSetting() throws Exception {
    UIFormInputSetWithAction permissionInputSet = new UIFormInputSetWithAction(USERS_INPUTSET);       
    UIFormStringInput formStringInput = new UIFormStringInput(USERS_STRINGINPUT, USERS_STRINGINPUT,
        null);
    formStringInput.setEditable(false);
    permissionInputSet.addChild(formStringInput);
    permissionInputSet.setActionInfo(USERS_STRINGINPUT, new String[] { "SelectUser",
        "SelectMember", "AddAny" });
    permissionInputSet.showActionInfo(true);
    addChild(permissionInputSet);
    addChild(new UIFormCheckBoxInput<String>(ACCESSIBLE, ACCESSIBLE, null));
    addChild(new UIFormCheckBoxInput<String>(EDITABLE, EDITABLE, null));
    setActions(new String[] { "Save", "Reset" });
  }

  /**
   * The listener interface for receiving saveAction events.
   * The class that is interested in processing a saveAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSaveActionListener<code> method. When
   * the saveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveActionEvent
   */
  public static class SaveActionListener extends EventListener<UIPermissionSetting> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIPermissionManager permissionManager = permissionSettingForm.getParent();
      RepositoryService repositoryService = permissionSettingForm
      .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      SessionProvider sessionProvider = Utils.getSessionProvider(permissionSettingForm);
      Session session = sessionProvider.getSession(workspace,manageableRepository);
      UIApplication uiApp = permissionSettingForm.getAncestorOfType(UIApplication.class);
      UIQuickCreationWizard quickCreationWizard = permissionSettingForm
      .getAncestorOfType(UIQuickCreationWizard.class);
      UIContentDialogForm contentDialogForm = quickCreationWizard
      .getChild(UIContentDialogForm.class);
      Node webContent = contentDialogForm.getWebContent();
      UIFormInputSetWithAction formInputSet = permissionSettingForm.getChildById(USERS_INPUTSET);
      String userOrGroup = ((UIFormStringInput) formInputSet.getChildById(USERS_STRINGINPUT))
      .getValue();
      List<String> permsList = new ArrayList<String>();
      if (!webContent.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (permissionSettingForm.getUIFormCheckBoxInput(ACCESSIBLE).isChecked()) {
        permsList.clear();
        permsList.add(PermissionType.READ);
      }
      if (permissionSettingForm.getUIFormCheckBoxInput(EDITABLE).isChecked()) {
        permsList.clear();
        for (String perm : PermissionType.ALL)
          permsList.add(perm);
      }
      if (userOrGroup == null || userOrGroup.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionSetting.msg.userOrGroup-required", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (permsList.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionSetting.msg.checkbox-require", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String[] permsArray = permsList.toArray(new String[permsList.size()]);
      ExtendedNode node = (ExtendedNode) webContent;
      if (node.canAddMixin("exo:privilegeable")) {
        node.addMixin("exo:privilegeable");
        node.setPermission(node.getProperty("exo:owner").getString(), PermissionType.ALL);
      }
      node.setPermission(userOrGroup, permsArray);
      node.save();
      session.save();
      //TODO: should have a other method to set permission to child node of web content
      String queryString = "select * from nt:base where jcr:path like '"+ node.getPath()+"/%'";
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString, Query.SQL) ;
      QueryResult result = query.execute() ;
      for(NodeIterator nodeIterator = result.getNodes(); nodeIterator.hasNext();) {
        ExtendedNode childExNode = (ExtendedNode) nodeIterator.nextNode();
        if (childExNode.canAddMixin("exo:privilegeable")) {
          childExNode.addMixin("exo:privilegeable");
          childExNode.setPermission(node.getProperty("exo:owner").getString(), PermissionType.ALL);
        }
        childExNode.setPermission(userOrGroup, permsArray);
        childExNode.save();
        session.save();
      }
      UIPermissionInfo permissionInfo = permissionManager.getChild(UIPermissionInfo.class);
      permissionInfo.updateGrid();
      permissionSettingForm.reset();
      permissionSettingForm.getUIFormCheckBoxInput(ACCESSIBLE).setChecked(false);
      permissionSettingForm.getUIFormCheckBoxInput(EDITABLE).setChecked(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(permissionManager);
    }
  }

  /**
   * The listener interface for receiving resetAction events.
   * The class that is interested in processing a resetAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addResetActionListener<code> method. When
   * the resetAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ResetActionEvent
   */
  public static class ResetActionListener extends EventListener<UIPermissionSetting> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIFormInputSetWithAction formInputSet = permissionSettingForm.getChildById(USERS_INPUTSET);
      ((UIFormStringInput) formInputSet.getChildById(USERS_STRINGINPUT)).setValue(null);
      permissionSettingForm.getUIFormCheckBoxInput(ACCESSIBLE).setChecked(false);
      permissionSettingForm.getUIFormCheckBoxInput(EDITABLE).setChecked(false);
    }
  }

  /**
   * The listener interface for receiving selectUserAction events.
   * The class that is interested in processing a selectUserAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectUserActionListener<code> method. When
   * the selectUserAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectUserActionEvent
   */
  public static class SelectUserActionListener extends EventListener<UIPermissionSetting> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIWCMUserContainer uiWCMUserContainer = permissionSettingForm.createUIComponent(UIWCMUserContainer.class, null, null);
      permissionSettingForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(
          uiWCMUserContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(permissionSettingForm.getParent());
    }
  }

  /**
   * The listener interface for receiving selectMemberAction events.
   * The class that is interested in processing a selectMemberAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectMemberActionListener<code> method. When
   * the selectMemberAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectMemberActionEvent
   */
  public static class SelectMemberActionListener extends EventListener<UIPermissionSetting> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIGroupMemberSelector uiGroupMemberSelector = permissionSettingForm.createUIComponent(UIGroupMemberSelector.class, null, null);
      uiGroupMemberSelector.setShowAnyPermission(false);
      uiGroupMemberSelector.setSourceComponent(permissionSettingForm, new String[] { USERS_STRINGINPUT });
      permissionSettingForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiGroupMemberSelector);
      event.getRequestContext().addUIComponentToUpdateByAjax(permissionSettingForm.getParent());
    }
  }

  /**
   * The listener interface for receiving addAnyAction events.
   * The class that is interested in processing a addAnyAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addAddAnyActionListener<code> method. When
   * the addAnyAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AddAnyActionEvent
   */
  public static class AddAnyActionListener extends EventListener<UIPermissionSetting> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting uiPermissionSetting = event.getSource();
      UIFormInputSetWithAction uiInputSet = uiPermissionSetting.getChildById(USERS_INPUTSET);
      ((UIFormStringInput) uiInputSet.getChildById(USERS_STRINGINPUT)).setValue(SystemIdentity.ANY);
      uiPermissionSetting.getUIFormCheckBoxInput(ACCESSIBLE).setChecked(true);
      uiPermissionSetting.getUIFormCheckBoxInput(EDITABLE).setChecked(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSetting.getParent());
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
  }

  /**
   * Fill form.
   * 
   * @param user the user
   * @param node the node
   * 
   * @throws Exception the exception
   */
  public void fillForm(String user, ExtendedNode node) throws Exception {
    UIFormInputSetWithAction inputSet = getChildById(USERS_INPUTSET);
    inputSet.getUIStringInput(USERS_STRINGINPUT).setValue(user);
    String owner = node.getProperty("exo:owner").getString();
    if (user.equals(owner)) {
      getUIFormCheckBoxInput(ACCESSIBLE).setChecked(true);
      getUIFormCheckBoxInput(EDITABLE).setChecked(true);
    } else {
      List<AccessControlEntry> permsList = node.getACL().getPermissionEntries();
      Iterator perIter = permsList.iterator();
      StringBuilder userPermission = new StringBuilder();
      while (perIter.hasNext()) {
        AccessControlEntry accessControlEntry = (AccessControlEntry) perIter.next();
        if (user.equals(accessControlEntry.getIdentity())) {
          userPermission.append(accessControlEntry.getPermission()).append(" ");
        }
      }
      int numPermission = 0;
      for (String perm : PermissionType.ALL) {
        if (userPermission.toString().contains(perm))
          numPermission++;
      }
      if (numPermission == PermissionType.ALL.length) {
        getUIFormCheckBoxInput(EDITABLE).setChecked(true);
        getUIFormCheckBoxInput(ACCESSIBLE).setChecked(true);
      } else {
        getUIFormCheckBoxInput(ACCESSIBLE).setChecked(true);
      }
    }
  }

  /**
   * Lock form.
   * 
   * @param isLock the is lock
   */
  protected void lockForm(boolean isLock) {
    UIFormInputSetWithAction inputSet = getChildById(USERS_INPUTSET);
    if (isLock) {
      setActions(new String[] { "Reset" });
      inputSet.setActionInfo(USERS_STRINGINPUT, null);
    } else {
      setActions(new String[] { "Save", "Reset" });
      inputSet.setActionInfo(USERS_STRINGINPUT, new String[] { "SelectUser", "SelectMember",
      "AddAny" });
    }
    getUIFormCheckBoxInput(ACCESSIBLE).setEnable(!isLock);
    getUIFormCheckBoxInput(EDITABLE).setEnable(!isLock);
  }

}
