/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.pclv;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequestWrapper;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.category.UICategoryNavigationConstant;
import org.exoplatform.wcm.webui.selector.page.UIPageSelector;
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
import org.exoplatform.webui.form.ext.UIFormInputSetWithAction;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * ngoc.tran@exoplatform.com
 * Jun 23, 2009
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class, 
                 template = "app:/groovy/ParameterizedContentListViewer/UIPCLVConfig.gtmpl",
                 events = {
                     @EventConfig(listeners = UIPCLVConfig.CancelActionListener.class),
                     @EventConfig(listeners = UIPCLVConfig.SelectTargetPageActionListener.class),
                     @EventConfig(listeners = UIPCLVConfig.SaveActionListener.class)
                     
                   }
               )
public class UIPCLVConfig extends UIForm implements UISelectable {

  /** The Constant HEADER. */
  public final static String HEADER                            = "Header";

  /** The Constant PORTLET_NAME. */
  public final static String PORTLET_NAME                      = "Parameterized Content List Viewer";

  /** The Constant FORM_VIEW_TEMPLATE_CATEGORY. */
  public final static String FORM_VIEW_TEMPLATE_CATEGORY       = "list-by-folder";

  /** The Constant PAGINATOR_TEMPLATE_CATEGORY. */
  public final static String PAGINATOR_TEMPLATE_CATEGORY       = "paginators";

  /** The Constant ITEMS_PER_PAGE_SELECTOR. */
  public final static String ITEMS_PER_PAGE_INPUT              = "ItemsPerPage";

  /** The Constant TARGET_PAGE_INPUT. */
  public final static String TARGET_PAGE_INPUT                 = "UIParameterizedTagetPageInput";

  /** The Constant TARGET_PATH_SELECTOR_POPUP_WINDOW. */
  public final static String TARGET_PATH_SELECTOR_POPUP_WINDOW = "UIParameterTargetPathPopupWindow";

  /** The Constant TARGET_PAGE_INPUT_SET_ACTION. */
  public final static String TARGET_PAGE_INPUT_SET_ACTION      = "UIParameterizedTagetPageInputSetAction";

  /** The Constant PREFERENCE_TARGET_PATH. */
  public final static String PREFERENCE_TARGET_PATH            = "targetPath";

  /** The Constant FORM_VIEW_TEMPLATES_SELECTOR. */
  public final static String FORM_VIEW_TEMPLATES_SELECTOR      = "FormViewTemplate";

  /** The Constant PAGINATOR_TEMPLATES_SELECTOR. */
  public final static String PAGINATOR_TEMPLATES_SELECTOR      = "PaginatorTemplate";

  /** The Constant VIEWER_BUTTON_REFRESH. */
  public final static String VIEWER_BUTTON_REFRESH             = "ViewerButtonRefresh";

  /** The Constant VIEWER_THUMBNAILS_IMAGE. */
  public static final String VIEWER_THUMBNAILS_IMAGE           = "ViewerThumbnailsView";

  /** The Constant VIEWER_TITLE. */
  public static final String VIEWER_TITLE                      = "ViewerTitle";

  /** The Constant VIEWER_DATE_CREATED. */
  public static final String VIEWER_DATE_CREATED               = "ViewerDateCreated";

  /** The Constant VIEWER_SUMMARY. */
  public static final String VIEWER_SUMMARY                    = "ViewerSummary";

  /** The Constant VIEWER_HEADER. */
  public static final String VIEWER_HEADER                     = "ViewerHeader";

  /** The Constant AUTO_DETECT. */
  public static final String AUTO_DETECT                       = "AutomaticDetection";

  /** The Constant SHOW_MORE_LINK. */
  public static final String SHOW_MORE_LINK                    = "ShowMoreLink";

  /** The Constant SHOW_RSS_LINK. */
  public static final String SHOW_RSS_LINK                     = "ShowRSSLink";

  /** The Constant SHOW_LINK. */
  public static final String SHOW_LINK                         = "ShowLink";

  /** The Constant ORDER_BY. */
  public static final String ORDER_BY                          = "OrderBy";

  /** The Constant ORDER_BY_DATE_CREATED. */
  public static final String ORDER_BY_DATE_CREATED             = "OrderByDateCreated";

  /** The Constant ORDER_TYPES. */
  public static final String ORDER_TYPES                       = "OrderTypes";

  /** The Constant ORDER_DESC. */
  public static final String ORDER_DESC                        = "OrderDesc";

  /** The Constant ORDER_ASC. */
  public static final String ORDER_ASC                         = "OrderAsc";

  /** The popup id. */
  private String popupId;
  
  /**
   * Instantiates a new uI parameterized management form.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public UIPCLVConfig() throws Exception {

    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = context.getRequest().getPreferences();
    
    ResourceBundle bundle = context.getApplicationResourceBundle();
    String rootBundleKey = "UIPCLVConfig.label.";
    
    List<SelectItemOption<String>> orderTypeOptions = new ArrayList<SelectItemOption<String>>();
    orderTypeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_DESC), "DESC"));
    orderTypeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_ASC), "ASC"));

    UIFormRadioBoxInput orderTypeRadioBoxInput = new UIFormRadioBoxInput(ORDER_TYPES, ORDER_TYPES, orderTypeOptions);
    String orderTypePref = portletPreferences.getValue(UIPCLVPortlet.ORDER_TYPE, null);
    if (orderTypePref == null) {
      orderTypeRadioBoxInput.setValue("DESC");
    } else {
      orderTypeRadioBoxInput.setValue(orderTypePref);
    }

    List<SelectItemOption<String>> orderByOptions = new ArrayList<SelectItemOption<String>>();
    orderByOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_BY_DATE_CREATED), "exo:dateCreated"));
    UIFormSelectBox orderBySelectBox = new UIFormSelectBox(ORDER_BY, ORDER_BY, orderByOptions);
    String orderByPref = portletPreferences.getValue(UIPCLVPortlet.ORDER_BY, null);
    orderBySelectBox.setValue(orderByPref);
    
    UIFormCheckBoxInput autoDetect = new UIFormCheckBoxInput(AUTO_DETECT, AUTO_DETECT, null);
    autoDetect.setChecked(true);
    String autoDetected = portletPreferences.getValue(UIPCLVPortlet.SHOW_AUTO_DETECT, null);
    autoDetect.setChecked(Boolean.parseBoolean(autoDetected));
    
    UIFormStringInput headerInput = new UIFormStringInput(HEADER, HEADER, null);
    
    if (!Boolean.parseBoolean(autoDetected)) {
      headerInput.setValue("");

      String headerValue = portletPreferences.getValue(UIPCLVPortlet.HEADER, null);
      headerInput.setValue(headerValue);
    } else {
      headerInput.setValue(this.getHeader());
    }
    
    List<SelectItemOption<String>> formViewerTemplateList = getTemplateList(PORTLET_NAME, FORM_VIEW_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> paginatorTemplateList = getTemplateList(PORTLET_NAME, PAGINATOR_TEMPLATE_CATEGORY);
    
    UIFormStringInput itemsPerPageStringInput = new UIFormStringInput(ITEMS_PER_PAGE_INPUT, ITEMS_PER_PAGE_INPUT, null);
    String itemsPerPageVal = portletPreferences.getValue(UIPCLVPortlet.ITEMS_PER_PAGE, null);
    itemsPerPageStringInput.setValue(itemsPerPageVal);
    itemsPerPageStringInput.setMaxLength(3);
    
    UIFormSelectBox formViewTemplateSelector = new UIFormSelectBox(FORM_VIEW_TEMPLATES_SELECTOR, FORM_VIEW_TEMPLATES_SELECTOR, formViewerTemplateList);
    String formViewTemplate = portletPreferences.getValue(UIPCLVPortlet.FORM_VIEW_TEMPLATE_PATH, null);
    formViewTemplateSelector.setValue(formViewTemplate);
    
    UIFormSelectBox paginatorTemplateSelector = new UIFormSelectBox(PAGINATOR_TEMPLATES_SELECTOR, PAGINATOR_TEMPLATES_SELECTOR, paginatorTemplateList);
    String paginatorTemplate = portletPreferences.getValue(UIPCLVPortlet.PAGINATOR_TEMPlATE_PATH, null);
    paginatorTemplateSelector.setValue(paginatorTemplate);
    
    UIFormCheckBoxInput viewerButtonRefreshCheckbox = new UIFormCheckBoxInput(VIEWER_BUTTON_REFRESH, VIEWER_BUTTON_REFRESH, null);
    viewerButtonRefreshCheckbox.setChecked(false);
    String refreshAble = portletPreferences.getValue(UIPCLVPortlet.SHOW_REFRESH_BUTTON, null);
    viewerButtonRefreshCheckbox.setChecked(Boolean.parseBoolean(refreshAble));
    
    UIFormCheckBoxInput thumbnailsViewCheckbox = new UIFormCheckBoxInput(VIEWER_THUMBNAILS_IMAGE, VIEWER_THUMBNAILS_IMAGE, null);
    thumbnailsViewCheckbox.setChecked(true);
    String imageShowAble = portletPreferences.getValue(UIPCLVPortlet.SHOW_THUMBNAILS_VIEW, null);
    thumbnailsViewCheckbox.setChecked(Boolean.parseBoolean(imageShowAble));
    
    UIFormCheckBoxInput titleViewerCheckbox = new UIFormCheckBoxInput(VIEWER_TITLE, VIEWER_TITLE, null);
    titleViewerCheckbox.setChecked(true);
    String titleShowAble = portletPreferences.getValue(UIPCLVPortlet.SHOW_TITLE, null);
    titleViewerCheckbox.setChecked(Boolean.parseBoolean(titleShowAble));
    
    UIFormCheckBoxInput summaryViewerCheckbox = new UIFormCheckBoxInput(VIEWER_SUMMARY, VIEWER_SUMMARY, null);
    summaryViewerCheckbox.setChecked(true);
    String summaryShowAble = portletPreferences.getValue(UIPCLVPortlet.SHOW_SUMMARY, null);
    summaryViewerCheckbox.setChecked(Boolean.parseBoolean(summaryShowAble));
    
    UIFormCheckBoxInput dateCreatedViewerCheckbox = new UIFormCheckBoxInput(VIEWER_DATE_CREATED, VIEWER_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(true);
    String dateShowAble = portletPreferences.getValue(UIPCLVPortlet.SHOW_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(Boolean.parseBoolean(dateShowAble));

    UIFormCheckBoxInput viewerHeader = new UIFormCheckBoxInput(VIEWER_HEADER, VIEWER_HEADER, null);
    viewerHeader.setChecked(true);
    String viewHeader = portletPreferences.getValue(UIPCLVPortlet.SHOW_HEADER, null);
    viewerHeader.setChecked(Boolean.parseBoolean(viewHeader));
    
    String preferenceTargetPath = portletPreferences.getValue(UIPCLVPortlet.TARGET_PAGE, null);
    UIFormInputSetWithAction targetPathFormInputSet = new UIFormInputSetWithAction(TARGET_PAGE_INPUT_SET_ACTION);
    UIFormStringInput targetPathFormStringInput = new UIFormStringInput(TARGET_PAGE_INPUT, TARGET_PAGE_INPUT, preferenceTargetPath);
    targetPathFormStringInput.setValue(preferenceTargetPath);
    targetPathFormStringInput.setEditable(false);
    targetPathFormInputSet.setActionInfo(TARGET_PAGE_INPUT, new String[] {"SelectTargetPage"}) ;
    targetPathFormInputSet.addUIFormInput(targetPathFormStringInput);
    
    UIFormCheckBoxInput addMoreLink = new UIFormCheckBoxInput(SHOW_MORE_LINK, SHOW_MORE_LINK, null);
    addMoreLink.setChecked(true);
    String showMoreLink = portletPreferences.getValue(UIPCLVPortlet.SHOW_MORE_LINK, null);
    addMoreLink.setChecked(Boolean.parseBoolean(showMoreLink));
    
    UIFormCheckBoxInput showRssLink = new UIFormCheckBoxInput(SHOW_RSS_LINK, SHOW_RSS_LINK, null);
    showRssLink.setChecked(true);
    String rssLink = portletPreferences.getValue(UIPCLVPortlet.SHOW_RSS_LINK, null);
    showRssLink.setChecked(Boolean.parseBoolean(rssLink));
    
    UIFormCheckBoxInput showLink = new UIFormCheckBoxInput(SHOW_LINK, SHOW_LINK, null);
    showLink.setChecked(true);
    String link = portletPreferences.getValue(UIPCLVPortlet.SHOW_LINK, null);
    showLink.setChecked(Boolean.parseBoolean(link));
    
    addChild(orderBySelectBox);
    addChild(orderTypeRadioBoxInput);
    addChild(headerInput);
    addChild(autoDetect);
    addChild(formViewTemplateSelector);
    addChild(paginatorTemplateSelector);
    addChild(itemsPerPageStringInput);    
    addChild(viewerButtonRefreshCheckbox);
    addChild(thumbnailsViewCheckbox);
    addChild(titleViewerCheckbox);
    addChild(dateCreatedViewerCheckbox);
    addChild(summaryViewerCheckbox);
    addChild(viewerHeader);
    addChild(showLink);
    addChild(targetPathFormInputSet);
    addChild(addMoreLink);
    addChild(showRssLink);
    
    setActions(new String[] { "Save", "Cancel" });
  }
 
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  public void doSelect(String selectField, Object value) throws Exception {
    UIFormStringInput formStringInput = findComponentById(selectField);
    formStringInput.setValue(value.toString()) ;
    Utils.closePopupWindow(this, UICategoryNavigationConstant.TARGET_PATH_SELECTOR_POPUP_WINDOW);
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
    List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository, portletName, category, Utils.getSessionProvider(this));
    for (Node templateNode : templateNodeList) {
      String templateName = templateNode.getName();
      String templatePath = templateNode.getPath();
      templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateOptionList;
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
  public static class CancelActionListener extends EventListener<UIPCLVConfig> {
    public void execute(Event<UIPCLVConfig> event) throws Exception {
      UIPCLVConfig viewerManagementForm = event.getSource();
      if (Utils.isQuickEditmode(viewerManagementForm, UIPCLVPortlet.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW)) {
        Utils.closePopupWindow(viewerManagementForm, UIPCLVPortlet.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW);
      }
      ((PortletRequestContext)event.getRequestContext()).setApplicationMode(PortletMode.VIEW);
    }
  }
  
  /**
   * The listener interface for receiving selectTargetPageAction events.
   * The class that is interested in processing a selectTargetPageAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectTargetPageActionListener<code> method. When
   * the selectTargetPageAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectTargetPageActionEvent
   */
  public static class SelectTargetPageActionListener extends EventListener<UIPCLVConfig> {
    public void execute(Event<UIPCLVConfig> event) throws Exception {
      UIPCLVConfig viewerManagementForm = event.getSource();
      UIPageSelector pageSelector = viewerManagementForm.createUIComponent(UIPageSelector.class, null, null);
      pageSelector.setSourceComponent(viewerManagementForm, new String[] {TARGET_PAGE_INPUT});
      Utils.createPopupWindow(viewerManagementForm, pageSelector, UICategoryNavigationConstant.TARGET_PATH_SELECTOR_POPUP_WINDOW, 800, 600);
      viewerManagementForm.setPopupId(UICategoryNavigationConstant.TARGET_PATH_SELECTOR_POPUP_WINDOW);
    }
  }

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
   * Gets the header.
   * 
   * @return the header
   */
  private String getHeader(){
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
    String requestURI = requestWrapper.getRequestURI();
    String[] param = requestURI.split("/");
    String header = param[param.length - 1];
    return header;
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
  public static class SaveActionListener extends EventListener<UIPCLVConfig> {
    public void execute(Event<UIPCLVConfig> event) throws Exception {
      UIPCLVConfig uiParameterizedManagementForm = event.getSource();
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      RepositoryService repositoryService = uiParameterizedManagementForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      
      String formViewTemplatePath = uiParameterizedManagementForm.getUIFormSelectBox(UIPCLVConfig.FORM_VIEW_TEMPLATES_SELECTOR).getValue();
      String paginatorTemplatePath = uiParameterizedManagementForm.getUIFormSelectBox(UIPCLVConfig.PAGINATOR_TEMPLATES_SELECTOR).getValue();
      String itemsPerPage = uiParameterizedManagementForm.getUIStringInput(UIPCLVConfig.ITEMS_PER_PAGE_INPUT).getValue();      
      String showRefreshButton = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.VIEWER_BUTTON_REFRESH).isChecked() ? "true" : "false";
      String viewThumbnails = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.VIEWER_THUMBNAILS_IMAGE).isChecked() ? "true" : "false";
      String viewTitle = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.VIEWER_TITLE).isChecked() ? "true" : "false";
      String viewSummary = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.VIEWER_SUMMARY).isChecked() ? "true" : "false";
      String viewDateCreated = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.VIEWER_DATE_CREATED).isChecked() ? "true" : "false";
      String viewerHeader = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.VIEWER_HEADER).isChecked() ? "true" : "false";
      String viewerLink = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.SHOW_LINK).isChecked() ? "true" : "false";
      UIFormRadioBoxInput orderTypeBoxInput = (UIFormRadioBoxInput) uiParameterizedManagementForm.getChildById(UIPCLVConfig.ORDER_TYPES);
      String orderType = orderTypeBoxInput.getValue();
      String orderBy = uiParameterizedManagementForm.getUIFormSelectBox(ORDER_BY).getValue();
      String showMoreLink = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.SHOW_MORE_LINK).isChecked() ? "true" : "false";
      String showRssLink = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.SHOW_RSS_LINK).isChecked() ? "true" : "false";
      String autoDetect = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIPCLVConfig.AUTO_DETECT).isChecked() ? "true" : "false";
      String header = "";
      if(!Boolean.parseBoolean(autoDetect)) {
        header = uiParameterizedManagementForm.getUIStringInput(UIPCLVConfig.HEADER).getValue();
      } 
      header = uiParameterizedManagementForm.getHeader();
      String targetPage = uiParameterizedManagementForm.getUIStringInput(UIPCLVConfig.TARGET_PAGE_INPUT).getValue();

      portletPreferences.setValue(UIPCLVPortlet.PREFERENCE_REPOSITORY, repository);
      portletPreferences.setValue(UIPCLVPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UIPCLVPortlet.FORM_VIEW_TEMPLATE_PATH, formViewTemplatePath);
      portletPreferences.setValue(UIPCLVPortlet.PAGINATOR_TEMPlATE_PATH, paginatorTemplatePath);
      portletPreferences.setValue(UIPCLVPortlet.ITEMS_PER_PAGE, itemsPerPage);      
      portletPreferences.setValue(UIPCLVPortlet.SHOW_REFRESH_BUTTON, showRefreshButton);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_THUMBNAILS_VIEW, viewThumbnails);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_TITLE, viewTitle);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_DATE_CREATED, viewDateCreated);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_SUMMARY, viewSummary);
      portletPreferences.setValue(UIPCLVPortlet.HEADER, header);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_HEADER, viewerHeader);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_LINK, viewerLink);
      portletPreferences.setValue(UIPCLVPortlet.ORDER_TYPE, orderType);
      portletPreferences.setValue(UIPCLVPortlet.ORDER_BY, orderBy);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_MORE_LINK, showMoreLink);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_RSS_LINK, showRssLink);
      portletPreferences.setValue(UIPCLVPortlet.SHOW_AUTO_DETECT, autoDetect);
      portletPreferences.setValue(UIPCLVPortlet.TARGET_PAGE, targetPage);
      portletPreferences.store();
      
      if (Utils.isQuickEditmode(uiParameterizedManagementForm, UIPCLVPortlet.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW)) {
        Utils.closePopupWindow(uiParameterizedManagementForm, UIPCLVPortlet.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW);
      } else {
        Utils.createPopupMessage(uiParameterizedManagementForm, "UIPCLVConfig.msg.saving-success", null, ApplicationMessage.INFO);
      }
    }
  }
}
