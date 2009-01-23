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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.wcm.publication.defaultlifecycle.Util;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
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
    template = "classpath:groovy/wcm/webui/UIWebContentSearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UIWebContentSearchForm.SearchWebContentActionListener.class),
      @EventConfig(listeners = UIWebContentSearchForm.AddMetadataTypeActionListener.class)
    }
)

public class UIWebContentSearchForm extends UIForm {

  public static final String LOCATION = "location".intern();
  public static final String SEARCH_BY_NAME = "name".intern();
  public static final String SEARCH_BY_CONTENT = "content".intern();
  public static final String RADIO_SEARCH = "WcmRadio".intern();
  final static public String TIME_OPTION = "timeOpt" ;
  final static public String PROPERTY = "property" ;  
  final static public String CONTAIN = "contain" ;
  final static public String START_TIME = "startTime" ;
  final static public String END_TIME = "endTime" ;
  final static public String DOC_TYPE = "docType" ;
  final static public String CATEGORY_TYPE = "categoryType" ;
  final static public String CREATED_DATE = "CREATED" ;
  final static public String MODIFIED_DATE = "MODIFIED" ;
  final static public String EXACTLY_PROPERTY = "exactlyPro" ;
  final static public String CONTAIN_PROPERTY = "containPro" ;
  final static public String NOT_CONTAIN_PROPERTY = "notContainPro" ;
  final static public String DATE_PROPERTY = "datePro" ;
  final static public String NODETYPE_PROPERTY = "nodetypePro" ;
  final static public String CATEGORY_PROPERTY = "categoryPro" ;
  final static private String SPLIT_REGEX = "/|\\s+|:" ;
  final static private String DATETIME_REGEX = 
    "^(\\d{1,2}\\/\\d{1,2}\\/\\d{1,4})\\s*(\\s+\\d{1,2}:\\d{1,2}:\\d{1,2})?$" ;

  public UIWebContentSearchForm() throws Exception {
  }

  public void init() throws Exception {
    List<SelectItemOption<String>> portalNameOptions = new ArrayList<SelectItemOption<String>>();
    for(String portalName: getPortalNames()) {
      portalNameOptions.add(new SelectItemOption<String>(portalName, portalName));
    }
    UIFormSelectBox portalNameSelectBox = new UIFormSelectBox(LOCATION, LOCATION, portalNameOptions);
    addChild(portalNameSelectBox);

    addUIFormInput(new UIFormStringInput(SEARCH_BY_NAME,SEARCH_BY_NAME,null));
    addUIFormInput(new UIFormStringInput(SEARCH_BY_CONTENT, SEARCH_BY_CONTENT, null));

    addUIFormInput(new UIFormStringInput(PROPERTY, PROPERTY, null));
    addUIFormInput(new UIFormStringInput(CONTAIN, CONTAIN, null));

    List<SelectItemOption<String>> dateOptions = new ArrayList<SelectItemOption<String>>();
    dateOptions.add(new SelectItemOption<String>(CREATED_DATE,CREATED_DATE));
    dateOptions.add(new SelectItemOption<String>(MODIFIED_DATE,MODIFIED_DATE));
    addUIFormInput(new UIFormSelectBox(TIME_OPTION,TIME_OPTION, dateOptions));
    UIFormDateTimeInput startTime = new UIFormDateTimeInput(START_TIME, START_TIME, null);
    startTime.setDisplayTime(false);
    addUIFormInput(startTime);
    UIFormDateTimeInput endTime = new UIFormDateTimeInput(END_TIME, END_TIME, null);
    endTime.setDisplayTime(false);
    addUIFormInput(endTime);

    addUIFormInput(new UIFormStringInput(DOC_TYPE, DOC_TYPE, null));

    addUIFormInput(new UIFormStringInput(CATEGORY_TYPE, CATEGORY_TYPE, null));

    setActions(new String[] {"SearchWebContent"} );
  }

  public static class AddMetadataTypeActionListener extends EventListener<UIWebContentSearchForm> {
    public void execute(Event<UIWebContentSearchForm> event) throws Exception {
      UIWebContentSearchForm uiWCSearchForm = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = uiWCSearchForm.getParent();
      uiWCTabSelector.initMetadataPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
    }
  }

//private WCMPaginatedQueryResult searchFullText(Node currentNode,String keyword) throws Exception {   
//QueryCriteria qCriteria = new QueryCriteria();
//qCriteria.setSearchDocument(false);
//qCriteria.setSearchWebpage(false);
//qCriteria.setSearchWebContent(true);
//qCriteria.setQueryPath(currentNode.getPath());
//qCriteria.setKeyword(keyword);
//SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
//int pageSize = 10;
//return siteSearchService.searchSiteContents(qCriteria, SessionProviderFactory.createSessionProvider(), pageSize);
//}

//private PaginatedQueryResult searchWebContentByName(Node currentNode, String keyword, String workspace, ManageableRepository maRepository) throws Exception {
//Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
//List<String> webContentTypes = getWebContentTypes(session);
//String sqlQuery = "SELECT * FROM nt:base " 
//+ "WHERE (";
//String tempQuery = "";
//int count = 0;
//int size = webContentTypes.size();
//for(String contentType : webContentTypes) {
//if(count == size -1 ) {
//tempQuery += "jcr:primaryType like '" + contentType +"') ";
//} else { 
//tempQuery += "jcr:primaryType like '" + contentType +"' OR ";
//}
//count++;
//}
//sqlQuery += tempQuery
//+ "AND jcr:path like '"
//+ currentNode.getPath()
//+ "/"
//+ keyword
//+ "' "
//+ "OR jcr:path like '" + currentNode.getPath() + "/%/" + keyword + "' "
//+ "ORDER BY jcr:score DESC";
//QueryManager queManager = session.getWorkspace().getQueryManager();
//Query query = queManager.createQuery(sqlQuery, Query.SQL);
//QueryResult queryResult = query.execute();
//PaginatedQueryResult paginatedQueryResult = null;
//int pageSize = 10;
//paginatedQueryResult = new PaginatedQueryResult(queryResult, pageSize); 
//return paginatedQueryResult;
//}

//private List<String> getWebContentTypes(Session session) throws Exception {
//List<String> list = new ArrayList<String>(10);
//NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
//for(NodeTypeIterator nodeTypeIterator = nodeTypeManager.getAllNodeTypes(); nodeTypeIterator.hasNext();) {
//NodeType nodeType = nodeTypeIterator.nextNodeType();
//if(nodeType.isNodeType("exo:webContent")) list.add(nodeType.getName());
//}
//return list;
//}

  private List<String> getPortalNames() throws Exception {
    List<String> portalNames = new ArrayList<String>();
    DataStorage service = Util.getServices(DataStorage.class);
    org.exoplatform.portal.config.Query<PortalConfig> query = 
      new org.exoplatform.portal.config.Query<PortalConfig>(null,null,null,null,PortalConfig.class);
    PageList pageList = service.find(query);
    UserACL userACL = Util.getServices(UserACL.class);
    org.exoplatform.web.application.RequestContext context = WebuiRequestContext.getCurrentInstance();
    String userId = context.getRemoteUser();
    for(Object object: pageList.getAll()) {
      PortalConfig portalConfig = (PortalConfig) object;
      if(userACL.hasPermission(portalConfig, userId)) {
        portalNames.add(portalConfig.getName()) ;
      }
    }
    return portalNames;
  }

  static public class SearchWebContentActionListener extends EventListener<UIWebContentSearchForm> {
    public void execute(Event<UIWebContentSearchForm> event) throws Exception {
      UIWebContentSearchForm uiWCSearch = event.getSource();
      RequestContext reqContext = event.getRequestContext();
    }
  }
}
