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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
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
    repositoryService = (RepositoryService) ExoContainerContext
      .getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class
      .cast(ExoContainerContext.getCurrentContainer()
            .getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }
  
  private NewsletterUserConfig getUserFromNode(Node userNode) throws Exception{
    NewsletterUserConfig user = new NewsletterUserConfig();
    user.setMail(userNode.getProperty(NewsletterConstant.USER_PROPERTY_MAIL).getString());
    user.setBanned(userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean());
    return user;
  }
  
  private List<String> convertValuesToArray(Value[] values){
    List<String> listString = new ArrayList<String>();
    for(Value value : values){
      try {
        listString.add(value.getString());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return listString;
  }
  
  public List<String> getAllAdministrator(String portalName){
    try{
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      Node categoriesNode = (Node) session.getItem(NewsletterConstant.generateCategoryPath(portalName));
      if(categoriesNode.hasProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR))
        return convertValuesToArray(categoriesNode.getProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR).getValues());
    }catch(Exception ex){
      ex.printStackTrace();
    }
    return new ArrayList<String>();
  }
  
  public List<String> getAllModerators(String portalName){
    try{
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      Node categoriesNode = (Node) session.getItem(NewsletterConstant.generateCategoryPath(portalName));
      if(categoriesNode.hasProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR))
        return convertValuesToArray(categoriesNode.getProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR).getValues());
    }catch(Exception ex){
      ex.printStackTrace();
    }
    return new ArrayList<String>();
  }
  
  public void addAdministrator(String portalName, String userId) throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
    Node categoriesNode = (Node) session.getItem(NewsletterConstant.generateCategoryPath(portalName));
    List<String> listUsers = new ArrayList<String>();
    if(categoriesNode.hasProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR))
      listUsers.addAll(convertValuesToArray(categoriesNode.getProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR).getValues()));
    if(listUsers.contains(userId)) return;
    listUsers.add(userId);
    categoriesNode.setProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR, listUsers.toArray(new String[]{}));
    session.save();
  }
  
  public void deleteUserAddministrator(String portalName, String userId) throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
    Node categoriesNode = (Node) session.getItem(NewsletterConstant.generateCategoryPath(portalName));
    List<String> listUsers = new ArrayList<String>();
    if(categoriesNode.hasProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR))
      listUsers.addAll(convertValuesToArray(categoriesNode.getProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR).getValues()));
    listUsers.remove(userId);
    categoriesNode.setProperty(NewsletterConstant.CATEGORIES_PROPERTY_ADDMINISTRATOR, listUsers.toArray(new String[]{}));
    session.save();
  }
  
  public Node add(String portalName, String userMail, SessionProvider sessionProvider) {
    log.info("Trying to add user " + userMail);
    Node userNode = null;
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String userPath = NewsletterConstant.generateUserPath(portalName);
      Node userFolderNode = (Node)session.getItem(userPath);
      userNode = userFolderNode.addNode(userMail, NewsletterConstant.USER_NODETYPE);
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_MAIL, userMail);
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_BANNED, false);
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_VALIDATION_CODE, "PublicUser" + IdGenerator.generate() );
      session.save();
    } catch (Exception e) {
      log.error("Add user " + userMail + " failed because of " + e.getMessage());
      e.printStackTrace();
    }
    return userNode;
  }
  
  private Node getUserNodeByEmail(String portalName, String userMail, Session session) throws Exception{
    String userPath = NewsletterConstant.generateUserPath(portalName);
    Node userFolderNode = (Node)session.getItem(userPath);
    try{
      return userFolderNode.getNode(userMail);
    }catch(Exception ex){
      return null;
    }
  }
  
  public void changeBanStatus(String portalName, String userMail, boolean isBanClicked) {
    log.info("Trying to ban/unban user " + userMail);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
      if(sessionProvider == null) sessionProvider = SessionProvider.createSystemProvider();
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      Node userNode = getUserNodeByEmail(portalName, userMail, session);
      if (userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean() == isBanClicked) return;
      userNode.setProperty(NewsletterConstant.USER_PROPERTY_BANNED, 
                           !userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean());
      session.save();
    } catch (Exception e) {
    	e.printStackTrace();
      log.error("Ban/UnBan user " + userMail + " failed because of " + e.getMessage());
    }
  }
  
  public void delete(String portalName, String userMail) {
    log.info("Trying to delete user " + userMail);
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
      if(sessionProvider == null) sessionProvider = SessionProvider.createSystemProvider();
      Session session = sessionProvider.getSession(workspace, manageableRepository);
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

  public List<NewsletterUserConfig> getUsers(String portalName, String categoryName, String subscriptionName) throws Exception{
    List<NewsletterUserConfig> listUsers = new ArrayList<NewsletterUserConfig>();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
    if(sessionProvider == null) sessionProvider = SessionProvider.createSystemProvider();
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    String userPath = NewsletterConstant.generateUserPath(portalName);
    Node userHomeNode = (Node)session.getItem(userPath);
    if(categoryName == null && subscriptionName == null){ // get all user email
      NodeIterator nodeIterator = userHomeNode.getNodes();
      while(nodeIterator.hasNext()){
        listUsers.add(getUserFromNode(nodeIterator.nextNode()));
      }
    } else{
      List<String> listEmail = new ArrayList<String>();
      if(categoryName != null && subscriptionName == null){ // get user of category
        Node categoryNode = (Node)session.getItem(NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName);
        NodeIterator nodeIterator = categoryNode.getNodes();
        Node subscriptionNode;
        Value subscribedUserValues[];
        while(nodeIterator.hasNext()){
          subscriptionNode = nodeIterator.nextNode();
          if(subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)){
            Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
            subscribedUserValues = subscribedUserProperty.getValues();
            for (Value value : subscribedUserValues) {
              if(!listEmail.contains(value.getString())) listEmail.add(value.getString());
            }
          }
        }
      }else{ // get user of subscription
        listEmail = getUsersBySubscription(portalName, categoryName, subscriptionName, session);
      }
      // convert form email to userConfig
      for(String email : listEmail){
        listUsers.add(getUserFromNode((userHomeNode.getNode(email))));
      }
    }
    return listUsers;
  }

  private List<String> getUsersBySubscription(String portalName, String categoryName, String subscriptionName, Session session) {
    log.info("Trying to get list user by subscription " + portalName + "/" + categoryName + "/" + subscriptionName);
    List<String> subscribedUsers = new ArrayList<String>();
    try {
      String subscriptionPath = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subscriptionName;
      Node subscriptionNode = Node.class.cast(session.getItem(subscriptionPath));
      if (subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)) {
        Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
        Value subscribedUserValues[] = subscribedUserProperty.getValues();
        for (Value value : subscribedUserValues) {
          subscribedUsers.add(value.getString());
        }
      }
    } catch (Exception e) {
      log.error("Get list user by subscription " + portalName + "/" + categoryName + "/" + subscriptionName + " failed because of " + e.getMessage());
    }
    return subscribedUsers;
  }
  
  public int getQuantityUserBySubscription(String portalName, String categoryName, String subscriptionName) {
    log.info("Trying to get user's quantity by subscription " + portalName + "/" + categoryName + "/" + subscriptionName);
    int countUser = 0;
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
      if(sessionProvider == null) sessionProvider = SessionProvider.createSystemProvider();
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      String subscriptionPath = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subscriptionName;
      Node subscriptionNode = Node.class.cast(session.getItem(subscriptionPath));
      if (subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)) {
        Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
        countUser = subscribedUserProperty.getValues().length;
      }
    } catch (Exception e) {
      log.error("Get user's quantity by subscription " + portalName + "/" + categoryName + "/" + subscriptionName + " failed because of " + e.getMessage());
    }
    return countUser;
  }
  
  public boolean checkExistedEmail(String portalName, String email) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
      if(sessionProvider == null) sessionProvider = SessionProvider.createSystemProvider();
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      Node userNode = getUserNodeByEmail(portalName, email, session);
      if(userNode != null){
        return true;
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  
}
