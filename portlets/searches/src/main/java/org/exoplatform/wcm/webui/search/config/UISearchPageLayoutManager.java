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

import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
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
import org.exoplatform.webui.form.UIFormSelectBox;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
@ComponentConfig(
	lifecycle = UIFormLifecycle.class, 
	template = "app:/groovy/webui/wcm-search/config/UISearchPageLayoutManager.gtmpl", 
	events = {
		@EventConfig(listeners = UISearchPageLayoutManager.SaveActionListener.class),
		@EventConfig(listeners = UISearchPageLayoutManager.CancelActionListener.class),
		@EventConfig(listeners = UISearchPageLayoutManager.SelectSearchModeActionListener.class) 
	}
)
public class UISearchPageLayoutManager extends UIForm {

	/** The Constant PORTLET_NAME. */
	public static final String	PORTLET_NAME												= "WCM Advance Search".intern();

	/** The Constant SEARCH_PAGE_LAYOUT_CATEGORY. */
	public static final String	SEARCH_PAGE_LAYOUT_CATEGORY					= "search-page-layout".intern();

	/** The Constant SEARCH_PAGE_LAYOUT_SELECTOR. */
	public static final String	SEARCH_PAGE_LAYOUT_SELECTOR					= "searchPageLayoutSelector".intern();

	/** The Constant SEARCH_FORM_TEMPLATE_CATEGORY. */
	public static final String	SEARCH_FORM_TEMPLATE_CATEGORY				= "search-form".intern();

	/** The Constant SEARCH_PAGINATOR_TEMPLATE_CATEGORY. */
	public static final String	SEARCH_PAGINATOR_TEMPLATE_CATEGORY	= "search-paginator";

	/** The Constant SEARCH_RESULT_TEMPLATE_CATEGORY. */
	public static final String	SEARCH_RESULT_TEMPLATE_CATEGORY			= "search-result";

	/** The Constant SEARCH_FORM_TEMPLATE_SELECTOR. */
	public static final String	SEARCH_FORM_TEMPLATE_SELECTOR				= "searchFormSelector";

	/** The Constant SEARCH_PAGINATOR_TEMPLATE_SELECTOR. */
	public static final String	SEARCH_PAGINATOR_TEMPLATE_SELECTOR	= "searchPaginatorSelector";

	/** The Constant SEARCH_RESULT_TEMPLATE_SELECTOR. */
	public static final String	SEARCH_RESULT_TEMPLATE_SELECTOR			= "searchResultSelector";

	/** The Constant SEARCH_BOX_TEMPLATE_CATEGORY. */
	public static final String	SEARCH_BOX_TEMPLATE_CATEGORY				= "search-box";

	/** The Constant SEARCH_BOX_TEMPLATE_SELECTOR. */
	public static final String	SEARCH_BOX_TEMPLATE_SELECTOR				= "searchBoxSelector";

	/** The Constant SEARCH_MODE_SELECTOR. */
	public static final String	SEARCH_MODE_SELECTOR								= "searchModeSelector";

	/** The Constant SEARCH_BOX_MODE_OPTION. */
	public static final String	SEARCH_BOX_MODE_OPTION							= "searchBoxMode";

	/** The Constant SEARCH_PAGE_MODE_OPTION. */
	public static final String	SEARCH_PAGE_MODE_OPTION							= "searchPageMode";

	/** The Constant SEARCH_MODES_OPTION. */
	public static final String	SEARCH_MODES_OPTION									= "searchModes";

	/** The Constant ITEMS_PER_PAGE_SELECTOR. */
	public final static String	ITEMS_PER_PAGE_SELECTOR							= "itemsPerPageSelector";

	/**
	 * Instantiates a new uI search page layout manager.
	 * 
	 * @throws Exception the exception
	 */
	public UISearchPageLayoutManager() throws Exception {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();

		String itemsPerpage = portletPreferences.getValue(UIWCMSearchPortlet.ITEMS_PER_PAGE, null);
		String searchFormTemplate = portletPreferences.getValue(UIWCMSearchPortlet.SEARCH_FORM_TEMPLATE_PATH,
																														null);
		String searchResultTemplate = portletPreferences.getValue(UIWCMSearchPortlet.SEARCH_RESULT_TEMPLATE_PATH,
																															null);
		String searchPaginatorTemplate = portletPreferences.getValue(	UIWCMSearchPortlet.SEARCH_PAGINATOR_TEMPLATE_PATH,
																																	null);
		String searchPageLayoutTemplate = portletPreferences.getValue(UIWCMSearchPortlet.SEARCH_PAGE_LAYOUT_TEMPLATE_PATH,
																																	null);
		String searchBoxTemplate = portletPreferences.getValue(	UIWCMSearchPortlet.SEARCH_BOX_TEMPLATE_PATH,
																														null);
		String searchMode = portletPreferences.getValue(UIWCMSearchPortlet.SEARCH_MODE, null);

		List<SelectItemOption<String>> searchModeList = createSearchModeList();
		List<SelectItemOption<String>> searchFormTemplateList = createTemplateList(	PORTLET_NAME,
																																								SEARCH_FORM_TEMPLATE_CATEGORY);
		List<SelectItemOption<String>> searchResultTemplateList = createTemplateList(	PORTLET_NAME,
																																									SEARCH_RESULT_TEMPLATE_CATEGORY);
		List<SelectItemOption<String>> searchPaginatorTemplateList = createTemplateList(PORTLET_NAME,
																																										SEARCH_PAGINATOR_TEMPLATE_CATEGORY);
		List<SelectItemOption<String>> searchBoxTemplateList = createTemplateList(PORTLET_NAME,
																																							SEARCH_BOX_TEMPLATE_CATEGORY);
		List<SelectItemOption<String>> searchPageLayoutTemplateList = createTemplateList(	PORTLET_NAME,
																																											SEARCH_PAGE_LAYOUT_CATEGORY);
		List<SelectItemOption<String>> itemsPerPageList = new ArrayList<SelectItemOption<String>>();
		itemsPerPageList.add(new SelectItemOption<String>("5", "5"));
		itemsPerPageList.add(new SelectItemOption<String>("10", "10"));
		itemsPerPageList.add(new SelectItemOption<String>("20", "20"));

		UIFormSelectBox searchModeSelector = new UIFormSelectBox(	SEARCH_MODE_SELECTOR,
																															SEARCH_MODE_SELECTOR,
																															searchModeList);
		UIFormSelectBox itemsPerPageSelector = new UIFormSelectBox(	ITEMS_PER_PAGE_SELECTOR,
																																ITEMS_PER_PAGE_SELECTOR,
																																itemsPerPageList);
		UIFormSelectBox searchFormTemplateSelector = new UIFormSelectBox(	SEARCH_FORM_TEMPLATE_SELECTOR,
																																			SEARCH_FORM_TEMPLATE_SELECTOR,
																																			searchFormTemplateList).setRendered(false);
		UIFormSelectBox searchResultTemplateSelector = new UIFormSelectBox(	SEARCH_RESULT_TEMPLATE_SELECTOR,
																																				SEARCH_RESULT_TEMPLATE_SELECTOR,
																																				searchResultTemplateList).setRendered(false);
		UIFormSelectBox searchPaginatorTemplateSelector = new UIFormSelectBox(SEARCH_PAGINATOR_TEMPLATE_SELECTOR,
																																					SEARCH_PAGINATOR_TEMPLATE_SELECTOR,
																																					searchPaginatorTemplateList).setRendered(false);
		UIFormSelectBox searchPageLayoutTemplateSelector = new UIFormSelectBox(	SEARCH_PAGE_LAYOUT_SELECTOR,
																																						SEARCH_PAGE_LAYOUT_SELECTOR,
																																						searchPageLayoutTemplateList).setRendered(false);
		UIFormSelectBox searchBoxTemplateSelector = new UIFormSelectBox(SEARCH_BOX_TEMPLATE_SELECTOR,
																																		SEARCH_BOX_TEMPLATE_SELECTOR,
																																		searchBoxTemplateList).setRendered(false);

		searchModeSelector.setOnChange("SelectSearchMode");

		itemsPerPageSelector.setValue(itemsPerpage);
		searchBoxTemplateSelector.setValue(searchBoxTemplate);
		searchFormTemplateSelector.setValue(searchFormTemplate);
		searchResultTemplateSelector.setValue(searchResultTemplate);
		searchPaginatorTemplateSelector.setValue(searchPaginatorTemplate);
		searchPageLayoutTemplateSelector.setValue(searchPageLayoutTemplate);
		searchModeSelector.setValue(searchMode);

		addUIFormInput(searchModeSelector);
		addUIFormInput(itemsPerPageSelector);
		addUIFormInput(searchBoxTemplateSelector);
		addUIFormInput(searchFormTemplateSelector);
		addUIFormInput(searchResultTemplateSelector);
		addUIFormInput(searchPaginatorTemplateSelector);
		addUIFormInput(searchPageLayoutTemplateSelector);

		if (UISearchPageLayoutManager.SEARCH_BOX_MODE_OPTION.equals(searchMode)) {
			searchBoxTemplateSelector.setRendered(true);
			searchFormTemplateSelector.setRendered(false);
			searchPaginatorTemplateSelector.setRendered(false);
			searchResultTemplateSelector.setRendered(false);
			searchPageLayoutTemplateSelector.setRendered(false);
		} else if (UISearchPageLayoutManager.SEARCH_PAGE_MODE_OPTION.equals(searchMode)) {
			searchBoxTemplateSelector.setRendered(false);
			searchFormTemplateSelector.setRendered(true);
			searchPaginatorTemplateSelector.setRendered(true);
			searchResultTemplateSelector.setRendered(true);
			searchPageLayoutTemplateSelector.setRendered(true);
			searchModeSelector.setValue(searchMode);
		}
		setActions(new String[] { "Save", "Cancel" });
	}

	/**
	 * Creates the template list.
	 * 
	 * @param portletName the portlet name
	 * @param category the category
	 * 
	 * @return the list< select item option< string>>
	 * 
	 * @throws Exception the exception
	 */
	private List<SelectItemOption<String>> createTemplateList(String portletName, String category) throws Exception {
		List<SelectItemOption<String>> templateList = new ArrayList<SelectItemOption<String>>();
		ApplicationTemplateManagerService templateManagerService = getApplicationComponent(ApplicationTemplateManagerService.class);
		RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
		ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
		String repository = manageableRepository.getConfiguration().getName();
		List<Node> templateNodeList = templateManagerService.getTemplatesByCategory(repository,
																																								portletName,
																																								category,
																																								Utils.getSessionProvider(this));
		for (Node templateNode : templateNodeList) {
			String templateName = templateNode.getName();
			String templatePath = templateNode.getPath();
			templateList.add(new SelectItemOption<String>(templateName, templatePath));
		}
		return templateList;
	}

	/**
	 * Creates the search mode list.
	 * 
	 * @return the list< select item option< string>>
	 * 
	 * @throws Exception the exception
	 */
	private List<SelectItemOption<String>> createSearchModeList() throws Exception {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		List<SelectItemOption<String>> searchModesList = new ArrayList<SelectItemOption<String>>();
		String modesLabel = portletRequestContext	.getApplicationResourceBundle()
																							.getString("UISearchPageLayoutManager.mode.selectOption.label");
		String boxModeLabel = portletRequestContext	.getApplicationResourceBundle()
																								.getString("UISearchPageLayoutManager.mode.box-search.label");
		String pageModeLabel = portletRequestContext.getApplicationResourceBundle()
																								.getString("UISearchPageLayoutManager.mode.page-search.label");
		searchModesList.add(new SelectItemOption<String>(modesLabel, SEARCH_MODES_OPTION));
		searchModesList.add(new SelectItemOption<String>(boxModeLabel, SEARCH_BOX_MODE_OPTION));
		searchModesList.add(new SelectItemOption<String>(pageModeLabel, SEARCH_PAGE_MODE_OPTION));
		return searchModesList;
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
	public static class SaveActionListener extends EventListener<UISearchPageLayoutManager> {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
		 * .event.Event)
		 */
		public void execute(Event<UISearchPageLayoutManager> event) throws Exception {
			UISearchPageLayoutManager uiSearchLayoutManager = event.getSource();
			UIApplication uiApp = uiSearchLayoutManager.getAncestorOfType(UIApplication.class);
			RepositoryService repositoryService = uiSearchLayoutManager.getApplicationComponent(RepositoryService.class);
			ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
			String repository = manageableRepository.getConfiguration().getName();
			String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
			PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
			PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();

			String searchMode = uiSearchLayoutManager	.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_MODE_SELECTOR)
																								.getValue();
			if (UISearchPageLayoutManager.SEARCH_MODES_OPTION.equals(searchMode)) {
				uiApp.addMessage(new ApplicationMessage("UISearchPageLayoutManager.message.search-mode-selecting",
																								null,
																								ApplicationMessage.WARNING));
				return;
			}
			String searchBoxTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_BOX_TEMPLATE_SELECTOR)
																													.getValue();
			String searchResultTemplatePath = uiSearchLayoutManager	.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_RESULT_TEMPLATE_SELECTOR)
																															.getValue();
			String searchFormTemplatePath = uiSearchLayoutManager	.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_FORM_TEMPLATE_SELECTOR)
																														.getValue();
			String searchPaginatorTemplatePath = uiSearchLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_PAGINATOR_TEMPLATE_SELECTOR)
																																.getValue();
			String searchPageLayoutTemplatePath = uiSearchLayoutManager	.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_PAGE_LAYOUT_SELECTOR)
																																	.getValue();
			String itemsPerPage = uiSearchLayoutManager	.getUIFormSelectBox(UISearchPageLayoutManager.ITEMS_PER_PAGE_SELECTOR)
																									.getValue();

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
			portletPreferences.store();
			if (Utils.isQuickEditmode(uiSearchLayoutManager, "nothing")) {
				uiApp.addMessage(new ApplicationMessage("UIMessageBoard.msg.saving-success",
																								null,
																								ApplicationMessage.INFO));
			} else {
				portletRequestContext.setApplicationMode(PortletMode.VIEW);
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
	public static class CancelActionListener extends EventListener<UISearchPageLayoutManager> {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
		 * .event.Event)
		 */
		public void execute(Event<UISearchPageLayoutManager> event) throws Exception {
			PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
			context.setApplicationMode(PortletMode.VIEW);
		}
	}

	/**
	 * The listener interface for receiving selectSearchModeAction events. The
	 * class that is interested in processing a selectSearchModeAction event
	 * implements this interface, and the object created with that class is
	 * registered with a component using the component's
	 * <code>addSelectSearchModeActionListener<code> method. When
	 * the selectSearchModeAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see SelectSearchModeActionEvent
	 */
	public static class SelectSearchModeActionListener extends
																										EventListener<UISearchPageLayoutManager> {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
		 * .event.Event)
		 */
		public void execute(Event<UISearchPageLayoutManager> event) throws Exception {
			UISearchPageLayoutManager uiSearchPageLayoutManager = event.getSource();
			String searchMode = uiSearchPageLayoutManager	.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_MODE_SELECTOR)
																										.getValue();
			UIFormSelectBox uiSearchBoxTemplateSelector = uiSearchPageLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_BOX_TEMPLATE_SELECTOR);
			UIFormSelectBox uiSearchFormTemplateSelector = uiSearchPageLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_FORM_TEMPLATE_SELECTOR);
			UIFormSelectBox uiSearchResultTemplateSelector = uiSearchPageLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_RESULT_TEMPLATE_SELECTOR);
			UIFormSelectBox uiSearchPaginatorTemplateSelector = uiSearchPageLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_PAGINATOR_TEMPLATE_SELECTOR);
			UIFormSelectBox uiSearchPageLayoutTemplateSelector = uiSearchPageLayoutManager.getUIFormSelectBox(UISearchPageLayoutManager.SEARCH_PAGE_LAYOUT_SELECTOR);
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
