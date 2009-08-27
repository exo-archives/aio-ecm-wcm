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
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.artifacts.BasePortalArtifactsPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Aug 21, 2009  
 */
public class InitialNewsletterPlugin extends BasePortalArtifactsPlugin {

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
  
  
  @SuppressWarnings("unchecked")
  public InitialNewsletterPlugin(InitParams initParams,
                                 ConfigurationManager configurationManager,
                                 RepositoryService repositoryService,
                                 NewsletterManagerService newsletterManagerService, 
                                 LivePortalManagerService livePortalManagerService) {
    super(initParams, configurationManager, repositoryService);
    
    categoryConfigs = initParams.getObjectParamValues(NewsletterCategoryConfig.class);
    subscriptionConfigs = initParams.getObjectParamValues(NewsletterSubscriptionConfig.class);
    userConfigs = initParams.getObjectParamValues(NewsletterUserConfig.class);
    this.livePortalManagerService = livePortalManagerService;
    this.newsletterManagerService = newsletterManagerService;
  }

  public void deployToPortal(String portalName, SessionProvider sessionProvider) throws Exception {
    try {
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
        
    } catch (Throwable e) {
      log.info("Starting NewsletterInitializationService fail because of " + e.getMessage());
    } finally {
      sessionProvider.close();
    }
  }
   
}
