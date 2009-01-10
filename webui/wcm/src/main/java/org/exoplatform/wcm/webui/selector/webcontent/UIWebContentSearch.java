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
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
  private static final String SEARCH_TYPE_NAME = "name".intern();
  private static final String SEARCH_TYPE_TEXT_FULL = "full text".intern();
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


  private List<Node> searchFullText(Node currentNode,String keyword, String workspace, ManageableRepository maRepository) throws Exception {
    List<Node> list = new ArrayList<Node>(10);
    if(currentNode == null) return list;
    Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    String sqlQuery = "select * from nt:base " +
    "where " +
    "jcr:path like '" + currentNode.getPath() +"/%'" +
    " and contains(.,'"+keyword+"')" +
    " and (jcr:primaryType like 'exo:webContent' or jcr:primaryType like 'nt:file' or jcr:primaryType like 'nt:resource')" +
    " order by exo:dateCreated DESC";
    QueryManager queManager = session.getWorkspace().getQueryManager();
    Query query = queManager.createQuery(sqlQuery, Query.SQL);
    QueryResult result = query.execute();
    for(NodeIterator nodeIterator = result.getNodes();nodeIterator.hasNext();) {
      Node child = nodeIterator.nextNode();
      Node parentNode = child.getParent();
      while(!parentNode.isNodeType("exo:webContent")) {
        parentNode = parentNode.getParent();
      }
      if(child.isNodeType("exo:hiddenable")) continue;
      list.add(parentNode);
    }
    return list;
  }

  private List<Node> searchWebContentByName(Node currentNode, String keyword, String workspace, ManageableRepository maRepository) throws Exception {
    List<Node> list = new ArrayList<Node>(10);
    if(currentNode == null) return list;
    Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    String sqlQuery = "SELECT * FROM nt:base " 
      + "WHERE jcr:primaryType like 'exo:webContent' "
      + "AND jcr:path like '"
      + currentNode.getPath()
      + "/"
      + keyword
      + "' "
      + "OR jcr:path like '" + currentNode.getPath() + "/%/" + keyword + "' ";
    QueryManager queManager = session.getWorkspace().getQueryManager();
    Query query = queManager.createQuery(sqlQuery, Query.SQL);
    QueryResult result = query.execute();
    for(NodeIterator nodeIterator = result.getNodes(); nodeIterator.hasNext();) {
      Node child = nodeIterator.nextNode();
      if(child.isNodeType("exo:hiddenable")) continue;
      list.add(child);
    }
    return list;
  }
  static public class SearchWebContentActionListener extends EventListener<UIWebContentSearch> {
    public void execute(Event<UIWebContentSearch> event) throws Exception {
      UIWebContentSearch uiWCSearch = event.getSource();
      UIWebContentPathSelector uiWCPathSelector = uiWCSearch.getAncestorOfType(UIWebContentPathSelector.class);
      
      Node currentNode = uiWCPathSelector.getCurrentNode();
      String workspace = currentNode.getSession().getWorkspace().getName();
      ManageableRepository maRepository = (ManageableRepository) currentNode.getSession().getRepository();
      String searchType = uiWCSearch.getUIFormSelectBox(SEARCH_TYPE).getValue();
      String keyword = uiWCSearch.getUIStringInput(KEYWORD_INPUT).getValue();
      List<Node> resultNodes = new ArrayList<Node>();
      if(searchType.equals(SEARCH_TYPE_NAME)) {
        resultNodes = uiWCSearch.searchWebContentByName(currentNode, keyword, workspace, maRepository);
      } else {
        resultNodes = uiWCSearch.searchFullText(currentNode, keyword, workspace, maRepository);
      }
      ObjectPageList objPageList = new ObjectPageList(resultNodes, 10);
      UISelectPathPanel uiSelectPathPanel = uiWCPathSelector.getChild(UISelectPathPanel.class);
      uiSelectPathPanel.getUIPageIterator().setPageList(objPageList);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCPathSelector);
    }
  }
}
