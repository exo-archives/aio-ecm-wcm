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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 5, 2009  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class    
)
public class UIVersionViewer extends UIBaseNodePresentation {

  private JCRResourceResolver resourceResolver ;
  public static final Log log = ExoLogger.getLogger("wcm:StageAndVersionPubliciation");
  
  public Node getNode() throws Exception {
    return getAncestorOfType(UIPublicationPanel.class).getCurrentNode() ;
  }
  
  public void setNode(Node node) {}
  
  public Node getOriginalNode() throws Exception {
    return getNode();
  }

  public String getRepositoryName() throws Exception {
    return null;
  }

  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(getNode(), false) ;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      String repository = getRepository();
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
      resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");
    }catch (Exception e) {
      if(log.isDebugEnabled()) {
        log.debug(e);
      }
    }    
    return resourceResolver ;   
  }
  
  public String getNodeType() throws Exception {
    return null;
  }

  public boolean isNodeTypeSupported() {
    return false;
  }
}
