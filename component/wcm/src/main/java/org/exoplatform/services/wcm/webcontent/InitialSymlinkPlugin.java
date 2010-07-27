/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.wcm.webcontent;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.portal.artifacts.CreatePortalPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 24, 2010  
 */
public class InitialSymlinkPlugin extends CreatePortalPlugin {

  /** The init params. */
  private InitParams initParams;

  /** The repository service. */
  private RepositoryService repositoryService;

  /** The link manager service. */
  private LinkManager linkManager;

  /** The log. */
  private Log log = ExoLogger.getLogger(this.getClass());
  
  public InitialSymlinkPlugin(InitParams initParams, ConfigurationManager configurationManager, RepositoryService repositoryService, LinkManager linkManager) {
    super(initParams, configurationManager, repositoryService);
    this.initParams = initParams;
    this.repositoryService = repositoryService;
    this.linkManager = linkManager;
  }

  @SuppressWarnings("unchecked")
  public void deployToPortal(SessionProvider sessionProvider, String portalName) throws Exception {
    ValueParam portalValue = initParams.getValueParam("portal");
    if (portalValue == null || (portalValue != null && !portalName.equals(portalValue.getValue()))) return;
    Iterator iterator = initParams.getObjectParamIterator();    
    while(iterator.hasNext()) {
      ObjectParameter objectParameter = (ObjectParameter)iterator.next();
      LinkDeploymentDescriptor deploymentDescriptor = (LinkDeploymentDescriptor)objectParameter.getObject();
      String sourcePath = deploymentDescriptor.getSourcePath();
      String targetPath = deploymentDescriptor.getTargetPath();
      // sourcePath should looks like : repository:collaboration:/sites content/live/acme

      String[] src = sourcePath.split(":");
      String[] tgt = targetPath.split(":");

      if (src.length==3 && tgt.length==3) {
        ManageableRepository repository = repositoryService.getRepository(src[0]);
        Session session = sessionProvider.getSession(src[1], repository);
        ManageableRepository repository2 = repositoryService.getRepository(tgt[0]);
        Session session2 = sessionProvider.getSession(tgt[1], repository2);
        try {
          Node nodeSrc = session.getRootNode().getNode(src[2].substring(1));
          Node nodeTgt = session2.getRootNode().getNode(tgt[2].substring(1));
          linkManager.createLink(nodeTgt, "exo:taxonomyLink", nodeSrc);
          log.info(sourcePath + " has a link into "+targetPath);
        } finally {
          session.logout();
          session2.logout();
        }
      }
    }  
  }
  
}
