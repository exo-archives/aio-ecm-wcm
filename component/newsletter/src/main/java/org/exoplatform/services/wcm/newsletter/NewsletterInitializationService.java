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

import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterInitializationService implements Startable {
  
  private List<NewsletterCategoryConfig> categoryConfigs;
  private List<NewsletterSubscriptionConfig> subscriptionConfigs;
  private List<NewsletterUserConfig> userConfigs;
  private NewsletterManagerService managerService;
  private LivePortalManagerService livePortalManagerService;
  private static Log log = ExoLogger.getLogger(NewsletterInitializationService.class);
  
  @SuppressWarnings("unchecked")
  public NewsletterInitializationService(InitParams initParams, NewsletterManagerService managerService, LivePortalManagerService livePortalManagerService) {
    categoryConfigs = initParams.getObjectParamValues(NewsletterCategoryConfig.class);
    subscriptionConfigs = initParams.getObjectParamValues(NewsletterSubscriptionConfig.class);
    userConfigs = initParams.getObjectParamValues(NewsletterUserConfig.class);
    this.managerService = managerService;
    this.livePortalManagerService = livePortalManagerService;
  }

  public void start() {
    log.info("Starting NewsletterInitializationService ... ");
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      List<Node> portalNodes = livePortalManagerService.getLivePortals(sessionProvider);
      if (portalNodes.isEmpty()) return;
      Node dummyNode = portalNodes.get(0);
      Session session = dummyNode.getSession();
      Node serviceFolder = session.getRootNode().getNode("exo:services");
      Node newsletterInitializationService = null;
      if (serviceFolder.hasNode("NewsletterInitializationService")) {
        newsletterInitializationService = serviceFolder.getNode("NewsletterInitializationService");
      } else {
        newsletterInitializationService = serviceFolder.addNode("NewsletterInitializationService", "nt:unstructured");
      }
      if (!newsletterInitializationService.hasNode("NewsletterInitializationServiceLog")) {
        for (Node portalNode : portalNodes) {
          String portalName = portalNode.getName();
          NewsletterCategoryHandler categoryHandler = managerService.getCategoryHandler();
          for (NewsletterCategoryConfig categoryConfig : categoryConfigs) {
            categoryHandler.add(portalName, categoryConfig, sessionProvider);
          }
          
          NewsletterSubscriptionHandler subscriptionHandler = managerService.getSubscriptionHandler();
          for (NewsletterSubscriptionConfig subscriptionConfig : subscriptionConfigs) {
            subscriptionHandler.add(sessionProvider, portalName, subscriptionConfig);
          }
          
          NewsletterManageUserHandler manageUserHandler = managerService.getManageUserHandler();
          for (NewsletterUserConfig userConfig : userConfigs) {
            manageUserHandler.add(portalName, userConfig.getMail());
          }
        }
        
        Node newsletterInitializationServiceLog = newsletterInitializationService.addNode("NewsletterInitializationServiceLog", "nt:file");
        Node newsletterInitializationServiceLogContent = newsletterInitializationServiceLog.addNode("jcr:content", "nt:resource");
        newsletterInitializationServiceLogContent.setProperty("jcr:encoding", "UTF-8");
        newsletterInitializationServiceLogContent.setProperty("jcr:mimeType", "text/plain");
        newsletterInitializationServiceLogContent.setProperty("jcr:data", "Newsletter was created successfully");
        newsletterInitializationServiceLogContent.setProperty("jcr:lastModified", new Date().getTime());
        session.save();
      }
    } catch (Throwable e) {
      log.info("Starting NewsletterInitializationService fail because of " + e.getMessage());
    } finally {
      sessionProvider.close();
    }
  }

  public void stop() {
    log.info("Stopping NewsletterInitializationService ... ");
  }
  
}