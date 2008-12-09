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

import java.io.Writer;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequestWrapper;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.WCMPaginatedQueryResult;
import org.exoplatform.wcm.webui.paginator.UICustomizeablePaginator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
@ComponentConfigs( {
		@ComponentConfig(lifecycle = Lifecycle.class),
		@ComponentConfig(type = UICustomizeablePaginator.class, events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class)) })
public class UISearchResult extends UIContainer {

	private String templatePath;

	private ResourceResolver resourceResolver;

	private UICustomizeablePaginator uiPaginator;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat(
			ISO8601.SIMPLE_DATETIME_FORMAT);

	private long searchTime;

	public final static String PARAMETER_REGX = "(portal=.+)&(keyword=.+)";

	public final static String RESULT_NOT_FOUND = "UISearchResult.msg.result-not-found";

	public void init(String templatePath, ResourceResolver resourceResolver)
			throws Exception {
		PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext
				.getCurrentInstance();
		PortletPreferences portletPreferences = portletRequestContext.getRequest()
				.getPreferences();
		String paginatorTemplatePath = portletPreferences.getValue(
				UIWCMSearchPortlet.SEARCH_PAGINATOR_TEMPLATE_PATH, null);
		this.templatePath = templatePath;
		this.resourceResolver = resourceResolver;
		uiPaginator = addChild(UICustomizeablePaginator.class, null, null);
		uiPaginator.setTemplatePath(paginatorTemplatePath);
		uiPaginator.setResourceResolver(resourceResolver);
	}

	@SuppressWarnings("static-access")
	public void processRender(WebuiRequestContext context) throws Exception {
		PortletRequestContext porletRequestContext = (PortletRequestContext) context;
		HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext
				.getRequest();
		String queryString = requestWrapper.getQueryString();
		if (queryString != null && queryString.matches(PARAMETER_REGX)) {
			queryString = URLDecoder.decode(queryString, "UTF-8");
			String[] params = queryString.split("&");
			String currentPortal = params[0].split("=")[1];
			String keyword = params[1].split("=")[1];
			if (keyword == null || keyword.trim().length() == 0) {
				renderErrorMessage(context, RESULT_NOT_FOUND);
				return;
			}
			SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
			QueryCriteria queryCriteria = new QueryCriteria();
			queryCriteria.setSiteName(currentPortal);
			queryCriteria.setKeyword(keyword);
			queryCriteria.setSearchWebpage(true);
			queryCriteria.setSearchDocument(true);
			PortletPreferences portletPreferences = ((PortletRequestContext) context
					.getCurrentInstance()).getRequest().getPreferences();
			SessionProvider provider = SessionProviderFactory.createSessionProvider();
			int itemsPerPage = Integer.parseInt(portletPreferences.getValue(
					UIWCMSearchPortlet.ITEMS_PER_PAGE, null));
			try {
				WCMPaginatedQueryResult paginatedQueryResult = siteSearchService
						.searchSiteContents(queryCriteria, provider, itemsPerPage);
				setPageList(paginatedQueryResult);
			} catch (Exception e) {
				UIApplication uiApp = getAncestorOfType(UIApplication.class);
				uiApp.addMessage(new ApplicationMessage(
						UISearchForm.MESSAGE_NOT_SUPPORT_KEYWORD, null,
						ApplicationMessage.WARNING));
			}
		}
		if (uiPaginator.getTotalItems() == 0) {
			renderErrorMessage(context, RESULT_NOT_FOUND);
			return;
		}
		super.processRender(context);
	}

	private void renderErrorMessage(WebuiRequestContext context, String keyBundle)
			throws Exception {
		Writer writer = context.getWriter();
		String message = context.getApplicationResourceBundle()
				.getString(keyBundle);
		writer
				.write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px; border: 1px solid gray; margin: 10px;\">");
		writer.write("<span>");
		writer.write(message);
		writer.write("</span>");
		writer.write("</div>");
		writer.close();
	}

	public void setPageList(PageList dataPageList) {
		uiPaginator.setPageList(dataPageList);
	}

	public String getTemplate() {
		return templatePath;
	}

	public ResourceResolver getTemplateResourceResolver(
			WebuiRequestContext context, String template) {
		return resourceResolver;
	}

	public List getCurrentPageData() throws Exception {
		return uiPaginator.getCurrentPageData();
	}

	public String getTitle(Node node) throws Exception {
		return node.hasProperty("exo:title") ? node.getProperty("exo:title")
				.getValue().getString() : node.getName();
	}

	public String getURL(Node node) throws Exception {
		String link = null;
		PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
		WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
		PortletRequestContext portletRequestContext = WebuiRequestContext
				.getCurrentInstance();
		String portalURI = portalRequestContext.getPortalURI();
		PortletPreferences portletPreferences = portletRequestContext.getRequest()
				.getPreferences();
		String repository = portletPreferences.getValue(
				UIWCMSearchPortlet.REPOSITORY, null);
		String workspace = portletPreferences.getValue(
				UIWCMSearchPortlet.WORKSPACE, null);
		String baseURI = portletRequestContext.getRequest().getScheme()
				+ "://"
				+ portletRequestContext.getRequest().getServerName()
				+ ":"
				+ String.format("%s", portletRequestContext.getRequest()
						.getServerPort());
		String parameterizedPageURI = wcmConfigurationService
				.getParameterizedPageURI();
		link = baseURI + portalURI
				+ parameterizedPageURI.substring(1, parameterizedPageURI.length())
				+ "/" + repository + "/" + workspace + node.getPath();
		return link;
	}

	public String getCreatedDate(Node node) throws Exception {
		if (node.hasProperty("exo:dateCreated")) {
			Calendar calendar = node.getProperty("exo:dateCreated").getValue()
					.getDate();
			return dateFormatter.format(calendar.getTime());
		}
		return null;
	}

	public boolean isShowPaginator() throws Exception {
		PortletPreferences portletPreferences = ((PortletRequestContext) WebuiRequestContext
				.getCurrentInstance()).getRequest().getPreferences();
		String itemsPerPage = portletPreferences.getValue(
				UIWCMSearchPortlet.ITEMS_PER_PAGE, null);
		int totalItems = uiPaginator.getTotalItems();
		if (totalItems > Integer.parseInt(itemsPerPage)) {
			return true;
		}
		return false;
	}

	public long getSearchTime() {
		return searchTime;
	}

	public void setSearchTime(long searchTime) {
		this.searchTime = searchTime;
	}
}
