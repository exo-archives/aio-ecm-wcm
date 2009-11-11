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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
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
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009
 */
public class NewsletterCategoryHandler {

  /** The log. */
  private static Log log = ExoLogger.getLogger(NewsletterCategoryHandler.class);
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
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
    this.repository = repository;
    this.workspace = workspace;
  }

  @SuppressWarnings("unchecked")
  private List<String> getAllPermissionOfCategoryNode(Node categoryNode) throws Exception{
    ExtendedNode webContent = (ExtendedNode)categoryNode;
    Iterator permissionIterator = webContent.getACL().getPermissionEntries().iterator();
    String currentIdentity;
    AccessControlEntry accessControlEntry;
    Map<String, Integer> mapPermission = new HashMap<String, Integer>();
    while (permissionIterator.hasNext()) {
      accessControlEntry = (AccessControlEntry) permissionIterator.next();
      currentIdentity = accessControlEntry.getIdentity();
      if(mapPermission.containsKey(currentIdentity)){
        mapPermission.put(currentIdentity, mapPermission.get(currentIdentity) + 1);
      } else {
        mapPermission.put(currentIdentity, 1);
      }
    }
    int size = PermissionType.ALL.length;
    List<String> listPermission = new ArrayList<String>();
    for(String key : mapPermission.keySet().toArray(new String[]{})){
      if(mapPermission.get(key) == size)
        listPermission.add(key);
    }
    return listPermission;
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
  @SuppressWarnings("unchecked")
  protected NewsletterCategoryConfig getCategoryFromNode(Node categoryNode) throws Exception{
  	NewsletterCategoryConfig categoryConfig = null;
  	categoryConfig = new NewsletterCategoryConfig();
  	categoryConfig.setName(categoryNode.getName());
  	categoryConfig.setTitle(categoryNode.getProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE).getString());
  	if (categoryNode.hasProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION)) {
  	  categoryConfig.setDescription(categoryNode.getProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION).getString());
  	}
  	// get permission for this category
  	String permission = "";
  	for(String per : getAllPermissionOfCategoryNode(categoryNode)){
  	  if(permission.length() > 0) permission += ",";
      permission += per;
  	}
    categoryConfig.setModerator(permission);
  	return categoryConfig;
  }
  
  private List<String> convertValuesToArray(Value[] values){
    List<String> listString = new ArrayList<String>();
    for(Value value : values){
      try {
        listString.add(value.getString());
      }catch (Exception e) {
        log.error("Error when convert values to array: ", e.fillInStackTrace());
      }
    }
    return listString;
  }
  
  /**
   * Update permission for category node.
   * 
   * @param categoryNode    node which is will be updated
   * @param categoryConfig  Category Object
   * @param isAddNew        is <code>True</code> if is add new category node and <code>False</code> if only update
   * @throws Exception      The Exception
   */
  private void updatePermissionForCategoryNode(Node categoryNode, NewsletterCategoryConfig categoryConfig, boolean isAddNew) throws Exception{
    ExtendedNode extendedCategoryNode = ExtendedNode.class.cast(categoryNode);
    if (extendedCategoryNode.canAddMixin("exo:privilegeable") || extendedCategoryNode.isNodeType("exo:privilegeable")) {
      if(extendedCategoryNode.canAddMixin("exo:privilegeable"))
        extendedCategoryNode.addMixin("exo:privilegeable");
      // get all administrator of newsletter
      List<String> listAddministrators = new ArrayList<String>();
      Node categoriesNode = categoryNode.getParent();
      if(categoriesNode.hasProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR)) {
        Value[] values = categoriesNode.getProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR).getValues();
        listAddministrators = convertValuesToArray(values);
      }
      // Set permission is all for moderators
      List<String> newModerators = new ArrayList<String>();
      newModerators.addAll(Arrays.asList(categoryConfig.getModerator().split(","))); 
      for(String permission : newModerators){
        extendedCategoryNode.setPermission(permission, PermissionType.ALL);
      }
      // Set permission is addNode, remove and setProperty for administrators
      if(isAddNew){
        for(String per : listAddministrators){
          if(newModerators.contains(per)) continue;
          extendedCategoryNode.setPermission(per, new String[]{PermissionType.ADD_NODE, PermissionType.REMOVE, PermissionType.SET_PROPERTY});
          newModerators.add(per);
        }
      }
      // set only read permission for normal users who are not administrator or moderator.
      for(String oldPer : getAllPermissionOfCategoryNode(categoryNode)){
        if(!newModerators.contains(oldPer)){
          extendedCategoryNode.removePermission(oldPer, PermissionType.ADD_NODE);
          extendedCategoryNode.removePermission(oldPer, PermissionType.REMOVE);
          extendedCategoryNode.removePermission(oldPer, PermissionType.SET_PROPERTY);
          extendedCategoryNode.removePermission(oldPer, PermissionType.CHANGE_PERMISSION);
          extendedCategoryNode.setPermission(oldPer, new String[]{PermissionType.READ});
        }
      }
    }
  }
  
  /**
   * Adds the.
   * 
   * @param portalName the portal name
   * @param categoryConfig the category config
   * @param sessionProvider the session provider
   */
  public void add(SessionProvider sessionProvider, String portalName, NewsletterCategoryConfig categoryConfig) {
    log.info("Trying to add category " + categoryConfig.getName());
    Session session = null;
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoriesNode = (Node)session.getItem(categoryPath);
      Node categoryNode = categoriesNode.addNode(categoryConfig.getName(), NewsletterConstant.CATEGORY_NODETYPE);
      categoryNode.addNode("Templates", "nt:unstructured");
      ExtendedNode extendedCategoryNode = ExtendedNode.class.cast(categoryNode);
      extendedCategoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE, categoryConfig.getTitle());
      extendedCategoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION, categoryConfig.getDescription());
      this.updatePermissionForCategoryNode(categoryNode, categoryConfig, true);
      session.save();
    } catch(Exception e) {
      e.printStackTrace();
      //log.error("Add category " + categoryConfig.getName() + " failed because of: ", e.fillInStackTrace());
    } finally {
      if (session != null) session.logout();
      sessionProvider.close();
    }
  }
  
  /**
   * Edits the.
   * 
   * @param portalName the portal name
   * @param categoryConfig the category config
   * @param sessionProvider the session provider
   */
  public void edit(SessionProvider sessionProvider, String portalName, NewsletterCategoryConfig categoryConfig) {
    log.info("Trying to edit category " + categoryConfig.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(categoryPath)).getNode(categoryConfig.getName());
      categoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION, categoryConfig.getDescription());
      categoryNode.setProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE, categoryConfig.getTitle());
      this.updatePermissionForCategoryNode(categoryNode, categoryConfig, false);
      session.save();
      session.logout();
    } catch (Exception e) {
      e.printStackTrace();
      //log.info("Edit category " + categoryConfig.getName() + " failed because of ", e.fillInStackTrace());
    }finally{
      sessionProvider.close();
    }
  }
  
  /**
   * Delete.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param sessionProvider the session provider
   */
  public void delete(SessionProvider sessionProvider, String portalName, String categoryName) {
    log.info("Trying to delete category " + categoryName);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(categoryPath)).getNode((categoryName));
      categoryNode.remove();
      session.save();
    } catch (Exception e) {
      log.error("Delete category " + categoryName + " failed because of ", e.fillInStackTrace());
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
  public NewsletterCategoryConfig getCategoryByName(SessionProvider sessionProvider, String portalName, String categoryName) throws Exception{
  	try{ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String categoryPath = NewsletterConstant.generateCategoryPath(portalName);
      Node categoriesNode = (Node)session.getItem(categoryPath);
      return getCategoryFromNode(categoriesNode.getNode(categoryName));
  	} catch(Exception ex){
  	  log.error("Error when getCategoryByName: " + ex.fillInStackTrace());
  	}
  	return null;
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
          log.error("Get category " + nodeIterator.nextNode().getName() + " failed because of ", ex.fillInStackTrace());
        }
      }
  	}catch(Exception e){
  	  log.error("Get list categories  failed because of ", e.fillInStackTrace());
	  }
    return listCategories;
  }
}
