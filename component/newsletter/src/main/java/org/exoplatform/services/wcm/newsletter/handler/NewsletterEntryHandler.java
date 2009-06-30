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
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.config.NewsletterManagerConfig;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterEntryHandler {

  private static Log log = ExoLogger.getLogger(NewsletterEntryHandler.class);
  private RepositoryService repositoryService;
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  private String repository;
  private String workspace;
  
  public NewsletterEntryHandler(String repository, String workspace) {
    repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    this.repository = repository;
    this.workspace = workspace;
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class
                                          .cast(ExoContainerContext.getCurrentContainer()
                                          .getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
  }
  
  private NewsletterManagerConfig getEntryFromNode(Node entryNode) throws Exception{
    NewsletterManagerConfig newsletterEntryConfig = new NewsletterManagerConfig();
    newsletterEntryConfig.setNewsletterName(entryNode.getName());
    if(entryNode.hasProperty("exo:title"))newsletterEntryConfig.setNewsletterTitle(entryNode.getProperty("exo:title").getString());
    newsletterEntryConfig.setNewsletterSentDate(entryNode.getProperty(NewsletterConstant.ENTRY_PROPERTY_DATE).getDate().getTime());
    newsletterEntryConfig.setStatus(entryNode.getProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS).getString());
    return newsletterEntryConfig;
  }
  
  public void add(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Add new newsletter entry node with node name is the title
      // Set property "exo:newsletterEntryType" is template type
      // Set property "exo:newsletterEntryDate" is sending date
      // Set property "exo:newsletterEntryStatus" is draft
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  public void updateStatus(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Update the status of newsletter, from draft to awaiting or sent, or from awaiting to sent
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  public void edit(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
    } catch (Exception e) {
      // TODO: handle exception
    }
  }
  
  public void delete(String portalName, String categoryName, String subscriptionName, List<String> listIds) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      String path = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subscriptionName;
      Node subscriptionNode = (Node)session.getItem(path);
      Node newsletterNode = null;
      for(String id : listIds){
        newsletterNode = subscriptionNode.getNode(id);
        newsletterNode.remove();
      }
      session.save();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public List<NewsletterManagerConfig> getNewsletterEntriesBySubscription(String portalName, String categoryName, String subscriptionName) throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
    String path = NewsletterConstant.generateCategoryPath(portalName) + "/" + categoryName + "/" + subscriptionName;
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "select * from " + NewsletterConstant.ENTRY_NODETYPE + " where jcr:path LIKE '" + path + "[%]/%'";
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator nodeIterator = queryResult.getNodes();
    List<NewsletterManagerConfig> listNewsletterEntry = new ArrayList<NewsletterManagerConfig>();
    while(nodeIterator.hasNext()){
      try{
        listNewsletterEntry.add(getEntryFromNode(nodeIterator.nextNode()));
      }catch(Exception ex){
        ex.printStackTrace();
        continue;
      }
    }
    return listNewsletterEntry;
  }
}
