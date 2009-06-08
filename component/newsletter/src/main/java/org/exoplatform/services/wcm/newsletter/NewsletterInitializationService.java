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

import java.util.List;

import javax.jcr.Node;

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

import sun.rmi.transport.LiveRef;

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
      for (Node portalNode : portalNodes) {
        String portalName = portalNode.getName();
        NewsletterCategoryHandler categoryHandler = managerService.getCategoryHandler();
        for (NewsletterCategoryConfig categoryConfig : categoryConfigs) {
          categoryHandler.add(portalName, categoryConfig, sessionProvider);
        }
        
        NewsletterSubscriptionHandler subscriptionHandler = managerService.getSubscriptionHandler();
        for (NewsletterSubscriptionConfig subscriptionConfig : subscriptionConfigs) {
          subscriptionHandler.add(sessionProvider, portalName, subscriptionConfig.getCategoryName(), subscriptionConfig);
        }

        NewsletterManageUserHandler manageUserHandler = managerService.getManageUserHandler();
        for (NewsletterUserConfig userConfig : userConfigs) {
          manageUserHandler.add(portalName, userConfig.getMail(), sessionProvider);
        }
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