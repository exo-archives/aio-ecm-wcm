/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wcm.web.banner;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.resolver.StringResourceResolver;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Author : Do Ngoc Anh *      
 * Email: anh.do@exoplatform.com *
 * May 9, 2008  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class  
)
public class UIBannerViewMode extends UIComponent {

  private PortletRequestContext portletRequestContext = null;
  private PortletRequest portletRequest = null;
  private String repository = null;
  private String workspace = null;
  private String nodeUUID = null;
  private String loginUIUUID = null;
  private boolean showLoginUI = false;

  public UIBannerViewMode() throws Exception {
    portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    portletRequest = portletRequestContext.getRequest();
    repository = portletRequest.getPreferences().getValue("repository", null);
    workspace = portletRequest.getPreferences().getValue("workspace", null);
    nodeUUID = portletRequest.getPreferences().getValue("nodeUUID", null);
    loginUIUUID = portletRequest.getPreferences().getValue("loginUIUUID", null);
    showLoginUI = Boolean.parseBoolean(portletRequest.getPreferences().getValue("showLoginUI", null));
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    String bannerData = loadJCRBanner();
    if(bannerData != null) {
      return new StringResourceResolver(bannerData);
    }    
    return super.getTemplateResourceResolver(context, template);
  }

  public String getTemplate() {
    return loadJCRBanner();
  }
  
  private String loadJCRBanner() {
    if (repository != null && workspace != null && nodeUUID != null) {
      try {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        // need use session provider if enable permission for banner
        SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
        ManageableRepository manageableRepository = (ManageableRepository)repositoryService.getRepository(repository);
        Session session = sessionProvider.getSession(workspace, manageableRepository);
        Node bannerWebContent = session.getNodeByUUID(nodeUUID);
        String bannerCSS = bannerWebContent.getNode("css").getNode("default.css").getNode("jcr:content").getProperty("jcr:data").getString();
        String bannerHTML = bannerWebContent.getNode("default.html").getNode("jcr:content").getProperty("jcr:data").getString();
        String bannerAccess = session.getNodeByUUID(loginUIUUID).getNode("jcr:content").getProperty("jcr:data").getString();
        StringBuffer buffer = new StringBuffer();
        if(showLoginUI)
          buffer.append("<style>").append(bannerCSS).append("</style>").append(bannerAccess).append(bannerHTML);
        else 
          buffer.append("<style>").append(bannerCSS).append("</style>").append(bannerHTML);
        return buffer.toString();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }  
}
