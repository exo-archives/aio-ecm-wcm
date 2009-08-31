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
package org.exoplatform.wcm.connector.collaboration;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.wcm.connector.BaseConnector;
import org.w3c.dom.Document;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * 29-08-2009  
 */

@URITemplate("/rss/")
public class RssConnector extends BaseConnector implements ResourceContainer {

  static private String WORKSPACE = "workspace".intern() ;
  static private String REPOSITORY = "repository".intern() ;
  
  static private String RSS_VERSION = "rss_2.0".intern() ;
  static private String FEED_TITLE = "exo:feedTitle".intern() ;
  static private String DESCRIPTION = "exo:description".intern() ;
  static private String TITLE = "exo:title";
  static private String LINK = "exo:link".intern() ;
  static private String QUERY_PATH = "exo:queryPath".intern() ;
  static private String SUMMARY = "exo:summary";
//  static private String FEED_NAME = "exo:feedName".intern() ;
//  static private String FEED_TYPE = "exo:feedType".intern() ;
//  static private String URL = "exo:url".intern() ;
//  static private String STORE_PATH = "exo:storePath".intern() ;
//  static private String KEYWORDS = "exo:keywords".intern() ;
//  static private String LANGUAGE = "exo:language".intern() ;
//  static private String COPYRIGHT = "exo:copyright".intern() ;
//  static private String PUBDATE = "exo:pubDate".intern() ;
//  static private String OWNER_NAME = "exo:ownerName".intern() ;
//  static private String OWNER_MAIL = "exo:ownerEmail".intern() ;
//  static private String IMAGE_URL = "exo:imageURL".intern() ;
//  static private String CATEGORY = "exo:podcastCategory".intern() ;
//  static private String PUBLISHED_DATE = "exo:publishedDate".intern() ;
//  static private String AUTHOR = "exo:author".intern() ;
//  static private String EXPLICIT = "exo:explicit".intern() ;
//  static private String LENGTH = "exo:length".intern() ;
//  static private String JCR_CONTENT = "jcr:content".intern() ;
//  static private String JCR_DATA = "jcr:data".intern() ;
//  static private String JCR_MIMETYPE = "jcr:mimeType".intern() ;
//  static private String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
//  static private String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
//  static private String NT_FILE = "nt:file".intern() ;
//  static private String NT_RESOURCE = "nt:resource".intern() ;  
//  static private String RSS = "/rss".intern() ;
//  static private String MIX_VERSIONABLE = "mix:versionable".intern() ;

  private RepositoryService repositoryService;
  
  private WCMConfigurationService wcmConfigurationService;
  
  private String categoryPath;
  
  public RssConnector(ExoContainer container) {
    super(container);
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    wcmConfigurationService = (WCMConfigurationService) container.getComponentInstanceOfType(WCMConfigurationService.class);
  }

  @HTTPMethod("GET")
  @URITemplate("/generate/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response generate ( 
      @QueryParam("repository") String repositoryName, 
      @QueryParam("workspace") String workspaceName,
      @QueryParam("categoryPath") String categoryPath) throws Exception {
    
    this.categoryPath = categoryPath;
    String currentCat = categoryPath;
    if (currentCat.lastIndexOf("/")!=-1) {
    	currentCat = currentCat.substring(currentCat.lastIndexOf("/")+1);
    }

    Map<String, String> contextRss = new HashMap<String, String>();
    contextRss.put(REPOSITORY, repositoryName);
    contextRss.put(WORKSPACE, workspaceName);
    contextRss.put("actionName", "actionName");
    contextRss.put(RSS_VERSION, "rss_2.0");
    contextRss.put(FEED_TITLE, currentCat);
    contextRss.put(DESCRIPTION, categoryPath.replaceAll("/", " : "));
    contextRss.put(QUERY_PATH, "select * from exo:taxonomyLink where jcr:path like '%/categories/" + categoryPath + "/%' order by exo:dateCreated DESC");    
    contextRss.put(LINK, "http://localhost:8080/portal/acme");
    String feedXML = generateRSS(contextRss);
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(feedXML.getBytes()));
    Response response = Response.Builder.ok(document).build();
    return response;
  }
  
  private String generateRSS(Map<String, String> context) {  
    String actionName = (String)context.get("actionName") ;
    String workspace = (String)context.get(WORKSPACE);                   
    String rssVersion = (String) context.get(RSS_VERSION) ;
    String feedTitle = (String) context.get(FEED_TITLE) ;    
    String feedDescription = (String) context.get(DESCRIPTION) ;
    String queryPath = (String) context.get(QUERY_PATH) ;
    String feedLink = (String) context.get(LINK) ;
    String repository = (String) context.get(REPOSITORY) ;
    if(feedTitle == null || feedTitle.length() == 0) feedTitle = actionName ;        
    try {
      Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, repositoryService.getRepository(repository));
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryPath, Query.SQL);
      QueryResult queryResult = query.execute();            
      
      SyndFeed feed = new SyndFeedImpl();      
      feed.setFeedType(rssVersion);      
      feed.setTitle(feedTitle.replaceAll("&nbsp;", " "));
      feed.setLink(feedLink);
      feed.setDescription(feedDescription.replaceAll("&nbsp;", " "));
      feed.setEncoding("UTF-8");
      
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      NodeIterator iter = queryResult.getNodes() ;
      while (iter.hasNext()) {        
        Node symlink = iter.nextNode();
        Node realnode = session.getNodeByUUID(symlink.getProperty("exo:uuid").getString());
        String url = getEntryUrl(feedLink, symlink.getName()) ;
        SyndEntry entry = new SyndEntryImpl();
        
        if (realnode.hasProperty(TITLE)) entry.setTitle(realnode.getProperty(TITLE).getString());                
        else entry.setTitle("") ;
        
        entry.setLink(url);        
        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        
        if (realnode.hasProperty(SUMMARY)) description.setValue(realnode.getProperty(SUMMARY).getString().replaceAll("&nbsp;", " "));
        else description.setValue("") ;
        
        entry.setDescription(description);        
        entries.add(entry);
        entry.getEnclosures() ;
      }      
      feed.setEntries(entries);      
           
      SyndFeedOutput output = new SyndFeedOutput();      
      String feedXML = output.outputString(feed);      
      feedXML = StringUtils.replace(feedXML,"&amp;","&");
      return feedXML;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getEntryUrl(String host, String nodeName) {
    String pcvPageUri = wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.PARAMETERIZED_PAGE_URI);
    return host + pcvPageUri + "/" + categoryPath +  "/" + nodeName;
  }
  
  protected String getContentStorageType() throws Exception {
    return null;
  }

  protected Node getRootContentStorage(Node node) throws Exception {
    return null;
  }

}
