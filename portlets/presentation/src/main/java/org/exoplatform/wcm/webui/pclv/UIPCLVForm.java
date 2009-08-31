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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.images.RESTImagesRendererService;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.paginator.UICustomizeablePaginator;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * ngoc.tran@exoplatform.com Jun 23, 2009
 */
@ComponentConfigs( {
	@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		events = @EventConfig(listeners = UIPCLVForm.RefreshActionListener.class)
	),
	@ComponentConfig(
		type = UICustomizeablePaginator.class, 
		events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class)) 
	}
)
public class UIPCLVForm extends UIForm {

	/** The template path. */
	private String										templatePath;

	/** The resource resolver. */
	private ResourceResolver					resourceResolver;

	/** The ui paginator. */
	private UICustomizeablePaginator	uiPaginator;

	/** The content column. */
	private String										contentColumn;

	/** The show link. */
	private boolean										showLink;

	/** The show header. */
	private boolean										showHeader;

	/** The show readmore. */
	private boolean										showReadmore;

	/** The header. */
	private String										header;

	/** The date formatter. */
	private DateFormat								dateFormatter	= null;

	/** Auto detection. */
	private String										autoDetection;

	/** Show more link. */
	private String										showMoreLink;

	/** Show RSS link. */
	private String										showRSSLink;
	
	private String                     rssLink;

	public String getRssLink() {
    return rssLink;
  }

  public void setRssLink(String rssLink) {
    this.rssLink = rssLink;
  }

  public UIPCLVForm() {
	}

	public void init(String templatePath, ResourceResolver resourceResolver, PageList dataPageList) throws Exception {

		PortletPreferences portletPreferences = getPortletPreferences();
		String paginatorTemplatePath = portletPreferences.getValue(	UIPCLVPortlet.PAGINATOR_TEMPlATE_PATH,
																																null);
		this.templatePath = templatePath;
		this.resourceResolver = resourceResolver;
		uiPaginator = addChild(UICustomizeablePaginator.class, null, null);
		uiPaginator.setTemplatePath(paginatorTemplatePath);
		uiPaginator.setResourceResolver(resourceResolver);
		uiPaginator.setPageList(dataPageList);
		Locale locale = Util.getPortalRequestContext().getLocale();
		dateFormatter = SimpleDateFormat.getDateTimeInstance(	SimpleDateFormat.MEDIUM,
																													SimpleDateFormat.MEDIUM,
																													locale);
	}

	/**
	 * Show refresh button.
	 * 
	 * @return true, if successful
	 */
	public boolean showRefreshButton() {
		PortletPreferences portletPreferences = getPortletPreferences();
		String isShow = portletPreferences.getValue(UIPCLVPortlet.SHOW_REFRESH_BUTTON, null);
		return (isShow != null) ? Boolean.parseBoolean(isShow) : false;
	}

	public boolean showRSSLink() {
		PortletPreferences portletPreferences = getPortletPreferences();
		String isShow = portletPreferences.getValue(UIPCLVPortlet.SHOW_RSS_LINK, null);
		return (isShow != null) ? Boolean.parseBoolean(isShow) : false;
	}

	/**
	 * Checks if is show field.
	 * 
	 * @param field the field
	 * @return true, if is show field
	 */
	public boolean isShowField(String field) {
		PortletPreferences portletPreferences = getPortletPreferences();
		String showAble = portletPreferences.getValue(field, null);
		return (showAble != null) ? Boolean.parseBoolean(showAble) : false;
	}

	/**
	 * Show paginator.
	 * 
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean showPaginator() throws Exception {
		PortletPreferences portletPreferences = getPortletPreferences();
		String itemsPerPage = portletPreferences.getValue(UIPCLVPortlet.ITEMS_PER_PAGE, null);
		int totalItems = uiPaginator.getTotalItems();
		if (totalItems > Integer.parseInt(itemsPerPage)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the datetime fommatter.
	 * 
	 * @return the datetime fommatter
	 */
	public DateFormat getDatetimeFommatter() {
		return dateFormatter;
	}

	/**
	 * Sets the date time format.
	 * 
	 * @param format the new date time format
	 */
	public void setDateTimeFormat(String format) {
		((SimpleDateFormat) dateFormatter).applyPattern(format);
	}

	/**
	 * Gets the uI page iterator.
	 * 
	 * @return the uI page iterator
	 */
	public UIPageIterator getUIPageIterator() {
		return uiPaginator;
	}

	public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
		return resourceResolver;
	}

	/**
	 * Gets the current page data.
	 * 
	 * @return the current page data
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public List getCurrentPageData() throws Exception {
		return uiPaginator.getCurrentPageData();
	}

	/**
	 * Gets the title.
	 * 
	 * @param node the node
	 * @return the title
	 * @throws Exception the exception
	 */
	public String getTitle(Node node) throws Exception {
		return node.hasProperty("exo:title") ? node.getProperty("exo:title").getString() : node.getName();
	}

	/**
	 * Gets the summary.
	 * 
	 * @param node the node
	 * @return the summary
	 * @throws Exception the exception
	 */
	public String getSummary(Node node) throws Exception {
		if (node.hasProperty("exo:summary")) {
			return node.getProperty("exo:summary").getValue().getString();
		}
		return null;
	}

	/**
	 * Gets the created date.
	 * 
	 * @param node the node
	 * @return the created date
	 * @throws Exception the exception
	 */
	public String getCreatedDate(Node node) throws Exception {
		if (node.hasProperty("exo:dateCreated")) {
			Calendar calendar = node.getProperty("exo:dateCreated").getValue().getDate();
			return dateFormatter.format(calendar.getTime());
		}
		return null;
	}

	/**
	 * Gets the illustrative image.
	 * 
	 * @param node the node
	 * @return the illustrative image
	 * @throws Exception the exception
	 */
	public String getIllustrativeImage(Node node) throws Exception {
		WebSchemaConfigService schemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
		WebContentSchemaHandler contentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
		Node illustrativeImage = null;
		RESTImagesRendererService imagesRendererService = getApplicationComponent(RESTImagesRendererService.class);
		String uri = null;
		try {
			illustrativeImage = contentSchemaHandler.getIllustrationImage(node);
			uri = imagesRendererService.generateURI(illustrativeImage);
		} catch (Exception e) {
		}
		return uri;
	}

	public String generateLink(Node node) throws Exception {
		PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
		PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
		UIPortal uiPortal = Util.getUIPortal();

		String link = null;
		String portalURI = portalRequestContext.getPortalURI();
		String requestURI = requestWrapper.getRequestURI();
		String pageNodeSelected = uiPortal.getSelectedNode().getUri();
		String parameters = null;

		try {
			parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI.concat(pageNodeSelected+ "/")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}

		PortletRequest portletRequest = portletRequestContext.getRequest();
		PortletPreferences portletPreferences = portletRequest.getPreferences();
		String preferenceRepository = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_REPOSITORY, "");
		String preferenceTreeName = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_TREE_NAME, "");
		String preferenceTargetPage = portletPreferences.getValue(UIPCLVPortlet.PREFERENCE_TARGET_PAGE, "");
		TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);

		Node treeNode = taxonomyService.getTaxonomyTree(preferenceRepository, preferenceTreeName);
		String categoryPath = parameters.substring(parameters.indexOf("/") + 1);
		if (preferenceTreeName.equals(categoryPath)) {
			categoryPath = "";
		}

		Node categoryNode = treeNode.getNode(categoryPath);

		Node newNode = categoryNode.getNode(node.getName());
		String path = newNode.getPath();

		String itemPath = path.substring(path.indexOf(preferenceTreeName));
		String backToCategory = "";
		if (categoryPath.equals("")) {

			backToCategory = pageNodeSelected;
		} else {

			backToCategory = itemPath.substring(0, itemPath.indexOf(newNode.getName()) - 1);
		}

		link = portalURI + preferenceTargetPage + "/" + itemPath + "?back" + "=" + "/" + backToCategory;

		return link;
	}

	private PortletPreferences getPortletPreferences() {
		PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		PortletPreferences portletPreferences = context.getRequest().getPreferences();
		return portletPreferences;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public ResourceResolver getResourceResolver() {
		return resourceResolver;
	}

	public void setResourceResolver(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

	public UICustomizeablePaginator getUiPaginator() {
		return uiPaginator;
	}

	public void setUiPaginator(UICustomizeablePaginator uiPaginator) {
		this.uiPaginator = uiPaginator;
	}

	public String getContentColumn() {
		return contentColumn;
	}

	public void setContentColumn(String contentColumn) {
		this.contentColumn = contentColumn;
	}

	public boolean isShowLink() {
		return showLink;
	}

	public void setShowLink(boolean showLink) {
		this.showLink = showLink;
	}

	public boolean isShowHeader() {
		return showHeader;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	public boolean isShowReadmore() {
		return showReadmore;
	}

	public void setShowReadmore(boolean showReadmore) {
		this.showReadmore = showReadmore;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public DateFormat getDateFormatter() {
		return dateFormatter;
	}

	public void setDateFormatter(DateFormat dateFormatter) {
		this.dateFormatter = dateFormatter;
	}

	public String getAutoDetection() {
		return autoDetection;
	}

	public void setAutoDetection(String autoDetection) {
		this.autoDetection = autoDetection;
	}

	public String getShowMoreLink() {
		return showMoreLink;
	}

	public void setShowMoreLink(String showMoreLink) {
		this.showMoreLink = showMoreLink;
	}

	public String getShowRSSLink() {
		return showRSSLink;
	}

	public void setShowRSSLink(String showRSSLink) {
		this.showRSSLink = showRSSLink;
	}

	public String getTemplate() {
		return templatePath;
	}

	public Node getNodeView(Node node) throws Exception {
	  String realNodeUUID = node.getProperty("exo:uuid").getString();
	  Node realNode = node.getSession().getNodeByUUID(realNodeUUID);
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    HashMap<String, Object> context = new HashMap<String, Object>();
    context.put(WCMComposer.FILTER_MODE, Utils.getCurrentMode());
    String lifecyleName = null;
    try {
      lifecyleName = publicationService.getNodeLifecycleName(realNode);
    } catch (NotInPublicationLifecycleException e) {}
    if (lifecyleName == null) return realNode;
      
    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecyleName);
    Node viewNode = publicationPlugin.getNodeView(realNode, context);
    return viewNode;
  }
	
	public static class RefreshActionListener extends EventListener<UIPCLVForm> {

		public void execute(Event<UIPCLVForm> event) throws Exception {
			UIPCLVForm contentListPresentation = event.getSource();
			UIPCLVContainer container = contentListPresentation.getParent();
			container.onRefresh(event);
		}
	}
}
