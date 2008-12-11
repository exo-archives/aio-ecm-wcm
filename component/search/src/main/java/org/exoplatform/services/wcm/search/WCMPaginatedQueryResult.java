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

import javax.jcr.Node;
import javax.jcr.query.QueryResult;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoa.phamvu@exoplatform.com
 * Oct 21, 2008
 */
public class WCMPaginatedQueryResult extends PaginatedQueryResult {

  private long queryTime;  
  private String spellSuggestion;
  private QueryCriteria queryCriteria;  
  /**
   * Instantiates a new wCM paginated query result.
   * 
   * @param pageSize
   *          the page size
   */
  public WCMPaginatedQueryResult(int pageSize) {
    super(pageSize);
  }

  /**
   * Instantiates a new wCM paginated query result.
   * 
   * @param queryResult
   *          the query result
   * @param pageSize
   *          the page size
   * @param allowDuplicated
   *          the allow duplicated
   * 
   * @throws Exception
   *           the exception
   */
  public WCMPaginatedQueryResult(QueryResult queryResult, QueryCriteria queryCriteria, int pageSize) throws Exception {
    super(queryResult, pageSize);
    this.queryCriteria = queryCriteria;
  }

  public void setQueryTime(long time) {
    this.queryTime = time;
  }

  public long getQueryTimeInSecond() {
    return this.queryTime / 1000;
  }

  public QueryCriteria getQueryCriteria() {
    return this.queryCriteria;
  }

  public void setQueryCriteria(QueryCriteria queryCriteria) {
    this.queryCriteria = queryCriteria;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.exoplatform.wcm.webui.paginator.PaginatedQueryResult#filterNodeToDisplay
   * (javax.jcr.Node)
   */
  protected Node filterNodeToDisplay(Node node) throws Exception {
    Node displayNode = node;
    if (displayNode.isNodeType("nt:resource")) {
      displayNode = node.getParent();
    }
    if (displayNode.isNodeType("exo:htmlFile")) {
      Node parent = displayNode.getParent();
      if (parent.isNodeType("exo:webContent"))
        displayNode = parent;
    }
    if (queryCriteria.isSearchWebpage() && !queryCriteria.isSearchDocument()) {
      if (!displayNode.isNodeType("publication:webpagesPublication"))
        return null;
    }
    if (queryCriteria.isSearchDocument() && !queryCriteria.isSearchWebpage()) {
      if (displayNode.isNodeType("publication:webpagesPublication"))
        return null;
    }    
    return displayNode;
  }

  public String getSpellSuggestion() {
    return spellSuggestion;
  }

  public void setSpellSuggestion(String spellSuggestion) {
    this.spellSuggestion = spellSuggestion;
  }
}
