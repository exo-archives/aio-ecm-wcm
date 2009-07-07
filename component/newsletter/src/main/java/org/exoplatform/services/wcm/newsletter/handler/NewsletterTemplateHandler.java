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
package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 26, 2009  
 */
public class NewsletterTemplateHandler {

  private static Log log = ExoLogger.getLogger(NewsletterTemplateHandler.class);
  private RepositoryService repositoryService;
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  private String repository;
  private String workspace;
  private List<Node> templates;
  
  public NewsletterTemplateHandler(String repository, String workspace) {
    repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }
  
  public List<Node> getTemplates(String portalName, NewsletterCategoryConfig categoryConfig) {
    log.info("Trying to get templates of category " + categoryConfig);
    try {
      List<Node> templates = new ArrayList<Node>();
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      
      Node defaultTemplateFolder = (Node)session.getItem(NewsletterConstant.generateDefaultTemplatePath(portalName));
      NodeIterator defaultTemplates = defaultTemplateFolder.getNodes();
      while(defaultTemplates.hasNext()) {
        templates.add(defaultTemplates.nextNode());
      }
      
      if (categoryConfig != null) {
        Node categoryTemplateFolder = (Node)session.getItem(NewsletterConstant.generateCategoryTemplateBasePath(portalName, categoryConfig.getName()));
        NodeIterator categoryTemplates = categoryTemplateFolder.getNodes();
        while(categoryTemplates.hasNext()) {
          templates.add(categoryTemplates.nextNode());
        }
      }
      
      this.templates = templates;
      return templates;
    } catch (Exception e) {
      log.error("Get templates of category " + categoryConfig + " failed because of " + e.getMessage());
    }
    return null;
  }
  
  public Node getTemplate(String portalName, NewsletterCategoryConfig categoryConfig, String templateName) {
    log.info("Trying to get template " + templateName);
    try {
      if (templates == null) templates = getTemplates(portalName, categoryConfig);
      if (templateName == null) return templates.get(0);
      for (Node template : templates) {
        if (templateName.equals(template.getName())) {
          return template;
        }
      }
    } catch (Exception e) {
      log.error("Get dialog " + templateName + " failed because of " + e.getMessage());
    }
    return null;
  }
  
  public boolean convertAsTemplate(String webcontentPath, String portalName, String categoryName) {
    log.info("Trying to convert node " + webcontentPath + " to template at category " + categoryName);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      Node categoryTemplateFolder = (Node)session.getItem(NewsletterConstant.generateCategoryTemplateBasePath(portalName, categoryName));
      session.getWorkspace().copy(webcontentPath, categoryTemplateFolder.getPath());
      session.save();
      session.logout();
      return true;
    } catch (Exception e) {
      log.error("Convert node " + webcontentPath + " to template at category " + categoryName + " failed because of " + e.getMessage());
    }
    return false;
  }
  
}
