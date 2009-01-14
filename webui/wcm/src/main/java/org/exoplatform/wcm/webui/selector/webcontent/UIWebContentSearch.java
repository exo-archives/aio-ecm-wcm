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
package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.WCMPaginatedQueryResult;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jan 5, 2009  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/UIWebContentSearch.gtmpl",
    events = {
      @EventConfig(listeners = UIWebContentSearch.SearchWebContentActionListener.class)
    }
)

public class UIWebContentSearch extends UIForm {

  private static final String KEYWORD_INPUT = "keyword".intern();
  private static final String SEARCH_TYPE_NAME = "By name".intern();
  private static final String SEARCH_TYPE_TEXT_FULL = "By content".intern();
  private static final String SEARCH_TYPE = "searchtype".intern();

  public UIWebContentSearch() throws Exception {
    UIFormStringInput uiFormStringInput = new UIFormStringInput(KEYWORD_INPUT, KEYWORD_INPUT, null);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(SEARCH_TYPE_NAME, SEARCH_TYPE_NAME));
    options.add(new SelectItemOption<String>(SEARCH_TYPE_TEXT_FULL, SEARCH_TYPE_TEXT_FULL));
    UIFormSelectBox uiFormSelectBox = new UIFormSelectBox(SEARCH_TYPE, SEARCH_TYPE,options);
    addChild(uiFormStringInput);
    addChild(uiFormSelectBox);
  }

  private WCMPaginatedQueryResult searchFullText(Node currentNode,String keyword) throws Exception {   
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setSearchDocument(false);
    qCriteria.setSearchWebpage(false);
    qCriteria.setSearchWebContent(true);
    qCriteria.setQueryPath(currentNode.getPath());
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
    int pageSize = 10;
    return siteSearchService.searchSiteContents(qCriteria, SessionProviderFactory.createSessionProvider(), pageSize);
  }

  private PaginatedQueryResult searchWebContentByName(Node currentNode, String keyword, String workspace, ManageableRepository maRepository) throws Exception {
    Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    List<String> webContentTypes = getWebContentTypes(session);
    String sqlQuery = "SELECT * FROM nt:base " 
      + "WHERE (";
    String tempQuery = "";
    int count = 0;
    int size = webContentTypes.size();
    for(String contentType : webContentTypes) {
      if(count == size -1 ) {
        tempQuery += "jcr:primaryType like '" + contentType +"') ";
      } else { 
        tempQuery += "jcr:primaryType like '" + contentType +"' OR ";
      }
      count++;
    }
    sqlQuery += tempQuery
    + "AND jcr:path like '"
    + currentNode.getPath()
    + "/"
    + keyword
    + "' "
    + "OR jcr:path like '" + currentNode.getPath() + "/%/" + keyword + "' "
    + "ORDER BY jcr:score DESC";
    QueryManager queManager = session.getWorkspace().getQueryManager();
    Query query = queManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    PaginatedQueryResult paginatedQueryResult = null;
    int pageSize = 10;
    paginatedQueryResult = new PaginatedQueryResult(queryResult, pageSize); 
    return paginatedQueryResult;
  }

  private List<String> getWebContentTypes(Session session) throws Exception {
    List<String> list = new ArrayList<String>(10);
    NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
    for(NodeTypeIterator nodeTypeIterator = nodeTypeManager.getAllNodeTypes(); nodeTypeIterator.hasNext();) {
      NodeType nodeType = nodeTypeIterator.nextNodeType();
      if(nodeType.isNodeType("exo:webContent")) list.add(nodeType.getName());
    }
    return list;
  }



  static public class SearchWebContentActionListener extends EventListener<UIWebContentSearch> {
    public void execute(Event<UIWebContentSearch> event) throws Exception {
      UIWebContentSearch uiWCSearch = event.getSource();
      UIWebContentPathSelector uiWCPathSelector = uiWCSearch.getAncestorOfType(UIWebContentPathSelector.class);
      Node currentNode = uiWCPathSelector.getCurrentNode();
      if(currentNode.getPath().equals("/sites content/live")) {
        UIApplication uiApp = uiWCSearch.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIWebContentSearch.msg-choose-portal",null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      }
      String workspace = currentNode.getSession().getWorkspace().getName();
      ManageableRepository maRepository = (ManageableRepository) currentNode.getSession().getRepository();
      String searchType = uiWCSearch.getUIFormSelectBox(SEARCH_TYPE).getValue();
      String keyword = uiWCSearch.getUIStringInput(KEYWORD_INPUT).getValue();
      UISelectPathPanel uiSelectPathPanel = uiWCPathSelector.getChild(UISelectPathPanel.class);
      if(searchType.equals(SEARCH_TYPE_NAME)) {
        PaginatedQueryResult paginatedQueryResult = uiWCSearch.searchWebContentByName(currentNode, keyword, workspace, maRepository);
        uiSelectPathPanel.getUIPageIterator().setPageList(paginatedQueryResult);
      } else {
        WCMPaginatedQueryResult wcmPQResult = uiWCSearch.searchFullText(currentNode, keyword);
        uiSelectPathPanel.getUIPageIterator().setPageList(wcmPQResult);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCPathSelector);
    }
  }
}
