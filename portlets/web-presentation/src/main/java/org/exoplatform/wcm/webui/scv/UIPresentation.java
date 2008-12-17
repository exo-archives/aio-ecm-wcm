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
package org.exoplatform.wcm.webui.scv;

import java.io.Writer;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Jun 9, 2008
 */
@ComponentConfig(
    lifecycle = Lifecycle.class    
)

public class UIPresentation extends UIBaseNodePresentation {

  /** The resource resolver. */
  private JCRResourceResolver  resourceResolver ;

  /**
   * Instantiates a new uI presentation.
   * 
   * @throws Exception the exception
   */
  public UIPresentation() throws Exception {}

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public Node getNode() throws Exception {
    UISingleContentViewerPortlet uiportlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    return uiportlet.getReferencedContent();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepositoryName()
   */
  public String getRepositoryName() {return getRepository() ;}

  /**
   * Gets the portlet preference.
   * 
   * @return the portlet preference
   */
  private PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepository()
   */
  public String getRepository() {
    return getPortletPreference().getValue(UISingleContentViewerPortlet.REPOSITORY, "repository");    
  }    
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getOriginalNode()
   */
  public Node getOriginalNode() throws Exception {
    return getNode();
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    try{
      return getTemplatePath() ;
    }catch (Exception e) {
      return null ;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      getTemplatePath();
    } catch (ItemNotFoundException e) {
      Writer writer = context.getWriter() ;
      writer.write("<div style=\"padding-bottom: 20px; font-size: 13px; text-align: center; padding-top: 10px;\">") ;
      writer.write("<span>") ;
      writer.write(context.getApplicationResourceBundle().getString("UIMessageBoard.msg.content-not-found")) ;
      writer.write("</span>") ;
      writer.write("</div>") ;
      return ;
    } catch (AccessDeniedException e) {
      Writer writer = context.getWriter() ;
      writer.write("<div class=\"Message\">") ;
      writer.write("<span>") ;
      writer.write(context.getApplicationResourceBundle().getString("UIMessageBoard.msg.no-permission")) ;
      writer.write("</span>") ;
      writer.write("</div>") ;
      return;
    } catch (RepositoryException e) {
      Writer writer = context.getWriter();
      writer.write("<div class=\"Message\">");
      writer.write("<span>");
      writer.write(context.getApplicationResourceBundle().getString("UIMessageBoard.msg.error-nodetype"));
      writer.write("</span>");
      writer.write("</div>");
      return;
    } catch (Exception e) {
      Writer writer = context.getWriter();
      writer.write("<div class=\"Message\">");
      writer.write("<span>");
      writer.write(context.getApplicationResourceBundle().getString("UIMessageBoard.msg.error-nodetype"));
      writer.write("</span>");
      writer.write("</div>");
      return;
    }      
    UISingleContentViewerPortlet viewerPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    UIPopupContainer popupContainer = viewerPortlet.getChild(UIPopupContainer.class);
    if(popupContainer!= null) {
      popupContainer.deActivate();
    }
    super.processRender(context) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  @Override
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(getNode(), false) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if(resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        String repository = getRepository();
        ManageableRepository manageableRepository = repositoryService.getRepository(repository);
        String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");
      }
    }catch (Exception e) {
      if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
        UISingleContentViewerPortlet.scvLog.debug(e);
      }
    }    
    return resourceResolver ;   
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getNodeType()
   */
  public String getNodeType() throws Exception {   
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#isNodeTypeSupported()
   */
  public boolean isNodeTypeSupported() {
    return false;
  }

  public void setNode(Node node) {
    
  }
}
