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
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.selector.UIPermissionSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
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
import org.exoplatform.webui.form.ext.UIFormInputSetWithAction;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Aug 13, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
  @EventConfig(listeners = UIPermissionSetting.SaveActionListener.class),
  @EventConfig(listeners = UIPermissionSetting.ResetActionListener.class),
  @EventConfig(listeners = UIPermissionSetting.SelectUserActionListener.class),
  @EventConfig(listeners = UIPermissionSetting.SelectMemberActionListener.class),
  @EventConfig(listeners = UIPermissionSetting.AddAnyActionListener.class) })
  public class UIPermissionSetting extends UIForm implements UISelectable {

  final static public String USERS_INPUTSET    = "usersInputSet";

  final static public String USERS_STRINGINPUT = "usersStringInput";

  final static public String PERMISSION        = "permission";

  final static public String POPUP_SELECT      = "SelectUserOrGroup";

  final static public String ACCESSIBLE        = "accessible";

  final static public String EDITABLE          = "editable";

  public UIPermissionSetting() throws Exception {
    UIFormInputSetWithAction permissionInputSet = new UIFormInputSetWithAction(USERS_INPUTSET);
    UIFormStringInput formStringInput = new UIFormStringInput(USERS_STRINGINPUT, USERS_STRINGINPUT,
        null);
    formStringInput.addValidator(MandatoryValidator.class);
    formStringInput.setEditable(false);
    permissionInputSet.addChild(formStringInput);
    permissionInputSet.setActionInfo(USERS_STRINGINPUT, new String[] { "SelectUser",
        "SelectMember", "AddAny" });
    addChild(permissionInputSet);
    addChild(new UIFormCheckBoxInput<String>(ACCESSIBLE, ACCESSIBLE, null));
    addChild(new UIFormCheckBoxInput<String>(EDITABLE, EDITABLE, null));
    setActions(new String[] { "Save", "Reset" });
  }

  public static class SaveActionListener extends EventListener<UIPermissionSetting> {
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIPermissionManager permissionManager = permissionSettingForm.getParent();
      RepositoryService repositoryService = permissionSettingForm
      .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      Session session = SessionProvider.createSystemProvider().getSession(workspace,
          manageableRepository);
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
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.userOrGroup-required", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (permsList.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.checkbox-require", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String[] permsArray = permsList.toArray(new String[permsList.size()]);
      ExtendedNode node = (ExtendedNode) webContent;
      if (node.canAddMixin("exo:privilegeable"))
        node.addMixin("exo:privilegeable");
      node.setPermission(node.getProperty("exo:owner").getString(), PermissionType.ALL);
      node.setPermission(userOrGroup, permsArray);
      UIPermissionInfo permissionInfo = permissionManager.getChild(UIPermissionInfo.class);
      permissionInfo.updateGrid();
      node.save();
      session.save();
      permissionSettingForm.reset();
      permissionSettingForm.getUIFormCheckBoxInput(ACCESSIBLE).setChecked(false);
      permissionSettingForm.getUIFormCheckBoxInput(EDITABLE).setChecked(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(permissionManager);
    }
  }

  public static class ResetActionListener extends EventListener<UIPermissionSetting> {
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIFormInputSetWithAction formInputSet = permissionSettingForm.getChildById(USERS_INPUTSET);
      ((UIFormStringInput) formInputSet.getChildById(USERS_STRINGINPUT)).setValue(null);
      permissionSettingForm.getUIFormCheckBoxInput(ACCESSIBLE).setChecked(false);
      permissionSettingForm.getUIFormCheckBoxInput(EDITABLE).setChecked(false);
    }
  }

  public static class SelectUserActionListener extends EventListener<UIPermissionSetting> {
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIPermissionSelector permissionSelector = permissionSettingForm.createUIComponent(
          UIPermissionSelector.class, null, null);
      permissionSelector.setSelectedUser(true);
      permissionSelector.setSourceComponent(permissionSettingForm,
          new String[] { USERS_STRINGINPUT });
      permissionSettingForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(
          permissionSelector);
      event.getRequestContext().addUIComponentToUpdateByAjax(permissionSettingForm.getParent());
    }
  }

  public static class SelectMemberActionListener extends EventListener<UIPermissionSetting> {
    public void execute(Event<UIPermissionSetting> event) throws Exception {
      UIPermissionSetting permissionSettingForm = event.getSource();
      UIPermissionSelector permissionSelector = permissionSettingForm.createUIComponent(
          UIPermissionSelector.class, null, null);
      permissionSelector.setSelectedMembership(true);
      permissionSelector.setSourceComponent(permissionSettingForm,
          new String[] { USERS_STRINGINPUT });
      permissionSettingForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(
          permissionSelector);
      event.getRequestContext().addUIComponentToUpdateByAjax(permissionSettingForm.getParent());
    }
  }

  public static class AddAnyActionListener extends EventListener<UIPermissionSetting> {
    public void execute(Event<UIPermissionSetting> event) throws Exception {

    }
  }

  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
  }

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
