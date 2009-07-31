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
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009
 */
public class NewsletterCategoryHandler {

  /** The log. */
  private static Log log = ExoLogger.getLogger(NewsletterCategoryHandler.class);
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
  /** The thread local session provider service. */
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  
  /** The repository. */
  private String repository;
  
  /** The workspace. */
  private String workspace;
  
  /**
   * Instantiates a new newsletter category handler.
   * 
   * @param repository the repository
   * @param workspace the workspace
   */
  public NewsletterCategoryHandler(String repository, String workspace) {
    repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class.cast(
                                                                                     ExoContainerContext.getCurrentContainer()
                                                                                     .getComponentInstanceOfType(
                                                                                                                 ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }

  /**
   * Gets the category from node.
   * 
   * @param categoryNode the category node
   * 
   * @return the category from node
   * 
   * @throws Exception the exception
   */
  protected NewsletterCategoryConfig getCategoryFromNode(Node categoryNode) throws Exception{
  	NewsletterCategoryConfig categoryConfig = null;
  	categoryConfig = new NewsletterCategoryConfig();
  	categoryConfig.setName(categoryNode.getName());
  	categoryConfig.setTitle(categoryNode.getProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE).getString());
  	if (categoryNode.hasProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION)) {
  	  categoryConfig.setDescription(categoryNode.getProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION).getString());
  	}
  	// get permission for this category
  	ExtendedNode webContent = (ExtendedNode)categoryNode;
    Iterator permissionIterator = webContent.getACL().getPermissionEntries().iterator();
    List<String> listPermission = new ArrayList<String>();
    String permission = "";
    while (permissionIterator.hasNext()) {
      AccessControlEntry accessControlEntry = (AccessControlEntry) permissionIterator.next();
      String currentIdentity = accessControlEntry.getIdentity();
      if(!listPermission.contains(currentIdentity)){
        listPermission.add(currentIdentity);
        if(permission.length() > 0) permission += ",";
        permission += currentIdentity;
      }
    }
    categoryConfig.setModerator(permission);
  	return categoryConfig;
  }
  
  /**
   * Adds the.
   * 
   * @param portalName the portal name
   * @param categoryConfig the category config
   * @param sessionProvider the session provider
   */
  public void add(String portalName, NewsletterCategoryConfig categoryConfig, SessionProvider sessionProvider) {
    log.info("Trying to add category " + categoryConfig.getName());
    
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoriesNode = (Node)session.getItem(categoryPath);
      Node categoryNode = categoriesNode.addNode(categoryConfig.getName(), NewsletterConstant.CATEGORY_NODETYPE);
      categoryNode.addNode("Templates", "nt:unstructured");
      ExtendedNode extendedCategoryNode = ExtendedNode.class.cast(categoryNode);
      extendedCategoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE, categoryConfig.getTitle());
      extendedCategoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION, categoryConfig.getDescription());
      if (extendedCategoryNode.canAddMixin("exo:privilegeable")) {
        extendedCategoryNode.addMixin("exo:privilegeable");
        extendedCategoryNode.setPermission(categoryConfig.getModerator(), PermissionType.ALL);
      }
      session.save();
    } catch(Exception e) {
    	e.printStackTrace();
      log.error("Add category " + categoryConfig.getName() + " failed because of " + e.getMessage());
    } 
  }
  
  /**
   * Edits the.
   * 
   * @param portalName the portal name
   * @param categoryConfig the category config
   * @param sessionProvider the session provider
   */
  public void edit(String portalName, NewsletterCategoryConfig categoryConfig, SessionProvider sessionProvider) {
    log.info("Trying to edit category " + categoryConfig.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(categoryPath)).getNode(categoryConfig.getName());
      categoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION, categoryConfig.getDescription());
      categoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE, categoryConfig.getTitle());
      
      ExtendedNode extendedCategoryNode = ExtendedNode.class.cast(categoryNode);
      if (extendedCategoryNode.canAddMixin("exo:privilegeable")) {
        extendedCategoryNode.addMixin("exo:privilegeable");
        extendedCategoryNode.setPermission(categoryConfig.getModerator(), PermissionType.ALL);
      }
      
      session.save();
    } catch (Exception e) {
      log.info("Edit category " + categoryConfig.getName() + " failed because of " + e.getMessage());
    }
  }
  
  /**
   * Delete.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param sessionProvider the session provider
   */
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
  
  /**
   * Gets the category by name.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param sessionProvider the session provider
   * 
   * @return the category by name
   * 
   * @throws Exception the exception
   */
  public NewsletterCategoryConfig getCategoryByName(String portalName, String categoryName, SessionProvider sessionProvider) throws Exception{
  	try{ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoriesNode = (Node)session.getItem(categoryPath);
      return getCategoryFromNode(categoriesNode.getNode(categoryName));
  	}catch(Exception ex){
  	  return null;
  	}
  }
  
  /**
   * Gets the list categories.
   * 
   * @param portalName the portal name
   * @param sessionProvider the session provider
   * 
   * @return the list categories
   * 
   * @throws Exception the exception
   */
  public List<NewsletterCategoryConfig> getListCategories(String portalName, SessionProvider sessionProvider) throws Exception{
    List<NewsletterCategoryConfig> listCategories = new ArrayList<NewsletterCategoryConfig>();
  	try{
    	ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoriesNode = (Node)session.getItem(categoryPath);
      NodeIterator nodeIterator = categoriesNode.getNodes();
      while(nodeIterator.hasNext()){
        try{
          listCategories.add(getCategoryFromNode(nodeIterator.nextNode()));
        }catch(Exception ex){
          ex.printStackTrace();
        }
      }
  	}catch(Exception e){
	    e.printStackTrace();
	  }
    return listCategories;
  }

}
