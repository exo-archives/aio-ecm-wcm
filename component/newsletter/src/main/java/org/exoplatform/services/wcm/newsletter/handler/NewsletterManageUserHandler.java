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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterManageUserHandler {

  private static Log log = ExoLogger.getLogger(NewsletterManageUserHandler.class);
  private RepositoryService repositoryService;
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  private String repository;
  private String workspace;
  
  public NewsletterManageUserHandler(String repository, String workspace) {
    repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }
  
  public void add(String portalName, String userMail, SessionProvider sessionProvider) {
    log.info("Trying to add user " + userMail);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String userPath = NewsletterConstant.generateUserPath(portalName);
      Node userFolderNode = (Node)session.getItem(userPath);
      Node userNode = userFolderNode.addNode(userMail, NewsletterConstant.USER_NODETYPE);
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_MAIL, userMail);
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_BANNED, false);
      session.save();
    } catch (Exception e) {
      log.error("Add user " + userMail + " failed because of " + e.getMessage());
    }
  }
  
  public void ban(String portalName, String userMail) {
    log.info("Trying to ban user " + userMail);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      String userPath = NewsletterConstant.generateUserPath(portalName);
      Node userFolderNode = (Node)session.getItem(userPath);
      Node userNode = userFolderNode.getNode(userMail);
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_BANNED, true);
      session.save();
    } catch (Exception e) {
      log.error("Ban user " + userMail + " failed because of " + e.getMessage());
    }
  }
  
  public void delete(String portalName, String userMail) {
    log.info("Trying to delete user " + userMail);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      String userPath = NewsletterConstant.generateUserPath(portalName);
      Node userFolderNode = (Node)session.getItem(userPath);
      Node userNode = userFolderNode.getNode(userMail);
      userNode.remove();
      
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      String sqlQuery = "select * from " + NewsletterConstant.SUBSCRIPTION_NODETYPE + " where " + NewsletterConstant.SUBSCRIPTION_PROPERTY_USER + " like '%" + userMail + "%'";
      Query query = queryManager.createQuery(sqlQuery, Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator nodeIterator = queryResult.getNodes();
      for (;nodeIterator.hasNext();) {
        Node subscriptionNode = nodeIterator.nextNode();
        if (subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)) {
          Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
          List<Value> oldSubscribedUsers = Arrays.asList(subscribedUserProperty.getValues());
          List<Value> newSubscribedUsers = new ArrayList<Value>();
          for (Value value: oldSubscribedUsers) {
            String subscribedUserMail = value.getString();
            if (userMail.equals(subscribedUserMail)) {
              continue;
            }
            newSubscribedUsers.add(value);
          }
          subscribedUserProperty.setValue(newSubscribedUsers.toArray(new Value[newSubscribedUsers.size()]));
        }
      }
      session.save();
    } catch (Exception e) {
      log.error("Delete user " + userMail + " failed because of " + e.getMessage());
    }
  }

  public List<String> getUsersBySubscription(String portalName, String categoryName, String subscriptionName) {
    log.info("Trying to get list user by subscription " + portalName + "/" + categoryName + "/" + subscriptionName);
    try {
      List<String> subscribedUsers = new ArrayList<String>();
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      String subscriptionPath = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subscriptionName;
      Node subscriptionNode = Node.class.cast(session.getItem(subscriptionPath));
      if (subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)) {
        Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
        Value subscribedUserValues[] = subscribedUserProperty.getValues();
        for (Value value : subscribedUserValues) {
          subscribedUsers.add(value.getString());
        }
      }
      return subscribedUsers;
    } catch (Exception e) {
      log.error("Get list user by subscription " + portalName + "/" + categoryName + "/" + subscriptionName + " failed because of " + e.getMessage());
    }
    return null;
  }
  
  public int getQuantityUserBySubscription(String portalName, String categoryName, String subscriptionName) {
    log.info("Trying to get user's quantity by subscription " + portalName + "/" + categoryName + "/" + subscriptionName);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      String subscriptionPath = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subscriptionName;
      Node subscriptionNode = Node.class.cast(session.getItem(subscriptionPath));
      int countUser = 0;
      if (subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)) {
        Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
        countUser = subscribedUserProperty.getValues().length;
      }
      return countUser;
    } catch (Exception e) {
      log.error("Get user's quantity by subscription " + portalName + "/" + categoryName + "/" + subscriptionName + " failed because of " + e.getMessage());
    }
    return 0;
  }
  
}
