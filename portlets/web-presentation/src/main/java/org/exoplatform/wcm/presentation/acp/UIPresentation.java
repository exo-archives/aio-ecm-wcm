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
package org.exoplatform.wcm.presentation.acp;

import java.io.Writer;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 18, 2008  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class    
)

public class UIPresentation extends UIBaseNodePresentation {

  private JCRResourceResolver  resourceResolver ;

  public UIPresentation() throws Exception {}

  public Node getNode() throws Exception {     
    String repository = getPortletPreference().getValue(UIAdvancedPresentationPortlet.REPOSITORY, "repository");    
    String worksapce = getPortletPreference().getValue(UIAdvancedPresentationPortlet.WORKSPACE, "collaboration");
    String uuid = getPortletPreference().getValue(UIAdvancedPresentationPortlet.UUID, "") ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    SessionProvider sessionProvider = null;
    if(userId == null) {
      sessionProvider = SessionProviderFactory.createAnonimProvider();
    }else {
      sessionProvider = SessionProviderFactory.createSessionProvider();
    }
    Session session = sessionProvider.getSession(worksapce, manageableRepository) ;
    return session.getNodeByUUID(uuid) ;    
  }

  public String getRepositoryName() {return getRepository() ;}

  private PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  public String getRepository() {
    return getPortletPreference().getValue(UIAdvancedPresentationPortlet.REPOSITORY, "repository");    
  }    
  public Node getOriginalNode() throws Exception {
    return null;
  }  

  public String getTemplate() {
    try{
      return getTemplatePath() ;
    }catch (Exception e) {
      return null ;
    }
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      getNode() ;
    } catch (ItemNotFoundException e) {
      Writer writer = context.getWriter() ;
      writer.write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px;\">") ;
      writer.write("<span>") ;
      writer.write(context.getApplicationResourceBundle().getString("UIMessageBoard.msg.content-not-found")) ;
      writer.write("</span>") ;
      writer.write("</div>") ;
      return ;
    } catch (AccessDeniedException e) {
      Writer writer = context.getWriter() ;
      writer.write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px;\">") ;
      writer.write("<span>") ;
      writer.write(context.getApplicationResourceBundle().getString("UIMessageBoard.msg.no-permission")) ;
      writer.write("</span>") ;
      writer.write("</div>") ;
      return;
    }
    super.processRender(context) ;
  }

  @Override
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);   
    return templateService.getTemplatePath(getNode(), false) ;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if(resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        String repository = getRepository();
        ManageableRepository manageableRepository = repositoryService.getRepository(repository);
        String worksapce = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(repository, worksapce, "exo:templateFile");
      }
    }catch (Exception e) {
    }    
    return resourceResolver ;   
  }

  public String getNodeType() throws Exception {   
    return null;
  }

  public boolean isNodeTypeSupported() {
    return false;
  }

  public void setNode(Node arg0) {

  }

}
