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

import java.util.ArrayList;
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
import org.apache.commons.httpclient.methods.GetMethod;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
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

  private ExoCache                 brokenLinksCache;
  private WCMConfigurationService  configurationService;
  private RepositoryService        repositoryService;
  private LivePortalManagerService livePortalManagerService;

  public LiveLinkManagerServiceImpl(
      WCMConfigurationService   configurationService, 
      RepositoryService         repositoryService, 
      LivePortalManagerService  livePortalManagerService,
      CacheService              cacheService) throws Exception {
    this.configurationService = configurationService;
    this.repositoryService = repositoryService;
    this.livePortalManagerService = livePortalManagerService;
    this.brokenLinksCache = cacheService.getCacheInstance(this.getClass().getName());    
  }
  
  public List<LinkBean> getActiveLinks(String portalName) throws Exception {
    return null;
  }
  
  public List<String> getActiveLinks(Node webContent) throws Exception {
    return null;
  }
    
  @SuppressWarnings("unchecked")
  public List<LinkBean> getBrokenLinks(String portalName) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    Node portal = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    String path = portal.getPath();
    Session session = portal.getSession();
    List<LinkBean> listBrokenLinks = new ArrayList<LinkBean>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from exo:webContent where jcr:path like '" + path + "/%'", Query.SQL);
    QueryResult results = query.execute();
    NodeIterator iter = results.getNodes();
    for (;iter.hasNext();) {
      Node webContent = iter.nextNode();
      List<String> listBrokenUrls = getBrokenLinks(webContent);
      for (String brokenUrl : listBrokenUrls) {
        LinkBean linkBean = new LinkBean(brokenUrl, LinkBean.STATUS_BROKEN);
        listBrokenLinks.add(linkBean);
      }
    }
    return listBrokenLinks;
  }

  @SuppressWarnings("unchecked")
  public List<String> getBrokenLinks(Node webContent) throws Exception {    
    List<String> listBrokenUrls = (List<String>)brokenLinksCache.get(webContent.getUUID());
    if(listBrokenUrls == null) {      
      for(Value value:webContent.getProperty("exo:links").getValues()) {
        String link = value.getString();
        LinkBean linkBean = LinkBean.parse(link);
        if(linkBean.isBroken()) {
          listBrokenUrls.add(linkBean.getUrl());
        }
      }
      brokenLinksCache.put(webContent.getUUID(), listBrokenUrls);
    }
    return listBrokenUrls;
  }
  
  public List<LinkBean> getUncheckedLinks(String portalName) throws Exception {
    return null;
  }
  
  public List<String> getUncheckedLinks(Node webContent) throws Exception {
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
      updateLinkStatus(session, "select * from exo:linkable where jcr:path like '" + path + "/%'");
    }
  }

  public void validateLink(String portalName) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    Node portal = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    String path = portal.getPath();
    Session session = portal.getSession();
    updateLinkStatus(session, "select * from exo:linkable where jcr:path like '" + path + "/%'");
  }
  
  protected void updateLinkStatus(Session session, String queryCommand) throws Exception{
    List<String> listBrokenLinks = new ArrayList<String>();
    ValueFactory valueFactory = session.getValueFactory();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryCommand, Query.SQL);
    QueryResult results = query.execute();
    NodeIterator iter = results.getNodes();
    for (;iter.hasNext();) {
      Node webContent = iter.nextNode();
      if (!webContent.hasProperty("jcr:isCheckedOut") || (webContent.hasProperty("jcr:isCheckedOut") && webContent.getProperty("jcr:isCheckedOut").getBoolean())) {
        Property links = webContent.getProperty("exo:links");
        Value[] oldValues = links.getValues();
        Value[] newValues = new Value[oldValues.length];
        for (int iValues = 0; iValues < oldValues.length; iValues++) {
          String oldLink = oldValues[iValues].getString();
          if (!oldLink.equals("")) {
            LinkBean linkBean = LinkBean.parse(oldLink);
            String oldUrl = linkBean.getUrl();
            String oldStatus = getLinkStatus(oldUrl);
            String updatedLink = new LinkBean(oldUrl, oldStatus).toString();
            System.out.println("[URL] " + updatedLink );
            newValues[iValues] = valueFactory.createValue(updatedLink);
            if (oldStatus.equals(LinkBean.STATUS_BROKEN)) {
              listBrokenLinks.add(oldUrl);
            }
          }
        }
        webContent.setProperty("exo:links",newValues);
        brokenLinksCache.put(webContent.getUUID(), listBrokenLinks);
      }
    }
    session.save();
  }
  
  protected String getLinkStatus(String strUrl) {
    try {
      HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());      
      GetMethod getMethod = new GetMethod(strUrl);      
      if(httpClient.executeMethod(getMethod) == 200) {
        return LinkBean.STATUS_ACTIVE;
      }
    } catch (Exception e) {}
    return LinkBean.STATUS_BROKEN;
  }

}
