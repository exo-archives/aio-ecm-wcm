/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.wcm.link;

import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

/**
 * Created by The eXo Platform SAS Author : Phan Le Thanh Chuong
 * chuong_phan@exoplatform.com Aug 6, 2008
 */
public class LiveLinkManagerServiceImpl implements LiveLinkManagerService {

  private WCMConfigurationService  configurationService;
  private RepositoryService        repositoryService;
  private LivePortalManagerService livePortalManagerService;

  public LiveLinkManagerServiceImpl(WCMConfigurationService configurationService, 
      RepositoryService repositoryService, LivePortalManagerService livePortalManagerService) {
    this.configurationService = configurationService;
    this.repositoryService = repositoryService;
    this.livePortalManagerService = livePortalManagerService;
  }

  public List<LinkBean> getActiveLinks(String portalName) throws Exception {
    return null;
  }

  public List<LinkBean> getBrokenLinks(String portalName) throws Exception {
    return null;
  }

  public List<LinkBean> getUncheckedLinks(String portalName) throws Exception {
    return null;
  }

  public void validateLink() throws Exception {
    Collection<NodeLocation> nodeLocationCollection = configurationService.getAllLivePortalsLocation();
    for (NodeLocation nodeLocation : nodeLocationCollection) {
      String repository = nodeLocation.getRepository();
      String workspace = nodeLocation.getWorkspace();
      String path = nodeLocation.getPath();
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = SessionProvider.createSystemProvider().getSession(workspace, manageableRepository);
      ValueFactory valueFactory = session.getValueFactory();
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery("select * from exo:linkable where jcr:path like '" + path + "/%'", Query.SQL);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      for (;iter.hasNext();) {
        Node node = iter.nextNode();
        Property links = node.getProperty("exo:links");
        Value[] oldValues = links.getValues();
        Value[] newValues = new Value[oldValues.length];
        for (int iValues = 0; iValues < oldValues.length; iValues++) {
          LinkBean linkBean = new LinkBean(oldValues[iValues].getString());
          String strUrl = linkBean.getLinkUrl();
          String strStatus = getLinkStatus(strUrl);
          newValues[iValues] = valueFactory.createValue(updateLinkStatus(strUrl, strStatus));
        }
      }
      session.save();
    }
  }

  public void validateLink(String portalName) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    Node portal = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    String path = portal.getPath();
    Session session = portal.getSession();
    ValueFactory valueFactory = session.getValueFactory();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from exo:linkable where jcr:path like '" + path + "/%'", Query.SQL);
    QueryResult results = query.execute();
    NodeIterator iter = results.getNodes();
    for (;iter.hasNext();) {
      Node node = iter.nextNode();
      Property links = node.getProperty("exo:links");
      Value[] oldValues = links.getValues();
      Value[] newValues = new Value[oldValues.length];
      for (int iValues = 0; iValues < oldValues.length; iValues++) {
        LinkBean linkBean = new LinkBean(oldValues[iValues].getString());
        String strUrl = linkBean.getLinkUrl();
        String strStatus = getLinkStatus(strUrl);
        newValues[iValues] = valueFactory.createValue(updateLinkStatus(strUrl, strStatus));
      }
    }
    session.save();
  }

  public String getLinkStatus(String strUrl) {
    try {
      HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
      PostMethod postMethod = new PostMethod(strUrl);
      if(httpClient.executeMethod(postMethod) == 200) {
        return "active";
      } else {
        return "broken";
      }
    } catch (Exception e) {
      return "broken";
    }
  }

  public String updateLinkStatus(String strUrl, String strStatus) throws Exception {
    LinkBean linkBean = new LinkBean(strUrl, strStatus);
    return linkBean.toString();
  }
}
