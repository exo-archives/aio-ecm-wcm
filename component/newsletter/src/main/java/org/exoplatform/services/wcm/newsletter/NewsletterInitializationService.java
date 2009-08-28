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
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.deployment.WCMContentInitializerService;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009
 */
public class NewsletterInitializationService implements Startable {
  
  private List<String> portalNames;
  
  /** The category configs. */
  private List<NewsletterCategoryConfig> categoryConfigs;
  
  /** The subscription configs. */
  private List<NewsletterSubscriptionConfig> subscriptionConfigs;
  
  /** The user configs. */
  private List<NewsletterUserConfig> userConfigs;
  
  /** The manager service. */
  private NewsletterManagerService newsletterManagerService;
  
  private LivePortalManagerService livePortalManagerService;
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(NewsletterInitializationService.class);
  
  /**
   * Instantiates a new newsletter initialization service.
   * 
   * @param initParams the init params
   * @param managerService the manager service
   * @param livePortalManagerService the live portal manager service
   */
  //  TODO: chuong.phan: DO NOT REMOVE WCMContentInitializerService, THIS IS DEPENDENCY FOR DEPLOYMENT
  @SuppressWarnings("unchecked")
  public NewsletterInitializationService(InitParams initParams, NewsletterManagerService newsletterManagerService, LivePortalManagerService livePortalManagerService, WCMContentInitializerService wcmContentInitializerService) {
    portalNames = initParams.getValuesParam("portalNames").getValues();
    categoryConfigs = initParams.getObjectParamValues(NewsletterCategoryConfig.class);
    subscriptionConfigs = initParams.getObjectParamValues(NewsletterSubscriptionConfig.class);
    userConfigs = initParams.getObjectParamValues(NewsletterUserConfig.class);
    this.livePortalManagerService = livePortalManagerService;
    this.newsletterManagerService = newsletterManagerService;
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    log.info("Starting NewsletterInitializationService ... ");
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      Node dummyNode = livePortalManagerService.getLivePortal(portalNames.get(0), sessionProvider);
      Session session = dummyNode.getSession();
      Node serviceFolder = session.getRootNode().getNode("exo:services");
      Node newsletterInitializationService = null;
      if (serviceFolder.hasNode("NewsletterInitializationService")) {
        newsletterInitializationService = serviceFolder.getNode("NewsletterInitializationService");
      } else {
        newsletterInitializationService = serviceFolder.addNode("NewsletterInitializationService", "nt:unstructured");
      }
      if (!newsletterInitializationService.hasNode("NewsletterInitializationServiceLog")) {
        for (String portalName : portalNames) {
          NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
          for (NewsletterCategoryConfig categoryConfig : categoryConfigs) {
            categoryHandler.add(portalName, categoryConfig, sessionProvider);
          }
          
          NewsletterSubscriptionHandler subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
          for (NewsletterSubscriptionConfig subscriptionConfig : subscriptionConfigs) {
            subscriptionHandler.add(sessionProvider, portalName, subscriptionConfig);
          }

          Node userNode = null;
          NewsletterManageUserHandler manageUserHandler = newsletterManagerService.getManageUserHandler();
          for (NewsletterUserConfig userConfig : userConfigs) {
            userNode = manageUserHandler.add(portalName, userConfig.getMail(), sessionProvider);
          }
          ExtendedNode userFolderNode = (ExtendedNode) userNode.getParent();
          if(userFolderNode.canAddMixin("exo:privilegeable")) 
            userFolderNode.addMixin("exo:privilegeable");
          userFolderNode.setPermission("any", PermissionType.ALL) ;
          
          Node newsletterInitializationServiceLog = newsletterInitializationService.addNode("NewsletterInitializationServiceLog", "nt:file");
          Node newsletterInitializationServiceLogContent = newsletterInitializationServiceLog.addNode("jcr:content", "nt:resource");
          newsletterInitializationServiceLogContent.setProperty("jcr:encoding", "UTF-8");
          newsletterInitializationServiceLogContent.setProperty("jcr:mimeType", "text/plain");
          newsletterInitializationServiceLogContent.setProperty("jcr:data", "Newsletter was created successfully");
          newsletterInitializationServiceLogContent.setProperty("jcr:lastModified", new Date().getTime());
          session.save();
        }
      }
    } catch (Throwable e) {
      log.info("Starting NewsletterInitializationService fail because of " + e.getMessage());
    } finally {
      sessionProvider.close();
    }
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    log.info("Stopping NewsletterInitializationService ... ");
  }
  
}