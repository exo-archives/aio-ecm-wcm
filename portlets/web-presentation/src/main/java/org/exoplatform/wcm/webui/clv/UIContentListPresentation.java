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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
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

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 21, 2008
 */

@ComponentConfigs( {
  @ComponentConfig(
      lifecycle = Lifecycle.class, 
      events = @EventConfig(listeners = UIContentListPresentation.RefreshActionListener.class)                  
  ),
  @ComponentConfig(
      type = UICustomizeablePaginator.class, 
      events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class)       
  )

})
public class UIContentListPresentation extends UIContainer {
  private String                   templatePath;

  private ResourceResolver         resourceResolver;

  private UICustomizeablePaginator uiPaginator;

  private String                   contentColumn;
  private boolean                  showHeader;

  private SimpleDateFormat         dateFormatter = new SimpleDateFormat(
      ISO8601.SIMPLE_DATETIME_FORMAT);

  public UIContentListPresentation() {
  }

  public void init(String templatePath, ResourceResolver resourceResolver, PageList dataPageList)
  throws Exception {
    PortletPreferences portletPreferences = ((UIFolderViewer) getParent()).getPortletPreference();
    String paginatorTemplatePath = portletPreferences.getValue(
        UIContentListViewerPortlet.PAGINATOR_TEMPlATE_PATH, null);
    this.templatePath = templatePath;
    this.resourceResolver = resourceResolver;
    uiPaginator = addChild(UICustomizeablePaginator.class, null, null);
    uiPaginator.setTemplatePath(paginatorTemplatePath);
    uiPaginator.setResourceResolver(resourceResolver);
    uiPaginator.setPageList(dataPageList);   
  }

  public boolean showRefreshButton() {
    PortletPreferences portletPreferences = ((UIFolderViewer) getParent()).getPortletPreference();
    String isShow = portletPreferences.getValue(UIContentListViewerPortlet.SHOW_REFRESH_BUTTON,
        null);
    return (isShow != null) ? Boolean.parseBoolean(isShow) : false;
  }
  
  public boolean isShowField(String field) {
    PortletPreferences portletPreferences = ((UIFolderViewer) getParent()).getPortletPreference();
    String showAble = portletPreferences.getValue(field, null);
    return (showAble != null) ? Boolean.parseBoolean(showAble) : false;
  }

  public boolean showPaginator() throws Exception {
    PortletPreferences portletPreferences = ((UIFolderViewer) getParent()).getPortletPreference();
    String itemsPerPage = portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE,
        null);
    int totalItems = uiPaginator.getTotalItems();
    if (totalItems > Integer.parseInt(itemsPerPage)) {
      return true;
    }     
    return false;
  }

  public String getTemplate() {
    return templatePath;
  }

  public SimpleDateFormat getDatetimeFommatter() {
    return dateFormatter;
  }

  public void setDateTimeFormat(String format) {
    dateFormatter.applyPattern(format);
  }

  public String getContentColumn() {
    return this.contentColumn;
  }

  public void setContentColumn(String column) {
    this.contentColumn = column;
  }

  public UIPageIterator getUIPageIterator() {
    return uiPaginator;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return resourceResolver;
  }

  public List getCurrentPageData() throws Exception {
    return uiPaginator.getCurrentPageData();
  }

  public String getTitle(Node node) throws Exception {
    return node.hasProperty("exo:title") ? node.getProperty("exo:title").getValue().getString()
        : node.getName();
  }

  public String getSummary(Node node) throws Exception {
    return node.hasProperty("exo:summary") ? node.getProperty("exo:summary").getValue().getString()
        : null;
  }

  public String getURL(Node node) throws Exception {
    String link = null;
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    String portalURI = portalRequestContext.getPortalURI();
    PortletPreferences portletPreferences = ((UIFolderViewer) getParent()).getPortletPreference();
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

  public String getCreatedDate(Node node) throws Exception {
    if (node.hasProperty("exo:dateCreated")) {
      Calendar calendar = node.getProperty("exo:dateCreated").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;
  }

  public String getModifiedDate(Node node) throws Exception {
    if (node.hasProperty("exo:dateModified")) {
      Calendar calendar = node.getProperty("exo:dateModified").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;
  }

  public String getContentType(Node node) {
    return null;
  }

  public String getContentIcon(Node node) {
    return null;
  }

  public String getContentSize(Node node) {
    return null;
  }

  public String getIllustrativeImage(Node node) throws Exception {
    String imagePath = null;
    if(node.isNodeType("exo:webContent")) {
      WebSchemaConfigService schemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
      WebContentSchemaHandler contentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
      Node illustrativeImage = null;
      try {
        illustrativeImage = contentSchemaHandler.getIllustrationImage(node);
        imagePath = Utils.getThumbnailImage(illustrativeImage,ThumbnailService.MEDIUM_SIZE);
      } catch (Exception e) { }
      if(imagePath == null && illustrativeImage != null) {
        Session session = illustrativeImage.getSession();
        String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
        String workspace = session.getWorkspace().getName();
        imagePath = "/portal/rest/jcr/"+repository+"/" + workspace + illustrativeImage.getPath();
      }
      return imagePath;
    }
    PortletRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    String showThumbnailPref = requestContext.getRequest().getPreferences().getValue(UIContentListViewerPortlet.SHOW_THUMBNAILS_VIEW,"false");
    boolean isShowThumbnail = Boolean.parseBoolean(showThumbnailPref);
    if(isShowThumbnail) {
      return Utils.getThumbnailImage(node,ThumbnailService.MEDIUM_SIZE);
    }
    return null;
  }

  public List<String> getCategories(Node node) {
    return null;
  }

  public List<String> getTags(Node node) {
    return null;
  }

  public float getVotingRate(Node node) {
    return 0;
  }

  public int getNumberOfComments(Node node) {
    return 0;
  }

  public List<Node> getRelatedContents(Node node) {
    return null;
  }

  public static class RefreshActionListener extends EventListener<UIContentListPresentation> {
    public void execute(Event<UIContentListPresentation> event) throws Exception {
      UIContentListPresentation contentListPresentation = event.getSource();
      RefreshDelegateActionListener refreshListener = (RefreshDelegateActionListener) contentListPresentation
      .getParent();
      refreshListener.onRefresh(event);
    }
  }

  public boolean isShowHeader() {
    return showHeader;
  }

  public void setShowHeader(boolean showHeader) {
    this.showHeader = showHeader;
  }

}
