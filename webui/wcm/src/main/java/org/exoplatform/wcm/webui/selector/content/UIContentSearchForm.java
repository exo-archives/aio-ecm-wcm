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
package org.exoplatform.wcm.webui.selector.content;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.QueryCriteria.DATE_RANGE_SELECTED;
import org.exoplatform.services.wcm.search.QueryCriteria.DatetimeRange;
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
    template = "classpath:groovy/wcm/webui/selector/content/UIContentSearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UIContentSearchForm.SearchWebContentActionListener.class),
      @EventConfig(listeners = UIContentSearchForm.AddMetadataTypeActionListener.class),
      @EventConfig(listeners = UIContentSearchForm.AddNodeTypeActionListener.class)
    }
)

public class UIContentSearchForm extends UIForm {

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
  final static public String CATEGORY = "category";
  final static public String CREATED_DATE = "CREATED";
  final static public String MODIFIED_DATE = "MODIFIED";
  final static public String EXACTLY_PROPERTY = "exactlyPro";
  final static public String CONTAIN_PROPERTY = "containPro";
  final static public String NOT_CONTAIN_PROPERTY = "notContainPro";
  final static public String DATE_PROPERTY = "datePro";
  final static public String NODETYPE_PROPERTY = "nodetypePro";
  final static public String CHECKED_RADIO_ID = "checkedRadioId".intern();

  private String checkedRadioId;

  public UIContentSearchForm() throws Exception {
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
    UIFormDateTimeInput startTime = new UIFormDateTimeInput(START_TIME, START_TIME, null, true);
    addUIFormInput(startTime);
    UIFormDateTimeInput endTime = new UIFormDateTimeInput(END_TIME, END_TIME, null, true);
    addUIFormInput(endTime);
    addUIFormInput(new UIFormStringInput(DOC_TYPE, DOC_TYPE, null));
//  addUIFormInput(new UIFormStringInput(CATEGORY, CATEGORY, null));

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

  public static class AddMetadataTypeActionListener extends EventListener<UIContentSearchForm> {
    public void execute(Event<UIContentSearchForm> event) throws Exception {
      UIContentSearchForm contentSearchForm = event.getSource();
      UIContentSelector contentSelector = contentSearchForm.getParent();
      contentSearchForm.setCheckedRadioId(event.getRequestContext().getRequestParameter(CHECKED_RADIO_ID));
      contentSelector.initMetadataPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(contentSelector);
    }
  }

  public static class AddNodeTypeActionListener extends EventListener<UIContentSearchForm> {
    public void execute(Event<UIContentSearchForm> event) throws Exception {
      UIContentSearchForm contentSearchForm = event.getSource();
      UIContentSelector contentSelector = contentSearchForm.getParent();
      contentSearchForm.setCheckedRadioId(event.getRequestContext().getRequestParameter(CHECKED_RADIO_ID));
      contentSelector.initNodeTypePopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(contentSelector);
    }    
  } 

  private PaginatedQueryResult searchWebContentByName(String keyword, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(false);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchWebContentByFulltext(String keyword, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(QueryCriteria.ALL_PROPERTY_SCOPE);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchWebContentByProperty(String property, 
      String keyword, QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(property);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
    return siteSearchService.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchWebContentByDate(DATE_RANGE_SELECTED dateRangeSelected, 
      Calendar fromDate, Calendar endDate, QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setDateRangeSelected(dateRangeSelected);
    DatetimeRange dateTimeRange = new QueryCriteria.DatetimeRange(fromDate, endDate);
    if(DATE_RANGE_SELECTED.CREATED.equals(dateRangeSelected)) {
      qCriteria.setCreatedDateRange(dateTimeRange);
    } else if(DATE_RANGE_SELECTED.MODIFIDED.equals(dateRangeSelected)) {
      qCriteria.setLastModifiedDateRange(dateTimeRange);
    }
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(null);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchWebContentByDocumentType(String documentType, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(null);
    qCriteria.setContentTypes(documentType.split(","));
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private QueryCriteria getInitialQueryCriteria(String siteName) {
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setSearchDocument(false);
    qCriteria.setSearchWebpage(false);
    qCriteria.setSearchWebContent(true);
    qCriteria.setSiteName(siteName);
    qCriteria.setLiveMode(false);
    return qCriteria;
  }

  private boolean haveEmptyField(UIApplication uiApp, Event<UIContentSearchForm> event, Object... fields) throws Exception {
    for(Object field : fields) {
      if(field == null) {
        uiApp.addMessage(new ApplicationMessage(
            "UIWebContentSearchForm.empty-field", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return true;
      }
    }
    return false;
  }
  
  private PaginatedQueryResult searchDocumentByName(String keyword, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(false);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchDocumentByFulltext(String keyword, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(QueryCriteria.ALL_PROPERTY_SCOPE);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchDocumentByProperty(String property, 
      String keyword, QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(property);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
    return siteSearchService.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchDocumentByDate(DATE_RANGE_SELECTED dateRangeSelected, 
      Calendar fromDate, Calendar endDate, QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setDateRangeSelected(dateRangeSelected);
    DatetimeRange dateTimeRange = new QueryCriteria.DatetimeRange(fromDate, endDate);
    if(DATE_RANGE_SELECTED.CREATED.equals(dateRangeSelected)) {
      qCriteria.setCreatedDateRange(dateTimeRange);
    } else if(DATE_RANGE_SELECTED.MODIFIDED.equals(dateRangeSelected)) {
      qCriteria.setLastModifiedDateRange(dateTimeRange);
    }
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(null);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  private PaginatedQueryResult searchDocumentByType(String documentType, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(null);
    qCriteria.setContentTypes(documentType.split(","));
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(Utils.getSessionProvider(this), qCriteria, pageSize);
  }

  static public class SearchWebContentActionListener extends EventListener<UIContentSearchForm> {
    public void execute(Event<UIContentSearchForm> event) throws Exception {
      UIContentSearchForm uiWCSearch = event.getSource();
      String typeSearch = uiWCSearch.getAncestorOfType(UIContentSelector.class)
                                      .getChild(UIContentBrowsePanel.class).contentType;
      int pageSize = 5;
      String radioValue = event.getRequestContext().getRequestParameter(RADIO_NAME);
      String siteName = uiWCSearch.getUIStringInput(UIContentSearchForm.LOCATION).getValue();
      UIContentSelector uiWCTabSelector = uiWCSearch.getParent();
      UIApplication uiApp = uiWCSearch.getAncestorOfType(UIApplication.class);
      QueryCriteria qCriteria = uiWCSearch.getInitialQueryCriteria(siteName);
      PaginatedQueryResult pagResult = null; 

      if(typeSearch.equals(UIContentBrowsePanel.WEBCONENT)){
        if(UIContentSearchForm.SEARCH_BY_NAME.equals(radioValue)) {
          String keyword = uiWCSearch.getUIStringInput(radioValue).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, keyword)) return;
          pagResult = uiWCSearch.searchWebContentByName(keyword, qCriteria, pageSize);
        } else if(UIContentSearchForm.SEARCH_BY_CONTENT.equals(radioValue)) {
          String keyword = uiWCSearch.getUIStringInput(radioValue).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, keyword)) return;
          pagResult =  uiWCSearch.searchWebContentByFulltext(keyword, qCriteria, pageSize);
        } else if(UIContentSearchForm.PROPERTY.equals(radioValue)) {
          String property = uiWCSearch.getUIStringInput(UIContentSearchForm.PROPERTY).getValue();
          String keyword = uiWCSearch.getUIStringInput(UIContentSearchForm.CONTAIN).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, property, keyword)) return;
          pagResult = uiWCSearch.searchWebContentByProperty(property, keyword, qCriteria, pageSize);
        } else if(UIContentSearchForm.TIME_OPTION.equals(radioValue)) {
        	UIFormDateTimeInput startDateInput = uiWCSearch.getUIFormDateTimeInput(UIContentSearchForm.START_TIME);
        	UIFormDateTimeInput endDateInput = uiWCSearch.getUIFormDateTimeInput(UIContentSearchForm.END_TIME);
        	try {
        		new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(startDateInput.getValue());
					} catch (ParseException e) {
						uiApp.addMessage(new ApplicationMessage("UIWebContentSearchForm.invalid-format", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
						return;
					}
					Calendar startDate = startDateInput.getCalendar();
					Calendar endDate = endDateInput.getCalendar();
          if(uiWCSearch.haveEmptyField(uiApp, event, startDate)) return;
          if(endDate == null) {
          	if (startDate.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
          		endDate = startDate;
          	} else {
          		endDate = Calendar.getInstance();
          	}
          }
          if (startDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            uiApp.addMessage(new ApplicationMessage("UIWebContentSearchForm.invalid-date", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          String dateRangeSelected = uiWCSearch.getUIStringInput(UIContentSearchForm.TIME_OPTION).getValue();
          if(UIContentSearchForm.CREATED_DATE.equals(dateRangeSelected)) {
            pagResult =  uiWCSearch.searchWebContentByDate(DATE_RANGE_SELECTED.CREATED, 
                                                           startDate, endDate, qCriteria, pageSize);
          } else {
            pagResult = uiWCSearch.searchWebContentByDate(DATE_RANGE_SELECTED.MODIFIDED, 
                                                          startDate, endDate, qCriteria, pageSize);
          }
        } else if(UIContentSearchForm.DOC_TYPE.equals(radioValue)) {
          String documentType = uiWCSearch.getUIStringInput(UIContentSearchForm.DOC_TYPE).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, documentType)) return;
          try{
            pagResult = uiWCSearch.searchWebContentByDocumentType(documentType, qCriteria, pageSize);
          }catch(NoSuchNodeTypeException ex){
            uiApp.addMessage(new ApplicationMessage("UIWebContentSearchForm.invalid-nodeType", new Object[]{documentType}, ApplicationMessage.ERROR));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }else if(typeSearch.equals(UIContentBrowsePanel.DMSDOCUMENT)){
        if(UIContentSearchForm.SEARCH_BY_NAME.equals(radioValue)) {
          String keyword = uiWCSearch.getUIStringInput(radioValue).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, keyword)) return;
          pagResult = uiWCSearch.searchDocumentByName(keyword, qCriteria, pageSize);
        } else if(UIContentSearchForm.SEARCH_BY_CONTENT.equals(radioValue)) {
          String keyword = uiWCSearch.getUIStringInput(radioValue).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, keyword)) return;
          pagResult =  uiWCSearch.searchDocumentByFulltext(keyword, qCriteria, pageSize);
        } else if(UIContentSearchForm.PROPERTY.equals(radioValue)) {
          String property = uiWCSearch.getUIStringInput(UIContentSearchForm.PROPERTY).getValue();
          String keyword = uiWCSearch.getUIStringInput(UIContentSearchForm.CONTAIN).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, property, keyword)) return;
          pagResult = uiWCSearch.searchDocumentByProperty(property, keyword, qCriteria, pageSize);
        } else if(UIContentSearchForm.TIME_OPTION.equals(radioValue)) {
        	UIFormDateTimeInput startDateInput = uiWCSearch.getUIFormDateTimeInput(UIContentSearchForm.START_TIME);
        	UIFormDateTimeInput endDateInput = uiWCSearch.getUIFormDateTimeInput(UIContentSearchForm.END_TIME);
        	try {
        		new SimpleDateFormat(startDateInput.getDatePattern_()).parse(startDateInput.getValue());
        		new SimpleDateFormat(endDateInput.getDatePattern_()).parse(endDateInput.getValue());
					} catch (Exception e) {
						uiApp.addMessage(new ApplicationMessage("UIWebContentSearchForm.invalid-format", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
						return;
					}
					Calendar startDate = startDateInput.getCalendar();
					Calendar endDate = endDateInput.getCalendar();
          if(uiWCSearch.haveEmptyField(uiApp, event, startDate)) return;
          if(endDate == null) {
          	if (startDate.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
          		endDate = startDate;
          	} else {
          		endDate = Calendar.getInstance();
          	}
          }
          if (startDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            uiApp.addMessage(new ApplicationMessage("UIWebContentSearchForm.invalid-date", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          String dateRangeSelected = uiWCSearch.getUIStringInput(UIContentSearchForm.TIME_OPTION).getValue();
          if(UIContentSearchForm.CREATED_DATE.equals(dateRangeSelected)) {
            pagResult =  uiWCSearch.searchDocumentByDate(DATE_RANGE_SELECTED.CREATED, 
                                                         startDate, endDate, qCriteria, pageSize);
          } else {
            pagResult = uiWCSearch.searchDocumentByDate(DATE_RANGE_SELECTED.MODIFIDED, 
                                                        startDate, endDate, qCriteria, pageSize);
          }
        } else if(UIContentSearchForm.DOC_TYPE.equals(radioValue)) {
          String documentType = uiWCSearch.getUIStringInput(UIContentSearchForm.DOC_TYPE).getValue();
          if(uiWCSearch.haveEmptyField(uiApp, event, documentType)) return;
          pagResult = uiWCSearch.searchDocumentByType(documentType, qCriteria, pageSize);
        }
      }
      UIContentSearchResult uiWCSearchResult = uiWCTabSelector.getChild(UIContentSearchResult.class);
      uiWCSearchResult.updateGrid(pagResult);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
      uiWCTabSelector.setSelectedTab(uiWCSearchResult.getId());
    }
  }

  public String getCheckedRadioId() {
    return checkedRadioId;
  }

  public void setCheckedRadioId(String checkedRadioId) {
    this.checkedRadioId = checkedRadioId;
  }
}
