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
package org.exoplatform.wcm.webui.search;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.WCMPaginatedQueryResult;
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
import org.exoplatform.webui.form.UIFormStringInput;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, events = { @EventConfig(listeners = UISearchForm.SearchActionListener.class) })
public class UISearchForm extends UIForm {

	private String templatePath;

	private ResourceResolver resourceResolver;

	public static final String KEYWORD_INPUT = "keywordInput";

	public static final String DOCUMENT_CHECKING = "documentCheckBox";

	public static final String PAGE_CHECKING = "pageCheckBox";

	public static final String PORTALS_SELECTOR = "portalSelector";

	public static final String ALL_OPTION = "all";

	public static final String MESSAGE_NOT_CHECKED_TYPE_SEARCH = "UISearchForm.message.not-checked";
	
	public static final String MESSAGE_NOT_SUPPORT_KEYWORD = "UISearchForm.message.keyword-not-support";
	
	public static final String MESSAGE_NOT_EMPTY_KEYWORD = "UISearchForm.message.keyword-not-empty";

	@SuppressWarnings("unchecked")
	public UISearchForm() throws Exception {
		UIFormStringInput uiKeywordInput = new UIFormStringInput(KEYWORD_INPUT,
				KEYWORD_INPUT, null);
		UIFormSelectBox uiPortalSelectBox = new UIFormSelectBox(PORTALS_SELECTOR,
				PORTALS_SELECTOR, getPortalList());
		UIFormCheckBoxInput uiPageCheckBox = new UIFormCheckBoxInput(PAGE_CHECKING,
				PAGE_CHECKING, null);
		uiPageCheckBox.setChecked(true);
		UIFormCheckBoxInput uiDocumentCheckBox = new UIFormCheckBoxInput(
				DOCUMENT_CHECKING, DOCUMENT_CHECKING, null);
		uiDocumentCheckBox.setChecked(true);

		addUIFormInput(uiKeywordInput);
		addUIFormInput(uiPortalSelectBox);
		addUIFormInput(uiPageCheckBox);
		addUIFormInput(uiDocumentCheckBox);
	}

	public void init(String templatePath, ResourceResolver resourceResolver) {
		this.templatePath = templatePath;
		this.resourceResolver = resourceResolver;
	}

	public String getTemplate() {
		return templatePath;
	}

	public ResourceResolver getTemplateResourceResolver(
			WebuiRequestContext context, String template) {
		return resourceResolver;
	}

	@SuppressWarnings("unchecked")
	private List getPortalList() throws Exception {
		List<SelectItemOption<String>> portals = new ArrayList<SelectItemOption<String>>();
		DataStorage service = getApplicationComponent(DataStorage.class);
		Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null,
				PortalConfig.class);
		List<PortalConfig> list = service.find(query).getAll();
		portals.add(new SelectItemOption<String>(ALL_OPTION, ALL_OPTION));
		for (PortalConfig portalConfig : list) {
			portals.add(new SelectItemOption<String>(portalConfig.getName(),
					portalConfig.getName()));
		}
		return portals;
	}

	public static class SearchActionListener extends EventListener<UISearchForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiSearchForm = event.getSource();
			PortletRequestContext portletRequestContext = (PortletRequestContext) event
					.getRequestContext();
			PortletPreferences portletPreferences = portletRequestContext
					.getRequest().getPreferences();
			UIApplication uiApp = uiSearchForm.getAncestorOfType(UIApplication.class);
			SiteSearchService siteSearchService = uiSearchForm
					.getApplicationComponent(SiteSearchService.class);
			SessionProvider provider = SessionProviderFactory.createSessionProvider();
			UISearchPageLayout uiSearchPageContainer = uiSearchForm.getParent();
			UISearchResult uiSearchResult = uiSearchPageContainer
					.getChild(UISearchResult.class);
			UIFormStringInput uiKeywordInput = uiSearchForm
					.getUIStringInput(UISearchForm.KEYWORD_INPUT);
			UIFormSelectBox uiPortalSelectBox = uiSearchForm
					.getUIFormSelectBox(UISearchForm.PORTALS_SELECTOR);
			String keyword = uiKeywordInput.getValue();
			UIFormCheckBoxInput uiPageCheckbox = uiSearchForm
					.getUIFormCheckBoxInput(UISearchForm.PAGE_CHECKING);
			UIFormCheckBoxInput uiDocumentCheckbox = uiSearchForm
					.getUIFormCheckBoxInput(UISearchForm.DOCUMENT_CHECKING);
			String pageChecked = (uiPageCheckbox.isChecked()) ? "true" : "false";
			String documentChecked = (uiDocumentCheckbox.isChecked()) ? "true"
					: "false";
			if (keyword == null || keyword.trim().length() == 0) {
				uiApp.addMessage(new ApplicationMessage(MESSAGE_NOT_EMPTY_KEYWORD, null,
						ApplicationMessage.WARNING));
				return;
			}
			if (!Boolean.parseBoolean(pageChecked)
					&& !Boolean.parseBoolean(documentChecked)) {
				uiApp.addMessage(new ApplicationMessage(
						MESSAGE_NOT_CHECKED_TYPE_SEARCH, null,
						ApplicationMessage.WARNING));
				return;
			}
			String selectedPortal = (uiPortalSelectBox.getValue()
					.equals(UISearchForm.ALL_OPTION)) ? null : uiPortalSelectBox
					.getValue();
			QueryCriteria queryCriteria = new QueryCriteria();
			queryCriteria.setSiteName(selectedPortal);
			queryCriteria.setKeyword(keyword);
			queryCriteria.setSearchDocument(Boolean.parseBoolean(documentChecked));
			queryCriteria.setSearchWebpage(Boolean.parseBoolean(pageChecked));
			int itemsPerPage = Integer.parseInt(portletPreferences.getValue(
					UIWCMSearchPortlet.ITEMS_PER_PAGE, null));
			try {
				WCMPaginatedQueryResult paginatedQueryResult = siteSearchService
						.searchSiteContents(queryCriteria, provider, itemsPerPage, false);
				uiSearchResult.setPageList(paginatedQueryResult);
				long timeSearch = paginatedQueryResult.getQueryTimeInSecond();
				uiSearchResult.setSearchTime(timeSearch);
			} catch (Exception e) {
				uiApp.addMessage(new ApplicationMessage(
						MESSAGE_NOT_SUPPORT_KEYWORD, null,
						ApplicationMessage.WARNING));
				return;
			}
			portletRequestContext.addUIComponentToUpdateByAjax(uiSearchPageContainer);
		}
	}

}
