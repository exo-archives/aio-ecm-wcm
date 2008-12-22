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
import org.exoplatform.wcm.webui.clv.UIFolderViewer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
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

/**
 * The Class UIViewerManagementForm.
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

  /** The Constant HEADER. */
  public final static String HEADER                       = "Header";

  /** The Constant PORTLET_NAME. */
  public final static String PORTLET_NAME                 = "Content List Viewer";

  /** The Constant FORM_VIEW_TEMPLATE_CATEGORY. */
  public final static String FORM_VIEW_TEMPLATE_CATEGORY  = "list-by-folder";

  /** The Constant PAGINATOR_TEMPLATE_CATEGORY. */
  public final static String PAGINATOR_TEMPLATE_CATEGORY  = "paginators";

  /** The Constant FOLDER_PATH_INPUTSET. */
  public final static String FOLDER_PATH_INPUTSET         = "FolderPathInputSet";

  /** The Constant FOLDER_PATH_INPUT. */
  public final static String FOLDER_PATH_INPUT            = "FolderPathInput";

  /** The Constant FORM_VIEW_TEMPLATES_SELECTOR. */
  public final static String FORM_VIEW_TEMPLATES_SELECTOR = "FormViewTemplate";

  /** The Constant PAGINATOR_TEMPLATES_SELECTOR. */
  public final static String PAGINATOR_TEMPLATES_SELECTOR = "PaginatorTemplate";

  /** The Constant ITEMS_PER_PAGE_SELECTOR. */
  public final static String ITEMS_PER_PAGE_SELECTOR      = "ItemsPerPage";

  /** The Constant VIEWER_BUTTON_QUICK_EDIT. */
  public final static String VIEWER_BUTTON_QUICK_EDIT     = "ViewerButtonQuickEdit";

  /** The Constant VIEWER_BUTTON_REFRESH. */
  public final static String VIEWER_BUTTON_REFRESH        = "ViewerButtonRefresh";

  /** The Constant VIEWER_THUMBNAILS_IMAGE. */
  public static final String VIEWER_THUMBNAILS_IMAGE      = "ViewerThumbnailsView";

  /** The Constant VIEWER_TITLE. */
  public static final String VIEWER_TITLE                 = "ViewerTitle";

  /** The Constant VIEWER_DATE_CREATED. */
  public static final String VIEWER_DATE_CREATED          = "ViewerDateCreated";

  /** The Constant VIEWER_SUMMARY. */
  public static final String VIEWER_SUMMARY               = "ViewerSummary";

  /** The Constant VIEWER_HEADER. */
  public static final String VIEWER_HEADER                = "ViewerHeader";

  /**
   * Instantiates a new uI viewer management form.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public UIViewerManagementForm() throws Exception {
    PortletPreferences portletPreferences = ((PortletRequestContext) WebuiRequestContext.getCurrentInstance()).getRequest()
                                                                                                              .getPreferences();
    String folderPath = portletPreferences.getValue(UIContentListViewerPortlet.FOLDER_PATH,
                                                    UIContentListViewerPortlet.FOLDER_PATH);

    UIFormStringInput headerInput = new UIFormStringInput(HEADER, HEADER, null);
    String headerValue = portletPreferences.getValue(UIContentListViewerPortlet.HEADER, null);
    headerInput.setValue(headerValue);

    List<SelectItemOption<String>> formViewerTemplateList = getTemplateList(PORTLET_NAME,
                                                                            FORM_VIEW_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> paginatorTemplateList = getTemplateList(PORTLET_NAME,
                                                                           PAGINATOR_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> itemsPerPageList = new ArrayList<SelectItemOption<String>>();
    itemsPerPageList.add(new SelectItemOption<String>("5", "5"));
    itemsPerPageList.add(new SelectItemOption<String>("10", "10"));
    itemsPerPageList.add(new SelectItemOption<String>("20", "20"));

    UIFormInputSetWithAction folderPathInputSet = new UIFormInputSetWithAction(FOLDER_PATH_INPUTSET);
    UIFormStringInput folderPathInput = new UIFormStringInput(FOLDER_PATH_INPUT,
                                                              FOLDER_PATH_INPUT,
                                                              null);
    folderPathInput.setEditable(false);
    if (folderPath != null)
      folderPathInput.setValue(folderPath);
    folderPathInputSet.addChild(folderPathInput);
    folderPathInputSet.setActionInfo(FOLDER_PATH_INPUT, new String[] { "SelectFolderPath" });
    UIFormSelectBox itemsPerPage = new UIFormSelectBox(ITEMS_PER_PAGE_SELECTOR,
                                                       ITEMS_PER_PAGE_SELECTOR,
                                                       itemsPerPageList);
    UIFormSelectBox formViewTemplateSelector = new UIFormSelectBox(FORM_VIEW_TEMPLATES_SELECTOR,
                                                                   FORM_VIEW_TEMPLATES_SELECTOR,
                                                                   formViewerTemplateList);
    UIFormSelectBox paginatorTemplateSelector = new UIFormSelectBox(PAGINATOR_TEMPLATES_SELECTOR,
                                                                    PAGINATOR_TEMPLATES_SELECTOR,
                                                                    paginatorTemplateList);
    UIFormCheckBoxInput viewerButtonQuickEditCheckbox = new UIFormCheckBoxInput(VIEWER_BUTTON_QUICK_EDIT,
                                                                                VIEWER_BUTTON_QUICK_EDIT,
                                                                                null);
    viewerButtonQuickEditCheckbox.setChecked(true);
    UIFormCheckBoxInput viewerButtonRefreshCheckbox = new UIFormCheckBoxInput(VIEWER_BUTTON_REFRESH,
                                                                              VIEWER_BUTTON_REFRESH,
                                                                              null);
    viewerButtonRefreshCheckbox.setChecked(true);
    UIFormCheckBoxInput thumbnailsViewCheckbox = new UIFormCheckBoxInput(VIEWER_THUMBNAILS_IMAGE,
                                                                         VIEWER_THUMBNAILS_IMAGE,
                                                                         null);
    thumbnailsViewCheckbox.setChecked(true);
    UIFormCheckBoxInput titleViewerCheckbox = new UIFormCheckBoxInput(VIEWER_TITLE,
                                                                      VIEWER_TITLE,
                                                                      null);
    titleViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput summaryViewerCheckbox = new UIFormCheckBoxInput(VIEWER_SUMMARY,
                                                                        VIEWER_SUMMARY,
                                                                        null);
    summaryViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput dateCreatedViewerCheckbox = new UIFormCheckBoxInput(VIEWER_DATE_CREATED,
                                                                            VIEWER_DATE_CREATED,
                                                                            null);
    dateCreatedViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput viewerHeader = new UIFormCheckBoxInput(VIEWER_HEADER, VIEWER_HEADER, null);
    viewerHeader.setChecked(Boolean.parseBoolean(portletPreferences.getValue(UIContentListViewerPortlet.SHOW_HEADER,
                                                                             null)));

    String quickEditAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_QUICK_EDIT_BUTTON,
                                                       null);
    viewerButtonQuickEditCheckbox.setChecked(Boolean.parseBoolean(quickEditAble));
    String refreshAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON,
                                                     null);
    viewerButtonRefreshCheckbox.setChecked(Boolean.parseBoolean(refreshAble));
    String imageShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_THUMBNAILS_VIEW,
                                                       null);
    thumbnailsViewCheckbox.setChecked(Boolean.parseBoolean(imageShowAble));
    String titleShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_TITLE, null);
    titleViewerCheckbox.setChecked(Boolean.parseBoolean(titleShowAble));
    String summaryShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_SUMMARY,
                                                         null);
    summaryViewerCheckbox.setChecked(Boolean.parseBoolean(summaryShowAble));
    String dateShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_DATE_CREATED,
                                                      null);
    dateCreatedViewerCheckbox.setChecked(Boolean.parseBoolean(dateShowAble));
    String formViewTemplate = portletPreferences.getValue(UIContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH,
                                                          null);
    formViewTemplateSelector.setValue(formViewTemplate);
    String paginatorTemplate = portletPreferences.getValue(UIContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH,
                                                           null);
    paginatorTemplateSelector.setValue(paginatorTemplate);
    String itemsPerPageVal = portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE,
                                                         null);
    itemsPerPage.setValue(itemsPerPageVal);

    addChild(folderPathInputSet);
    addChild(headerInput);
    addChild(formViewTemplateSelector);
    addChild(paginatorTemplateSelector);
    addChild(itemsPerPage);
    addChild(viewerButtonQuickEditCheckbox);
    addChild(viewerButtonRefreshCheckbox);
    addChild(thumbnailsViewCheckbox);
    addChild(titleViewerCheckbox);
    addChild(dateCreatedViewerCheckbox);
    addChild(summaryViewerCheckbox);
    addChild(viewerHeader);

    setActions(new String[] { "Save", "Cancel" });
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
    showPopupComponent(null);
  }

  /**
   * Show popup component.
   * 
   * @param uiComponent the ui component
   * 
   * @throws Exception the exception
   */
  public void showPopupComponent(UIComponent uiComponent) throws Exception {
    UIPortletConfig uiPortletConfig = getAncestorOfType(UIPortletConfig.class);
    UIPopupContainer uiPopupContainer = uiPortletConfig.getChild(UIPopupContainer.class);
    if (uiComponent == null) {
      uiPopupContainer.deActivate();
      return;
    }
    uiComponent.setRendered(true);
    uiPopupContainer.activate(uiComponent, 600, 300);
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    context.addUIComponentToUpdateByAjax(uiPopupContainer);
  }

  /**
   * Gets the template list.
   * 
   * @param portletName the portlet name
   * @param category the category
   * 
   * @return the template list
   * 
   * @throws Exception the exception
   */
  private List<SelectItemOption<String>> getTemplateList(String portletName, String category) throws Exception {
    List<SelectItemOption<String>> templateOptionList = new ArrayList<SelectItemOption<String>>();
    ApplicationTemplateManagerService templateManagerService = getApplicationComponent(ApplicationTemplateManagerService.class);
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
    String repository = manageableRepository.getConfiguration().getName();
    List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository,
                                                                                portletName,
                                                                                category,
                                                                                provider);
    for (Node templateNode : templateNodeList) {
      String templateName = templateNode.getName();
      String templatePath = templateNode.getPath();
      templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateOptionList;
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
  public static class SaveActionListener extends EventListener<UIViewerManagementForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm viewerManagementForm = event.getSource();
      UIApplication uiApp = viewerManagementForm.getAncestorOfType(UIApplication.class);
      RepositoryService repositoryService = viewerManagementForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String folderPath = viewerManagementForm.getUIStringInput(UIViewerManagementForm.FOLDER_PATH_INPUT)
                                              .getValue();
      String header = viewerManagementForm.getUIStringInput(UIViewerManagementForm.HEADER)
                                          .getValue();
      String formViewTemplatePath = viewerManagementForm.getUIFormSelectBox(UIViewerManagementForm.FORM_VIEW_TEMPLATES_SELECTOR)
                                                        .getValue();
      String paginatorTemplatePath = viewerManagementForm.getUIFormSelectBox(UIViewerManagementForm.PAGINATOR_TEMPLATES_SELECTOR)
                                                         .getValue();
      String itemsPerPage = viewerManagementForm.getUIFormSelectBox(UIViewerManagementForm.ITEMS_PER_PAGE_SELECTOR)
                                                .getValue();
      String showQuickEdit = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_BUTTON_QUICK_EDIT)
                                                 .isChecked() ? "true" : "false";
      String showRefreshButton = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_BUTTON_REFRESH)
                                                     .isChecked() ? "true" : "false";
      String viewThumbnails = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_THUMBNAILS_IMAGE)
                                                  .isChecked() ? "true" : "false";
      String viewTitle = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_TITLE)
                                             .isChecked() ? "true" : "false";
      String viewSummary = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_SUMMARY)
                                               .isChecked() ? "true" : "false";
      String viewDateCreated = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_DATE_CREATED)
                                                   .isChecked() ? "true" : "false";
      String viewerHeader = viewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_HEADER)
                                                .isChecked() ? "true" : "false";
      if (folderPath == null || folderPath.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.empty-folder-path",
                                                null,
                                                ApplicationMessage.WARNING));
        return;
      }
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
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
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON, showRefreshButton);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_THUMBNAILS_VIEW, viewThumbnails);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_TITLE, viewTitle);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_DATE_CREATED, viewDateCreated);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_SUMMARY, viewSummary);
      portletPreferences.setValue(UIContentListViewerPortlet.HEADER, header);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_HEADER, viewerHeader);

      portletPreferences.store();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.saving-success",
                                                null,
                                                ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
        UIContentListViewerPortlet uiContentListViewerPortlet = viewerManagementForm.getAncestorOfType(UIContentListViewerPortlet.class);
        UIFolderViewer uiFolderViewer = uiContentListViewerPortlet.getChild(UIFolderViewer.class);
        uiFolderViewer.getChildren().clear();
        uiFolderViewer.init();
        Utils.refreshBrowser(portletRequestContext);
      }
      UIPopupContainer uiPopupContainer = (UIPopupContainer) viewerManagementForm.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
    }
  }

  /**
   * The listener interface for receiving cancelAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCancelActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CancelActionEvent
   */
  public static class CancelActionListener extends EventListener<UIViewerManagementForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm viewerManagementForm = event.getSource();
      UIApplication uiApp = viewerManagementForm.getAncestorOfType(UIApplication.class);
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.none-action-excuted",
                                                null,
                                                ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
      }
      UIPopupContainer uiPopupContainer = (UIPopupContainer) viewerManagementForm.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
    }
  }

  /**
   * The listener interface for receiving selectFolderPathAction events.
   * The class that is interested in processing a selectFolderPathAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectFolderPathActionListener<code> method. When
   * the selectFolderPathAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectFolderPathActionEvent
   */
  public static class SelectFolderPathActionListener extends EventListener<UIViewerManagementForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiViewerManagementForm = event.getSource();
      UIFolderPathSelectorForm folderPathSelector = uiViewerManagementForm.createUIComponent(UIFolderPathSelectorForm.class,
                                                                                             null,
                                                                                             null);
      folderPathSelector.setSourceComponent(uiViewerManagementForm,
                                            new String[] { UIViewerManagementForm.FOLDER_PATH_INPUT });
      folderPathSelector.init();
      uiViewerManagementForm.showPopupComponent(folderPathSelector);
    }
  }

}
