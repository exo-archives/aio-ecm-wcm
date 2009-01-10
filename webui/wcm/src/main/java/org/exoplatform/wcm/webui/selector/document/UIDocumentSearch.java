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
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
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

  private static final String KEYWORD_INPUT = "keyword".intern();
  private static final String SEARCH_TYPE_NAME = "name".intern();
  private static final String SEARCH_TYPE_TEXT_FULL = "textfull".intern();
  private static final String SEARCH_TYPE = "searchtype".intern();

  public UIDocumentSearch() throws Exception {
    UIFormStringInput uiFormStringInput = new UIFormStringInput(KEYWORD_INPUT, KEYWORD_INPUT, null);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(SEARCH_TYPE_NAME, SEARCH_TYPE_NAME));
    options.add(new SelectItemOption<String>(SEARCH_TYPE_TEXT_FULL, SEARCH_TYPE_TEXT_FULL));
    UIFormSelectBox uiFormSelectBox = new UIFormSelectBox(SEARCH_TYPE, SEARCH_TYPE,options);
    addChild(uiFormStringInput);
    addChild(uiFormSelectBox);
  }

  private List<Node> searchFullText(Node currentNode,String keyword, String workspace, ManageableRepository maRepository, String[] acceptedNodeTypes) throws Exception {
    List<Node> list = new ArrayList<Node>(10);
    if(currentNode == null) return list;
    Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    String sqlQuery = "SELECT * FROM nt:base " 
      + "WHERE " 
      + "jcr:path LIKE '" + currentNode.getPath() +"/%'" 
      + " AND contains(.,'"+keyword+"')" 
      + " AND (";
    String tempPrimaryType = "";
    for(String acceptedNodeType: acceptedNodeTypes) {
      tempPrimaryType += "jcr:primaryType LIKE '" + acceptedNodeType+"' OR ";
    }
    sqlQuery += tempPrimaryType + "jcr:primaryType LIKE 'nt:resource')" 
    + " order by exo:dateCreated DESC";
    QueryManager queManager = session.getWorkspace().getQueryManager();
    Query query = queManager.createQuery(sqlQuery, Query.SQL);
    QueryResult result = query.execute();
    for(NodeIterator nodeIterator = result.getNodes();nodeIterator.hasNext();) {
      Node child = nodeIterator.nextNode();
      Node parentNode = child.getParent();
      if(child.isNodeType("exo:hiddenable")) continue;
      list.add(child);
    }
    return list;
  }

  private List<Node> searchWebContentByName(Node currentNode, String keyword, String workspace, ManageableRepository maRepository, String[] acceptedNodeTypes) throws Exception {
    List<Node> list = new ArrayList<Node>(10);
    if(currentNode == null) return list;
    Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    String sqlQuery = "SELECT * FROM nt:base " 
      + "WHERE (";
    String tempPrimaryType = "";
    for(String acceptedNodeType: acceptedNodeTypes) {
      tempPrimaryType += "jcr:primaryType LIKE '" + acceptedNodeType+"' OR ";
    }
    sqlQuery += tempPrimaryType 
    + "jcr:primaryType like 'nt:resource') "
    + "AND jcr:path like '"
    + currentNode.getPath()
    + "/"
    + keyword
    + "' "
    + "OR jcr:path LIKE '" + currentNode.getPath() + "/%/" + keyword + "' ";
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

  public static class SearchDMSActionListener extends EventListener<UIDocumentSearch> {
    public void execute(Event<UIDocumentSearch> event) throws Exception {
      UIDocumentSearch uiDMSSearch = event.getSource();
      UIDocumentPathSelector uiDMSPathSelector = uiDMSSearch.getAncestorOfType(UIDocumentPathSelector.class);
      Node currentNode = uiDMSPathSelector.getCurrentNode();
      String workspace = currentNode.getSession().getWorkspace().getName();
      ManageableRepository maRepository = (ManageableRepository) currentNode.getSession().getRepository();
      String repositoryName = maRepository.getConfiguration().getName();
      TemplateService templateService = uiDMSSearch.getApplicationComponent(TemplateService.class);
      List<String> acceptedNodeTypes = templateService.getDocumentTemplates(repositoryName);
      String[] nts = {};
      String [] arrAcceptedNodeTypes = new String[acceptedNodeTypes.size()];
      acceptedNodeTypes.toArray(arrAcceptedNodeTypes) ;
      UISelectPathPanel uiSelectPathPanel = uiDMSPathSelector.getChild(UISelectPathPanel.class);
      uiSelectPathPanel.setAcceptedNodeTypes(arrAcceptedNodeTypes);
      String searchType = uiDMSSearch.getUIFormSelectBox(SEARCH_TYPE).getValue();
      String keyword = uiDMSSearch.getUIStringInput(KEYWORD_INPUT).getValue();
      List<Node> resultNodes = new ArrayList<Node>();
      if(searchType.equals(SEARCH_TYPE_NAME)) {
        resultNodes = uiDMSSearch.searchWebContentByName(currentNode, keyword, workspace, maRepository, arrAcceptedNodeTypes);
      } else {
        resultNodes = uiDMSSearch.searchFullText(currentNode, keyword, workspace, maRepository, arrAcceptedNodeTypes);
      }
      ObjectPageList objPageList = new ObjectPageList(resultNodes, 2);
      uiSelectPathPanel.getUIPageIterator().setPageList(objPageList);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDMSPathSelector);
    }
  }
}
