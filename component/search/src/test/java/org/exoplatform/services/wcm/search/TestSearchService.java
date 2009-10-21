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
package org.exoplatform.services.wcm.search;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;
import org.exoplatform.services.wcm.search.QueryCriteria.DatetimeRange;
import org.exoplatform.services.wcm.search.QueryCriteria.QueryProperty;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jul 14, 2009
 */
public class TestSearchService extends BaseWCMTestCase {
  QueryCriteria queryCriteria = new QueryCriteria();
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(TestSearchService.class);
  
  /** The session provider. */
  protected SessionProvider sessionProvider;
  
  /** The site search service. */
  public SiteSearchService siteSearchService;
  
  /** The repository service. */
  RepositoryService repositoryService;
  
  /** The session provider service. */
  private static SessionProviderService sessionProviderService = null;
  
  /** The page. */
  Page page;
  
  /** The publication plugin. */
  WebpagePublicationPlugin publicationPlugin ;
  
  /** The wcm publication service. */
  WCMPublicationService wcmPublicationService;
  
  /** The keyword. */
  String searchKeyword = "This is";
  
  /** The page checked. */
  boolean searchPageChecked = true; 
  
  /** The document checked. */
  boolean searchDocumentChecked = true;
  
  /** Name of portal which is searched. if this parameter is null then search all portal 
   * else only search with all nodes which in selected portal 
   */
  String searchSelectedPortal = "shared";
  
  /** page index: number of item per pages. */
  int seachItemsPerPage = 100;
  
  /** is search with publication property. If isLiveMode = <code>true</code> then only search nodes 
   * which have property publication else search with all (have or don't have publication property) 
   */
  boolean searchIsLiveMode = false;
  
  static boolean isRunInitData = true;
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();
    queryCriteria = new QueryCriteria();
    siteSearchService = (SiteSearchService) container.getComponentInstanceOfType(SiteSearchService.class);
    repositoryService = getService(RepositoryService.class);
    session = repositoryService.getRepository("repository").getSystemSession("collaboration");
    sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sessionProvider = sessionProviderService.getSystemSessionProvider(null) ;
    wcmPublicationService = getService(WCMPublicationService.class);
    publicationPlugin = new StageAndVersionPublicationPlugin();
    initData();
  }
  
/*
  private String cutPath(String path) throws Exception {
    String pathTaxonomy = getPathTaxonomy() + "/";
    String returnString = path.replaceAll(pathTaxonomy, "");
    
    return returnString;
  }*/
  
  /**
   * Init the data: Create publicationPlugin and wcmPublicationService.
   * 
   * @throws Exception the exception
   */
  protected void initData()throws Exception{
    try{
      publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
      wcmPublicationService.addPublicationPlugin(publicationPlugin);
      session.save();
      if(isRunInitData){
        addContentForLiveNode();
        addContentForSharedPortal();
      }
      isRunInitData = false;
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Adds the child nodes.
   * 
   * @param parentNode the parent node
   * 
   * @throws Exception the exception
   */
  private void addChildNodes(Node parentNode)throws Exception{
    UserPortalConfigService userPortalConfigService = getService(UserPortalConfigService.class);
    try{
      page = userPortalConfigService.getPage("portal::"+parentNode.getName()+"::testpage");
    }catch(Exception ex){ }
    
    if(page == null){
      page = new Page();
      page.setPageId("portal::"+parentNode.getName()+"::testpage");
      page.setName("testpage");
      page.setOwnerType("portal");
      page.setOwnerId("classic");
      userPortalConfigService.create(page);
    }
    
    Node webContentNode = null;
    
    for(int i = 0; i < 5; i ++){
      try{
        webContentNode = createWebcontentNode(parentNode, parentNode.getName() + " webcontentNode " + i, null, null, null);
        if(!webContentNode.isNodeType("metadata:siteMetadata"))webContentNode.addMixin("metadata:siteMetadata");
        wcmPublicationService.enrollNodeInLifecycle(webContentNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
        wcmPublicationService.publishContentSCV(webContentNode, page, parentNode.getName());
        HashMap<String, String> context = new HashMap<String, String>();
        context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME, webContentNode.getName());
        publicationPlugin.changeState(webContentNode, PublicationDefaultStates.PUBLISHED, context);
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
    session.save();
    
    for(int i = 5; i < 10; i ++){
      try{
        webContentNode = createWebcontentNode(parentNode, parentNode.getName() + " webcontentNode " + i, null, null, null);
        if(!webContentNode.isNodeType("metadata:siteMetadata"))webContentNode.addMixin("metadata:siteMetadata");
        wcmPublicationService.enrollNodeInLifecycle(webContentNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
        wcmPublicationService.publishContentSCV(webContentNode, page, parentNode.getName());
        HashMap<String, String> context = new HashMap<String, String>();
        context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME, webContentNode.getName());
        publicationPlugin.changeState(webContentNode, PublicationDefaultStates.DRAFT, context);
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
    session.save();
  }
  
  /**
   * Gets the search result.
   * 
   * @return the search result
   * 
   * @throws Exception the exception
   */
  private WCMPaginatedQueryResult getSearchResult() throws Exception{
    queryCriteria.setSiteName(searchSelectedPortal);
    queryCriteria.setKeyword(searchKeyword);
    if (searchDocumentChecked) {
      queryCriteria.setSearchDocument(true);
      queryCriteria.setSearchWebContent(true);
    } else  {
      queryCriteria.setSearchDocument(false);
      queryCriteria.setSearchWebContent(false);
    }
    queryCriteria.setSearchWebpage(searchPageChecked);
    queryCriteria.setLiveMode(searchIsLiveMode);
    WCMPaginatedQueryResult queryResult = this.siteSearchService.searchSiteContents(sessionProvider, queryCriteria, seachItemsPerPage); 
    return queryResult;
  }
  
  /**
   * Test add content for shared portal.
   * 
   * @throws Exception the exception
   */
  public void addContentForSharedPortal() throws Exception{
    Node parentNode = (Node)session.getItem("/sites content/live/shared/documents");
    this.addChildNodes(parentNode);
    assertEquals(parentNode.getNodes().getSize(), 10);
  }
  
  /**
   * Test add content for live node.
   * 
   * @throws Exception the exception
   */
  public void addContentForLiveNode() throws Exception{
    Node parentNode = (Node)session.getItem("/sites content/live");
    this.addChildNodes(parentNode);
    assertEquals(parentNode.getNodes().getSize(), 12);
  }
  
  /**
   * Test case 1: search all node (includes have or don't have publication property)
   * in shared portal and not live mode. In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = false<br>
   * 
   * @throws Exception the exception
   */
  public void testSearchSharedPortalNotLiveMode() throws Exception {
    searchPageChecked = true;
    searchDocumentChecked = true;
    searchSelectedPortal = "shared";
    searchIsLiveMode = false;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 1: search in shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(10, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 2: search all node (includes have or don't have publication property)
   * in shared portal. In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   * 
   * @throws Exception the exception
   */
  public void testSearchSharedPortalLiveMode() throws Exception {
    searchPageChecked = true;
    searchDocumentChecked = true;
    searchSelectedPortal = "shared";
    searchIsLiveMode = true;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 2: search int Shared Portal to find all node with live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(5, paginatedQueryResult.getPage(1).size());
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 3: search all node (includes have or don't have publication property) in all portals.
   * In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = false<br>
   * 
   * @throws Exception the exception
   */
  public void testSearchAllPortalNotLiveMode() throws Exception {
    searchPageChecked = true;
    searchDocumentChecked = true;
    searchSelectedPortal = null;
    searchIsLiveMode = false;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\t Search in All Portals, find nodes which not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(20, paginatedQueryResult.getTotalNodes());
      assertEquals(20, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 4: search nodes which are live mode in all portals.
   * In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * 
   * @throws Exception the exception
   */
  public void testSearchAllPortalLiveMode() throws Exception {
    searchPageChecked = true;
    searchDocumentChecked = true;
    searchSelectedPortal = null;
    searchIsLiveMode = true;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 4: search in All Portals, find nodes which are live move \n\tTime search: " + timeSearch + " s\n");
      assertEquals(10, paginatedQueryResult.getPage(1).size());
      assertEquals(20, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  //---------------------------------------------- Test search document -----------------------------------------------------------
  /**
   * Test case 5: Test search document.
   * Search all documents in system (all portals) which are live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchDocumentLiveMode(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = false;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 5: search document with all portal and live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(10, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 6:Test search document.
   * Search all documents in system (all portals). With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = false<br>
   */
  public void testSearchDocumentNotLiveMode(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = false;
    this.searchIsLiveMode = false;
    this.searchSelectedPortal = null;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 6: search document with all portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(20, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 7:Test search document.
   * Search all documents in shared portal. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = false<br>
   */
  public void testSearchDocumentOfSharedPortal(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = false;
    this.searchIsLiveMode = false;
    this.searchSelectedPortal = "shared";
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 7: search document shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 8:Test search document.
   * Search all documents in shared portal. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchDocumentOfSharedPortalLiveMode(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = false;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = "shared";
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 8: search document in shared portal and live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(5, paginatedQueryResult.getPage(1).size());
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  //------------------------------------------- Test search pages ------------------------------------------------------------------
  /**
   * Test case 9:Test search pages.
   * Search all pages in all portals. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchPagesLiveMode(){
    //this.searchKeyword = "webcontentNode";
    this.searchDocumentChecked = false;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 9: search pages with all portal and live mode \n\tTime search: " +
               timeSearch + " s\n" +
               "\t with this case, don't search any webcontent node type. In this test now, all nodet" +
               "have node type is webcontent then reult is 0");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 10:Test search pages.
   * Search all pages in all portals and not live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = false<br>
   */
  public void testSearchPages(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = true;
    this.searchIsLiveMode = false;
    this.searchSelectedPortal = null;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 10: search pages with all portal and not live mode \tTime search: " + timeSearch + " s\n");
      assertEquals(20, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 11:Test search pages.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchPagesSharedLiveMode(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = "shared";
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 11: search pages in shared portal and live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 12:Test search pages.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchPagesShared(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = true;
    this.searchIsLiveMode = false;
    this.searchSelectedPortal = "shared";
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 12: search pages shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  //------------------------------------- test with not document or page --------------------------------------------------------------------

  /**
   * Test case 13:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchNotPagesDocument_AllPortalLiveMode(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = false;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 13: search contents are not page/documents with all portal and live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
      assertEquals(20, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 14:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchNotPagesDocument_AllPortalNotLiveMode(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = false;
    this.searchIsLiveMode = false;
    this.searchSelectedPortal = null;
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 14: search contents are not page/documents with all portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
      assertEquals(20, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 15:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchNotPagesDocument_SharedLiveMode(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = false;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = "shared";
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 15: search contents are not page/documents with shared portal and live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 16:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchNotPagesDocument_SharedNoLiveMode(){
    this.searchDocumentChecked = false;
    this.searchPageChecked = false;
    this.searchIsLiveMode = false;
    this.searchSelectedPortal = "shared";
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 16: search contents are not page/documents with shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
      assertEquals(10, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 17:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  @SuppressWarnings("deprecation")
  public void testSearchPagesDocument_Date(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    Date date = new Date(2009, 05, 05);
    GregorianCalendar calFrom = new GregorianCalendar() ;
    calFrom.setTime(date);
    date = new Date();
    GregorianCalendar calTo = new GregorianCalendar() ;
    calTo.setTime(date);
    DatetimeRange datetimeRange = new DatetimeRange(calFrom, calTo);
    queryCriteria.setCreatedDateRange(datetimeRange);
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 16: search contents are not page/documents with shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(10, paginatedQueryResult.getPage(1).size());
      assertEquals(20, paginatedQueryResult.getTotalNodes());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 18:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  public void testSearchPagesDocument_NotFultextSearch(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    queryCriteria.setFulltextSearch(false);
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 16: search contents are not page/documents with shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 19:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * keyWord = null;
   */
  public void testSearchPagesDocument_ContentType(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    this.searchKeyword = null;
    queryCriteria.setContentTypes(new String[]{"exo:webContent", "exo:htmlFile"});
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 19: search contents are not page/documents with shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(10, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 20:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * keyWord = null;
   */
  public void testSearchPagesDocument_Property(){
    this.searchDocumentChecked = true;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    this.searchKeyword = "This is*";
    QueryProperty queryProperty1 = queryCriteria.new QueryProperty();
    queryProperty1.setName("jcr:data");
    queryProperty1.setValue("This is the");
    QueryProperty queryProperty2 = queryCriteria.new QueryProperty();
    queryProperty2.setName("jcr:data");
    queryProperty2.setValue("the default.css file");
    queryCriteria.setQueryMetadatas(new QueryProperty[]{queryProperty1, queryProperty2});
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 20: search contents are not page/documents with shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  /**
   * Test case 21:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * keyWord = null;
   * @throws RepositoryException 
   * @throws PathNotFoundException 
   */
  public void testSearchPagesDocument_UUIDS() throws PathNotFoundException, RepositoryException{
    this.searchDocumentChecked = true;
    this.searchPageChecked = true;
    this.searchIsLiveMode = true;
    this.searchSelectedPortal = null;
    this.searchKeyword = "This is";
    Node documentNode = ((Node)session.getItem("/sites content/live/shared/documents")).getNode("documents webcontentNode 0");
    Node livenode = ((Node)session.getItem("/sites content/live")).getNode("live webcontentNode 0");
    queryCriteria.setCategoryUUIDs(new String[]{documentNode.getUUID(), livenode.getUUID()});
    try{
      WCMPaginatedQueryResult paginatedQueryResult = getSearchResult();
      float timeSearch = paginatedQueryResult.getQueryTimeInSecond();
      log.info("\n\tTest case 21: search contents are not page/documents with shared portal and not live mode \n\tTime search: " + timeSearch + " s\n");
      assertEquals(0, paginatedQueryResult.getPage(1).size());
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
}
