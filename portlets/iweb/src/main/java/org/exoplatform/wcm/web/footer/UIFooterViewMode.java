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
package org.exoplatform.wcm.web.footer;

import javax.jcr.Session;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.resolver.NTFileResourceResolver;
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
public class UIFooterViewMode extends UIComponent {

  private final String DEFAULT_TEMPLATE = "app:/groovy/footer/webui/UIFooterPortlet.gtmpl".intern();
  private PortletRequestContext portletRequestContext = null;
  private PortletRequest portletRequest = null;
  private String repository = null;
  private String workspace = null;
  private String nodeUUID = null;
  
  public UIFooterViewMode() throws Exception {
    portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    portletRequest = portletRequestContext.getRequest();
    repository = portletRequest.getPreferences().getValue("repository", null);
    workspace = portletRequest.getPreferences().getValue("workspace", null);
    nodeUUID = portletRequest.getPreferences().getValue("nodeUUID", null);
  }

  public String getTemplate() {
    if(isUseJCRTemplate())
      return "jcr:" + nodeUUID;
    return DEFAULT_TEMPLATE; 
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(isUseJCRTemplate()) {
      return new NTFileResourceResolver(repository, workspace);
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  private boolean isUseJCRTemplate() {
    if (repository != null && workspace != null && nodeUUID != null) {
      try {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
        ManageableRepository manageableRepository = (ManageableRepository)repositoryService.getRepository(repository);
        Session session = sessionProvider.getSession(workspace, manageableRepository);
        session.getNodeByUUID(nodeUUID);
        sessionProvider.close();
        return true;
      } catch (Exception e) {}
    }
    return false;
  }  
}
