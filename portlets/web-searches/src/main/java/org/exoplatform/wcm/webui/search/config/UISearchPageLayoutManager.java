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
package org.exoplatform.wcm.webui.search.config;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.search.UIWCMSearchPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
    @EventConfig(listeners = UISearchPageLayoutManager.SaveActionListener.class),
    @EventConfig(listeners = UISearchPageLayoutManager.CancelActionListener.class),
    @EventConfig(listeners = UISearchPageLayoutManager.SelectSearchModeActionListener.class) })
public class UISearchPageLayoutManager extends UIForm {

  public static final String PORTLET_NAME                       = "WCM Advance Search".intern();

  public static final String SEARCH_PAGE_LAYOUT_CATEGORY        = "search-page-layout".intern();

  public static final String SEARCH_PAGE_LAYOUT_SELECTOR        = "searchPageLayoutSelector"
                                                                    .intern();

  public static final String SEARCH_FORM_TEMPLATE_CATEGORY      = "search-form".intern();

  public static final String SEARCH_PAGINATOR_TEMPLATE_CATEGORY = "search-paginator";

  public static final String SEARCH_RESULT_TEMPLATE_CATEGORY    = "search-result";

  public static final String SEARCH_FORM_TEMPLATE_SELECTOR      = "searchFormSelector";

  public static final String SEARCH_PAGINATOR_TEMPLATE_SELECTOR = "searchPaginatorSelector";

  public static final String SEARCH_RESULT_TEMPLATE_SELECTOR    = "searchResultSelector";

  public static final String SEARCH_BOX_TEMPLATE_CATEGORY       = "search-box";

  public static final String SEARCH_BOX_TEMPLATE_SELECTOR       = "searchBoxSelector";

  public static final String SEARCH_MODE_SELECTOR               = "searchModeSelector";

  public static final String SEARCH_BOX_MODE_OPTION             = "searchBoxMode";

  public static final String SEARCH_PAGE_MODE_OPTION            = "searchPageMode";

  public static final String SEARCH_MODES_OPTION                = "searchModes";

  public final static String ITEMS_PER_PAGE_SELECTOR            = "itemsPerPageSelector";

  public final static String VIEWER_BUTTON_QUICK_EDIT           = "viewerButtonQuickEdit";

  @SuppressWarnings("unchecked")
  public UISearchPageLayoutManager() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    List<SelectItemOption<String>> searchModeList = createSearchModeList();
    List<SelectItemOption<String>> searchFormTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_FORM_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchResultTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_RESULT_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchPaginatorTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_PAGINATOR_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchBoxTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_BOX_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchPageLayoutTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_PAGE_LAYOUT_CATEGORY);
    List<SelectItemOption<String>> itemsPerPageList = new ArrayList<SelectItemOption<String>>();
    itemsPerPageList.add(new SelectItemOption<String>("5", "5"));
    itemsPerPageList.add(new SelectItemOption<String>("10", "10"));
    itemsPerPageList.add(new SelectItemOption<String>("20", "20"));

    UIFormSelectBox searchModeSelector = new UIFormSelectBox(SEARCH_MODE_SELECTOR,
        SEARCH_MODE_SELECTOR, searchModeList);
    UIFormSelectBox itemsPerPageSelector = new UIFormSelectBox(ITEMS_PER_PAGE_SELECTOR,
        ITEMS_PER_PAGE_SELECTOR, itemsPerPageList);
    UIFormSelectBox searchFormTemplateSelector = new UIFormSelectBox(SEARCH_FORM_TEMPLATE_SELECTOR,
        SEARCH_FORM_TEMPLATE_SELECTOR, searchFormTemplateList).setRendered(false);
    UIFormSelectBox searchResultTemplateSelector = new UIFormSelectBox(
        SEARCH_RESULT_TEMPLATE_SELECTOR, SEARCH_RESULT_TEMPLATE_SELECTOR, searchResultTemplateList)
        .setRendered(false);
    UIFormSelectBox searchPaginatorTemplateSelector = new UIFormSelectBox(
        SEARCH_PAGINATOR_TEMPLATE_SELECTOR, SEARCH_PAGINATOR_TEMPLATE_SELECTOR,
        searchPaginatorTemplateList).setRendered(false);
    UIFormSelectBox searchPageLayoutTemplateSelector = new UIFormSelectBox(
        SEARCH_PAGE_LAYOUT_SELECTOR, SEARCH_PAGE_LAYOUT_SELECTOR, searchPageLayoutTemplateList)
        .setRendered(false);
    UIFormSelectBox searchBoxTemplateSelector = new UIFormSelectBox(SEARCH_BOX_TEMPLATE_SELECTOR,
        SEARCH_BOX_TEMPLATE_SELECTOR, searchBoxTemplateList).setRendered(false);
    UIFormCheckBoxInput viewerButtonQuickEditCheckbox = new UIFormCheckBoxInput(
        VIEWER_BUTTON_QUICK_EDIT, VIEWER_BUTTON_QUICK_EDIT, null);
    String quickEditAble = portletPreferences.getValue(UIWCMSearchPortlet.SHOW_QUICK_EDIT_BUTTON,
        null);
    viewerButtonQuickEditCheckbox.setChecked(Boolean.parseBoolean(quickEditAble));

    searchModeSelector.setOnChange("SelectSearchMode");

    addUIFormInput(searchModeSelector);
    addUIFormInput(itemsPerPageSelector);
    addUIFormInput(searchBoxTemplateSelector);
    addUIFormInput(searchFormTemplateSelector);
    addUIFormInput(searchResultTemplateSelector);
    addUIFormInput(searchPaginatorTemplateSelector);
    addUIFormInput(searchPageLayoutTemplateSelector);
    addChild(viewerButtonQuickEditCheckbox);

    setActions(new String[] { "Save", "Cancel" });
  }

  private List<SelectItemOption<String>> createTemplateList(String portletName, String category)
      throws Exception {
    List<SelectItemOption<String>> templateList = new ArrayList<SelectItemOption<String>>();
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
      templateList.add(new SelectItemOption<String>(templateName, templatePath));
    }
    return templateList;
  }

  private List<SelectItemOption<String>> createSearchModeList() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    List<SelectItemOption<String>> searchModesList = new ArrayList<SelectItemOption<String>>();
    String modesLabel = portletRequestContext.getApplicationResourceBundle().getString(
        "UISearchPageLayoutManager.mode.selectOption.label");
    String boxModeLabel = portletRequestContext.getApplicationResourceBundle().getString(
        "UISearchPageLayoutManager.mode.box-search.label");
    String pageModeLabel = portletRequestContext.getApplicationResourceBundle().getString(
        "UISearchPageLayoutManager.mode.page-search.label");
    searchModesList.add(new SelectItemOption<String>(modesLabel, SEARCH_MODES_OPTION));
    searchModesList.add(new SelectItemOption<String>(boxModeLabel, SEARCH_BOX_MODE_OPTION));
    searchModesList.add(new SelectItemOption<String>(pageModeLabel, SEARCH_PAGE_MODE_OPTION));
    return searchModesList;
  }

  public static class SaveActionListener extends EventListener<UISearchPageLayoutManager> {
    public void execute(Event<UISearchPageLayoutManager> event) throws Exception {
      UISearchPageLayoutManager uiSearchLayoutManager = event.getSource();
      UIApplication uiApp = uiSearchLayoutManager.getAncestorOfType(UIApplication.class);
      RepositoryService repositoryService = uiSearchLayoutManager
          .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      PortletRequestContext portletRequestContext = (PortletRequestContext) event
          .getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();

      String searchMode = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_MODE_SELECTOR).getValue();
      if (UISearchPageLayoutManager.SEARCH_MODES_OPTION.equals(searchMode)) {
        uiApp.addMessage(new ApplicationMessage(
            "UISearchPageLayoutManager.message.search-mode-selecting", null,
            ApplicationMessage.WARNING));
        return;
      }
      String searchBoxTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_BOX_TEMPLATE_SELECTOR).getValue();
      String searchResultTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_RESULT_TEMPLATE_SELECTOR).getValue();
      String searchFormTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_FORM_TEMPLATE_SELECTOR).getValue();
      String searchPaginatorTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_PAGINATOR_TEMPLATE_SELECTOR).getValue();
      String searchPageLayoutTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_PAGE_LAYOUT_SELECTOR).getValue();
      String itemsPerPage = uiSearchLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.ITEMS_PER_PAGE_SELECTOR).getValue();
      String showQuickEditable = uiSearchLayoutManager.getUIFormCheckBoxInput(
          UISearchPageLayoutManager.VIEWER_BUTTON_QUICK_EDIT).isChecked() ? "true" : "false";

      portletPreferences.setValue(UIWCMSearchPortlet.REPOSITORY, repository);
      portletPreferences.setValue(UIWCMSearchPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UIWCMSearchPortlet.SEARCH_MODE, searchMode);
      portletPreferences.setValue(UIWCMSearchPortlet.SEARCH_BOX_TEMPLATE_PATH,
          searchBoxTemplatePath);
      portletPreferences.setValue(UIWCMSearchPortlet.SEARCH_RESULT_TEMPLATE_PATH,
          searchResultTemplatePath);
      portletPreferences.setValue(UIWCMSearchPortlet.SEARCH_FORM_TEMPLATE_PATH,
          searchFormTemplatePath);
      portletPreferences.setValue(UIWCMSearchPortlet.SEARCH_PAGINATOR_TEMPLATE_PATH,
          searchPaginatorTemplatePath);
      portletPreferences.setValue(UIWCMSearchPortlet.SEARCH_PAGE_LAYOUT_TEMPLATE_PATH,
          searchPageLayoutTemplatePath);
      portletPreferences.setValue(UIWCMSearchPortlet.ITEMS_PER_PAGE, itemsPerPage);
      portletPreferences.setValue(UIWCMSearchPortlet.SHOW_QUICK_EDIT_BUTTON, showQuickEditable);
      portletPreferences.store();
      if (Utils.isEditPortletInCreatePageWizard()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.saving-success", null,
            ApplicationMessage.INFO));
      } else {
        portletRequestContext.setApplicationMode(PortletMode.VIEW);
      }
    }
  }

  public static class CancelActionListener extends EventListener<UISearchPageLayoutManager> {
    public void execute(Event<UISearchPageLayoutManager> event) throws Exception {
    }
  }

  public static class SelectSearchModeActionListener extends
      EventListener<UISearchPageLayoutManager> {
    public void execute(Event<UISearchPageLayoutManager> event) throws Exception {
      UISearchPageLayoutManager uiSearchPageLayoutManager = event.getSource();
      String searchMode = uiSearchPageLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_MODE_SELECTOR).getValue();
      UIFormSelectBox uiSearchBoxTemplateSelector = uiSearchPageLayoutManager
          .getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_BOX_TEMPLATE_SELECTOR);
      UIFormSelectBox uiSearchFormTemplateSelector = uiSearchPageLayoutManager
          .getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_FORM_TEMPLATE_SELECTOR);
      UIFormSelectBox uiSearchResultTemplateSelector = uiSearchPageLayoutManager
          .getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_RESULT_TEMPLATE_SELECTOR);
      UIFormSelectBox uiSearchPaginatorTemplateSelector = uiSearchPageLayoutManager
          .getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_PAGINATOR_TEMPLATE_SELECTOR);
      UIFormSelectBox uiSearchPageLayoutTemplateSelector = uiSearchPageLayoutManager
          .getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_PAGE_LAYOUT_SELECTOR);
      if (UISearchPageLayoutManager.SEARCH_BOX_MODE_OPTION.equals(searchMode)) {
        uiSearchBoxTemplateSelector.setRendered(true);
        uiSearchFormTemplateSelector.setRendered(false);
        uiSearchPaginatorTemplateSelector.setRendered(false);
        uiSearchResultTemplateSelector.setRendered(false);
        uiSearchPageLayoutTemplateSelector.setRendered(false);
      } else if (UISearchPageLayoutManager.SEARCH_PAGE_MODE_OPTION.equals(searchMode)) {
        uiSearchBoxTemplateSelector.setRendered(false);
        uiSearchFormTemplateSelector.setRendered(true);
        uiSearchPaginatorTemplateSelector.setRendered(true);
        uiSearchResultTemplateSelector.setRendered(true);
        uiSearchPageLayoutTemplateSelector.setRendered(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchPageLayoutManager.getParent());
    }
  }

}
