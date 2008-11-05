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
import org.exoplatform.wcm.webui.search.UIAdvanceSearchPortlet;
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

  public static final String SEARCH_FORM_TEMPLATE_CATEGORY      = "search-form".intern();

  public static final String SEARCH_PAGINATOR_TEMPLATE_CATEGORY = "search-paginator".intern();

  public static final String SEARCH_RESULT_TEMPLATE_CATEGORY    = "search-result".intern();

  public static final String SEARCH_FORM_TEMPLATE_SELECTOR      = "searchFormSelector".intern();

  public static final String SEARCH_PAGINATOR_TEMPLATE_SELECTOR = "searchPaginatorSelector"
                                                                    .intern();

  public static final String SEARCH_RESULT_TEMPLATE_SELECTOR    = "searchResultSelector".intern();

  public static final String SEARCH_BOX_TEMPLATE_CATEGORY       = "search-box".intern();

  public static final String SEARCH_BOX_TEMPLATE_SELECTOR       = "searchBoxSelector".intern();

  public static final String SEARCH_MODE_SELECTOR               = "searchModeSelector".intern();

  public static final String SEARCH_BOX_MODE_OPTION             = "searchBoxMode";

  public static final String SEARCH_PAGE_MODE_OPTION            = "searchPageMode";

  public static final String SEARCH_MODES_OPTION                = "searchModes";

  public UISearchPageLayoutManager() throws Exception {
    List<SelectItemOption<String>> searchModeList = createSearchModeList();
    List<SelectItemOption<String>> searchFormTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_FORM_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchResultTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_RESULT_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchPaginatorTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_PAGINATOR_TEMPLATE_CATEGORY);
    List<SelectItemOption<String>> searchBoxTemplateList = createTemplateList(PORTLET_NAME,
        SEARCH_BOX_TEMPLATE_CATEGORY);

    UIFormSelectBox searchModeSelector = new UIFormSelectBox(SEARCH_MODE_SELECTOR,
        SEARCH_MODE_SELECTOR, searchModeList);
    UIFormSelectBox searchFormTemplateSelector = new UIFormSelectBox(SEARCH_FORM_TEMPLATE_SELECTOR,
        SEARCH_FORM_TEMPLATE_SELECTOR, searchFormTemplateList).setRendered(false);
    UIFormSelectBox searchResultTemplateSelector = new UIFormSelectBox(
        SEARCH_RESULT_TEMPLATE_SELECTOR, SEARCH_RESULT_TEMPLATE_SELECTOR, searchResultTemplateList)
        .setRendered(false);
    UIFormSelectBox searchPaginatorTemplateSelector = new UIFormSelectBox(
        SEARCH_PAGINATOR_TEMPLATE_SELECTOR, SEARCH_PAGINATOR_TEMPLATE_SELECTOR,
        searchPaginatorTemplateList).setRendered(false);
    UIFormSelectBox searchBoxTemplateSelector = new UIFormSelectBox(SEARCH_BOX_TEMPLATE_SELECTOR,
        SEARCH_BOX_TEMPLATE_SELECTOR, searchBoxTemplateList).setRendered(false);

    searchModeSelector.setOnChange("SelectSearchMode");
    
    addUIFormInput(searchModeSelector);
    addUIFormInput(searchBoxTemplateSelector);
    addUIFormInput(searchFormTemplateSelector);
    addUIFormInput(searchResultTemplateSelector);
    addUIFormInput(searchPaginatorTemplateSelector);

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
      UISearchPageLayoutManager uiLayoutManager = event.getSource();
      RepositoryService repositoryService = uiLayoutManager
          .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      PortletRequestContext portletRequestContext = (PortletRequestContext) event
          .getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();

      String searchMode = uiLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_MODE_SELECTOR).getValue();
      String searchBoxTemplatePath = uiLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_BOX_TEMPLATE_SELECTOR).getValue();
      String searchResultTemplatePath = uiLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_RESULT_TEMPLATE_SELECTOR).getValue();
      String searchFormTemplatePath = uiLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_FORM_TEMPLATE_SELECTOR).getValue();
      String searchPaginatorTemplatePath = uiLayoutManager.getUIFormSelectBox(
          UISearchPageLayoutManager.SEARCH_PAGINATOR_TEMPLATE_SELECTOR).getValue();

      portletPreferences.setValue(UIAdvanceSearchPortlet.REPOSITORY, repository);
      portletPreferences.setValue(UIAdvanceSearchPortlet.WORKSPACE, workspace);
      portletPreferences.setValue(UIAdvanceSearchPortlet.SEARCH_MODE, searchMode);
      portletPreferences.setValue(UIAdvanceSearchPortlet.SEARCH_BOX_TEMPLATE_PATH,
          searchBoxTemplatePath);
      portletPreferences.setValue(UIAdvanceSearchPortlet.SEARCH_RESULT_TEMPLATE_PATH,
          searchResultTemplatePath);
      portletPreferences.setValue(UIAdvanceSearchPortlet.SEARCH_FORM_TEMPLATE_PATH,
          searchFormTemplatePath);
      portletPreferences.setValue(UIAdvanceSearchPortlet.SEARCH_PAGINATOR_TEMPLATE_PATH,
          searchPaginatorTemplatePath);      
      portletPreferences.store();
      if (Utils.isEditPortletInCreatePageWizard()) {
        UIApplication uiApp = uiLayoutManager.getAncestorOfType(UIApplication.class);
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

      if (UISearchPageLayoutManager.SEARCH_BOX_MODE_OPTION.equals(searchMode)) {
        uiSearchBoxTemplateSelector.setRendered(true);
        uiSearchFormTemplateSelector.setRendered(false);
        uiSearchPaginatorTemplateSelector.setRendered(false);
        uiSearchResultTemplateSelector.setRendered(false);
      } else if (UISearchPageLayoutManager.SEARCH_PAGE_MODE_OPTION.equals(searchMode)) {
        uiSearchBoxTemplateSelector.setRendered(false);
        uiSearchFormTemplateSelector.setRendered(true);
        uiSearchPaginatorTemplateSelector.setRendered(true);
        uiSearchResultTemplateSelector.setRendered(true);
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchPageLayoutManager.getParent());
    }
  }

}
