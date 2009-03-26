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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import javax.jcr.Node;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.artifacts.BasePortalArtifactsPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          phan.le.thanh.chuong@gmail.com, chuong_phan@exoplatform.com
 * Mar 25, 2009  
 */
public class PublicationArtifactsInitializerPlugin extends BasePortalArtifactsPlugin{
  
  private PublicationInitializerService publicationInitializerService;
  private LivePortalManagerService livePortalManagerService;
  public PublicationArtifactsInitializerPlugin(InitParams initParams,
                                               ConfigurationManager configurationManager,
                                               RepositoryService repositoryService, 
                                               PublicationInitializerService publicationInitializerService, 
                                               LivePortalManagerService livePortalManagerService) {
    super(initParams, configurationManager, repositoryService);
    this.publicationInitializerService = publicationInitializerService;
    this.livePortalManagerService = livePortalManagerService;
  }

  public void deployToPortal(String portalName, SessionProvider sessionProvider) throws Exception {
    Node portal = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    publicationInitializerService.initializePublication(portal, false);
  }

}
