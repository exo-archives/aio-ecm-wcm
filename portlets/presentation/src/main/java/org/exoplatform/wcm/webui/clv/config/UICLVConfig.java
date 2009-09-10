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
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.UICLVContainer;
import org.exoplatform.wcm.webui.clv.UICLVFolderMode;
import org.exoplatform.wcm.webui.clv.UICLVManualMode;
import org.exoplatform.wcm.webui.clv.UICLVPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class, 
  template = "app:/groovy/ContentListViewer/config/UICLVConfig.gtmpl", 
  events = {
    @EventConfig(listeners = UICLVConfig.SaveActionListener.class),
    @EventConfig(listeners = UICLVConfig.CancelActionListener.class),
    @EventConfig(listeners = UICLVConfig.SelectFolderPathActionListener.class),
    @EventConfig(listeners = UICLVConfig.IncreaseActionListener.class),
    @EventConfig(listeners = UICLVConfig.DecreaseActionListener.class),
    @EventConfig(listeners = UICLVConfig.DeleteActionListener.class)
  }
)
public class UICLVConfig extends UIForm implements UISelectable {

  /** The content list. */
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

  /** The Constant VIEWER_BUTTON_REFRESH. */
  public final static String VIEWER_BUTTON_REFRESH        = "ViewerButtonRefresh";

  /** The Constant VIEWER_READMORE. */
  public final static String VIEWER_READMORE        = "ViewerReadmore";

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

  /** The Constant VIEWER_LINK. */
  public static final String VIEWER_LINK                = "ViewerLink";
  
  /** The Constant VIEWER_MODES. */
  public static final String VIEWER_MODES                 = "ViewerMode";

  /** The Constant VIEWER_MANUAL_MODE. */
  public static final String VIEWER_MANUAL_MODE           = "ManualViewerMode";

  /** The Constant VIEWER_AUTO_MODE. */
  public static final String VIEWER_AUTO_MODE             = "AutoViewerMode";

  /** The Constant ORDER_BY. */
  public static final String ORDER_BY                     = "OrderBy";

  /** The Constant ORDER_BY_TITLE. */
  public static final String ORDER_BY_TITLE               = "OrderByTitle";

  /** The Constant ORDER_BY_DATE_CREATED. */
  public static final String ORDER_BY_DATE_CREATED        = "OrderByDateCreated";

  /** The Constant ORDER_BY_DATE_MODIFIED. */
  public static final String ORDER_BY_DATE_MODIFIED       = "OrderByDateModified";

  /** The Constant ORDER_BY_DATE_PUBLISHED. */
  public static final String ORDER_BY_DATE_PUBLISHED      = "OrderByDatePublished";
  
  /** The Constant ORDER_TYPES. */
  public static final String ORDER_TYPES = "OrderTypes";
  
  /** The Constant ORDER_DESC. */
  public static final String ORDER_DESC = "OrderDesc";
  
  /** The Constant ORDER_ASC. */
  public static final String ORDER_ASC = "OrderAsc";
  
  /** The Constant popupWidth. */
  public static final int popupWidth = 700;
  
  /** The Constant FOLDER_PATH_SELECTOR_POPUP_WINDOW. */
  public static final String FOLDER_PATH_SELECTOR_POPUP_WINDOW = "FolderPathSelectorPopupWindow";
  
  /** The Constant CORRECT_CONTENT_SELECTOR_POPUP_WINDOW. */
  public static final String CORRECT_CONTENT_SELECTOR_POPUP_WINDOW = "CorrectContentSelectorPopupWindow";

  /** The popup id. */
  private String popupId;
  
  /**
   * Gets the popup id.
   * 
   * @return the popup id
   */
  public String getPopupId() {
		return popupId;
	}

	/**
	 * Sets the popup id.
	 * 
	 * @param popupId the new popup id
	 */
	public void setPopupId(String popupId) {
		this.popupId = popupId;
	}

	/**
	 * Instantiates a new uI viewer management form.
	 * 
	 * @throws Exception the exception
	 */
  public UICLVConfig() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = context.getRequest().getPreferences();
    ResourceBundle bundle = context.getApplicationResourceBundle();
    String rootBundleKey = "UICLVConfig.label.";
    String folderPath = portletPreferences.getValue(UICLVPortlet.FOLDER_PATH, UICLVPortlet.FOLDER_PATH);
    UIFormStringInput headerInput = new UIFormStringInput(HEADER, HEADER, null);
    String headerValue = portletPreferences.getValue(UICLVPortlet.HEADER, null);
    headerInput.setValue(headerValue);
    List<SelectItemOption<String>> formViewerTemplateList = getTemplateList(PORTLET_NAME, FORM_VIEW_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> paginatorTemplateList = getTemplateList(PORTLET_NAME, PAGINATOR_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> viewerModeOptions = new ArrayList<SelectItemOption<String>>();
    viewerModeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + VIEWER_AUTO_MODE), VIEWER_AUTO_MODE));
    viewerModeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + VIEWER_MANUAL_MODE), VIEWER_MANUAL_MODE));
    
    List<SelectItemOption<String>> orderTypeOptions = new ArrayList<SelectItemOption<String>>();
    orderTypeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_DESC), "DESC"));
    orderTypeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_ASC), "ASC"));
    UIFormRadioBoxInput orderTypeRadioBoxInput = new UIFormRadioBoxInput(ORDER_TYPES, ORDER_TYPES, orderTypeOptions);
    String orderTypePref = portletPreferences.getValue(UICLVPortlet.ORDER_TYPE, null);
    if (orderTypePref == null) {
      orderTypeRadioBoxInput.setValue("DESC");
    } else {
      orderTypeRadioBoxInput.setValue(orderTypePref);
    }
    
    List<SelectItemOption<String>> orderByOptions = new ArrayList<SelectItemOption<String>>();
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_TITLE), "exo:title"));
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_CREATED), "exo:dateCreated"));
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_MODIFIED), "exo:dateModified"));
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_PUBLISHED),"publication:liveDate"));    
    UIFormRadioBoxInput viewerModeRadioBoxInput = new UIFormRadioBoxInput(VIEWER_MODES, VIEWER_MODES, viewerModeOptions);
    UIFormSelectBox orderBySelectBox = new UIFormSelectBox(ORDER_BY, ORDER_BY, orderByOptions);
    String orderByPref = portletPreferences.getValue(UICLVPortlet.ORDER_BY, null);
    orderBySelectBox.setValue(orderByPref); 
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
    UIFormCheckBoxInput<String> viewerButtonRefreshCheckbox = new UIFormCheckBoxInput<String>(VIEWER_BUTTON_REFRESH, VIEWER_BUTTON_REFRESH, null);
    viewerButtonRefreshCheckbox.setChecked(true);
    UIFormCheckBoxInput<String> viewerReadmoreCheckbox = new UIFormCheckBoxInput<String>(VIEWER_READMORE, VIEWER_READMORE, null);
    viewerReadmoreCheckbox.setChecked(true);
    UIFormCheckBoxInput<String> thumbnailsViewCheckbox = new UIFormCheckBoxInput<String>(VIEWER_THUMBNAILS_IMAGE, VIEWER_THUMBNAILS_IMAGE, null);
    thumbnailsViewCheckbox.setChecked(true);
    UIFormCheckBoxInput<String> titleViewerCheckbox = new UIFormCheckBoxInput<String>(VIEWER_TITLE, VIEWER_TITLE, null);
    titleViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput<String> summaryViewerCheckbox = new UIFormCheckBoxInput<String>(VIEWER_SUMMARY, VIEWER_SUMMARY, null);
    summaryViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput<String> dateCreatedViewerCheckbox = new UIFormCheckBoxInput<String>(VIEWER_DATE_CREATED, VIEWER_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(true);
    UIFormCheckBoxInput<String> viewerHeader = new UIFormCheckBoxInput<String>(VIEWER_HEADER, VIEWER_HEADER, null);
    viewerHeader.setChecked(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_HEADER, null)));    
    UIFormCheckBoxInput<String> viewerLink = new UIFormCheckBoxInput<String>(VIEWER_LINK, VIEWER_LINK, null);
    viewerLink.setChecked(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_LINK, null)));    
    String refreshAble = portletPreferences.getValue(UICLVPortlet.SHOW_REFRESH_BUTTON, null);
    viewerButtonRefreshCheckbox.setChecked(Boolean.parseBoolean(refreshAble));
    String refreshAbleRM = portletPreferences.getValue(UICLVPortlet.SHOW_READMORE, null);
    viewerReadmoreCheckbox.setChecked(Boolean.parseBoolean(refreshAbleRM));
    String imageShowAble = portletPreferences.getValue(UICLVPortlet.SHOW_THUMBNAILS_VIEW, null);
    thumbnailsViewCheckbox.setChecked(Boolean.parseBoolean(imageShowAble));
    String titleShowAble = portletPreferences.getValue(UICLVPortlet.SHOW_TITLE, null);
    titleViewerCheckbox.setChecked(Boolean.parseBoolean(titleShowAble));
    String summaryShowAble = portletPreferences.getValue(UICLVPortlet.SHOW_SUMMARY, null);
    summaryViewerCheckbox.setChecked(Boolean.parseBoolean(summaryShowAble));
    String dateShowAble = portletPreferences.getValue(UICLVPortlet.SHOW_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(Boolean.parseBoolean(dateShowAble));
    String formViewTemplate = portletPreferences.getValue(UICLVPortlet.FORM_VIEW_TEMPLATE_PATH, null);
    formViewTemplateSelector.setValue(formViewTemplate);
    String paginatorTemplate = portletPreferences.getValue(UICLVPortlet.PAGINATOR_TEMPlATE_PATH, null);
    paginatorTemplateSelector.setValue(paginatorTemplate);
    String itemsPerPageVal = portletPreferences.getValue(UICLVPortlet.ITEMS_PER_PAGE, null);
    itemsPerPageStringInput.setValue(itemsPerPageVal);
    itemsPerPageStringInput.setMaxLength(3);    
    if (isManualMode()) {
      orderBySelectBox.setRendered(false);
      viewerModeRadioBoxInput.setValue(VIEWER_MANUAL_MODE);
      String[] arr = portletPreferences.getValues(UICLVPortlet.CONTENT_LIST, null);
      if (arr != null && arr.length != 0) {
        for (int i = 0; i < arr.length; i++) {
          this.contentList.add(arr[i]);
        }        
      }
    } else {
      viewerModeRadioBoxInput.setValue(VIEWER_AUTO_MODE);
      orderBySelectBox.setRendered(true);
    }

    addChild(viewerModeRadioBoxInput);
    addChild(folderPathInputSet);
    addChild(orderBySelectBox);
    addChild(orderTypeRadioBoxInput);
    addChild(headerInput);
    addChild(formViewTemplateSelector);
    addChild(paginatorTemplateSelector);
    addChild(itemsPerPageStringInput);    
    addChild(viewerButtonRefreshCheckbox);
    addChild(thumbnailsViewCheckbox);
    addChild(titleViewerCheckbox);
    addChild(dateCreatedViewerCheckbox);
    addChild(summaryViewerCheckbox);
    addChild(viewerHeader);
    addChild(viewerLink);
    addChild(viewerReadmoreCheckbox);

    setActions(new String[] { "Save", "Cancel" });
  }

  /**
   * Sets the view able content list.
   * 
   * @param list the new view able content list
   */
  public void setViewAbleContentList(List<String> list) {
    this.contentList = list;
  }

  /**
   * Gets the view able content list.
   * 
   * @return the view able content list
   */
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
    Utils.closePopupWindow(this, popupId);
  }

  /**
   * Checks if is manual mode.
   * 
   * @return true, if is manual mode
   */
  public boolean isManualMode() {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = context.getRequest().getPreferences();
    String viewerMode = preferences.getValue(UICLVPortlet.VIEWER_MODE, null);
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
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
    String repository = manageableRepository.getConfiguration().getName();
    List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(
                                                                                repository,
                                                                                portletName,
                                                                                category,
                                                                                Utils.getSessionProvider(this));
    for (Node templateNode : templateNodeList) {
      String templateName = templateNode.getName();
      String templatePath = templateNode.getPath();
      templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateOptionList;
  }
  
  /**
   * Active new viewer mode.
   * 
   * @param uiNewViewer the ui new viewer
   * 
   * @throws Exception the exception
   */
  public void activeNewViewerMode(UICLVContainer uiNewViewer) throws Exception {
    UICLVPortlet uiListViewerPortlet = getAncestorOfType(UICLVPortlet.class);
    uiListViewerPortlet.removeChild(UICLVContainer.class);
    uiListViewerPortlet.addChild(uiNewViewer);
    uiNewViewer.init();
  }
  
  /**
   * Reset viewer mode.
   * 
   * @param uiViewer the ui viewer
   * 
   * @throws Exception the exception
   */
  public void resetViewerMode(UICLVContainer uiViewer) throws Exception {
    uiViewer.getChildren().clear();
    uiViewer.init();
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
  public static class SaveActionListener extends EventListener<UICLVConfig> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVConfig> event) throws Exception {
      UICLVConfig uiViewerManagementForm = event.getSource();
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      String currentViewerMode = portletPreferences.getValue(UICLVPortlet.VIEWER_MODE, null);
      if (currentViewerMode == null) currentViewerMode = UICLVConfig.VIEWER_AUTO_MODE;
      RepositoryService repositoryService = uiViewerManagementForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String folderPath = uiViewerManagementForm.getUIStringInput(UICLVConfig.FOLDER_PATH_INPUT).getValue();
      String header = uiViewerManagementForm.getUIStringInput(UICLVConfig.HEADER).getValue();
      String formViewTemplatePath = uiViewerManagementForm.getUIFormSelectBox(UICLVConfig.FORM_VIEW_TEMPLATES_SELECTOR).getValue();
      String paginatorTemplatePath = uiViewerManagementForm.getUIFormSelectBox(UICLVConfig.PAGINATOR_TEMPLATES_SELECTOR).getValue();
      String itemsPerPage = uiViewerManagementForm.getUIStringInput(UICLVConfig.ITEMS_PER_PAGE_INPUT).getValue();      
      String showRefreshButton = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_BUTTON_REFRESH).isChecked() ? "true" : "false";
      String showReadmore = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_READMORE).isChecked() ? "true" : "false";
      String viewThumbnails = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_THUMBNAILS_IMAGE).isChecked() ? "true" : "false";
      String viewTitle = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_TITLE).isChecked() ? "true" : "false";
      String viewSummary = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_SUMMARY).isChecked() ? "true" : "false";
      String viewDateCreated = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_DATE_CREATED).isChecked() ? "true" : "false";
      String viewerHeader = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_HEADER).isChecked() ? "true" : "false";
      String viewerLink = uiViewerManagementForm.getUIFormCheckBoxInput(UICLVConfig.VIEWER_LINK).isChecked() ? "true" : "false";
      UIFormRadioBoxInput modeBoxInput = (UIFormRadioBoxInput) uiViewerManagementForm.getChildById(UICLVConfig.VIEWER_MODES);
      String newViewerMode = modeBoxInput.getValue();
      UIFormRadioBoxInput orderTypeBoxInput = (UIFormRadioBoxInput) uiViewerManagementForm.getChildById(UICLVConfig.ORDER_TYPES);
      String orderType = orderTypeBoxInput.getValue();
      String orderBy = uiViewerManagementForm.getUIFormSelectBox(ORDER_BY).getValue();
      if (folderPath == null || folderPath.length() == 0) {
      	Utils.createPopupMessage(uiViewerManagementForm, "UIMessageBoard.msg.empty-folder-path", null, ApplicationMessage.WARNING);
        return;
      }
      if ((folderPath.endsWith(";") && newViewerMode.equals(VIEWER_AUTO_MODE)) || (!folderPath.endsWith(";") && newViewerMode.equals(VIEWER_MANUAL_MODE))) {
      	Utils.createPopupMessage(uiViewerManagementForm, "UIMessageBoard.msg.not-valid-action", null, ApplicationMessage.WARNING);
        return;
      }      
      portletPreferences.setValue(UICLVPortlet.REPOSITORY, repository);
      portletPreferences.setValue(UICLVPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UICLVPortlet.FOLDER_PATH, folderPath);
      portletPreferences.setValue(UICLVPortlet.FORM_VIEW_TEMPLATE_PATH, formViewTemplatePath);
      portletPreferences.setValue(UICLVPortlet.PAGINATOR_TEMPlATE_PATH, paginatorTemplatePath);
      portletPreferences.setValue(UICLVPortlet.ITEMS_PER_PAGE, itemsPerPage);      
      portletPreferences.setValue(UICLVPortlet.SHOW_REFRESH_BUTTON, showRefreshButton);
      portletPreferences.setValue(UICLVPortlet.SHOW_READMORE, showReadmore);
      portletPreferences.setValue(UICLVPortlet.SHOW_THUMBNAILS_VIEW, viewThumbnails);
      portletPreferences.setValue(UICLVPortlet.SHOW_TITLE, viewTitle);
      portletPreferences.setValue(UICLVPortlet.SHOW_DATE_CREATED, viewDateCreated);
      portletPreferences.setValue(UICLVPortlet.SHOW_SUMMARY, viewSummary);
      portletPreferences.setValue(UICLVPortlet.HEADER, header);
      portletPreferences.setValue(UICLVPortlet.SHOW_HEADER, viewerHeader);
      portletPreferences.setValue(UICLVPortlet.SHOW_LINK, viewerLink);
      portletPreferences.setValue(UICLVPortlet.VIEWER_MODE, newViewerMode);
      portletPreferences.setValue(UICLVPortlet.ORDER_TYPE, orderType);
      
      if (uiViewerManagementForm.isManualMode()) {
        String[] sl = (String[]) uiViewerManagementForm.getViewAbleContentList().toArray(new String[0]);
        portletPreferences.setValues(UICLVPortlet.CONTENT_LIST, sl);
      } else {
        portletPreferences.setValue(UICLVPortlet.ORDER_BY, orderBy);
        portletPreferences.setValues(UICLVPortlet.CONTENT_LIST, new String [] {});
      }
      portletPreferences.store();
      if (!Utils.isQuickEditmode(uiViewerManagementForm, "UIViewerManagementPopupWindow")) {
      	Utils.createPopupMessage(uiViewerManagementForm, "UIMessageBoard.msg.saving-success", null, ApplicationMessage.INFO);
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
        UICLVPortlet uiContentListViewerPortlet = uiViewerManagementForm.getAncestorOfType(UICLVPortlet.class);
        UICLVFolderMode uiFolderViewer = null;
        UICLVManualMode uiCorrectContentsViewer = null;
        if (currentViewerMode.equals(UICLVConfig.VIEWER_AUTO_MODE)) {
          if (newViewerMode.equals(UICLVConfig.VIEWER_AUTO_MODE)) {
            uiFolderViewer = uiContentListViewerPortlet.getChild(UICLVFolderMode.class);            
            uiFolderViewer.getChildren().clear();
            uiFolderViewer.init();
          } else if (newViewerMode.equals(UICLVConfig.VIEWER_MANUAL_MODE)) {                       
            uiContentListViewerPortlet.removeChild(UICLVFolderMode.class);
            uiCorrectContentsViewer = uiContentListViewerPortlet.addChild(UICLVManualMode.class, null, null);            
            uiCorrectContentsViewer.init();            
          }
        } else if (currentViewerMode.equals(UICLVConfig.VIEWER_MANUAL_MODE)) {
          if (newViewerMode.equals(UICLVConfig.VIEWER_MANUAL_MODE)) {
            uiCorrectContentsViewer = uiContentListViewerPortlet.getChild(UICLVManualMode.class);            
            uiCorrectContentsViewer.getChildren().clear();
            uiCorrectContentsViewer.init();
          } else if (newViewerMode.equals(UICLVConfig.VIEWER_AUTO_MODE)) {            
            uiContentListViewerPortlet.removeChild(UICLVManualMode.class);
            uiFolderViewer = uiContentListViewerPortlet.addChild(UICLVFolderMode.class, null, null);
            uiFolderViewer.init();
          }
        }
        Utils.closePopupWindow(uiViewerManagementForm, "UIViewerManagementPopupWindow");
      }
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
  public static class CancelActionListener extends EventListener<UICLVConfig> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVConfig> event) throws Exception {
      UICLVConfig viewerManagementForm = event.getSource();
      if (Utils.isQuickEditmode(viewerManagementForm, "UIViewerManagementPopupWindow")) {
      	Utils.closePopupWindow(viewerManagementForm, "UIViewerManagementPopupWindow");
      } else {
      	Utils.createPopupMessage(viewerManagementForm, "UIMessageBoard.msg.none-action-excuted", null, ApplicationMessage.INFO);
      }
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
  public static class SelectFolderPathActionListener extends EventListener<UICLVConfig> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVConfig> event) throws Exception {
      UICLVConfig uiViewerManagementForm = event.getSource();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      UIFormRadioBoxInput modeBoxInput = (UIFormRadioBoxInput) uiViewerManagementForm.getChildById(UICLVConfig.VIEWER_MODES);
      UIFormSelectBox orderBySelector = uiViewerManagementForm.getUIFormSelectBox(ORDER_BY);
      String mode = modeBoxInput.getValue();
      if (mode.equals(UICLVConfig.VIEWER_AUTO_MODE)) {
        orderBySelector.setRendered(true);
        UICLVFolderSelector uiFolderPathSelector = uiViewerManagementForm.createUIComponent(UICLVFolderSelector.class, null, null);
        uiFolderPathSelector.setSourceComponent(uiViewerManagementForm, new String[] { UICLVConfig.FOLDER_PATH_INPUT });
        uiFolderPathSelector.init();
        Utils.createPopupWindow(uiViewerManagementForm, uiFolderPathSelector, FOLDER_PATH_SELECTOR_POPUP_WINDOW, 600, 400);
        uiViewerManagementForm.setPopupId(FOLDER_PATH_SELECTOR_POPUP_WINDOW);
      } else {
        orderBySelector.setRendered(false);
        UICLVContentSelector uiCorrectContentSelectorForm = uiViewerManagementForm.createUIComponent(UICLVContentSelector.class, null, null);
        uiCorrectContentSelectorForm.setSourceComponent(uiViewerManagementForm, new String[] { UICLVConfig.FOLDER_PATH_INPUT });
        uiCorrectContentSelectorForm.init(context);
        Utils.createPopupWindow(uiViewerManagementForm, uiCorrectContentSelectorForm, CORRECT_CONTENT_SELECTOR_POPUP_WINDOW, 600, 400);
        uiViewerManagementForm.setPopupId(CORRECT_CONTENT_SELECTOR_POPUP_WINDOW);
      }
    }
  }

  /**
   * The listener interface for receiving increaseAction events.
   * The class that is interested in processing a increaseAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addIncreaseActionListener<code> method. When
   * the increaseAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see IncreaseActionEvent
   */
  public static class IncreaseActionListener extends EventListener<UICLVConfig> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVConfig> event) throws Exception {
      UICLVConfig uiForm = event.getSource();
      List<String> contentList = uiForm.getViewAbleContentList();
      int currIndex = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      if (currIndex > 0) {
        String temp = contentList.get(currIndex - 1);
        contentList.set(currIndex - 1, contentList.get(currIndex));
        contentList.set(currIndex, temp);
      }
    }
  }

  /**
   * The listener interface for receiving decreaseAction events.
   * The class that is interested in processing a decreaseAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addDecreaseActionListener<code> method. When
   * the decreaseAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DecreaseActionEvent
   */
  public static class DecreaseActionListener extends EventListener<UICLVConfig> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVConfig> event) throws Exception {
      UICLVConfig uiForm = event.getSource();
      List<String> contentList = uiForm.getViewAbleContentList();
      int currIndex = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      if (currIndex < contentList.size() - 1) {
        String temp = contentList.get(currIndex + 1);
        contentList.set(currIndex + 1, contentList.get(currIndex));
        contentList.set(currIndex, temp);
      }
    }
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
  public static class DeleteActionListener extends EventListener<UICLVConfig> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVConfig> event) throws Exception {
      UICLVConfig uiForm = event.getSource();      
      int currIndex = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      List<String> contentList = uiForm.getViewAbleContentList();
      contentList.remove(currIndex);
    }
  }
}
