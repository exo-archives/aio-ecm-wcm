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
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.selector.page.UIPageSelector;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
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
 *          ngoc.tran@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class, 
                 template = "app:/groovy/ParameterizedContentListViewer/UIParameterizedManagementForm.gtmpl",
                 events = {
                     @EventConfig(listeners = UIParameterizedManagementForm.CancelActionListener.class),
                     @EventConfig(listeners = UIParameterizedManagementForm.SelectTargetPageActionListener.class),
                     @EventConfig(listeners = UIParameterizedManagementForm.SaveActionListener.class)
                     
                   }
               )
public class UIParameterizedManagementForm extends UIForm implements UISelectable {

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

  public final static String TARGET_PAGE_INPUT                 = "UIParameterizedTagetPageInput";

  public final static String TARGET_PATH_SELECTOR_POPUP_WINDOW = "UIParameterTargetPathPopupWindow";

  public final static String TARGET_PAGE_INPUT_SET_ACTION      = "UIParameterizedTagetPageInputSetAction";

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

  public static final String SHOW_MORE_LINK                    = "ShowMoreLink";

  public static final String SHOW_RSS_LINK                     = "ShowRSSLink";

  public static final String SHOW_LINK                         = "ShowLink";

  public static final String ORDER_BY                          = "OrderBy";

  public static final String ORDER_BY_TITLE                    = "OrderByTitle";

  public static final String ORDER_BY_DATE_CREATED             = "OrderByDateCreated";

  public static final String ORDER_BY_DATE_MODIFIED            = "OrderByDateModified";

  public static final String ORDER_BY_DATE_PUBLISHED           = "OrderByDatePublished";

  public static final String ORDER_TYPES                       = "OrderTypes";

  public static final String ORDER_DESC                        = "OrderDesc";

  public static final String ORDER_ASC                         = "OrderAsc";

  private String popupId;
  
  @SuppressWarnings("unchecked")
  public UIParameterizedManagementForm() throws Exception {

    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = context.getRequest().getPreferences();
    
    ResourceBundle bundle = context.getApplicationResourceBundle();
    String rootBundleKey = "UIParameterizedManagementForm.label.";
    
    List<SelectItemOption<String>> orderTypeOptions = new ArrayList<SelectItemOption<String>>();
    orderTypeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_DESC), "DESC"));
    orderTypeOptions.add(new SelectItemOption<String>(bundle.getString(rootBundleKey + ORDER_ASC), "ASC"));

    UIFormRadioBoxInput orderTypeRadioBoxInput = new UIFormRadioBoxInput(ORDER_TYPES, ORDER_TYPES, orderTypeOptions);
    String orderTypePref = portletPreferences.getValue(UIParameterizedContentListViewerConstant.ORDER_TYPE, null);
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
    UIFormSelectBox orderBySelectBox = new UIFormSelectBox(ORDER_BY, ORDER_BY, orderByOptions);
    String orderByPref = portletPreferences.getValue(UIParameterizedContentListViewerConstant.ORDER_BY, null);
    orderBySelectBox.setValue(orderByPref);
    
    UIFormCheckBoxInput autoDetect = new UIFormCheckBoxInput(AUTO_DETECT, AUTO_DETECT, null);
    autoDetect.setChecked(true);
    String autoDetected = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_AUTO_DETECT, null);
    autoDetect.setChecked(Boolean.parseBoolean(autoDetected));
    
    UIFormStringInput headerInput = new UIFormStringInput(HEADER, HEADER, null);
    
    if (!Boolean.parseBoolean(autoDetected)) {
      String headerValue = portletPreferences.getValue(UIParameterizedContentListViewerConstant.HEADER, null);
      headerInput.setValue(headerValue);
    } else {
      headerInput.setValue(this.getHeader());
    }
    
    List<SelectItemOption<String>> formViewerTemplateList = getTemplateList(PORTLET_NAME, FORM_VIEW_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> paginatorTemplateList = getTemplateList(PORTLET_NAME, PAGINATOR_TEMPLATE_CATEGORY);
    
    UIFormStringInput itemsPerPageStringInput = new UIFormStringInput(ITEMS_PER_PAGE_INPUT, ITEMS_PER_PAGE_INPUT, null);
    String itemsPerPageVal = portletPreferences.getValue(UIParameterizedContentListViewerConstant.ITEMS_PER_PAGE, null);
    itemsPerPageStringInput.setValue(itemsPerPageVal);
    itemsPerPageStringInput.setMaxLength(3);
    
    UIFormSelectBox formViewTemplateSelector = new UIFormSelectBox(FORM_VIEW_TEMPLATES_SELECTOR, FORM_VIEW_TEMPLATES_SELECTOR, formViewerTemplateList);
    String formViewTemplate = portletPreferences.getValue(UIParameterizedContentListViewerConstant.FORM_VIEW_TEMPLATE_PATH, null);
    formViewTemplateSelector.setValue(formViewTemplate);
    
    UIFormSelectBox paginatorTemplateSelector = new UIFormSelectBox(PAGINATOR_TEMPLATES_SELECTOR, PAGINATOR_TEMPLATES_SELECTOR, paginatorTemplateList);
    String paginatorTemplate = portletPreferences.getValue(UIParameterizedContentListViewerConstant.PAGINATOR_TEMPlATE_PATH, null);
    paginatorTemplateSelector.setValue(paginatorTemplate);
    
    UIFormCheckBoxInput viewerButtonRefreshCheckbox = new UIFormCheckBoxInput(VIEWER_BUTTON_REFRESH, VIEWER_BUTTON_REFRESH, null);
    viewerButtonRefreshCheckbox.setChecked(false);
    String refreshAble = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_REFRESH_BUTTON, null);
    viewerButtonRefreshCheckbox.setChecked(Boolean.parseBoolean(refreshAble));
    
    UIFormCheckBoxInput thumbnailsViewCheckbox = new UIFormCheckBoxInput(VIEWER_THUMBNAILS_IMAGE, VIEWER_THUMBNAILS_IMAGE, null);
    thumbnailsViewCheckbox.setChecked(true);
    String imageShowAble = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_THUMBNAILS_VIEW, null);
    thumbnailsViewCheckbox.setChecked(Boolean.parseBoolean(imageShowAble));
    
    UIFormCheckBoxInput titleViewerCheckbox = new UIFormCheckBoxInput(VIEWER_TITLE, VIEWER_TITLE, null);
    titleViewerCheckbox.setChecked(true);
    String titleShowAble = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_TITLE, null);
    titleViewerCheckbox.setChecked(Boolean.parseBoolean(titleShowAble));
    
    UIFormCheckBoxInput summaryViewerCheckbox = new UIFormCheckBoxInput(VIEWER_SUMMARY, VIEWER_SUMMARY, null);
    summaryViewerCheckbox.setChecked(true);
    String summaryShowAble = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_SUMMARY, null);
    summaryViewerCheckbox.setChecked(Boolean.parseBoolean(summaryShowAble));
    
    UIFormCheckBoxInput dateCreatedViewerCheckbox = new UIFormCheckBoxInput(VIEWER_DATE_CREATED, VIEWER_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(true);
    String dateShowAble = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_DATE_CREATED, null);
    dateCreatedViewerCheckbox.setChecked(Boolean.parseBoolean(dateShowAble));

    UIFormCheckBoxInput viewerHeader = new UIFormCheckBoxInput(VIEWER_HEADER, VIEWER_HEADER, null);
    viewerHeader.setChecked(true);
    String viewHeader = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_HEADER, null);
    viewerHeader.setChecked(Boolean.parseBoolean(viewHeader));
    
    String preferenceTargetPath = portletPreferences.getValue(UIParameterizedContentListViewerConstant.TARGET_PAGE, null);
    UIFormInputSetWithAction targetPathFormInputSet = new UIFormInputSetWithAction(TARGET_PAGE_INPUT_SET_ACTION);
    UIFormStringInput targetPathFormStringInput = new UIFormStringInput(TARGET_PAGE_INPUT, TARGET_PAGE_INPUT, preferenceTargetPath);
    targetPathFormStringInput.setValue(preferenceTargetPath);
    targetPathFormStringInput.setEditable(false);
    targetPathFormInputSet.setActionInfo(TARGET_PAGE_INPUT, new String[] {"SelectTargetPage"}) ;
    targetPathFormInputSet.addUIFormInput(targetPathFormStringInput);
    
    UIFormCheckBoxInput addMoreLink = new UIFormCheckBoxInput(SHOW_MORE_LINK, SHOW_MORE_LINK, null);
    addMoreLink.setChecked(true);
    String showMoreLink = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_MORE_LINK, null);
    addMoreLink.setChecked(Boolean.parseBoolean(showMoreLink));
    
    UIFormCheckBoxInput showRssLink = new UIFormCheckBoxInput(SHOW_RSS_LINK, SHOW_RSS_LINK, null);
    showRssLink.setChecked(true);
    String rssLink = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_RSS_LINK, null);
    showRssLink.setChecked(Boolean.parseBoolean(rssLink));
    
    UIFormCheckBoxInput showLink = new UIFormCheckBoxInput(SHOW_LINK, SHOW_LINK, null);
    showLink.setChecked(true);
    String link = portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_LINK, null);
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
 
  public void doSelect(String selectField, Object value) throws Exception {
    UIFormStringInput formStringInput = findComponentById(selectField);
    formStringInput.setValue(value.toString()) ;
    
    UIParameterizedContentListViewerPortlet categoryNavigationPortlet = getAncestorOfType(UIParameterizedContentListViewerPortlet.class);
    UIPopupContainer uiPopupContainer = categoryNavigationPortlet.getChild(UIPopupContainer.class);
    Utils.closePopupWindow(uiPopupContainer, UIParameterizedContentListViewerConstant.PARAMETERIZED_TARGET_PAGE_POPUP_WINDOW);
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
    try {
      List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository, portletName, category, provider);
      for (Node templateNode : templateNodeList) {
        String templateName = templateNode.getName();
        String templatePath = templateNode.getPath();
        templateOptionList.add(new SelectItemOption<String>(templateName, templatePath));
      }
    } finally {
      provider.close();
    }
    return templateOptionList;
  }
  
  public static class CancelActionListener extends EventListener<UIParameterizedManagementForm> {

    public void execute(Event<UIParameterizedManagementForm> event) throws Exception {
      UIParameterizedManagementForm viewerManagementForm = event.getSource();
      UIApplication uiApp = viewerManagementForm.getAncestorOfType(UIApplication.class);
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("Test =============================== Test", null, ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
      }
      UIPopupContainer uiPopupContainer = (UIPopupContainer) viewerManagementForm.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(uiPopupContainer, UIParameterizedContentListViewerConstant.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW);
    }
  }
  
  public static class SelectTargetPageActionListener extends EventListener<UIParameterizedManagementForm> {

    public void execute(Event<UIParameterizedManagementForm> event) throws Exception {
      UIParameterizedManagementForm viewerManagementForm = event.getSource();
      
      UIParameterizedContentListViewerPortlet parameterizedPortlet = viewerManagementForm.getAncestorOfType(UIParameterizedContentListViewerPortlet.class);
      
      UIPopupContainer popupContainer = parameterizedPortlet.getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UIParameterizedContentListViewerConstant.PARAMETERIZED_TARGET_PAGE_POPUP_WINDOW);
      if (popupWindow == null) {
        UIPageSelector pageSelector = popupContainer.createUIComponent(UIPageSelector.class, null, null);
        pageSelector.setSourceComponent(viewerManagementForm, new String[] {TARGET_PAGE_INPUT});
        Utils.createPopupWindow(popupContainer, pageSelector, event.getRequestContext(), UIParameterizedContentListViewerConstant.PARAMETERIZED_TARGET_PAGE_POPUP_WINDOW, 800, 600);
      } else {
        popupWindow.setShow(true);
      }
      viewerManagementForm.setPopupId(UIParameterizedContentListViewerConstant.PARAMETERIZED_TARGET_PAGE_POPUP_WINDOW);
    }
  }

  public String getPopupId() {
    return popupId;
  }

  public void setPopupId(String popupId) {
    this.popupId = popupId;
  }

  private String getHeader(){
    
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
    
    String requestURI = requestWrapper.getRequestURI();
    
    String[] param = requestURI.split("/");
    String header = param[param.length - 1];
    return header;
  }
  public static class SaveActionListener extends EventListener<UIParameterizedManagementForm> {

    public void execute(Event<UIParameterizedManagementForm> event) throws Exception {
      UIParameterizedManagementForm uiParameterizedManagementForm = event.getSource();

      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      UIApplication uiApp = uiParameterizedManagementForm.getAncestorOfType(UIApplication.class);
      
      RepositoryService repositoryService = uiParameterizedManagementForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String header = uiParameterizedManagementForm.getUIStringInput(UIParameterizedManagementForm.HEADER).getValue();
      String formViewTemplatePath = uiParameterizedManagementForm.getUIFormSelectBox(UIParameterizedManagementForm.FORM_VIEW_TEMPLATES_SELECTOR).getValue();
      String paginatorTemplatePath = uiParameterizedManagementForm.getUIFormSelectBox(UIParameterizedManagementForm.PAGINATOR_TEMPLATES_SELECTOR).getValue();
      String itemsPerPage = uiParameterizedManagementForm.getUIStringInput(UIParameterizedManagementForm.ITEMS_PER_PAGE_INPUT).getValue();      
      String showRefreshButton = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.VIEWER_BUTTON_REFRESH).isChecked() ? "true" : "false";
      String viewThumbnails = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.VIEWER_THUMBNAILS_IMAGE).isChecked() ? "true" : "false";
      String viewTitle = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.VIEWER_TITLE).isChecked() ? "true" : "false";
      String viewSummary = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.VIEWER_SUMMARY).isChecked() ? "true" : "false";
      String viewDateCreated = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.VIEWER_DATE_CREATED).isChecked() ? "true" : "false";
      String viewerHeader = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.VIEWER_HEADER).isChecked() ? "true" : "false";
      String viewerLink = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.SHOW_LINK).isChecked() ? "true" : "false";
      UIFormRadioBoxInput orderTypeBoxInput = (UIFormRadioBoxInput) uiParameterizedManagementForm.getChildById(UIParameterizedManagementForm.ORDER_TYPES);
      String orderType = orderTypeBoxInput.getValue();
      String orderBy = uiParameterizedManagementForm.getUIFormSelectBox(ORDER_BY).getValue();
      String showMoreLink = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.SHOW_MORE_LINK).isChecked() ? "true" : "false";
      String showRssLink = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.SHOW_RSS_LINK).isChecked() ? "true" : "false";
      String autoDetect = uiParameterizedManagementForm.getUIFormCheckBoxInput(UIParameterizedManagementForm.AUTO_DETECT).isChecked() ? "true" : "false";
      String targetPage = uiParameterizedManagementForm.getUIStringInput(UIParameterizedManagementForm.TARGET_PAGE_INPUT).getValue();
      
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.PREFERENCE_REPOSITORY, repository);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.WORKSPACE, workspace);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.FORM_VIEW_TEMPLATE_PATH, formViewTemplatePath);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.PAGINATOR_TEMPlATE_PATH, paginatorTemplatePath);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.ITEMS_PER_PAGE, itemsPerPage);      
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_REFRESH_BUTTON, showRefreshButton);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_THUMBNAILS_VIEW, viewThumbnails);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_TITLE, viewTitle);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_DATE_CREATED, viewDateCreated);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_SUMMARY, viewSummary);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.HEADER, header);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_HEADER, viewerHeader);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_LINK, viewerLink);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.ORDER_TYPE, orderType);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.ORDER_BY, orderBy);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_MORE_LINK, showMoreLink);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_RSS_LINK, showRssLink);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.SHOW_AUTO_DETECT, autoDetect);
      portletPreferences.setValue(UIParameterizedContentListViewerConstant.TARGET_PAGE, targetPage);
      
      portletPreferences.store();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.saving-success", null, ApplicationMessage.INFO));
      }
      portletRequestContext.setApplicationMode(PortletMode.VIEW);

      UIParameterizedContentListViewerPortlet uiparameterContentListViewerPortlet = uiParameterizedManagementForm.getAncestorOfType(UIParameterizedContentListViewerPortlet.class);
      UIParameterizedContentListViewerContainer uiparameterContentListViewerContainer = uiparameterContentListViewerPortlet.getChild(UIParameterizedContentListViewerContainer.class);
      
      uiparameterContentListViewerContainer.getChildren().clear();
      uiparameterContentListViewerContainer.init();
      
      UIPopupContainer uiPopupContainer = (UIPopupContainer) uiParameterizedManagementForm.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(uiPopupContainer, UIParameterizedContentListViewerConstant.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW);
    }
  }
}
