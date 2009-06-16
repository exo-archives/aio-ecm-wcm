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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterPublicUserHandler {

  private static Log log = ExoLogger.getLogger(NewsletterPublicUserHandler.class);
  private RepositoryService repositoryService;
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  private String repository;
  private String workspace;
  
  public NewsletterPublicUserHandler(String repository, String workspace) {
    repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }
  
  protected void updateSubscriptions(Session session, List<String> listCategorySubscription, String portalName, String userMail) throws Exception{
    ValueFactory valueFactory = session.getValueFactory();
    String categoryName ;
    String subscriptionName ;
    Node subscriptionNode ;
    Property subscribedUserProperty ;
    List<Value> subscribedUsers = new ArrayList<Value>() ;
    String categryHomePath = NewsletterConstant.generateCategoryPath(portalName);
    for (String categoryAndSubscription : listCategorySubscription) {
      categoryName = categoryAndSubscription.split("#")[0];
      subscriptionName = categoryAndSubscription.split("#")[1];
      try{
        subscriptionNode = Node.class.cast(session.getItem(categryHomePath + "/" + categoryName + "/" + subscriptionName));
        if(subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)){
          subscribedUsers = new ArrayList<Value>() ;
          subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
          subscribedUsers.addAll(Arrays.asList(subscribedUserProperty.getValues()));
          subscribedUsers.add(valueFactory.createValue(userMail));
          subscribedUserProperty.setValue(subscribedUsers.toArray(new Value[]{}));
        }else{
          subscriptionNode.setProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER, new String[]{userMail});
        }
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
    session.save();
  }
  
  protected void clearEmailInSubscription(String email){
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      String sqlQuery = "select * from " + NewsletterConstant.SUBSCRIPTION_NODETYPE + " where " + NewsletterConstant.SUBSCRIPTION_PROPERTY_USER + " like '%" + email + "%'";
      Query query = queryManager.createQuery(sqlQuery, Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator nodeIterator = queryResult.getNodes();
      
      // Clean user's node
      for (;nodeIterator.hasNext();) {
        Node subscriptionNode = nodeIterator.nextNode();
        Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
        List<Value> oldSubscribedUsers = Arrays.asList(subscribedUserProperty.getValues());
        List<Value> newSubscribedUsers = new ArrayList<Value>();
        for (Value value: oldSubscribedUsers) {
          String subscribedUserMail = value.getString();
          if (email.equals(subscribedUserMail)) {
            continue;
          }
          newSubscribedUsers.add(value);
        }
        subscribedUserProperty.setValue(newSubscribedUsers.toArray(new Value[newSubscribedUsers.size()]));
      }
      session.save();
    } catch (Exception e) {
      log.error("Update user's subscription for user " + email + " failed because of " + e.getMessage());
    }
  }

  public void subscribe(String portalName, String userMail, List<String> listCategorySubscription, SessionProvider sessionProvider) {
    log.info("Trying to subscribe user " + userMail);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      // add new user email into users node
      NewsletterManageUserHandler manageUserHandler = new NewsletterManageUserHandler(repository, workspace);
      manageUserHandler.add(portalName, userMail, sessionProvider);
      
      // update email into subscription
      updateSubscriptions(session, listCategorySubscription, portalName, userMail);
      //Send a verification code to user's email to validate and to get link
    } catch (Exception e) {
      log.error("Subscribe user " + userMail + " failed because of " + e.getMessage());
    }
  }
  
  public void forgetEmail(String portalName, String email){
    log.info("Trying to update user's subscriptions for user " + email);
    try {
      clearEmailInSubscription(email);
      //  update for users node
      NewsletterManageUserHandler manageUserHandler = new NewsletterManageUserHandler(repository, workspace);
      manageUserHandler.delete(portalName, email);
    } catch (Exception e) {
      log.error("Update user's subscription for user " + email + " failed because of " + e.getMessage());
    }
  }

  // Pattern for categoryAndSubscriptions: categoryAAA#subscriptionBBB
  public void updateSubscriptions(String portalName, String email, List<String> categoryAndSubscriptions) {
    log.info("Trying to update user's subscriptions for user " + email);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      
      clearEmailInSubscription(email);
      
      // Update new data
      this.updateSubscriptions(session, categoryAndSubscriptions, portalName, email);
      
      // Get current subscriptions which user subscribed (by query), compare with input subscriptions
      // to get which subscription user remove, which subscription user add, then update reference
    } catch (Exception e) {
      log.error("Update user's subscription for user " + email + " failed because of " + e.getMessage());
    }
  }

}