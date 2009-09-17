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
package org.exoplatform.wcm.webui.selector.document;

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
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.WCMPaginatedQueryResult;
import org.exoplatform.wcm.webui.Utils;
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
 * dzungdev@gmail.com
 * Jan 7, 2009
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/UIDocumentSearch.gtmpl",
    events = {
      @EventConfig(listeners = UIDocumentSearch.SearchDMSActionListener.class)
    }
)

public class UIDocumentSearch extends UIForm {

  /** The Constant KEYWORD_INPUT. */
  private static final String KEYWORD_INPUT = "keyword".intern();
  
  /** The Constant SEARCH_TYPE_NAME. */
  private static final String SEARCH_TYPE_NAME = "By name".intern();
  
  /** The Constant SEARCH_TYPE_TEXT_FULL. */
  private static final String SEARCH_TYPE_TEXT_FULL = "By content".intern();
  
  /** The Constant SEARCH_TYPE. */
  private static final String SEARCH_TYPE = "searchtype".intern();

  /**
   * Instantiates a new uI document search.
   * 
   * @throws Exception the exception
   */
  public UIDocumentSearch() throws Exception {
    UIFormStringInput uiFormStringInput = new UIFormStringInput(KEYWORD_INPUT, KEYWORD_INPUT, null);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(SEARCH_TYPE_NAME, SEARCH_TYPE_NAME));
    options.add(new SelectItemOption<String>(SEARCH_TYPE_TEXT_FULL, SEARCH_TYPE_TEXT_FULL));
    UIFormSelectBox uiFormSelectBox = new UIFormSelectBox(SEARCH_TYPE, SEARCH_TYPE,options);
    addChild(uiFormStringInput);
    addChild(uiFormSelectBox);
  }

  /**
   * Search full text.
   * 
   * @param currentNode the current node
   * @param keyword the keyword
   * 
   * @return the wCM paginated query result
   * 
   * @throws Exception the exception
   */
  private WCMPaginatedQueryResult searchFullText(Node currentNode,String keyword) throws Exception {
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setSearchDocument(true);
    qCriteria.setSearchWebContent(false);
    qCriteria.setSearchWebpage(false);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
    int pageSize = 10;
    SessionProvider sessionProvider = Utils.getSessionProvider(this);
    WCMPaginatedQueryResult wcmPaginatedQueryResult = siteSearchService.searchSiteContents(qCriteria, sessionProvider, pageSize);
    return wcmPaginatedQueryResult;
  }

  /**
   * Search dms by name.
   * 
   * @param currentNode the current node
   * @param keyword the keyword
   * @param workspace the workspace
   * @param maRepository the ma repository
   * 
   * @return the paginated query result
   * 
   * @throws Exception the exception
   */
  private PaginatedQueryResult searchDMSByName(Node currentNode, String keyword, String workspace, ManageableRepository maRepository) throws Exception {
    SessionProvider sessionProvider = Utils.getSessionProvider(this);
    Session session = sessionProvider.getSession(workspace, maRepository);
    String sqlQuery = "SELECT * FROM nt:base " + "WHERE (";
    String tempPrimaryType = "";
    List<String> documentTypes = getDocumentTypes(session);
    int count = 0;
    int size = documentTypes.size();
    for(String documentType: documentTypes) {
      if(count == size - 1 ) {
        tempPrimaryType += "jcr:primaryType LIKE '" + documentType +"') ";
      } else {
        tempPrimaryType += "jcr:primaryType LIKE '" + documentType + "' OR ";
      }
      count++;
    }
    sqlQuery += tempPrimaryType 
    + "AND jcr:path like '"
    + currentNode.getPath()
    + "/"
    + keyword
    + "' "
    + "OR jcr:path LIKE '" + currentNode.getPath() + "/%/" + keyword + "' "
    + "ORDER BY jcr:score DESC";
    QueryManager queManager = session.getWorkspace().getQueryManager();
    Query query = queManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    int pageSize = 10;
    return new PaginatedQueryResult(queryResult, pageSize);
  }

  /**
   * Gets the document types.
   * 
   * @param session the session
   * 
   * @return the document types
   * 
   * @throws Exception the exception
   */
  private List<String> getDocumentTypes(Session session) throws Exception {
    List<String> webContentTypes = new ArrayList<String>(10);
    NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
    for(NodeTypeIterator nodeTypeIterator = nodeTypeManager.getAllNodeTypes(); nodeTypeIterator.hasNext();) {
      NodeType nodeType = nodeTypeIterator.nextNodeType();
      if(nodeType.isNodeType("exo:webContent")) webContentTypes.add(nodeType.getName());
    }
    String repositoryName = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
    TemplateService tempService = getApplicationComponent(TemplateService.class);
    List<String> documentTypes = tempService.getDocumentTemplates(repositoryName);
    documentTypes.removeAll(webContentTypes);
    return documentTypes;
  }

  /**
   * The listener interface for receiving searchDMSAction events.
   * The class that is interested in processing a searchDMSAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSearchDMSActionListener<code> method. When
   * the searchDMSAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SearchDMSActionEvent
   */
  public static class SearchDMSActionListener extends EventListener<UIDocumentSearch> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIDocumentSearch> event) throws Exception {
      UIDocumentSearch uiDMSSearch = event.getSource();
      UIDocumentPathSelector uiDMSPathSelector = uiDMSSearch.getAncestorOfType(UIDocumentPathSelector.class);
      Node currentNode = uiDMSPathSelector.getCurrentNode();
      if(currentNode.getPath().equals("/sites content/live")) {
        UIApplication uiApp = uiDMSSearch.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIWebContentSearchForm.msg-choose-portal",null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      }
      String workspace = currentNode.getSession().getWorkspace().getName();
      ManageableRepository maRepository = (ManageableRepository) currentNode.getSession().getRepository();
      UISelectPathPanel uiSelectPathPanel = uiDMSPathSelector.getChild(UISelectPathPanel.class);
      String searchType = uiDMSSearch.getUIFormSelectBox(SEARCH_TYPE).getValue();
      String keyword = uiDMSSearch.getUIStringInput(KEYWORD_INPUT).getValue();
      if(searchType.equals(SEARCH_TYPE_NAME)) {
        PaginatedQueryResult paQueryResult = uiDMSSearch.searchDMSByName(currentNode, keyword, workspace, maRepository);
        uiSelectPathPanel.getUIPageIterator().setPageList(paQueryResult);
      } else {
        WCMPaginatedQueryResult wcmPaQueryResult = uiDMSSearch.searchFullText(currentNode, keyword);
        uiSelectPathPanel.getUIPageIterator().setPageList(wcmPaQueryResult);        
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDMSPathSelector);
    }
  }
}
