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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.images.RESTImagesRendererService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationState;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant.SITE_MODE;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.paginator.UICustomizeablePaginator;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 23, 2009  
 */
/*@ComponentConfig(
                 lifecycle = UIFormLifecycle.class, 
                 template = "app:/groovy/ParameterizedContentListViewer/UIParameterizedContentListViewerForm.gtmpl"
               )*/
               
@ComponentConfigs( {
  @ComponentConfig(
      lifecycle = Lifecycle.class, 
      events = {
        @EventConfig(listeners = UIParameterizedContentListViewerForm.RefreshActionListener.class)
      }
  ),
  @ComponentConfig(type = UICustomizeablePaginator.class, events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class)) })

public class UIParameterizedContentListViewerForm extends UIForm {

  /** The template path. */
  private String                   templatePath;

  /** The resource resolver. */
  private ResourceResolver         resourceResolver;

  /** The ui paginator. */
  private UICustomizeablePaginator uiPaginator;

  /** The content column. */
  private String                   contentColumn;

  /** The show link. */
  private boolean                  showLink;

  /** The show header. */
  private boolean                  showHeader;

  /** The show readmore. */
  private boolean                  showReadmore;

  /** The header. */
  private String                   header;

  /** The date formatter. */
  private DateFormat               dateFormatter = null;

  /** Add date to the link. */
  private String                   addDateToLink;

  /** Auto detection. */
  private String                   autoDetection;

  /** Show more link. */
  private String                   showMoreLink;

  /** Show RSS link. */
  private String                   showRSSLink;
  
  private List<PCLVViewerConfig> listPCLVConfig;
  
  public UIParameterizedContentListViewerForm(){
  }
  
  public void init(String templatePath, ResourceResolver resourceResolver, PageList dataPageList) throws Exception {
    
    PortletPreferences portletPreferences = getPortletPreferences();
    String paginatorTemplatePath = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH, null);
    this.templatePath = templatePath;
    this.resourceResolver = resourceResolver;
    uiPaginator = addChild(UICustomizeablePaginator.class, null, null);
    uiPaginator.setTemplatePath(paginatorTemplatePath);
    uiPaginator.setResourceResolver(resourceResolver);
    uiPaginator.setPageList(dataPageList);
    Locale locale = Util.getPortalRequestContext().getLocale();
    dateFormatter = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM,
        SimpleDateFormat.MEDIUM,
        locale);
    
    PCLVViewerConfig pclvConfig = null;
    listPCLVConfig = new ArrayList<PCLVViewerConfig>();
    for(int i = 0; i < 4; i++) {
      pclvConfig = new PCLVViewerConfig();
      pclvConfig.setTitle("This is the title of PCLV");
      pclvConfig.setIllustrationSumary("This is the illustration of PCLV");
      pclvConfig.setIllustrationImage("This is the illustration image");

      listPCLVConfig.add(pclvConfig);
    }
  }

  /**
   * Show refresh button.
   * 
   * @return true, if successful
   */
  public boolean showRefreshButton() {
    PortletPreferences portletPreferences = getPortletPreferences();
    String isShow = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_REFRESH_BUTTON,
        null);
    return (isShow != null) ? Boolean.parseBoolean(isShow) : false;
  }
  
  public boolean showRSSLink() {
    PortletPreferences portletPreferences = getPortletPreferences();
    String isShow = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_RSS_LINK, null);
    return (isShow != null) ? Boolean.parseBoolean(isShow) : false;
  }

  /**
   * Checks if is show field.
   * 
   * @param field the field
   * 
   * @return true, if is show field
   */
  public boolean isShowField(String field) {
    PortletPreferences portletPreferences = getPortletPreferences();
    String showAble = portletPreferences.getValue(field, null);
    return (showAble != null) ? Boolean.parseBoolean(showAble) : false;
  }

  public Node getNodeView(Node node) throws Exception {
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    HashMap<String, Object> context = new HashMap<String, Object>();
    if (org.exoplatform.wcm.webui.Utils.isLiveMode()) {
      context.put(StageAndVersionPublicationConstant.RUNTIME_MODE, SITE_MODE.LIVE);
    } else {
      context.put(StageAndVersionPublicationConstant.RUNTIME_MODE, SITE_MODE.EDITING);
    }
    String lifecyleName = publicationService.getNodeLifecycleName(node);
    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
    .get(lifecyleName);
    Node viewNode = publicationPlugin.getNodeView(node, context);
    return viewNode;
  }

  public boolean showDraftButton() throws Exception {
    /*String currentState = null;
    if (Utils.isLiveMode()) return false;
    try {
      currentState = node.getProperty("publication:currentState").getString();
    } catch (Exception e) {
    } 
    if(StageAndVersionPublicationState.DRAFT.equals(currentState))
      return true;*/
    return true;
  }

  /**
   * Show paginator.
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  public boolean showPaginator() throws Exception {
    PortletPreferences portletPreferences = getPortletPreferences();
    String itemsPerPage = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.ITEMS_PER_PAGE,
        null);
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

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext,
   *      java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return resourceResolver;
  }

  /**
   * Gets the current page data.
   * 
   * @return the current page data
   * 
   * @throws Exception the exception
   */
  public List getCurrentPageData() throws Exception {
    return uiPaginator.getCurrentPageData();
  }

  /**
   * Gets the title.
   * 
   * @param node the node
   * 
   * @return the title
   * 
   * @throws Exception the exception
   */
  public String getTitle() throws Exception {
    /*return node.hasProperty("exo:title") ? node.getProperty("exo:title").getValue().getString()
        : node.getName();*/
    return "Title parameterized content list viewer";
  }

  /**
   * Gets the summary.
   * 
   * @param node the node
   * 
   * @return the summary
   * 
   * @throws Exception the exception
   */
  public String getSummary() throws Exception {
    /*return node.hasProperty("exo:summary") ? node.getProperty("exo:summary").getValue().getString()
        : null;*/
    return "Sumary";
  }

  /**
   * Gets the uRL.
   * 
   * @param node the node
   * 
   * @return the uRL
   * 
   * @throws Exception the exception
   */
  public String getURL() throws Exception {
    /*String link = null;
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    String portalURI = portalRequestContext.getPortalURI();
    PortletPreferences portletPreferences = getPortletPreferences();
    String repository = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.REPOSITORY, null);
    String workspace = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.WORKSPACE, null);
    String baseURI = portletRequestContext.getRequest().getScheme() + "://"
    + portletRequestContext.getRequest().getServerName() + ":"
    + String.format("%s", portletRequestContext.getRequest().getServerPort());
    String parameterizedPageURI = wcmConfigurationService.getParameterizedPageURI();
    link = baseURI + portalURI + parameterizedPageURI.substring(1, parameterizedPageURI.length())
    + "/" + repository + "/" + workspace + node.getPath();
    return link;*/
    
    return "Link for parameterized test";
  }

  /**
   * Gets the author.
   * 
   * @param node the node
   * 
   * @return the author
   * 
   * @throws Exception the exception
   */
  public String getAuthor(Node node) throws Exception {
    if (node.hasProperty("exo:owner")) {
      String ownerId = node.getProperty("exo:owner").getValue().getString();
      OrganizationService organizationService = getApplicationComponent(OrganizationService.class);
      UserProfileHandler handler = organizationService.getUserProfileHandler();
      UserProfile userProfile = handler.findUserProfileByName(ownerId);
      return userProfile.getUserInfoMap().get("user.name.given");
    }
    return null;
  }

  /**
   * Gets the created date.
   * 
   * @param node the node
   * 
   * @return the created date
   * 
   * @throws Exception the exception
   */
  public String getCreatedDate() throws Exception {
    /*if (node.hasProperty("exo:dateCreated")) {
      Calendar calendar = node.getProperty("exo:dateCreated").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;*/
    
    Calendar calendar = Calendar.getInstance();
    return dateFormatter.format(calendar.getTime()) + "Created";
  }

  /**
   * Gets the modified date.
   * 
   * @param node the node
   * 
   * @return the modified date
   * 
   * @throws Exception the exception
   */
  public String getModifiedDate(Node node) throws Exception {
    /*if (node.hasProperty("exo:dateModified")) {
      Calendar calendar = node.getProperty("exo:dateModified").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }*/
    Calendar calendar = Calendar.getInstance();
    return dateFormatter.format(calendar.getTime()) + "Modified";
  }

  /**
   * Gets the content type.
   * 
   * @param node the node
   * 
   * @return the content type
   */
  public String getContentType(Node node) {
    return null;
  }

  /**
   * Gets the content icon.
   * 
   * @param node the node
   * 
   * @return the content icon
   */
  public String getContentIcon(Node node) {
    return null;
  }

  /**
   * Gets the content size.
   * 
   * @param node the node
   * 
   * @return the content size
   */
  public String getContentSize(Node node) {
    return null;
  }

  /**
   * Gets the illustrative image.
   * 
   * @param node the node
   * 
   * @return the illustrative image
   * 
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

  /**
   * Gets the categories.
   * 
   * @param node the node
   * 
   * @return the categories
   */
  public List<String> getCategories(Node node) {
    return null;
  }

  /**
   * Gets the tags.
   * 
   * @param node the node
   * 
   * @return the tags
   */
  public List<String> getTags(Node node) {
    return null;
  }

  /**
   * Gets the voting rate.
   * 
   * @param node the node
   * 
   * @return the voting rate
   */
  public float getVotingRate(Node node) {
    return 0;
  }

  /**
   * Gets the number of comments.
   * 
   * @param node the node
   * 
   * @return the number of comments
   */
  public int getNumberOfComments(Node node) {
    return 0;
  }

  /**
   * Gets the related contents.
   * 
   * @param node the node
   * 
   * @return the related contents
   */
  public List<Node> getRelatedContents(Node node) {
    return null;
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

  public String getAddDateToLink() {
    return addDateToLink;
  }

  public void setAddDateToLink(String addDateToLink) {
    this.addDateToLink = addDateToLink;
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

  public static class RefreshActionListener extends EventListener<UIParameterizedContentListViewerForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIParameterizedContentListViewerForm> event) throws Exception {
      UIParameterizedContentListViewerForm contentListPresentation = event.getSource();
      UIParameterizedContentListViewerContainer container = contentListPresentation.getParent();
      container.onRefresh(event);
    }
  }
}
