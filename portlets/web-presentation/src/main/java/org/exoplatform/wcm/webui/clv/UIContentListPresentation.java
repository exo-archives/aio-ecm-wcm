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
package org.exoplatform.wcm.webui.clv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.images.RESTImagesRendererService;
import org.exoplatform.services.wcm.publication.PublicationState;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant.SITE_MODE;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.paginator.UICustomizeablePaginator;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 21, 2008
 */

/**
 * The Class UIContentListPresentation.
 */
@ComponentConfigs( {
  @ComponentConfig(
      lifecycle = Lifecycle.class, 
      events = {
        @EventConfig(listeners = UIContentListPresentation.RefreshActionListener.class),
        @EventConfig(listeners = UIContentListPresentation.EditContentActionListener.class)
      }
  ),
  @ComponentConfig(type = UICustomizeablePaginator.class, events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class)) })
  public class UIContentListPresentation extends UIContainer {

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
  
  /** The header. */
  private String                   header;

  /** The date formatter. */
  private DateFormat               dateFormatter = null;
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(UIContentListPresentation.class);
  
  /**
   * Instantiates a new uI content list presentation.
   */
  public UIContentListPresentation() {
  }

  /**
   * Inits the.
   * 
   * @param templatePath the template path
   * @param resourceResolver the resource resolver
   * @param dataPageList the data page list
   * 
   * @throws Exception the exception
   */
  public void init(String templatePath, ResourceResolver resourceResolver, PageList dataPageList) throws Exception {
    PortletPreferences portletPreferences = getPortletPreferences();
    String paginatorTemplatePath = portletPreferences.getValue(UIContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH,
        null);
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
  }

  private PortletPreferences getPortletPreferences() {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = context.getRequest().getPreferences();
    return portletPreferences;
  }

  /**
   * Show refresh button.
   * 
   * @return true, if successful
   */
  public boolean showRefreshButton() {
    PortletPreferences portletPreferences = getPortletPreferences();
    String isShow = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON,
        null);
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
      context.put(Constant.RUNTIME_MODE, SITE_MODE.LIVE);
    } else {
      context.put(Constant.RUNTIME_MODE, SITE_MODE.EDITING);
    }
    String lifecyleName = publicationService.getNodeLifecycleName(node);
    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
    .get(lifecyleName);
    Node viewNode = publicationPlugin.getNodeView(node, context);
    return viewNode;
  }

  public boolean showDraftButton(Node node) throws Exception {
    String currentState = null;
    if (Utils.isLiveMode()) return false;
    try {
      currentState = node.getProperty("publication:currentState").getString();
    } catch (Exception e) {
    } 
    if(PublicationState.DRAFT.equals(currentState))
      return true;
    return false;
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
    String itemsPerPage = portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE,
        null);
    int totalItems = uiPaginator.getTotalItems();
    if (totalItems > Integer.parseInt(itemsPerPage)) {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    return templatePath;
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
   * Gets the content column.
   * 
   * @return the content column
   */
  public String getContentColumn() {
    return this.contentColumn;
  }

  /**
   * Sets the content column.
   * 
   * @param column the new content column
   */
  public void setContentColumn(String column) {
    this.contentColumn = column;
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
  public String getTitle(Node node) throws Exception {
    return node.hasProperty("exo:title") ? node.getProperty("exo:title").getValue().getString()
        : node.getName();
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
  public String getSummary(Node node) throws Exception {
    return node.hasProperty("exo:summary") ? node.getProperty("exo:summary").getValue().getString()
        : null;
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
  public String getURL(Node node) throws Exception {
    String link = null;
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    String portalURI = portalRequestContext.getPortalURI();
    PortletPreferences portletPreferences = getPortletPreferences();
    String repository = portletPreferences.getValue(UIContentListViewerPortlet.REPOSITORY, null);
    String workspace = portletPreferences.getValue(UIContentListViewerPortlet.WORKSPACE, null);
    String baseURI = portletRequestContext.getRequest().getScheme() + "://"
    + portletRequestContext.getRequest().getServerName() + ":"
    + String.format("%s", portletRequestContext.getRequest().getServerPort());
    String parameterizedPageURI = wcmConfigurationService.getParameterizedPageURI();
    link = baseURI + portalURI + parameterizedPageURI.substring(1, parameterizedPageURI.length())
    + "/" + repository + "/" + workspace + node.getPath();
    return link;
  }

  /**
   * Gets the WebDAV uRL.
   * 
   * @param node the node
   * 
   * @return the WebDAV URL
   * 
   * @throws Exception the exception
   */
  public String getWebdavURL(Node node) throws Exception {
  	PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
  	PortletRequest portletRequest = portletRequestContext.getRequest();
  	PortletPreferences portletPreferences = getPortletPreferences();
  	String repository = portletPreferences.getValue(UIContentListViewerPortlet.REPOSITORY, null);
  	String workspace = portletPreferences.getValue(UIContentListViewerPortlet.WORKSPACE, null);
  	String baseURI = portletRequest.getScheme() + "://" + portletRequest.getServerName() + ":" + String.format("%s", portletRequest.getServerPort());
  	
  	if (node.isNodeType("nt:frozenNode")){
  		String uuid = node.getProperty("jcr:frozenUuid").getString();
  		Node originalNode = node.getSession().getNodeByUUID(uuid);
  		return baseURI + "/rest/jcr/" + repository + "/" + workspace + originalNode.getPath() + "?version=" + node.getParent().getName();
  	} else {
  		return baseURI + "/rest/jcr/" + repository + "/" + workspace + node.getPath();
  	}
	  
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
  public String getCreatedDate(Node node) throws Exception {
    if (node.hasProperty("exo:dateCreated")) {
      Calendar calendar = node.getProperty("exo:dateCreated").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;
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
    if (node.hasProperty("exo:dateModified")) {
      Calendar calendar = node.getProperty("exo:dateModified").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;
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
  public String getIllustrativeImage(Node node) {
    WebSchemaConfigService schemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    WebContentSchemaHandler contentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
    RESTImagesRendererService imagesRendererService = getApplicationComponent(RESTImagesRendererService.class);
    Node illustrativeImage = null;
    String uri = null;
    try{
      illustrativeImage = contentSchemaHandler.getIllustrationImage(node);
      uri = imagesRendererService.generateURI(illustrativeImage);
    } catch(PathNotFoundException ex) {
      // We don't do anything here because so many documents doesn't have illustration image
    } catch (Exception e) {
      log.error("Error when get illustration image: ", e.fillInStackTrace());
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

  /**
   * The listener interface for receiving refreshAction events. The class that
   * is interested in processing a refreshAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addRefreshActionListener<code> method. When
   * the refreshAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see RefreshActionEvent
   */
  public static class RefreshActionListener extends EventListener<UIContentListPresentation> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentListPresentation> event) throws Exception {
      UIContentListPresentation contentListPresentation = event.getSource();
      RefreshDelegateActionListener refreshListener = (RefreshDelegateActionListener) contentListPresentation.getParent();
      refreshListener.onRefresh(event);
    }
  }

  public static class EditContentActionListener extends EventListener<UIContentListPresentation> {
    public void execute(Event<UIContentListPresentation> event) throws Exception {
      UIContentListPresentation contentListPresentation = event.getSource();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences preferences = context.getRequest().getPreferences();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);      
      String repository = preferences.getValue(UIContentListViewerPortlet.REPOSITORY, null);
      String worksapce = preferences.getValue(UIContentListViewerPortlet.WORKSPACE, null);      
      if (repository == null || worksapce == null)
        throw new ItemNotFoundException();
      RepositoryService repositoryService = contentListPresentation.getApplicationComponent(RepositoryService.class);      
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Session session = sessionProvider.getSession(worksapce, manageableRepository);
      String contentType=null, nodePath=null;
      try {
    	  Node node = (Node) session.getItem(path);
    	  contentType = node.getPrimaryNodeType().getName();
    	  nodePath = node.getPath();
      } finally {
    	  sessionProvider.close();
      }
      UIListViewerBase uiListViewerBase = contentListPresentation.getAncestorOfType(UIListViewerBase.class);
      UIContentListViewerPortlet uiListViewerPortlet = uiListViewerBase.getAncestorOfType(UIContentListViewerPortlet.class);
      UIPopupContainer uiMaskPopupContainer = uiListViewerPortlet.getChild(UIPopupContainer.class);
      UIContentEdittingPopup uiContentEdittingForm = uiMaskPopupContainer.createUIComponent(UIContentEdittingPopup.class, null, null);
      UIDocumentDialogForm uiDocumentDialogForm = uiContentEdittingForm.getChild(UIDocumentDialogForm.class);

      uiDocumentDialogForm.setRepositoryName(repository);
      uiDocumentDialogForm.setWorkspace(worksapce);
      uiDocumentDialogForm.setContentType(contentType);
      uiDocumentDialogForm.setNodePath(nodePath);
      uiDocumentDialogForm.setStoredPath(nodePath);

      uiListViewerBase.addChild(uiContentEdittingForm);
      uiContentEdittingForm.setRendered(true);
      uiMaskPopupContainer.activate(uiContentEdittingForm, UIContentListViewerPortlet.portletConfigFormWidth, -1);      
      context.addUIComponentToUpdateByAjax(uiMaskPopupContainer);      
    }
  }

  /**
   * Checks if is show link.
   * 
   * @return true, if is show link
   */
  public boolean isShowLink() {
    return showLink;
  }

  /**
   * Sets the show link.
   * 
   * @param showLink the new show link
   */
  public void setShowLink(boolean showLink) {
    this.showLink = showLink;
  }
  
  /**
   * Checks if is show header.
   * 
   * @return true, if is show header
   */
  public boolean isShowHeader() {
	  return showHeader;
  }
  
  /**
   * Sets the show header.
   * 
   * @param showHeader the new show header
   */
  public void setShowHeader(boolean showHeader) {
	  this.showHeader = showHeader;
  }

  /**
   * Sets the header.
   * 
   * @param header the new header
   */
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * Gets the header.
   * 
   * @return the header
   */
  public String getHeader() {
    return this.header;
  }
}
