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
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterCategoryHandler {

  private static Log log = ExoLogger.getLogger(NewsletterCategoryHandler.class);
  private RepositoryService repositoryService;
  private String repository;
  private String workspace;
  
  public NewsletterCategoryHandler(String repository, String workspace) {
    repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    this.repository = repository;
    this.workspace = workspace;
  }

  protected NewsletterCategoryConfig getCategoryFromNode(Node categoryNode) throws Exception{
  	NewsletterCategoryConfig categoryConfig = null;
  	categoryConfig = new NewsletterCategoryConfig();
  	categoryConfig.setName(categoryNode.getName());
  	categoryConfig.setTitle(categoryNode.getProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE).getString());
  	categoryConfig.setDescription(categoryNode.getProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION).getString());
  	return categoryConfig;
  }
  
  public void add(String portalName, NewsletterCategoryConfig categoryConfig, SessionProvider sessionProvider) {
    log.info("Trying to add category " + categoryConfig.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoriesNode = (Node)session.getItem(categoryPath);
      Node categoryNode = categoriesNode.addNode(categoryConfig.getName(), NewsletterConstant.CATEGORY_NODETYPE);
      ExtendedNode extendedCategoryNode = ExtendedNode.class.cast(categoryNode);
      extendedCategoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE, categoryConfig.getTitle());
      extendedCategoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION, categoryConfig.getDescription());
      if (extendedCategoryNode.canAddMixin("exo:privilegeable")) {
        extendedCategoryNode.addMixin("exo:privilegeable");
        extendedCategoryNode.setPermission(categoryConfig.getModerator(), PermissionType.ALL);
      }
      session.save();
    } catch(Exception e) {
      log.error("Add category " + categoryConfig.getName() + " failed because of " + e.getMessage());
    } 
  }
  
  public void edit(String portalName, NewsletterCategoryConfig categoryConfig, SessionProvider sessionProvider) {
    log.info("Trying to edit category " + categoryConfig.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(categoryPath)).getNode(categoryConfig.getName());
      categoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION, categoryConfig.getDescription());
      categoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE, categoryConfig.getTitle());
      session.save();
    } catch (Exception e) {
      log.info("Edit category " + categoryConfig.getName() + " failed because of " + e.getMessage());
    }
  }
  
  public void delete(String portalName, String categoryName, SessionProvider sessionProvider) {
    log.info("Trying to delete category " + categoryName);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(categoryPath)).getNode((categoryName));
      categoryNode.remove();
      session.save();
    } catch (Exception e) {
      log.error("Delete category " + categoryName + " failed because of " + e.getMessage());
    }
  }
  
  public NewsletterCategoryConfig getCategoryByName(String portalName, String categoryName, SessionProvider sessionProvider) throws Exception{
  	ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
    Node categoriesNode = (Node)session.getItem(categoryPath);
    return getCategoryFromNode(categoriesNode.getNode(categoryName));
  }
  
  public List<NewsletterCategoryConfig> getListCategories(String portalName, SessionProvider sessionProvider) throws Exception{
  	List<NewsletterCategoryConfig> listCategories = new ArrayList<NewsletterCategoryConfig>();
  	ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
    Node categoriesNode = (Node)session.getItem(categoryPath);
    NodeIterator nodeIterator = categoriesNode.getNodes();
    while(nodeIterator.hasNext()){
    	listCategories.add(getCategoryFromNode(nodeIterator.nextNode()));
    }
    return listCategories;
  }

}
