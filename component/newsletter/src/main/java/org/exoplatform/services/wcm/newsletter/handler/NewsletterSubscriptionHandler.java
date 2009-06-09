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
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterSubscriptionHandler {

  private static Log log = ExoLogger.getLogger(NewsletterSubscriptionHandler.class);
  private RepositoryService repositoryService;
  private String repository;
  private String workspace;
  
  public NewsletterSubscriptionHandler(String repository, String workspace) {
    repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer()
    .getComponentInstanceOfType(RepositoryService.class);
    this.repository = repository;
    this.workspace = workspace;
  }
  
  public void add(SessionProvider sessionProvider, String portalName,
                  NewsletterSubscriptionConfig subscription) throws Exception {
   
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
      
      session.save();
    } catch (Exception e) {
      log.error("Add subcription " + subscription.getName() + " failed because of " + e.getMessage());
    }
  }
  
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
      
      categoryNode.save();
    } catch (Exception e) {
      log.error("Edit subcription " + subscription.getName() + " failed because of " + e.getMessage());
    }
  }

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
      log.error("Delete subcription " + subscription.getName() + " failed because of " + e.getMessage());
    }
  }
  
  public List<NewsletterSubscriptionConfig> getSubscriptionsByCategory(SessionProvider sessionProvider,
                                                                       String portalName, String categoryName)throws Exception{
    
    List<NewsletterSubscriptionConfig> listSubscriptions = new ArrayList<NewsletterSubscriptionConfig>();

    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName);
    Node categoryNode = ((Node)session.getItem(path)).getNode(categoryName);
    NodeIterator nodeIterator = categoryNode.getNodes();
    Node subscriptionNode = null;
    NewsletterSubscriptionConfig subscriptionConfig = null;
    while(nodeIterator.hasNext()){
      subscriptionNode = nodeIterator.nextNode();
      subscriptionConfig = new NewsletterSubscriptionConfig();
      subscriptionConfig.setName(subscriptionNode.getName());
      subscriptionConfig.setTitle(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE).getString());      
      subscriptionConfig.setDescription(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION).getString());
      subscriptionConfig.setCategoryName(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME).getString());
      listSubscriptions.add(subscriptionConfig);
    }

    return listSubscriptions;
  }
  
  public NewsletterSubscriptionConfig getSubscriptionsByName(SessionProvider sessionProvider,String portalName, String categoryName, String subCriptionName) throws Exception{
    NewsletterSubscriptionConfig subscription = new NewsletterSubscriptionConfig();

    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName);
    Node categoryNode = ((Node)session.getItem(path)).getNode(categoryName);
    NodeIterator nodeIterator = categoryNode.getNodes();
    Node subscriptionNode = null;
    while(nodeIterator.hasNext()){
      
      subscriptionNode = nodeIterator.nextNode();
      if (subCriptionName.equals(subscriptionNode.getName())) {
        
        subscription.setName(subscriptionNode.getName());
        subscription.setTitle(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE).getString());
        subscription.setDescription(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION).getString());
        subscription.setCategoryName(subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME).getString());
      }   
    }

    return subscription;
  }
}
