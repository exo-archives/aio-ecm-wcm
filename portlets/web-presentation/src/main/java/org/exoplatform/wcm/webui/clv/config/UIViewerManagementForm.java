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
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.UIContentListViewerPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/ContentListViewer/config/UIFolderListViewerConfigForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIViewerManagementForm.SaveActionListener.class),
      @EventConfig(listeners = UIViewerManagementForm.CancelActionListener.class),
      @EventConfig(listeners = UIViewerManagementForm.SelectFolderPathActionListener.class)     
    }
)
public class UIViewerManagementForm extends UIForm implements UISelectable {

  public final static String PORTLET_NAME                 = "Content List Viewer";

  public final static String FORM_VIEW_TEMPLATE_CATEGORY  = "list-by-folder";

  public final static String PAGINATOR_TEMPLATE_CATEGORY  = "paginators";

  public final static String FOLDER_PATH_INPUTSET         = "FolderPathInputSet";

  public final static String FOLDER_PATH_INPUT            = "FolderPathInput";

  public final static String FORM_VIEW_TEMPLATES_SELECTOR = "FormViewTemplate";

  public final static String PAGINATOR_TEMPLATES_SELECTOR = "PaginatorTemplate";

  public final static String ITEMS_PER_PAGE_SELECTOR      = "ItemsPerPage";

  public final static String VIEWER_BUTTON_QUICK_EDIT     = "ViewerButtonQuickEdit";

  public final static String VIEWER_BUTTON_REFRESH        = "ViewerButtonRefresh";

  public static final String VIEWER_THUMBNAILS_VIEW       = "ViewerThumbnailsView";

  @SuppressWarnings("unchecked")
  public UIViewerManagementForm() throws Exception {
    PortletPreferences portletPreferences = getPortletPreferences();
    String folderPath = portletPreferences.getValue(UIContentListViewerPortlet.FOLDER_PATH,
        UIContentListViewerPortlet.FOLDER_PATH);
    List<SelectItemOption<String>> formViewerTemplateList = getTemplateList(PORTLET_NAME,
        FORM_VIEW_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> paginatorTemplateList = getTemplateList(PORTLET_NAME,
        PAGINATOR_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> itemsPerPageList = new ArrayList<SelectItemOption<String>>();
    itemsPerPageList.add(new SelectItemOption<String>("5", "5"));
    itemsPerPageList.add(new SelectItemOption<String>("10", "10"));
    itemsPerPageList.add(new SelectItemOption<String>("20", "20"));

    UIFormInputSetWithAction folderPathInputSet = new UIFormInputSetWithAction(FOLDER_PATH_INPUTSET);
    UIFormStringInput folderPathInput = new UIFormStringInput(FOLDER_PATH_INPUT, FOLDER_PATH_INPUT,
        null);
    folderPathInput.setEditable(false);
    if (folderPath != null) folderPathInput.setValue(folderPath);
    folderPathInputSet.addChild(folderPathInput);
    folderPathInputSet.setActionInfo(FOLDER_PATH_INPUT, new String[] { "SelectFolderPath" });
    UIFormSelectBox itemsPerPage = new UIFormSelectBox(ITEMS_PER_PAGE_SELECTOR,
        ITEMS_PER_PAGE_SELECTOR, itemsPerPageList);
    UIFormSelectBox formViewTemplateSelector = new UIFormSelectBox(FORM_VIEW_TEMPLATES_SELECTOR,
        FORM_VIEW_TEMPLATES_SELECTOR, formViewerTemplateList);
    UIFormSelectBox paginatorTemplateSelector = new UIFormSelectBox(PAGINATOR_TEMPLATES_SELECTOR,
        PAGINATOR_TEMPLATES_SELECTOR, paginatorTemplateList);
    UIFormCheckBoxInput viewerButtonQuickEditCheckbox = new UIFormCheckBoxInput(
        VIEWER_BUTTON_QUICK_EDIT, VIEWER_BUTTON_QUICK_EDIT, null);
    viewerButtonQuickEditCheckbox.setChecked(true);
    UIFormCheckBoxInput viewerButtonRefreshCheckbox = new UIFormCheckBoxInput(
        VIEWER_BUTTON_REFRESH, VIEWER_BUTTON_REFRESH, null);
    viewerButtonRefreshCheckbox.setChecked(true);
    UIFormCheckBoxInput thumbnailsViewCheckbox = new UIFormCheckBoxInput(VIEWER_THUMBNAILS_VIEW,
        VIEWER_THUMBNAILS_VIEW, null);
    thumbnailsViewCheckbox.setChecked(true);

    addChild(folderPathInputSet);
    addChild(formViewTemplateSelector);
    addChild(paginatorTemplateSelector);
    addChild(itemsPerPage);
    addChild(viewerButtonQuickEditCheckbox);
    addChild(viewerButtonRefreshCheckbox);
    addChild(thumbnailsViewCheckbox);
    setActions(new String[] { "Save", "Cancel" });
  }

  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
    showPopupComponent(null);
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
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

  private List<SelectItemOption<String>> getTemplateList(String portletName, String category)
      throws Exception {
    List<SelectItemOption<String>> templateOptionList = new ArrayList<SelectItemOption<String>>();
    ApplicationTemplateManagerService templateManagerService = getApplicationComponent(ApplicationTemplateManagerService.class);
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
    String repository = manageableRepository.getConfiguration().getName();
    List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository,
        portletName, category, provider);
    for (Node templateNode : templateNodeList) {
      String templateName = templateNode.getName();
      String templatePath = templateNode.getPath();
      templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateOptionList;
  }

  public static class SaveActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm viewerManagementForm = event.getSource();
      RepositoryService repositoryService = viewerManagementForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String folderPath = viewerManagementForm.getUIStringInput(UIViewerManagementForm.FOLDER_PATH_INPUT)
          .getValue();
      String formViewTemplatePath = viewerManagementForm.getUIFormSelectBox(
          UIViewerManagementForm.FORM_VIEW_TEMPLATES_SELECTOR).getValue();
      String paginatorTemplatePath = viewerManagementForm.getUIFormSelectBox(
          UIViewerManagementForm.PAGINATOR_TEMPLATES_SELECTOR).getValue();
      String itemsPerPage = viewerManagementForm.getUIFormSelectBox(
          UIViewerManagementForm.ITEMS_PER_PAGE_SELECTOR).getValue();
      String showQuickEdit = viewerManagementForm.getUIFormCheckBoxInput(
          UIViewerManagementForm.VIEWER_BUTTON_QUICK_EDIT).isChecked() ? "true" : "false";
      String showRefreshButton = viewerManagementForm.getUIFormCheckBoxInput(
          UIViewerManagementForm.VIEWER_BUTTON_REFRESH).isChecked() ? "true" : "false";
      String viewThumbnails = viewerManagementForm.getUIFormCheckBoxInput(
          UIViewerManagementForm.VIEWER_THUMBNAILS_VIEW).isChecked() ? "true" : "false";

      PortletRequestContext portletRequestContext = (PortletRequestContext) event
          .getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      portletPreferences.setValue(UIContentListViewerPortlet.REPOSITORY, repository);
      portletPreferences.setValue(UIContentListViewerPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UIContentListViewerPortlet.FOLDER_PATH, folderPath);
      portletPreferences.setValue(UIContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH,
          formViewTemplatePath);
      portletPreferences.setValue(UIContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH,
          paginatorTemplatePath);
      portletPreferences.setValue(UIContentListViewerPortlet.ITEMS_PER_PAGE, itemsPerPage);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_QUICK_EDIT_BUTTON, showQuickEdit);
      portletPreferences
          .setValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON, showRefreshButton);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_THUMBNAILS_VIEW, viewThumbnails);
      portletPreferences.store();
      if (Utils.isEditPortletInCreatePageWizard()) {        
        UIApplication uiApp = viewerManagementForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.saving-success", null, ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW); 
      }      
    }
  }

  public static class CancelActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm viewerManagementForm = event.getSource();
      viewerManagementForm.getUIStringInput(UIViewerManagementForm.FOLDER_PATH_INPUT).setValue(null);
      viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_BUTTON_QUICK_EDIT).setChecked(true);
      viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_BUTTON_REFRESH).setChecked(true);
      viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_THUMBNAILS_VIEW).setChecked(true);
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
