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
package org.exoplatform.services.wcm.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.utils.SQLQueryBuilder;
import org.exoplatform.services.wcm.utils.AbstractQueryBuilder.LOGICAL;
import org.exoplatform.services.wcm.utils.AbstractQueryBuilder.ORDERBY;
import org.exoplatform.services.wcm.utils.AbstractQueryBuilder.PATH_TYPE;
import org.exoplatform.services.wcm.utils.AbstractQueryBuilder.QueryTermHelper;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Oct 8, 2008
 */
public class SiteSearchServiceImpl implements SiteSearchService {

  /** The live portal manager service. */
  private LivePortalManagerService livePortalManagerService;

  /** The ecm template service. */
  private TemplateService templateService;

  /** The wcm configuration service. */
  private WCMConfigurationService configurationService;

  /** The jcr repository service. */
  private RepositoryService repositoryService;

  /** The current repository. */
  private String currentRepository;  

  /**
   * Instantiates a new site search service impl.
   * 
   * @param portalManagerService the portal manager service
   * @param templateService the template service
   * @param configurationService the configuration service
   * @param repositoryService the repository service
   * 
   * @throws Exception the exception
   */
  public SiteSearchServiceImpl(LivePortalManagerService portalManagerService, 
      TemplateService templateService, 
      WCMConfigurationService configurationService,
      RepositoryService repositoryService) throws Exception {
    this.livePortalManagerService = portalManagerService;
    this.templateService = templateService;    
    this.repositoryService = repositoryService;
    this.configurationService = configurationService;
    this.currentRepository = repositoryService.getCurrentRepository().getConfiguration().getName();
  }

  public WCMPaginatedQueryResult searchSiteContents(QueryCriteria queryCriteria, SessionProvider sessionProvider, int pageSize, boolean allowDuplicated) throws Exception {
    long startTime = System.currentTimeMillis();
    QueryResult queryResult = searchSiteContents(queryCriteria,sessionProvider);    
    long queryTime = System.currentTimeMillis() - startTime;
    WCMPaginatedQueryResult paginatedQueryResult = new WCMPaginatedQueryResult(queryResult,pageSize,allowDuplicated);
    paginatedQueryResult.setQueryTime(queryTime);
    String checkingWord = queryCriteria.getKeyword();
    NodeLocation location = configurationService.getLivePortalsLocation(currentRepository);
    Session session = sessionProvider.getSession(location.getWorkspace(),repositoryService.getCurrentRepository());    
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("SELECT rep:spellcheck() FROM nt:base WHERE jcr:path = '/' AND SPELLCHECK('" + checkingWord + "')", Query.SQL);
    RowIterator rows = query.execute().getRows();
    Value value = rows.nextRow().getValue("rep:spellcheck()");
    String suggestion = null;
    if (value != null) {
      suggestion = value.getString();
    }
    paginatedQueryResult.setSpellSuggestion(suggestion);
    return paginatedQueryResult;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.search.SiteSearchService#search(org.exoplatform.services.wcm.search.QueryCriteria, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public QueryResult searchSiteContents(QueryCriteria queryCriteria, SessionProvider sessionProvider) throws Exception {
    SQLQueryBuilder queryBuilder = new SQLQueryBuilder();
    mapQueryPath(queryCriteria,queryBuilder,sessionProvider);
    mapQueryTypes(queryCriteria,queryBuilder,sessionProvider);
    mapQueryTerm(queryCriteria,queryBuilder);
    orderBy(queryCriteria,queryBuilder,sessionProvider);
    String queryStatement = queryBuilder.createQueryStatement();
    NodeLocation location = configurationService.getLivePortalsLocation(currentRepository);
    Session session = sessionProvider.getSession(location.getWorkspace(),repositoryService.getCurrentRepository());    
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryStatement,Query.SQL);
    return query.execute();    
  }

  /**
   * Map query path.
   * 
   * @param queryCriteria the query criteria
   * @param queryBuilder the query builder
   * @param sessionProvider the session provider
   * 
   * @throws Exception the exception
   */
  private void mapQueryPath(final QueryCriteria queryCriteria, final SQLQueryBuilder queryBuilder, SessionProvider sessionProvider) throws Exception {
    String siteName = queryCriteria.getSiteName();
    String queryPath = null;
    if(siteName != null) {  
      Node portal = livePortalManagerService.getLivePortal(siteName,sessionProvider);
      queryPath = portal.getPath();
    }else {      
      queryPath = configurationService.getLivePortalsLocation(currentRepository).getPath();
    }
    queryBuilder.setQueryPath(queryPath,PATH_TYPE.DECENDANTS);
  }

  /**
   * Map query term.
   * 
   * @param queryCriteria the query criteria
   * @param queryBuilder the query builder
   */
  private void mapQueryTerm(final QueryCriteria queryCriteria, final SQLQueryBuilder queryBuilder) {
    String keyword = queryCriteria.getKeyword();
    QueryTermHelper queryTermHelper = new QueryTermHelper();    
    String queryTerm = queryTermHelper.contains(keyword).allowFuzzySearch().allowSynonymSearch().buildTerm();
    queryBuilder.contains(null,queryTerm,LOGICAL.NULL);
  }

  /**
   * Map query types.
   * 
   * @param queryCriteria the query criteria
   * @param queryBuilder the query builder
   * @param sessionProvider the session provider
   * 
   * @throws Exception the exception
   */
  private void mapQueryTypes(final QueryCriteria queryCriteria, final SQLQueryBuilder queryBuilder, SessionProvider sessionProvider) throws Exception {    
    List<String> selectedNodeTypes = new ArrayList<String>();
    queryBuilder.selectTypes(null);
    boolean searchWebPage = queryCriteria.isSearchWebpage();
    boolean searchDocument = queryCriteria.isSearchDocument();
    //Searh on nt:base
    queryBuilder.fromNodeTypes(null);
    if(searchDocument) {
      List<String> contentTypes = templateService.getDocumentTemplates(currentRepository);
      selectedNodeTypes.addAll(contentTypes);
      if(!searchWebPage) {
        selectedNodeTypes.remove("exo:webContent");
        selectedNodeTypes.remove("exo:htmlFile");
        selectedNodeTypes.remove("exo:cssFile");
        selectedNodeTypes.remove("exo:jsFile");
      }                  
    }else {
      selectedNodeTypes.add("exo:webContent");
      selectedNodeTypes.add("exo:htmlFile");
      selectedNodeTypes.add("nt:file");       
    }            
    NodeTypeManager manager = repositoryService.getRepository(currentRepository).getNodeTypeManager();
    queryBuilder.openGroup(LOGICAL.AND);
    queryBuilder.equal("jcr:primaryType","nt:resource",LOGICAL.NULL);
    for(String type: selectedNodeTypes) {      
      NodeType nodetype = manager.getNodeType(type);
      if(nodetype.isMixin()) {
        queryBuilder.like("jcr:mixinTypes",type,LOGICAL.OR);
      }else {
        queryBuilder.equal("jcr:primaryType",type,LOGICAL.OR);
      }
    }       
    //select * from nt:base where jcr:path lile folderPath/% and NOT jcr:path like folderPath/%/% AND (
    //jcr:primaryType like exo:artile OR jcr:primaryType like exo:webocontent OR jcr:mixinTypes like exo:htmlFile
    queryBuilder.closeGroup();   
  }

  /**
   * Order by.
   * 
   * @param queryCriteria the query criteria
   * @param queryBuilder the query builder
   * @param sessionProvider the session provider
   */
  private void orderBy(final QueryCriteria queryCriteria, final SQLQueryBuilder queryBuilder, SessionProvider sessionProvider) {
    queryBuilder.orderBy("exo:dateCreated",ORDERBY.ASC);
  }    
}
