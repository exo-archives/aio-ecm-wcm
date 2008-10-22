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

import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
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
    @ComponentConfig(lifecycle = Lifecycle.class, events = @EventConfig(listeners = UIContentListPresentation.RefreshActionListener.class)),
    @ComponentConfig(type = UIPageIterator.class, template = "system:/groovy/webui/core/UIPageIterator.gtmpl", events = @EventConfig(listeners = UIPageIterator.ShowPageActionListener.class)) })
public class UIContentListPresentation extends UIContainer {
  private String           templatePath;

  private ResourceResolver resourceResolver;

  private UIPageIterator   uiPageIterator;

  public UIContentListPresentation() {
  }

  public void init(String templatePath, ResourceResolver resourceResolver, PageList dataPageList)
      throws Exception {
    this.templatePath = templatePath;
    this.resourceResolver = resourceResolver;
    uiPageIterator = addChild(UIPageIterator.class, null, null);
    uiPageIterator.setPageList(dataPageList);
  }

  public String getTemplate() {
    return templatePath;
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator; }
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return resourceResolver;
  }

  public List getCurrentPageData() throws Exception {
    return uiPageIterator.getCurrentPageData();
  }

  public String getTitle(Node node) throws Exception {
    return node.hasProperty("exo:title") ? node.getProperty("exo:title").getValue().getString()
        : null;
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
    String repository = portletPreferences.getValue(UIContentListViewerPortlet.REPOSITORY,
        UIContentListViewerPortlet.REPOSITORY);
    String workspace = portletPreferences.getValue(UIContentListViewerPortlet.WORKSPACE,
        UIContentListViewerPortlet.WORKSPACE);
    String baseURI = portletRequestContext.getRequest().getScheme() + "://"
        + portletRequestContext.getRequest().getServerName() + ":"
        + String.format("%s", portletRequestContext.getRequest().getServerPort());
    String parameterizedPageURI = wcmConfigurationService.getParameterizedPageURI();
    link = baseURI + portalURI + parameterizedPageURI.substring(1, parameterizedPageURI.length())
        + "/" + repository + "/" + workspace + node.getPath();
    return link;
  }

  public String getAuthor(Node node) throws Exception {

    return node.getProperty("exo:owner").getValue().getString();
  }

  public String getCreatedDate(Node node) throws Exception {
    return node.hasProperty("exo:dateCreated") ? node.getProperty("exo:dateCreated").getValue()
        .getString() : null;
  }

  public String getModifiedDate(Node node) throws Exception {
    return node.hasProperty("exo:dateModified") ? node.getProperty("exo:dateModified").getValue()
        .getString() : null;
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

  public static class RefreshActionListener extends EventListener<UIContentListPresentation> {
    public void execute(Event<UIContentListPresentation> event) throws Exception {
      UIContentListPresentation contentListPresentation = event.getSource();      
      RefreshDelegateActionListener refreshListener = (RefreshDelegateActionListener)contentListPresentation.getParent();     
      refreshListener.onRefresh(event);
    }
  }

}
