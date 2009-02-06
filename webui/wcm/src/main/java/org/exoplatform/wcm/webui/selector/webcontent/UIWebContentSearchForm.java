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
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.WCMPaginatedQueryResult;
import org.exoplatform.services.wcm.search.QueryCriteria.DATE_RANGE_SELECTED;
import org.exoplatform.services.wcm.search.QueryCriteria.DatetimeRange;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
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
      @EventConfig(listeners = UIWebContentSearchForm.AddMetadataTypeActionListener.class),
      @EventConfig(listeners = UIWebContentSearchForm.AddNodeTypeActionListener.class),
      @EventConfig(listeners = UIWebContentSearchForm.AddCategoryActionListener.class)
    }
)

public class UIWebContentSearchForm extends UIForm implements UISelectable {

  public static final String LOCATION = "location".intern();
  public static final String SEARCH_BY_NAME = "name".intern();
  public static final String SEARCH_BY_CONTENT = "content".intern();
  public static final String RADIO_NAME = "WcmRadio".intern();
  final static public String TIME_OPTION = "timeOpt";
  final static public String PROPERTY = "property";  
  final static public String CONTAIN = "contain";
  final static public String START_TIME = "startTime";
  final static public String END_TIME = "endTime";
  final static public String DOC_TYPE = "docType";
  final static public String CATEGORY_TYPE = "categoryType";
  final static public String CREATED_DATE = "CREATED";
  final static public String MODIFIED_DATE = "MODIFIED";
  final static public String EXACTLY_PROPERTY = "exactlyPro";
  final static public String CONTAIN_PROPERTY = "containPro";
  final static public String NOT_CONTAIN_PROPERTY = "notContainPro";
  final static public String DATE_PROPERTY = "datePro";
  final static public String NODETYPE_PROPERTY = "nodetypePro";
  final static public String CHECKED_RADIO_ID = "checkedRadioId".intern();
  final static private String SPLIT_REGEX = "/|\\s+|:";
  final static private String DATETIME_REGEX = 
    "^(\\d{1,2}\\/\\d{1,2}\\/\\d{1,4})\\s*(\\s+\\d{1,2}:\\d{1,2}:\\d{1,2})?$";

  private String checkedRadioId;

  public UIWebContentSearchForm() throws Exception {
  }

  public void init() throws Exception {
    List<SelectItemOption<String>> portalNameOptions = new ArrayList<SelectItemOption<String>>();
    List<String> portalNames = getPortalNames();
    for(String portalName: portalNames) {
      portalNameOptions.add(new SelectItemOption<String>(portalName, portalName));
    }
    UIFormSelectBox portalNameSelectBox = new UIFormSelectBox(LOCATION, LOCATION, portalNameOptions);
    portalNameSelectBox.setDefaultValue(portalNames.get(0));
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

  private List<String> getPortalNames() throws Exception {
    List<String> portalNames = new ArrayList<String>();
    String currentPortalName = org.exoplatform.portal.webui.util.Util.getUIPortal().getName();
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    String repository = repoService.getCurrentRepository().getConfiguration().getName();
    WCMConfigurationService configService = getApplicationComponent(WCMConfigurationService.class);
    String sharedPortalName = configService.getSharedPortalName(repository);
    portalNames.add(currentPortalName);
    portalNames.add(sharedPortalName);
    return portalNames;
  }

  public static class AddMetadataTypeActionListener extends EventListener<UIWebContentSearchForm> {
    public void execute(Event<UIWebContentSearchForm> event) throws Exception {
      UIWebContentSearchForm uiWCSearchForm = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = uiWCSearchForm.getParent();
      uiWCSearchForm.setCheckedRadioId(event.getRequestContext().getRequestParameter(CHECKED_RADIO_ID));
      uiWCTabSelector.initMetadataPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
    }
  }

  public static class AddNodeTypeActionListener extends EventListener<UIWebContentSearchForm> {
    public void execute(Event<UIWebContentSearchForm> event) throws Exception {
      UIWebContentSearchForm uiWCSearchForm = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = uiWCSearchForm.getParent();
      uiWCSearchForm.setCheckedRadioId(event.getRequestContext().getRequestParameter(CHECKED_RADIO_ID));
      uiWCTabSelector.initNodeTypePopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
    }    
  }

  public static class AddCategoryActionListener extends EventListener<UIWebContentSearchForm> {
    public void execute(Event<UIWebContentSearchForm> event) throws Exception {
      UIWebContentSearchForm uiWCSearchForm = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = uiWCSearchForm.getParent();
      uiWCSearchForm.setCheckedRadioId(event.getRequestContext().getRequestParameter(CHECKED_RADIO_ID));
      uiWCTabSelector.initCategoryPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
    }    
  } 

  private void searchWebContentByName(String keyword, 
      String siteName, UISelectPathPanel uiSelectPathPanel) throws Exception {
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setSearchDocument(false);
    qCriteria.setSearchWebContent(true);
    qCriteria.setFulltextSearch(false);
    qCriteria.setSearchWebpage(false);
    qCriteria.setSiteName(siteName);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    int pageSize = 10;
    PaginatedQueryResult pagResult = siteSearch.searchSiteContents(qCriteria, SessionProviderFactory.createSessionProvider(), pageSize);
    uiSelectPathPanel.getUIPageIterator().setPageList(pagResult);
  }

  private void searchWebContentByFulltext(String keyword, 
      String siteName, UISelectPathPanel uiSelectPathPanel) throws Exception {
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setSearchDocument(false);
    qCriteria.setSearchWebpage(false);
    qCriteria.setSearchWebContent(true);
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(QueryCriteria.ALL_PROPERTY_SCOPE);
    qCriteria.setSiteName(siteName);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    int pageSize = 10;
    PaginatedQueryResult pagResult = siteSearch.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
    uiSelectPathPanel.getUIPageIterator().setPageList(pagResult);
  }

  private void searchWebContentByProperty(String property, String keyword, 
      String siteName, UISelectPathPanel uiSelectPathPanel) throws Exception {
//  QueryCriteria qCriteria = new QueryCriteria();
//  qCriteria.setSearchDocument(false);
//  qCriteria.setSearchWebpage(false);
//  qCriteria.setSearchWebContent(true)
//  qCriteria.setQueryMetadatas(queryMetadatas)
  }

  private void searchWebContentByDate(DATE_RANGE_SELECTED dateRangeSelected, 
      Calendar fromDate, Calendar endDate, String siteName, UISelectPathPanel uiSelectPathPanel) throws Exception {
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setDateRangeSelected(dateRangeSelected);
    DatetimeRange dateTimeRange = new QueryCriteria.DatetimeRange(fromDate, endDate);
    if(DATE_RANGE_SELECTED.CREATED.equals(dateRangeSelected)) {
      qCriteria.setCreatedDateRange(dateTimeRange);
    } else if(DATE_RANGE_SELECTED.MODIFIDED.equals(dateRangeSelected)) {
      qCriteria.setLastModifiedDateRange(dateTimeRange);
    }
    qCriteria.setSearchDocument(false);
    qCriteria.setSearchWebpage(false);
    qCriteria.setSearchWebContent(true);
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(null);
    qCriteria.setSiteName(siteName);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    int pageSize = 10;
    WCMPaginatedQueryResult pagResult = siteSearch.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
    uiSelectPathPanel.getUIPageIterator().setPageList(pagResult);
  }


  private boolean haveEmptyField(UIApplication uiApp, Event<UIWebContentSearchForm> event, String... fields) throws Exception {
    for(String field : fields) {
      if(field == null) {
        uiApp.addMessage(new ApplicationMessage(
            "UIWebContentSearchForm.empty-field", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return true;
      }
    }
    return false;
  }

  static public class SearchWebContentActionListener extends EventListener<UIWebContentSearchForm> {
    public void execute(Event<UIWebContentSearchForm> event) throws Exception {
      UIWebContentSearchForm uiWCSearch = event.getSource();
      String radioValue = event.getRequestContext().getRequestParameter(RADIO_NAME);
      String siteName = uiWCSearch.getUIStringInput(UIWebContentSearchForm.LOCATION).getValue();
      UIWebContentTabSelector uiWCTabSelector = uiWCSearch.getParent();
      UISelectPathPanel uiSelectPathPanel = uiWCTabSelector.getChild(UISelectPathPanel.class);
      UIApplication uiApp = uiWCSearch.getAncestorOfType(UIApplication.class);
      if(UIWebContentSearchForm.SEARCH_BY_NAME.equals(radioValue)) {
        String keyword = uiWCSearch.getUIStringInput(radioValue).getValue();
        if(uiWCSearch.haveEmptyField(uiApp, event, keyword)) return;
        uiWCSearch.searchWebContentByName(keyword, siteName, uiSelectPathPanel);
      } else if(UIWebContentSearchForm.SEARCH_BY_CONTENT.equals(radioValue)) {
        String keyword = uiWCSearch.getUIStringInput(radioValue).getValue();
        if(uiWCSearch.haveEmptyField(uiApp, event, keyword)) return;
        uiWCSearch.searchWebContentByFulltext(keyword, siteName, uiSelectPathPanel);
      } else if(UIWebContentSearchForm.PROPERTY.equals(radioValue)) {
        String property = uiWCSearch.getUIStringInput(UIWebContentSearchForm.PROPERTY).getValue();
        String keyword = uiWCSearch.getUIStringInput(UIWebContentSearchForm.CONTAIN).getValue();
        if(uiWCSearch.haveEmptyField(uiApp, event, property, keyword)) return;

      } else if(UIWebContentSearchForm.TIME_OPTION.equals(radioValue)) {
        String dateRangeSelected = uiWCSearch.getUIStringInput(UIWebContentSearchForm.TIME_OPTION).getValue();
        String fromDateStr = uiWCSearch.getUIFormDateTimeInput(UIWebContentSearchForm.START_TIME).getValue();
        String endDateStr = uiWCSearch.getUIFormDateTimeInput(UIWebContentSearchForm.END_TIME).getValue();
        if(uiWCSearch.haveEmptyField(uiApp, event, fromDateStr, endDateStr)) return;
        Calendar fromDate = uiWCSearch.getUIFormDateTimeInput(UIWebContentSearchForm.START_TIME).getCalendar();
        Calendar endDate = uiWCSearch.getUIFormDateTimeInput(UIWebContentSearchForm.END_TIME).getCalendar();
        System.out.println("=========> dateRangeSelected: " + dateRangeSelected);
        System.out.println("=========> formDate: " + fromDate);
        System.out.println("=========> endDate: " + endDate);
        if(UIWebContentSearchForm.CREATED_DATE.equals(dateRangeSelected)) {
          uiWCSearch.searchWebContentByDate(DATE_RANGE_SELECTED.CREATED, 
              fromDate, endDate, siteName, uiSelectPathPanel);
        } else {
          uiWCSearch.searchWebContentByDate(DATE_RANGE_SELECTED.MODIFIDED, 
              fromDate, endDate, siteName, uiSelectPathPanel);
        }
      }
      uiWCTabSelector.setSelectedTab(uiSelectPathPanel.getId());
    }
  }

  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue(value.toString());
    UIWebContentTabSelector uiWCTabSelector = 
      getAncestorOfType(UIWebContentTabSelector.class);
    uiWCTabSelector.removeChild(UIPopupWindow.class);
    UIWebContentSearchForm uiWCSearchForm = uiWCTabSelector.getChild(UIWebContentSearchForm.class);
    uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
    String checkedRadioId = uiWCSearchForm.getCheckedRadioId();
    org.exoplatform.portal.webui.util.Util.getPortalRequestContext()
    .getJavascriptManager().addJavascript("document.getElementById('"+checkedRadioId+"').checked = true;");
  }

  public String getCheckedRadioId() {
    return checkedRadioId;
  }

  public void setCheckedRadioId(String checkedRadioId) {
    this.checkedRadioId = checkedRadioId;
  }
}
