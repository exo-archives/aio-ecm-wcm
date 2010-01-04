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
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009
 */
public class NewsletterSubscriptionHandler {

  /** The log. */
  private static Log log = ExoLogger.getLogger(NewsletterSubscriptionHandler.class);
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
  /** The repository. */
  private String repository;
  
  /** The workspace. */
  private String workspace;
  
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
   * @param subscriptionNode    node which is will be updated
   * @param subscriptionConfig  Category Object
   * @param isAddNew        is <code>True</code> if is add new category node and <code>False</code> if only update
   * @throws Exception      The Exception
   */
  private void updatePermissionForSubscriptionNode(Node subscriptionNode, NewsletterSubscriptionConfig subscriptionConfig, 
                                                   boolean isAddNew) throws Exception{
    ExtendedNode extendedSubscriptionNode = ExtendedNode.class.cast(subscriptionNode);
    if (extendedSubscriptionNode.canAddMixin("exo:privilegeable") || extendedSubscriptionNode.isNodeType("exo:privilegeable")) {
      if(extendedSubscriptionNode.canAddMixin("exo:privilegeable"))
        extendedSubscriptionNode.addMixin("exo:privilegeable");
      List<String> newRedactors = new ArrayList<String>();
      if(subscriptionConfig.getRedactor() != null && subscriptionConfig.getRedactor().trim().length() > 0)
        newRedactors.addAll(Arrays.asList(subscriptionConfig.getRedactor().split(",")));
      
      // get all administrator of newsletter and moderator of category which contain this subscription
      Node categoryNode = subscriptionNode.getParent();
      Node categoriesNode = categoryNode.getParent();
      List<String> listModerators = NewsletterConstant.getAllPermissionOfNode(categoryNode);
      List<String> listAddministrators = new ArrayList<String>();
      if(categoriesNode.hasProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR)) {
        Value[] values = categoriesNode.getProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR).getValues();
        listAddministrators = convertValuesToArray(values);
      }
      //listAddministrators.add(PublicationUtil.getServices(UserACL.class).getSuperUser());// add supper user into list administrator
      listAddministrators.addAll(listModerators);
      
      // Set permission is all for Redactors
      String[] permissions = new String[]{PermissionType.READ, PermissionType.ADD_NODE, PermissionType.REMOVE, PermissionType.SET_PROPERTY};
      ExtendedNode categoryExtend = ExtendedNode.class.cast(categoryNode);
      for(String redactor : newRedactors){
        extendedSubscriptionNode.setPermission(redactor, PermissionType.ALL);
        // Set read permission in category which contain subscription for this redactor
        if(!listModerators.contains(redactor))categoryExtend.setPermission(redactor, permissions);
      }
      
      // Set permission is addNode, remove and setProperty for administrators
      for(String admin : listAddministrators){
        if(newRedactors.contains(admin)) continue;
        extendedSubscriptionNode.setPermission(admin, permissions);
        newRedactors.add(admin);
      }
      
      permissions = new String[]{PermissionType.READ, PermissionType.SET_PROPERTY}; // permission for normal users
      // set permission for any user when add new
      if(isAddNew){
        extendedSubscriptionNode.setPermission("any", permissions);
      }
      
      // set only read permission for normal users who are not administrator ,moderator or redactor.
      List<String> allPermissions = NewsletterConstant.getAllPermissionOfNode(subscriptionNode);
      if(allPermissions != null && allPermissions.size() > 0){
        for(String oldPer : allPermissions){
          if(!newRedactors.contains(oldPer)){
            extendedSubscriptionNode.removePermission(oldPer, PermissionType.ADD_NODE);
            extendedSubscriptionNode.removePermission(oldPer, PermissionType.REMOVE);
            extendedSubscriptionNode.removePermission(oldPer, PermissionType.CHANGE_PERMISSION);
            extendedSubscriptionNode.setPermission(oldPer, permissions);
          }
        }
      }
      
    }
  }
  
  /**
   * Instantiates a new newsletter subscription handler.
   * 
   * @param repository the repository
   * @param workspace the workspace
   */
  public NewsletterSubscriptionHandler(String repository, String workspace) {
    repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer()
      .getComponentInstanceOfType(RepositoryService.class);
    this.repository = repository;
    this.workspace = workspace;
  }
  
  /**
   * Gets the subscription form node.
   * 
   * @param subscriptionNode the subscription node
   * 
   * @return the subscription form node
   * 
   * @throws Exception the exception
   */
  private NewsletterSubscriptionConfig getSubscriptionFormNode(Node subscriptionNode) throws Exception{
    NewsletterSubscriptionConfig subscriptionConfig = new NewsletterSubscriptionConfig();
    subscriptionConfig.setName(subscriptionNode.getName());
    subscriptionConfig.setTitle(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE).getString());      
    if(subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION))
      subscriptionConfig.setDescription(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION).getString());
    subscriptionConfig.setCategoryName(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME).getString());
    // get permission for this category
    String permission = "";
    for(String per : NewsletterConstant.getAllPermissionOfNode(subscriptionNode)){
      if(permission.length() > 0) permission += ",";
      permission += per;
    }
    subscriptionConfig.setRedactor(permission);
    return subscriptionConfig;
  }
  
  /**
   * Adds the.
   * 
   * @param sessionProvider the session provider
   * @param portalName the portal name
   * @param subscription the subscription
   * 
   * @throws Exception the exception
   */
  public void add(SessionProvider sessionProvider, String portalName, NewsletterSubscriptionConfig subscription) throws Exception {
    log.info("Trying to add subcription " + subscription.getName());
    Session session = null;
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      session = sessionProvider.getSession(workspace, manageableRepository);
      String path = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(path)).getNode(subscription.getCategoryName());
      Node subscriptionNode = categoryNode.addNode(subscription.getName(), NewsletterConstant.SUBSCRIPTION_NODETYPE);
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE, subscription.getTitle());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION, subscription.getDescription());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME, subscription.getCategoryName());

      this.updatePermissionForSubscriptionNode(subscriptionNode, subscription, true);
      session.save();
    } catch (Exception e) {
      log.error("Add subcription " + subscription.getName() + " failed because of ", e.fillInStackTrace());
      throw e;
    } finally {
      if (session != null) session.logout();
    }
  }
  
  /**
   * Edits the.
   * 
   * @param portalName the portal name
   * @param subscription the subscription
   * @param sessionProvider the session provider
   */
  public void edit(SessionProvider sessionProvider, String portalName, NewsletterSubscriptionConfig subscription) {
    log.info("Trying to edit subcription " + subscription.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String path = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(path)).getNode(subscription.getCategoryName());
      Node subscriptionNode = categoryNode.getNode(subscription.getName());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE, subscription.getTitle());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION, subscription.getDescription());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME, subscription.getCategoryName());
      this.updatePermissionForSubscriptionNode(subscriptionNode, subscription, false);
      categoryNode.save();
    } catch (Exception e) {
      log.error("Edit subcription " + subscription.getName() + " failed because of ", e.fillInStackTrace());
    }
  }

  /**
   * Delete.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param subscription the subscription
   * @param sessionProvider the session provider
   */
  public void delete(SessionProvider sessionProvider, String portalName,
                     String categoryName, NewsletterSubscriptionConfig subscription) {
    
    log.info("Trying to delete subcription " + subscription.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String path = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(path)).getNode(categoryName);
      Node subscriptionNode = categoryNode.getNode(subscription.getName());
      subscriptionNode.remove();
      session.save();
    } catch (Exception e) {
      log.error("Delete subcription " + subscription.getName() + " failed because of ", e.fillInStackTrace());
    }
  }
  
  /**
   * Gets the subscriptions by category.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param sessionProvider the session provider
   * 
   * @return the subscriptions by category
   * 
   * @throws Exception the exception
   */
  public List<NewsletterSubscriptionConfig> getSubscriptionsByCategory(
                                                                       SessionProvider sessionProvider, 
                                                                       String portalName,
                                                                       String categoryName)
                                                                       throws Exception{
    
    List<NewsletterSubscriptionConfig> listSubscriptions = new ArrayList<NewsletterSubscriptionConfig>();

    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName);
    Node categoryNode = ((Node)session.getItem(path)).getNode(categoryName);
    NodeIterator nodeIterator = categoryNode.getNodes();
    while(nodeIterator.hasNext()){
      try{
        Node childNode = nodeIterator.nextNode();
        if(!childNode.isNodeType(NewsletterConstant.SUBSCRIPTION_NODETYPE)) continue;
        listSubscriptions.add(getSubscriptionFormNode(childNode));
      }catch(Exception ex){
        log.error("Error when get subcriptions by category " + categoryName + " failed because of ", ex.fillInStackTrace());
      }
    }
    return listSubscriptions;
  }

  public List<NewsletterSubscriptionConfig> getSubscriptionByRedactor(String portalName, String categoryName,
                                                                      String userName, SessionProvider sessionProvider) throws Exception{
    List<NewsletterSubscriptionConfig> listSubs = new ArrayList<NewsletterSubscriptionConfig>();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName);
    List<String> userPermissionMembership = NewsletterConstant.getAllGroupAndMembershipOfCurrentUser(userName);
    List<String> allPermission;
    Node childNode;
    for(NodeIterator nodeIterator = ((Node)session.getItem(path)).getNode(categoryName).getNodes();nodeIterator.hasNext();){
      try{
        childNode = nodeIterator.nextNode();
        if(!childNode.isNodeType(NewsletterConstant.SUBSCRIPTION_NODETYPE)) continue;
        allPermission = NewsletterConstant.getAllPermissionOfNode(childNode);
        if(NewsletterConstant.havePermission(allPermission, userPermissionMembership)) listSubs.add(getSubscriptionFormNode(childNode));
      }catch(Exception ex){
        log.error("Error when get subcriptions by category " + categoryName + " failed because of ", ex.fillInStackTrace());
      }
    }
    return listSubs;
  }
  
  /**
   * Gets the subscription ids by public user.
   * 
   * @param portalName the portal name
   * @param userEmail the user email
   * @param sessionProvider the session provider
   * 
   * @return the subscription ids by public user
   * 
   * @throws Exception the exception
   */
  public List<NewsletterSubscriptionConfig> getSubscriptionIdsByPublicUser(
                                                                           SessionProvider sessionProvider, 
                                                                           String portalName,
                                                                           String userEmail)
                                                                           throws Exception{
    List<NewsletterSubscriptionConfig> listSubscriptions = new ArrayList<NewsletterSubscriptionConfig>();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "select * from " + NewsletterConstant.SUBSCRIPTION_NODETYPE + 
                      " where " + NewsletterConstant.SUBSCRIPTION_PROPERTY_USER + " = '" + userEmail + "'";
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator nodeIterator = queryResult.getNodes();
    while(nodeIterator.hasNext()){
      try{
        listSubscriptions.add(getSubscriptionFormNode(nodeIterator.nextNode()));
      } catch(Exception ex) {
        log.error("getSubscriptionIdsByPublicUser() failed because of ", ex.fillInStackTrace());
      }
    }
    return listSubscriptions;
  }

  /**
   * Gets the subscriptions by name.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param subCriptionName the sub cription name
   * @param sessionProvider the session provider
   * 
   * @return the subscriptions by name
   * 
   * @throws Exception the exception
   */
  public NewsletterSubscriptionConfig getSubscriptionsByName(
                                                             SessionProvider sessionProvider, 
                                                             String portalName,
                                                             String categoryName,
                                                             String subCriptionName)
                                                             throws Exception{
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String path = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(path)).getNode(categoryName);
      try {
        Node subNode = categoryNode.getNode(subCriptionName);
        return getSubscriptionFormNode(subNode);
      } catch (Exception e) {
        log.info("Node name is not found: " + subCriptionName);
        return null;
      }
  }
  
  /**
   * Gets the number of newsletters waiting.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param subScriptionName the sub scription name
   * @param sessionProvider the session provider
   * 
   * @return the number of newsletters waiting
   * 
   * @throws Exception the exception
   */
  public long getNumberOfNewslettersWaiting(
                                            SessionProvider sessionProvider, 
                                            String portalName,
                                            String categoryName,
                                            String subScriptionName)
                                            throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subScriptionName;
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "select * from " + NewsletterConstant.ENTRY_NODETYPE + 
                      " where jcr:path LIKE '" + path + "[%]/%' and " + NewsletterConstant.ENTRY_PROPERTY_STATUS + 
                      " = '" + NewsletterConstant.STATUS_AWAITING + "'";
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator nodeIterator = queryResult.getNodes();
    return nodeIterator.getSize();
  }
  
  /**
   * Get all redactors in all subscriptions of newsletter
   * @param portalName        name of portal
   * @param sessionProvider   The SessionProvider
   * @return                  List of redactor
   * @throws Exception        The exception
   */
  public List<String> getAllRedactor(String portalName, SessionProvider sessionProvider)throws Exception{
    List<String> list = new ArrayList<String>();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "select * from " + NewsletterConstant.SUBSCRIPTION_NODETYPE + 
                      " where jcr:path LIKE '" + path + "[%]/%'";
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    Node subNode;
    for(NodeIterator nodeIterator = queryResult.getNodes(); nodeIterator.hasNext();){
      subNode = nodeIterator.nextNode();
      for(String str : NewsletterConstant.getAllPermissionOfNode(subNode)){
        if(list.contains(str)) continue;
        list.add(str);
      }
    }
    return list;
  }
}
