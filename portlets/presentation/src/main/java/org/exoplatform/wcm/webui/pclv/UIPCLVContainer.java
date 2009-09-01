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

import java.net.URLDecoder;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS 
 * Author : Tran Nguyen Ngoc
 *          ngoc.tran@exoplatform.com
 * Jun 23, 2009
 */
@ComponentConfig(
	template = "app:/groovy/ParameterizedContentListViewer/UIPCLVContainer.gtmpl", 
	events = @EventConfig(listeners = UIPCLVContainer.QuickEditActionListener.class)
)
public class UIPCLVContainer extends UIContainer {

  /**
   * Gets the header.
   * 
   * @return the header
   */
  private String getHeader(){
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
    String requestURI = requestWrapper.getRequestURI();
    String selectedPage = Util.getUIPortal().getSelectedNode().getUri();
    if (requestURI.endsWith(selectedPage)) return null;
    String[] param = requestURI.split("/");
    String header = param[param.length - 1];
    header = header.replaceAll("%20", " ");
    return header;
  }
  
	public void init() throws Exception {
		PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
		PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
		UIPortal uiPortal = Util.getUIPortal();
		String portalURI = portalRequestContext.getPortalURI();
		String requestURI = requestWrapper.getRequestURI();
		String pageNodeSelected = uiPortal.getSelectedNode().getUri();
		String siteName = uiPortal.getOwner();
		
		String parameters = null;
		try {
			parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI.concat(pageNodeSelected + "/")), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		PortletRequest portletRequest = portletRequestContext.getRequest();
		PortletPreferences portletPreferences = portletRequest.getPreferences();
		String preferenceRepository = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_REPOSITORY, "");
		String preferenceTreeName = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_TREE_NAME, "");
		String treeName = null;
		
		if(parameters == null || parameters.trim().length() < 1) treeName = preferenceTreeName;
		else treeName = parameters.substring(0, parameters.indexOf("/"));
		
		TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
		Node treeNode = taxonomyService.getTaxonomyTree(preferenceRepository, treeName);
		
		String categoryPath = parameters.substring(parameters.indexOf("/") + 1);
		if (treeName.equals(categoryPath))
			categoryPath = "";
		
		Node categoryNode = treeNode.getNode(categoryPath);
		NodeIterator categoriesChildNode = this.getListSymlinkNode(	portletPreferences, categoryNode.getPath());
		int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIPCLVPortlet.ITEMS_PER_PAGE, null));
		PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(categoriesChildNode, itemsPerPage);
		getChildren().clear();

		UIPCLVForm parameterizedContentListViewer = addChild(	UIPCLVForm.class, null, null);
		String templatePath = getFormViewTemplatePath();
		ResourceResolver resourceResolver = getTemplateResourceResolver();
		parameterizedContentListViewer.init(templatePath, resourceResolver, paginatedNodeIterator);
		parameterizedContentListViewer.setContentColumn(portletPreferences.getValue(UIPCLVPortlet.HEADER, null));
		parameterizedContentListViewer.setShowLink(Boolean.parseBoolean(portletPreferences.getValue(UIPCLVPortlet.SHOW_LINK, null)));
		
		String autoDetect = portletPreferences.getValue(UIPCLVPortlet.SHOW_AUTO_DETECT, null);
		String currentHeader = getHeader();
		if ("true".equals(autoDetect) && currentHeader != null)
		  parameterizedContentListViewer.setHeader(currentHeader);
		else 
		  parameterizedContentListViewer.setHeader(portletPreferences.getValue(UIPCLVPortlet.HEADER, null));
		
		parameterizedContentListViewer.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIPCLVPortlet.SHOW_HEADER, null)));
		parameterizedContentListViewer.setShowReadmore(Boolean.parseBoolean(portletPreferences.getValue(UIPCLVPortlet.SHOW_READMORE, null)));
		parameterizedContentListViewer.setAutoDetection(autoDetect);
		parameterizedContentListViewer.setShowMoreLink(portletPreferences.getValue(	UIPCLVPortlet.SHOW_MORE_LINK, null));
		parameterizedContentListViewer.setShowRSSLink(portletPreferences.getValue(UIPCLVPortlet.SHOW_RSS_LINK, null));
		String repository = ((ManageableRepository)categoryNode.getSession().getRepository()).getConfiguration().getName();
		String workspace = categoryNode.getSession().getWorkspace().getName();
		
		String server =  Util.getPortalRequestContext().getRequest().getRequestURL().toString();
		server = server.substring(0, server.indexOf('/', 8));
		
		parameterizedContentListViewer.setRssLink("/rest/rss/generate?repository=" + repository + "&workspace=" + workspace + "&server=" + server + "&siteName=" + siteName + "&categoryPath=" + ("".equals(categoryPath) ? preferenceTreeName : preferenceTreeName + "/" + categoryPath));
	}

	public PortletPreferences getPortletPreference() {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		return portletRequestContext.getRequest().getPreferences();
	}

	public String getFormViewTemplatePath() {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		PortletPreferences references = portletRequestContext.getRequest().getPreferences();
		return references.getValue(UIPCLVPortlet.FORM_VIEW_TEMPLATE_PATH, null);
	}

	public ResourceResolver getTemplateResourceResolver() throws Exception {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		PortletPreferences references = portletRequestContext.getRequest().getPreferences();
		String repository = references.getValue(UIPCLVPortlet.PREFERENCE_REPOSITORY, null);
		DMSConfiguration dmsConfiguration = Utils.getService(this, DMSConfiguration.class);
		String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();

		return new JCRResourceResolver(repository, workspace, "exo:templateFile");
	}

	public String getPortletId() {
		PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		return pContext.getWindowId();
	}

	public void processRender(WebuiRequestContext context) throws Exception {
		init();
		super.processRender(context);
	}

	public static class QuickEditActionListener extends EventListener<UIPCLVContainer> {
		public void execute(Event<UIPCLVContainer> event) throws Exception {
			UIPCLVContainer uiContentViewerContainer = event.getSource();
			UIPCLVConfig parameterizedForm = uiContentViewerContainer.createUIComponent(UIPCLVConfig.class, null, null);
			Utils.createPopupWindow(uiContentViewerContainer,
															parameterizedForm,
															UIPCLVPortlet.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW,
															650,
															800);
		}
	}

	public void onRefresh(Event<UIPCLVForm> event) throws Exception {
		UIPCLVForm contentListPresentation = event.getSource();
		UIPCLVContainer uiParameterizedContentListontainer = contentListPresentation.getParent();
		uiParameterizedContentListontainer.getChildren().clear();
		uiParameterizedContentListontainer.init();
	}

	private NodeIterator getListSymlinkNode(PortletPreferences portletPreferences, String categoryPath)	throws RepositoryException,
																																																			RepositoryConfigurationException {
		String repository = portletPreferences.getValue(UIPCLVPortlet.REPOSITORY, "");
		String worksapce = portletPreferences.getValue(UIPCLVPortlet.WORKSPACE, "");
		String orderType = portletPreferences.getValue(UIPCLVPortlet.ORDER_TYPE, "");
		String orderBy = portletPreferences.getValue(UIPCLVPortlet.ORDER_BY, "");
		if ("".equals(orderType)) orderType = "DESC";
		if ("".equals(orderBy)) orderBy = "exo:dateCreated";
		String orderQuery = " ORDER BY ";
		orderQuery += orderBy + " " + orderType;
		RepositoryService repositoryService = Utils.getService(this, RepositoryService.class);
		ManageableRepository manageableRepository = repositoryService.getRepository(repository);
		Session session = Utils.getSessionProvider(this).getSession(worksapce, manageableRepository);
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		StringBuffer sqlQuery = new StringBuffer("select * from exo:taxonomyLink where jcr:path LIKE '").append(categoryPath)
																																																		.append("/%'")
																																																		.append(" AND NOT jcr:path LIKE '")
																																																		.append(categoryPath)
																																																		.append("/%/%'")
																																																		.append(" "
																																																				+ orderQuery);
		Query query = queryManager.createQuery(sqlQuery.toString(), Query.SQL);
		QueryResult queryResult = query.execute();
		NodeIterator iterator = queryResult.getNodes();
		return iterator;
	}
}
