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
  public void add(String portalName,
                  NewsletterSubscriptionConfig subscription, SessionProvider sessionProvider) throws Exception {
   
    log.info("Trying to add subcription " + subscription.getName());
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String path = NewsletterConstant.generateCategoryPath(portalName);
      Node categoryNode = ((Node)session.getItem(path)).getNode(subscription.getCategoryName());
      Node subscriptionNode = categoryNode.addNode(subscription.getName(), NewsletterConstant.SUBSCRIPTION_NODETYPE);
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE, subscription.getTitle());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION, subscription.getDescription());
      subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME, subscription.getCategoryName());

      ExtendedNode extSubscriptionNode = (ExtendedNode)subscriptionNode ;
      if (extSubscriptionNode.canAddMixin("exo:privilegeable")) extSubscriptionNode.addMixin("exo:privilegeable");
      String[] arrayPers = {PermissionType.READ, PermissionType.SET_PROPERTY} ;
      extSubscriptionNode.setPermission("any", arrayPers) ;
      
      session.save();
    } catch (Exception e) {
      log.error("Add subcription " + subscription.getName() + " failed because of " + e.getMessage());
      throw e;
    }
  }
  
  /**
   * Edits the.
   * 
   * @param portalName the portal name
   * @param subscription the subscription
   * @param sessionProvider the session provider
   */
  public void edit(String portalName, NewsletterSubscriptionConfig subscription, SessionProvider sessionProvider) {
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
      
      categoryNode.save();
    } catch (Exception e) {
      log.error("Edit subcription " + subscription.getName() + " failed because of " + e.getMessage());
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
  public void delete(String portalName,
                     String categoryName, NewsletterSubscriptionConfig subscription, SessionProvider sessionProvider) {
    
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
      log.error("Delete subcription " + subscription.getName() + " failed because of " + e.getMessage());
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
                                                                       String portalName,
                                                                       String categoryName,
                                                                       SessionProvider sessionProvider)
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
        log.error("Error when get subcriptions by category " + categoryName + " failed because of " + ex.getMessage());
      }
    }
    return listSubscriptions;
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
                                                                           String portalName,
                                                                           String userEmail,
                                                                           SessionProvider sessionProvider)
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
        log.error("getSubscriptionIdsByPublicUser() failed because of " + ex.getMessage());
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
                                                             String portalName,
                                                             String categoryName,
                                                             String subCriptionName,
                                                             SessionProvider sessionProvider)
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
                                            String portalName,
                                            String categoryName,
                                            String subScriptionName,
                                            SessionProvider sessionProvider)
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
}
