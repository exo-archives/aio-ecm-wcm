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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.webui.clv.UIContentListViewerPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
    @EventConfig(listeners = UIViewerManagementForm.SaveActionListener.class),
    @EventConfig(listeners = UIViewerManagementForm.CancelActionListener.class),
    @EventConfig(listeners = UIViewerManagementForm.SelectFolderPathActionListener.class) })
public class UIViewerManagementForm extends UIForm implements UISelectable {

  public final static String FOLDER_PATH_INPUTSET  = "FolderPathInputSet";

  public final static String FOLDER_PATH_INPUT     = "FolderPathInput";

  public final static String TEMPLATES_PATH_SELECT = "TemplatePath";

  public final static String ITEMS_PER_PAGE_SELECT = "ItemsPerPage";

  public final static String QUICK_EDIT_CHECKBOX   = "QuickEdit";
  
  public final static String PORTLET_NAME   = "Content List Viewer";
  
  public final static String TEMPLATE_CATEGORY   = "list-by-folder";

  public UIViewerManagementForm() throws Exception {
    List<SelectItemOption<String>> templateList = getTemplateList();
    List<SelectItemOption<String>> itemsPerPageList = new ArrayList<SelectItemOption<String>>();
    itemsPerPageList.add(new SelectItemOption<String>("2", "2"));
    itemsPerPageList.add(new SelectItemOption<String>("10", "10"));
    itemsPerPageList.add(new SelectItemOption<String>("15", "15"));
    itemsPerPageList.add(new SelectItemOption<String>("20", "20"));
    itemsPerPageList.add(new SelectItemOption<String>("30", "30"));

    UIFormInputSetWithAction folderPathInputSet = new UIFormInputSetWithAction(FOLDER_PATH_INPUTSET);
    UIFormStringInput folderPathInput = new UIFormStringInput(FOLDER_PATH_INPUT, FOLDER_PATH_INPUT,
        null);
    folderPathInput.setEditable(false);
    folderPathInputSet.addChild(folderPathInput);
    folderPathInputSet.setActionInfo(FOLDER_PATH_INPUT, new String[] { "SelectFolderPath" });
    UIFormSelectBox itemsPerPage = new UIFormSelectBox(ITEMS_PER_PAGE_SELECT,
        ITEMS_PER_PAGE_SELECT, itemsPerPageList);
    UIFormSelectBox templatesPath = new UIFormSelectBox(TEMPLATES_PATH_SELECT,
        TEMPLATES_PATH_SELECT, templateList);
    UIFormCheckBoxInput quickEditCheckbox = new UIFormCheckBoxInput(QUICK_EDIT_CHECKBOX,
        QUICK_EDIT_CHECKBOX, null);
    addChild(folderPathInputSet);
    addChild(templatesPath);
    addChild(itemsPerPage);
    addChild(quickEditCheckbox);
    setActions(new String[] { "Save", "Cancel" });
  }

  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
    showPopupComponent(null);
  }

  public void showPopupComponent(UIComponent uiComponent) throws Exception {
    UIContainer uiParent = getParent();
    if (uiComponent == null) {
      uiParent.removeChild(UIPopupWindow.class);
      return;
    }
    UIPopupWindow uiPopup = uiParent.getChild(UIPopupWindow.class);
    if (uiPopup == null)
      uiPopup = uiParent.addChild(UIPopupWindow.class, null, null);
    uiPopup.setUIComponent(uiComponent);
    uiPopup.setWindowSize(610, 300);
    uiPopup.setResizable(true);
    uiPopup.setShow(true);
  }
  
  private List<SelectItemOption<String>> getTemplateList() throws Exception {
    List<SelectItemOption<String>> templateOptionList = new ArrayList<SelectItemOption<String>>();
    ApplicationTemplateManagerService templateManagerService = getApplicationComponent(ApplicationTemplateManagerService.class);
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
    String repository = manageableRepository.getConfiguration().getName();    
    List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository,
        PORTLET_NAME, TEMPLATE_CATEGORY, provider);
    for (Node templateNode : templateNodeList) {
      String templateName = templateNode.getName();
      String templatePath = templateNode.getPath();
      templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateOptionList;
  }

  public static class SaveActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiForm = event.getSource();   
      RepositoryService repositoryService = uiForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName(); 
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String folderPath = uiForm.getUIStringInput(UIViewerManagementForm.FOLDER_PATH_INPUT).getValue();
      String templatePath = uiForm.getUIFormSelectBox(UIViewerManagementForm.TEMPLATES_PATH_SELECT).getValue();      
      String itemsPerPage = uiForm.getUIFormSelectBox(UIViewerManagementForm.ITEMS_PER_PAGE_SELECT).getValue();      
      String showQuickEdit = uiForm.getUIFormCheckBoxInput(UIViewerManagementForm.QUICK_EDIT_CHECKBOX).isChecked() ? "true" : "false";
      
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      portletPreferences.setValue(UIContentListViewerPortlet.REPOSITORY, repository);
      portletPreferences.setValue(UIContentListViewerPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UIContentListViewerPortlet.FOLDER_PATH, folderPath);
      portletPreferences.setValue(UIContentListViewerPortlet.TEMPLATE_PATH, templatePath);
      portletPreferences.setValue(UIContentListViewerPortlet.ITEMS_PER_PAGE, itemsPerPage);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_QUICK_EDIT, showQuickEdit);
      portletPreferences.store();
      
      portletRequestContext.setApplicationMode(PortletMode.VIEW);      
    }
  }

  public static class CancelActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {

    }
  }

  public static class SelectFolderPathActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiViewerManagementForm = event.getSource();
      UIFolderPathSelectorForm folderPathSelector = uiViewerManagementForm.createUIComponent(
          UIFolderPathSelectorForm.class, null, null);
      folderPathSelector.setSourceComponent(uiViewerManagementForm,
          new String[] { UIViewerManagementForm.FOLDER_PATH_INPUT });
      folderPathSelector.init();
      uiViewerManagementForm.showPopupComponent(folderPathSelector);
    }
  }

}
