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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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
import org.exoplatform.webui.form.UIFormRadioBoxInput;
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
    @EventConfig(listeners = UIViewerManagementForm.SelectFolderPathActionListener.class),
    @EventConfig(listeners = UIViewerManagementForm.IncreaseActionListener.class),
    @EventConfig(listeners = UIViewerManagementForm.DecreaseActionListener.class)     
  }
)
public class UIViewerManagementForm extends UIForm implements UISelectable {

  private List<String>       contentList                  = new ArrayList<String>();

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
  public final static String ITEMS_PER_PAGE_INPUT         = "ItemsPerPage";

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

  public static final String VIEWER_MODES                 = "ViewerMode";

  public static final String VIEWER_MANUAL_MODE           = "ManualViewerMode";

  public static final String VIEWER_AUTO_MODE             = "AutoViewerMode";

  public static final String ORDER_BY                     = "OrderBy";

  public static final String ORDER_BY_TITLE               = "OrderByTitle";

  public static final String ORDER_BY_DATE_CREATED        = "OrderByDateCreated";

  public static final String ORDER_BY_DATE_MODIFIED       = "OrderByDateModified";

  public static final String ORDER_BY_DATE_PUBLISHED      = "OrderByDatePublished";

  /**
   * Instantiates a new uI viewer management form.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public UIViewerManagementForm() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = context.getRequest().getPreferences();
    ResourceBundle bundle = context.getApplicationResourceBundle();
    String rootBundleKey = "UIViewerManagementForm.label.";
    String folderPath = portletPreferences.getValue(UIContentListViewerPortlet.FOLDER_PATH, UIContentListViewerPortlet.FOLDER_PATH);
    UIFormStringInput headerInput = new UIFormStringInput(HEADER, HEADER, null);
    String headerValue = portletPreferences.getValue(UIContentListViewerPortlet.HEADER, null);
    headerInput.setValue(headerValue);
    List<SelectItemOption<String>> formViewerTemplateList = getTemplateList(PORTLET_NAME, FORM_VIEW_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> paginatorTemplateList = getTemplateList(PORTLET_NAME, PAGINATOR_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> viewerModeOptions = new ArrayList<SelectItemOption<String>>();
    viewerModeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + VIEWER_AUTO_MODE), VIEWER_AUTO_MODE));
    viewerModeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + VIEWER_MANUAL_MODE), VIEWER_MANUAL_MODE));
    List<SelectItemOption<String>> orderByOptions = new ArrayList<SelectItemOption<String>>();
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_TITLE), ORDER_BY_TITLE));
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_CREATED), ORDER_BY_DATE_CREATED));
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_MODIFIED), ORDER_BY_DATE_MODIFIED));
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_PUBLISHED), ORDER_BY_DATE_PUBLISHED));
    UIFormRadioBoxInput viewerModeRadioBoxInput = new UIFormRadioBoxInput(VIEWER_MODES, VIEWER_MODES, viewerModeOptions);
    UIFormSelectBox orderBySelectBox = new UIFormSelectBox(ORDER_BY, ORDER_BY, orderByOptions);
    UIFormInputSetWithAction folderPathInputSet = new UIFormInputSetWithAction(FOLDER_PATH_INPUTSET);
    UIFormStringInput folderPathInput = new UIFormStringInput(FOLDER_PATH_INPUT, FOLDER_PATH_INPUT, null);
    folderPathInput.setEditable(false);
    if (folderPath != null) {
      folderPathInput.setValue(folderPath);
    }
    folderPathInputSet.addChild(folderPathInput);
    folderPathInputSet.setActionInfo(FOLDER_PATH_INPUT, new String[] { "SelectFolderPath" });
    UIFormStringInput itemsPerPageStringInput = new UIFormStringInput(ITEMS_PER_PAGE_INPUT, ITEMS_PER_PAGE_INPUT, null);
    UIFormSelectBox formViewTemplateSelector = new UIFormSelectBox(FORM_VIEW_TEMPLATES_SELECTOR, FORM_VIEW_TEMPLATES_SELECTOR, formViewerTemplateList);
    UIFormSelectBox paginatorTemplateSelector = new UIFormSelectBox(PAGINATOR_TEMPLATES_SELECTOR, PAGINATOR_TEMPLATES_SELECTOR, paginatorTemplateList);
    UIFormCheckBoxInput viewerButtonQuickEditCheckbox = new UIFormCheckBoxInput(VIEWER_BUTTON_QUICK_EDIT, VIEWER_BUTTON_QUICK_EDIT, null);
    viewerButtonQuickEditCheckbox.setChecked(true);
    UIFormCheckBoxInput viewerButtonRefreshCheckbox = new UIFormCheckBoxInput(VIEWER_BUTTON_REFRESH, VIEWER_BUTTON_REFRESH, null);
    viewerButtonRefreshCheckbox.setChecked(true);
    UIFormCheckBoxInput thumbnailsViewCheckbox = new UIFormCheckBoxInput(VIEWER_THUMBNAILS_IMAGE, VIEWER_THUMBNAILS_IMAGE, null);
    thumbnailsViewCheckbox.setChecked(true);
    UIFormCheckBoxInput titleViewerCheckbox = new UIFormCheckBoxInput(VIEWER_TITLE, VIEWER_TITLE, null);
    titleViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput summaryViewerCheckbox = new UIFormCheckBoxInput(VIEWER_SUMMARY, VIEWER_SUMMARY, null);
    summaryViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput dateCreatedViewerCheckbox = new UIFormCheckBoxInput(VIEWER_DATE_CREATED, VIEWER_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput viewerHeader = new UIFormCheckBoxInput(VIEWER_HEADER, VIEWER_HEADER, null);
    viewerHeader.setChecked(Boolean.parseBoolean(portletPreferences.getValue(UIContentListViewerPortlet.SHOW_HEADER, null)));
    String quickEditAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_QUICK_EDIT_BUTTON, null);
    viewerButtonQuickEditCheckbox.setChecked(Boolean.parseBoolean(quickEditAble));
    String refreshAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON, null);
    viewerButtonRefreshCheckbox.setChecked(Boolean.parseBoolean(refreshAble));
    String imageShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_THUMBNAILS_VIEW, null);
    thumbnailsViewCheckbox.setChecked(Boolean.parseBoolean(imageShowAble));
    String titleShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_TITLE, null);
    titleViewerCheckbox.setChecked(Boolean.parseBoolean(titleShowAble));
    String summaryShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_SUMMARY, null);
    summaryViewerCheckbox.setChecked(Boolean.parseBoolean(summaryShowAble));
    String dateShowAble = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(Boolean.parseBoolean(dateShowAble));
    String formViewTemplate = portletPreferences.getValue(UIContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH, null);
    formViewTemplateSelector.setValue(formViewTemplate);
    String paginatorTemplate = portletPreferences.getValue(UIContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH, null);
    paginatorTemplateSelector.setValue(paginatorTemplate);
    String itemsPerPageVal = portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE, null);
    itemsPerPageStringInput.setValue(itemsPerPageVal);
    if (isManualMode()) {
      orderBySelectBox.setRendered(false);
      viewerModeRadioBoxInput.setValue(VIEWER_MANUAL_MODE);
      String[] arr = portletPreferences.getValues(UIContentListViewerPortlet.CONTENT_LIST, null);
      if (arr != null && arr.length != 0) {
        this.contentList = Arrays.asList(arr);
      }
    } else {
      viewerModeRadioBoxInput.setValue(VIEWER_AUTO_MODE);
      orderBySelectBox.setRendered(true);
    }

    addChild(viewerModeRadioBoxInput);
    addChild(folderPathInputSet);
    addChild(orderBySelectBox);
    addChild(headerInput);
    addChild(formViewTemplateSelector);
    addChild(paginatorTemplateSelector);
    addChild(itemsPerPageStringInput);
    addChild(viewerButtonQuickEditCheckbox);
    addChild(viewerButtonRefreshCheckbox);
    addChild(thumbnailsViewCheckbox);
    addChild(titleViewerCheckbox);
    addChild(dateCreatedViewerCheckbox);
    addChild(summaryViewerCheckbox);
    addChild(viewerHeader);

    setActions(new String[] { "Save", "Cancel" });
  }

  public void setViewAbleContentList(List<String> list) {
    this.contentList = list;
  }

  public List<String> getViewAbleContentList() {
    return this.contentList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String,
   *      java.lang.Object)
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

  public boolean isManualMode() {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = context.getRequest().getPreferences();
    String viewerMode = preferences.getValue(UIContentListViewerPortlet.VIEWER_MODE, null);
    if (viewerMode == null || VIEWER_AUTO_MODE.equals(viewerMode.toString()))
      return false;
    return true;
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
    List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository, portletName, category, provider);
    for (Node templateNode : templateNodeList) {
      String templateName = templateNode.getName();
      String templatePath = templateNode.getPath();
      templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateOptionList;
  }

  /**
   * The listener interface for receiving saveAction events. The class that is
   * interested in processing a saveAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addSaveActionListener<code> method. When
   * the saveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveActionEvent
   */
  public static class SaveActionListener extends EventListener<UIViewerManagementForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiViewerManagementForm = event.getSource();
      UIApplication uiApp = uiViewerManagementForm.getAncestorOfType(UIApplication.class);
      RepositoryService repositoryService = uiViewerManagementForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String folderPath = uiViewerManagementForm.getUIStringInput(UIViewerManagementForm.FOLDER_PATH_INPUT).getValue();
      String header = uiViewerManagementForm.getUIStringInput(UIViewerManagementForm.HEADER).getValue();
      String formViewTemplatePath = uiViewerManagementForm.getUIFormSelectBox(UIViewerManagementForm.FORM_VIEW_TEMPLATES_SELECTOR).getValue();
      String paginatorTemplatePath = uiViewerManagementForm.getUIFormSelectBox(UIViewerManagementForm.PAGINATOR_TEMPLATES_SELECTOR).getValue();
      String itemsPerPage = uiViewerManagementForm.getUIStringInput(UIViewerManagementForm.ITEMS_PER_PAGE_INPUT).getValue();
      String showQuickEdit = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_BUTTON_QUICK_EDIT).isChecked() ? "true" : "false";
      String showRefreshButton = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_BUTTON_REFRESH).isChecked() ? "true" : "false";
      String viewThumbnails = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_THUMBNAILS_IMAGE).isChecked() ? "true" : "false";
      String viewTitle = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_TITLE).isChecked() ? "true" : "false";
      String viewSummary = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_SUMMARY).isChecked() ? "true" : "false";
      String viewDateCreated = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_DATE_CREATED).isChecked() ? "true" : "false";
      String viewerHeader = uiViewerManagementForm.getUIFormCheckBoxInput(UIViewerManagementForm.VIEWER_HEADER).isChecked() ? "true" : "false";
      UIFormRadioBoxInput modeBoxInput = (UIFormRadioBoxInput) uiViewerManagementForm.getChildById(UIViewerManagementForm.VIEWER_MODES);
      String viewerMode = modeBoxInput.getValue();
      String orderBy = uiViewerManagementForm.getUIFormSelectBox(ORDER_BY).getValue();
      if (folderPath == null || folderPath.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.empty-folder-path", null, ApplicationMessage.WARNING));
        return;
      }
      if ((folderPath.endsWith(";") && viewerMode.equals(VIEWER_AUTO_MODE))
          || (!folderPath.endsWith(";") && viewerMode.equals(VIEWER_MANUAL_MODE))) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.not-valid-action", null, ApplicationMessage.WARNING));
        return;
      }

      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      portletPreferences.setValue(UIContentListViewerPortlet.REPOSITORY, repository);
      portletPreferences.setValue(UIContentListViewerPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UIContentListViewerPortlet.FOLDER_PATH, folderPath);
      portletPreferences.setValue(UIContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH, formViewTemplatePath);
      portletPreferences.setValue(UIContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH, paginatorTemplatePath);
      portletPreferences.setValue(UIContentListViewerPortlet.ITEMS_PER_PAGE, itemsPerPage);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_QUICK_EDIT_BUTTON, showQuickEdit);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON, showRefreshButton);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_THUMBNAILS_VIEW, viewThumbnails);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_TITLE, viewTitle);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_DATE_CREATED, viewDateCreated);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_SUMMARY, viewSummary);
      portletPreferences.setValue(UIContentListViewerPortlet.HEADER, header);
      portletPreferences.setValue(UIContentListViewerPortlet.SHOW_HEADER, viewerHeader);
      portletPreferences.setValue(UIContentListViewerPortlet.VIEWER_MODE, viewerMode);
      if (uiViewerManagementForm.isManualMode()) {
        String[] sl = (String[]) uiViewerManagementForm.getViewAbleContentList().toArray(new String[0]);
        portletPreferences.setValues(UIContentListViewerPortlet.CONTENT_LIST, sl);
      } else {
        portletPreferences.setValue(UIContentListViewerPortlet.ORDER_BY, orderBy);
      }
      portletPreferences.store();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.saving-success", null, ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
        UIContentListViewerPortlet uiContentListViewerPortlet = uiViewerManagementForm.getAncestorOfType(UIContentListViewerPortlet.class);
        UIFolderViewer uiFolderViewer = uiContentListViewerPortlet.getChild(UIFolderViewer.class);
        uiFolderViewer.getChildren().clear();
        uiFolderViewer.init();
      }
      UIPopupContainer uiPopupContainer = (UIPopupContainer) uiViewerManagementForm.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
    }
  }

  /**
   * The listener interface for receiving cancelAction events. The class that is
   * interested in processing a cancelAction event implements this interface,
   * and the object created with that class is registered with a component using
   * the component's <code>addCancelActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CancelActionEvent
   */
  public static class CancelActionListener extends EventListener<UIViewerManagementForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm viewerManagementForm = event.getSource();
      UIApplication uiApp = viewerManagementForm.getAncestorOfType(UIApplication.class);
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.none-action-excuted", null, ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
      }
      UIPopupContainer uiPopupContainer = (UIPopupContainer) viewerManagementForm.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
    }
  }

  /**
   * The listener interface for receiving selectFolderPathAction events. The
   * class that is interested in processing a selectFolderPathAction event
   * implements this interface, and the object created with that class is
   * registered with a component using the component's
   * <code>addSelectFolderPathActionListener<code> method. When
   * the selectFolderPathAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectFolderPathActionEvent
   */
  public static class SelectFolderPathActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiViewerManagementForm = event.getSource();
      UIFormRadioBoxInput modeBoxInput = (UIFormRadioBoxInput) uiViewerManagementForm.getChildById(UIViewerManagementForm.VIEWER_MODES);
      UIFormSelectBox orderBySelector = uiViewerManagementForm.getUIFormSelectBox(ORDER_BY);
      String mode = modeBoxInput.getValue();
      if (mode.equals(UIViewerManagementForm.VIEWER_AUTO_MODE)) {
        orderBySelector.setRendered(true);
        UIFolderPathSelectorForm uiFolderPathSelector = uiViewerManagementForm.createUIComponent(UIFolderPathSelectorForm.class, null, null);
        uiFolderPathSelector.setSourceComponent(uiViewerManagementForm, new String[] { UIViewerManagementForm.FOLDER_PATH_INPUT });
        uiFolderPathSelector.init();
        uiViewerManagementForm.showPopupComponent(uiFolderPathSelector);
      } else {
        orderBySelector.setRendered(false);
        UICorrectContentSelectorForm uiCorrectContentSelectorForm = uiViewerManagementForm.createUIComponent(UICorrectContentSelectorForm.class, null, null);
        uiCorrectContentSelectorForm.setSourceComponent(uiViewerManagementForm, new String[] { UIViewerManagementForm.FOLDER_PATH_INPUT });
        uiCorrectContentSelectorForm.init();
        uiViewerManagementForm.showPopupComponent(uiCorrectContentSelectorForm);
      }
    }
  }

  public static class IncreaseActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiForm = event.getSource();
      List<String> contentList = uiForm.getViewAbleContentList();
      int currIndex = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      if (currIndex > 0) {
        String temp = contentList.get(currIndex - 1);
        contentList.set(currIndex - 1, contentList.get(currIndex));
        contentList.set(currIndex, temp);
      }
    }
  }

  public static class DecreaseActionListener extends EventListener<UIViewerManagementForm> {
    public void execute(Event<UIViewerManagementForm> event) throws Exception {
      UIViewerManagementForm uiForm = event.getSource();
      List<String> contentList = uiForm.getViewAbleContentList();
      int currIndex = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      if (currIndex < contentList.size() - 1) {
        String temp = contentList.get(currIndex + 1);
        contentList.set(currIndex + 1, contentList.get(currIndex));
        contentList.set(currIndex, temp);
      }
    }
  }

}
