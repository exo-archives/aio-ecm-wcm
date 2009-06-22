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
package org.exoplatform.services.wcm.newsletter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.watch.impl.MessageConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterEntryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterPublicUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;
import org.exoplatform.services.wcm.utils.SQLQueryBuilder;
import org.exoplatform.services.wcm.utils.AbstractQueryBuilder.LOGICAL;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterManagerService {
  
  private NewsletterCategoryHandler categoryHandler;
  private NewsletterSubscriptionHandler subscriptionHandler;
  private NewsletterEntryHandler entryHandler;
  private NewsletterTemplateHandler templateHandler;
  private NewsletterManageUserHandler manageUserHandler;
  private NewsletterPublicUserHandler publicUserHandler;
  private String repository;
  private String workspace;
  private RepositoryService repositoryService;
  private static Log log = ExoLogger.getLogger(NewsletterManagerService.class);

  public NewsletterManagerService(InitParams initParams) {
    log.info("Starting NewsletterManagerService ... ");
    repository = initParams.getValueParam("repository").getValue();
    workspace = initParams.getValueParam("workspace").getValue();
    
    categoryHandler = new NewsletterCategoryHandler(repository, workspace);
    subscriptionHandler = new NewsletterSubscriptionHandler(repository, workspace);
    entryHandler = new NewsletterEntryHandler(repository, workspace);
    templateHandler = new NewsletterTemplateHandler(repository, workspace);
    manageUserHandler = new NewsletterManageUserHandler(repository, workspace);
    publicUserHandler = new NewsletterPublicUserHandler(repository, workspace);
    
    repositoryService = (RepositoryService) ExoContainerContext
    .getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
  }          
  
  public NewsletterCategoryHandler getCategoryHandler() {
    return categoryHandler;
  }
  
  public NewsletterSubscriptionHandler getSubscriptionHandler() {
    return subscriptionHandler;
  }
  
  public NewsletterEntryHandler getEntryHandler() {
    return entryHandler;
  }
  
  public NewsletterTemplateHandler getTemplateHandler() {
    return templateHandler;
  }
  
  public NewsletterManageUserHandler getManageUserHandler() {
    return manageUserHandler;
  }
  
  public NewsletterPublicUserHandler getPublicUserHandler() {
    return publicUserHandler;
  }
  
  /*public void sendNewsletter() {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    MailService mailService = 
      (MailService)container.getComponentInstanceOfType(MailService.class) ;

    MessageConfig messageConfig = new MessageConfig();
    String receiver = "ngoc.aptech@gmail.com";
    Message message = createMessage(receiver,messageConfig) ;  
    try {
        mailService.sendMessage(message) ; 
      }catch (Exception e) {
        System.out.println("===> Exeption when send message to: " + message.getTo());
        e.printStackTrace() ;
        
      }      
  }*/
  
  public void sendNewsletter() throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, manageableRepository);
    Property subscribedUserProperty ;

    Calendar toDate = Calendar.getInstance();

    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    MailService mailService = 
      (MailService)container.getComponentInstanceOfType(MailService.class) ;
    
    Message message = null;

    QueryManager queryManager = session.getWorkspace().getQueryManager();
    SQLQueryBuilder queryBuilder = new SQLQueryBuilder();

    queryBuilder.selectTypes(null);
    queryBuilder.fromNodeTypes(new String [] {NewsletterConstant.ENTRY_NODETYPE});
    queryBuilder.like(NewsletterConstant.ENTRY_PROPERTY_STATUS, "awaiting", null);
    queryBuilder.beforeDate(NewsletterConstant.ENTRY_PROPERTY_DATE, ISO8601.format(toDate), LOGICAL.AND);

    //Create queryBuilder.
    String sqlQuery = queryBuilder.createQueryStatement();
    
    System.out.println("\n\n\n\n----------->sqlQuery:" + sqlQuery);
    
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator nodeIterator = queryResult.getNodes();

    System.out.println("Query rerult is : " + nodeIterator.getSize());
    List<String> listEmailAddress = null;
    String receiver = "";
    for (;nodeIterator.hasNext();) {
      Node newsletterEntry = nodeIterator.nextNode();
      
      Node subscriptionNode = newsletterEntry.getParent();

      subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
      listEmailAddress = convertValuesToArray(subscribedUserProperty.getValues());

      System.out.println("After convert : " + nodeIterator.getSize());
      if (listEmailAddress.size() > 0) {
        message = new Message() ;
        message.setTo(listEmailAddress.get(0));
        for (int i = 0; i < listEmailAddress.size(); i ++) {
          
          receiver += listEmailAddress.get(i + 1) + ",";
        }
  
        message.setCC(receiver);
        message.setSubject("Test phat!!!") ;
        message.setBody("Hi Ngoc, you receive this email because i'm testing") ;
        message.setMimeType("1") ;
        
        try {
          
          mailService.sendMessage(message);
        }catch (Exception e) {
          
          e.printStackTrace();
        }
      }
    }   
  }

  public void sendVerificationMail(String email) throws RepositoryException, RepositoryConfigurationException {
     
  }

  private Message createMessage(String receiver, MessageConfig messageConfig) {
    Message message = new Message() ;
    message.setFrom("root@exoplatform.com") ;
    message.setTo(receiver) ;
    message.setSubject("Test phat!!!") ;
    message.setBody("Hi Ngoc, you receive this email because i'm testing") ;
    message.setMimeType(messageConfig.getMimeType()) ;

    return message ;
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
  
}
