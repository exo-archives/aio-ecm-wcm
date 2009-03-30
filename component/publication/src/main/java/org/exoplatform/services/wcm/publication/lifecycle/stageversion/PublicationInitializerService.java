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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          phan.le.thanh.chuong@gmail.com, chuong_phan@exoplatform.com
 * Mar 25, 2009  
 */
public class PublicationInitializerService implements Startable{      
  
  private LivePortalManagerService livePortalManagerService;
  private PublicationService publicationService;
  
  public PublicationInitializerService(LivePortalManagerService livePortalManagerService, PublicationService publicationService) {
    this.livePortalManagerService = livePortalManagerService;
    this.publicationService = publicationService;
  }
  
  public void initializePublication(Node portalNode) throws Exception{
    String sqlQuery = "select * from exo:webContent where jcr:path like '" + portalNode.getPath() + "/%' and not jcr:mixinTypes like '%" + Constant.PUBLICATION_LIFECYCLE_TYPE + "%' order by exo:dateCreated";
    QueryManager queryManager = portalNode.getSession().getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult results = query.execute();
    for (NodeIterator nodeIterator = results.getNodes(); nodeIterator.hasNext();) {
      Node content = nodeIterator.nextNode();
      publicationService.enrollNodeInLifecycle(content, Constant.LIFECYCLE_NAME);
      publicationService.changeState(content, Constant.LIVE_STATE, new HashMap<String, String>());
    }
  }
  
  public void start() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      List<Node> livePortals = livePortalManagerService.getLivePortals(sessionProvider);
      if (livePortals.isEmpty()) return;
      Node dummyNode = livePortals.get(0);
      Session session = dummyNode.getSession();
      Node serviceFolder = session.getRootNode().getNode("exo:services");
      Node publicationInitializerService = null;
      if (serviceFolder.hasNode("PublicationInitializerService")) {
        publicationInitializerService = serviceFolder.getNode("PublicationInitializerService");
      } else {
        publicationInitializerService = serviceFolder.addNode("PublicationInitializerService", "nt:unstructured");
      }
      if (!publicationInitializerService.hasNode("PublicationInitializerServiceLog")) {
        for(Node portalNode: livePortals) {
          initializePublication(portalNode);
        }
        
        Node publicationInitializerServiceLog = publicationInitializerService.addNode("ContentInitializerServiceLog", "nt:file");
        Node publicationInitializerServiceLogContent = publicationInitializerServiceLog.addNode("jcr:content", "nt:resource");
        publicationInitializerServiceLogContent.setProperty("jcr:encoding", "UTF-8");
        publicationInitializerServiceLogContent.setProperty("jcr:mimeType", "text/plain");
        publicationInitializerServiceLogContent.setProperty("jcr:data", "All node in site artifacts is published");
        publicationInitializerServiceLogContent.setProperty("jcr:lastModified", new Date().getTime());
        session.save();
      }
    } catch (Exception e) {
    } finally {
      sessionProvider.close();
    }
  }
  
  public void stop() {   
  }
}