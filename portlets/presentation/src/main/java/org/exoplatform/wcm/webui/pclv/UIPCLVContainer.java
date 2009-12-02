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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
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
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.pclv.config.UIPCLVConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Nguyen Ngoc
 * ngoc.tran@exoplatform.com
 * Jun 23, 2009
 */
@ComponentConfig(
	template = "app:/groovy/ParameterizedContentListViewer/UIPCLVContainer.gtmpl", 
	events = @EventConfig(listeners = UIPCLVContainer.QuickEditActionListener.class)
)
public class UIPCLVContainer extends UIContainer {

  /** The list noode. */
  private List<Node> listNode;
  
  /** The boolean isError. */
  private boolean isError;
  
  /**
   * @return the isError
   */
  public boolean isError() {
    return isError;
  }

  /**
   * @param isError the isError to set
   */
  public void setError(boolean isError) {
    this.isError = isError;
  }

  /**
   * Gets the list noode.
   * 
   * @return the listNoode
   */
  public List<Node> getListNode() {
    return listNode;
  }

  /**
   * Sets the list noode.
   * 
   * @param listNoode the listNoode to set
   */
  public void setListNode(List<Node> listNode) {
    this.listNode = listNode;
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
    String selectedPage = Util.getUIPortal().getSelectedNode().getUri();
    if (requestURI.endsWith(selectedPage)) return null;
    String[] param = requestURI.split("/");
    String header = param[param.length - 1];
    header = header.replaceAll("%20", " ");
    return header;
  }
  
	/**
	 * Inits the.
	 * 
	 * @throws Exception the exception
	 */
	public void init() throws Exception {
		PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
		PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
		UIPortal uiPortal = Util.getUIPortal();
		String portalURI = portalRequestContext.getPortalURI();
		String requestURI = requestWrapper.getRequestURI();
		String pageNodeSelected = uiPortal.getSelectedNode().getUri();
		String siteName = uiPortal.getOwner();
		
		String categoryPath = null;
		try {
			categoryPath = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI.concat(pageNodeSelected + "/")), "UTF-8");
		} catch (Exception e) {
		  Utils.createPopupMessage(this, "UIPCLVConfig.msg.decode", null, ApplicationMessage.ERROR);
		}

		PortletRequest portletRequest = portletRequestContext.getRequest();
		PortletPreferences portletPreferences = portletRequest.getPreferences();
		String preferenceRepository = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_REPOSITORY, "");
		String preferenceTreeName = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_TREE_NAME, "");
		TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
		Node treeNode = null;
		try {
		  treeNode = taxonomyService.getTaxonomyTree(preferenceRepository, preferenceTreeName);
		} catch(Exception ex){}
		
		if (preferenceTreeName.equals(categoryPath))
			categoryPath = "";
		Node categoryNode = null;
		List<Node> nodes = null;
		if(treeNode != null) {
			try {
				categoryNode = treeNode.getNode(categoryPath);
				nodes = this.getListSymlinkNode(portletPreferences, categoryNode.getPath());
			} catch (Exception e) {
				nodes = this.getListSymlinkNode(portletPreferences, null);
			}
    } else {
      nodes = this.getListSymlinkNode(portletPreferences, null);
    }
		if(nodes == null) {
		  nodes = new ArrayList<Node>();
		}
		this.setListNode(nodes);
		int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIPCLVPortlet.ITEMS_PER_PAGE, null));
		PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(nodes, itemsPerPage);
		getChildren().clear();

		UIPCLVForm parameterizedContentListViewer = addChild(	UIPCLVForm.class, null, null);
		String templatePath = getFormViewTemplatePath();
		ResourceResolver resourceResolver = getTemplateResourceResolver();
		parameterizedContentListViewer.init(templatePath, resourceResolver, paginatedNodeIterator);
		parameterizedContentListViewer.setContentColumn(portletPreferences.getValue(UIPCLVPortlet.HEADER, null));
		parameterizedContentListViewer.setShowLink(Boolean.parseBoolean(portletPreferences.getValue(UIPCLVPortlet.SHOW_LINK, null)));
		
		String autoDetect = portletPreferences.getValue(UIPCLVPortlet.SHOW_AUTO_DETECT, null);
		String currentHeader = getHeader();
		if (treeNode != null && "true".equals(autoDetect) && currentHeader != null)
		  parameterizedContentListViewer.setHeader(currentHeader);
		else 
		  parameterizedContentListViewer.setHeader(portletPreferences.getValue(UIPCLVPortlet.HEADER, null));
		
		parameterizedContentListViewer.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIPCLVPortlet.SHOW_HEADER, null)));
		parameterizedContentListViewer.setShowReadmore(Boolean.parseBoolean(portletPreferences.getValue(UIPCLVPortlet.SHOW_READMORE, null)));
		parameterizedContentListViewer.setAutoDetection(autoDetect);
		parameterizedContentListViewer.setShowRSSLink(portletPreferences.getValue(UIPCLVPortlet.SHOW_RSS_LINK, null));
		String workspace = portletPreferences.getValue(UIPCLVPortlet.WORKSPACE, null);
		String server =  Util.getPortalRequestContext().getRequest().getRequestURL().toString();
		server = server.substring(0, server.indexOf('/', 8));
		
		parameterizedContentListViewer.setRssLink("/rest/rss/generate?repository=" + preferenceRepository + "&workspace=" + workspace + "&server=" + server + "&siteName=" + siteName + "&categoryPath=" + ("".equals(categoryPath) ? preferenceTreeName : preferenceTreeName + "/" + categoryPath));
	}

	/**
	 * Gets the portlet preference.
	 * 
	 * @return the portlet preference
	 */
	public PortletPreferences getPortletPreference() {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		return portletRequestContext.getRequest().getPreferences();
	}

	/**
	 * Gets the form view template path.
	 * 
	 * @return the form view template path
	 */
	public String getFormViewTemplatePath() {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		PortletPreferences references = portletRequestContext.getRequest().getPreferences();
		return references.getValue(UIPCLVPortlet.FORM_VIEW_TEMPLATE_PATH, null);
	}

	/**
	 * Gets the template resource resolver.
	 * 
	 * @return the template resource resolver
	 * 
	 * @throws Exception the exception
	 */
	public ResourceResolver getTemplateResourceResolver() throws Exception {
		PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
		PortletPreferences references = portletRequestContext.getRequest().getPreferences();
		String repository = references.getValue(UIPCLVPortlet.PREFERENCE_REPOSITORY, null);
		DMSConfiguration dmsConfiguration = Utils.getService(DMSConfiguration.class);
		String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();

		return new JCRResourceResolver(repository, workspace, "exo:templateFile");
	}

	/**
	 * Gets the portlet id.
	 * 
	 * @return the portlet id
	 */
	public String getPortletId() {
		PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		return pContext.getWindowId();
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
	 */
	public void processRender(WebuiRequestContext context) throws Exception {
		init();
		super.processRender(context);
	}

	/**
	 * The listener interface for receiving quickEditAction events.
	 * The class that is interested in processing a quickEditAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addQuickEditActionListener<code> method. When
	 * the quickEditAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see QuickEditActionEvent
	 */
	public static class QuickEditActionListener extends EventListener<UIPCLVContainer> {
		
		/* (non-Javadoc)
		 * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
		 */
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

	/**
	 * On refresh.
	 * 
	 * @param event the event
	 * 
	 * @throws Exception the exception
	 */
	public void onRefresh(Event<UIPCLVForm> event) throws Exception {
		UIPCLVForm contentListPresentation = event.getSource();
		UIPCLVContainer uiParameterizedContentListontainer = contentListPresentation.getParent();
		uiParameterizedContentListontainer.getChildren().clear();
		uiParameterizedContentListontainer.init();
	}

	/**
	 * Gets the list symlink node.
	 * 
	 * @param portletPreferences the portlet preferences
	 * @param categoryPath the category path
	 * 
	 * @return the list symlink node
	 * 
	 * @throws Exception the exception
	 */
	private List<Node> getListSymlinkNode(PortletPreferences portletPreferences, String categoryPath)	throws Exception {
		String repository = portletPreferences.getValue(UIPCLVPortlet.REPOSITORY, "");
		String workspace = portletPreferences.getValue(UIPCLVPortlet.WORKSPACE, "");
		String orderType = portletPreferences.getValue(UIPCLVPortlet.ORDER_TYPE, "");
		String orderBy = portletPreferences.getValue(UIPCLVPortlet.ORDER_BY, "");
		if ("".equals(orderType)) orderType = "DESC";
		if ("".equals(orderBy)) orderBy = "exo:dateCreated";
    WCMComposer wcmComposer = getApplicationComponent(WCMComposer.class);
    HashMap<String, String> filters = new HashMap<String, String>();
    filters.put(WCMComposer.FILTER_MODE, Utils.getCurrentMode());
    filters.put(WCMComposer.FILTER_ORDER_BY, orderBy);
    filters.put(WCMComposer.FILTER_ORDER_TYPE, orderType);
    filters.put(WCMComposer.FILTER_PRIMARY_TYPE, "exo:taxonomyLink");
    List<Node> nodes = wcmComposer.getContents(repository, workspace, categoryPath, filters, Utils.getSessionProvider());
		return nodes;
	}
}
