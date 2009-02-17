package org.exoplatform.wcm.webui.selector.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.QueryCriteria.DATE_RANGE_SELECTED;
import org.exoplatform.services.wcm.search.QueryCriteria.DatetimeRange;
import org.exoplatform.wcm.webui.selector.webcontent.UIWCMSearchResult;
import org.exoplatform.wcm.webui.selector.webcontent.UIWebContentSearchForm;
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
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Feb 14, 2009  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/UIWebContentSearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UIDocumentSearchForm.SearchWebContentActionListener.class),
      @EventConfig(listeners = UIDocumentSearchForm.AddMetadataTypeActionListener.class),
      @EventConfig(listeners = UIDocumentSearchForm.AddNodeTypeActionListener.class)
    }
)

public class UIDocumentSearchForm extends UIForm {

  private String checkedRadioId;

  public String getCheckedRadioId() {
    return checkedRadioId;
  }

  public void setCheckedRadioId(String checkedRadioId) {
    this.checkedRadioId = checkedRadioId;
  }

  public void init() throws Exception {
    List<SelectItemOption<String>> portalNameOptions = new ArrayList<SelectItemOption<String>>();
    List<String> portalNames = getPortalNames();
    for(String portalName: portalNames) {
      portalNameOptions.add(new SelectItemOption<String>(portalName, portalName));
    }
    UIFormSelectBox portalNameSelectBox = 
      new UIFormSelectBox(UIWebContentSearchForm.LOCATION, UIWebContentSearchForm.LOCATION, portalNameOptions);
    portalNameSelectBox.setDefaultValue(portalNames.get(0));
    addChild(portalNameSelectBox);

    addUIFormInput(new UIFormStringInput(UIWebContentSearchForm.SEARCH_BY_NAME, UIWebContentSearchForm.SEARCH_BY_NAME,null));
    addUIFormInput(new UIFormStringInput(UIWebContentSearchForm.SEARCH_BY_CONTENT, UIWebContentSearchForm.SEARCH_BY_CONTENT, null));

    addUIFormInput(new UIFormStringInput(UIWebContentSearchForm.PROPERTY, UIWebContentSearchForm.PROPERTY, null));
    addUIFormInput(new UIFormStringInput(UIWebContentSearchForm.CONTAIN, UIWebContentSearchForm.CONTAIN, null));

    List<SelectItemOption<String>> dateOptions = new ArrayList<SelectItemOption<String>>();
    dateOptions.add(new SelectItemOption<String>(UIWebContentSearchForm.CREATED_DATE,UIWebContentSearchForm.CREATED_DATE));
    dateOptions.add(new SelectItemOption<String>(UIWebContentSearchForm.MODIFIED_DATE,UIWebContentSearchForm.MODIFIED_DATE));
    addUIFormInput(new UIFormSelectBox(UIWebContentSearchForm.TIME_OPTION, UIWebContentSearchForm.TIME_OPTION, dateOptions));
    UIFormDateTimeInput startTime = new UIFormDateTimeInput(UIWebContentSearchForm.START_TIME, UIWebContentSearchForm.START_TIME, null);
    startTime.setDisplayTime(false);
    addUIFormInput(startTime);
    UIFormDateTimeInput endTime = new UIFormDateTimeInput(UIWebContentSearchForm.END_TIME, UIWebContentSearchForm.END_TIME, null);
    endTime.setDisplayTime(false);
    addUIFormInput(endTime);

    addUIFormInput(new UIFormStringInput(UIWebContentSearchForm.DOC_TYPE, UIWebContentSearchForm.DOC_TYPE, null));

    addUIFormInput(new UIFormStringInput(UIWebContentSearchForm.CATEGORY_TYPE, UIWebContentSearchForm.CATEGORY_TYPE, null));

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

  private QueryCriteria getInitialQueryCriteria(String siteName) {
    QueryCriteria qCriteria = new QueryCriteria();
    qCriteria.setSearchDocument(true);
    qCriteria.setSearchWebpage(false);
    qCriteria.setSearchWebContent(false);
    qCriteria.setSiteName(siteName);
    return qCriteria;
  }

  private PaginatedQueryResult searchDocumentByName(String keyword, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(false);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
  }

  private PaginatedQueryResult searchDocumentByFulltext(String keyword, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(QueryCriteria.ALL_PROPERTY_SCOPE);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
  }

  private PaginatedQueryResult searchDocumentByProperty(String property, 
      String keyword, QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(property);
    qCriteria.setKeyword(keyword);
    SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
    return siteSearchService.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
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
    return siteSearch.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
  }

  private PaginatedQueryResult searchDocumentByType(String documentType, 
      QueryCriteria qCriteria, int pageSize) throws Exception {
    qCriteria.setFulltextSearch(true);
    qCriteria.setFulltextSearchProperty(null);
    qCriteria.setContentTypes(documentType.split(","));
    SiteSearchService siteSearch = getApplicationComponent(SiteSearchService.class);
    return siteSearch.searchSiteContents(qCriteria, 
        SessionProviderFactory.createSessionProvider(), pageSize);
  }

  private boolean haveEmptyField(UIApplication uiApp, Event<UIDocumentSearchForm> event, Object... fields) throws Exception {
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

  public static class SearchWebContentActionListener extends EventListener<UIDocumentSearchForm> {
    public void execute(Event<UIDocumentSearchForm> event) throws Exception {
      UIDocumentSearchForm uiDocSearchForm = event.getSource();
      String radioValue = event.getRequestContext().getRequestParameter(UIWebContentSearchForm.RADIO_NAME);
      String siteName = uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.LOCATION).getValue();
      UIDocumentTabSelector uiDocTabSelector = uiDocSearchForm.getParent();
      UIApplication uiApp = uiDocSearchForm.getAncestorOfType(UIApplication.class);
      QueryCriteria qCriteria = uiDocSearchForm.getInitialQueryCriteria(siteName);
      int pageSize = 10;
      PaginatedQueryResult pagResult = new PaginatedQueryResult(pageSize); 
      if(UIWebContentSearchForm.SEARCH_BY_NAME.equals(radioValue)) {
        String keyword = uiDocSearchForm.getUIStringInput(radioValue).getValue();
        if(uiDocSearchForm.haveEmptyField(uiApp, event, keyword)) return;
        pagResult = uiDocSearchForm.searchDocumentByName(keyword, qCriteria, pageSize);
      } else if(UIWebContentSearchForm.SEARCH_BY_CONTENT.equals(radioValue)) {
        String keyword = uiDocSearchForm.getUIStringInput(radioValue).getValue();
        if(uiDocSearchForm.haveEmptyField(uiApp, event, keyword)) return;
        pagResult =  uiDocSearchForm.searchDocumentByFulltext(keyword, qCriteria, pageSize);
      } else if(UIWebContentSearchForm.PROPERTY.equals(radioValue)) {
        String property = uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.PROPERTY).getValue();
        String keyword = uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.CONTAIN).getValue();
        if(uiDocSearchForm.haveEmptyField(uiApp, event, property, keyword)) return;
        pagResult = uiDocSearchForm.searchDocumentByProperty(property, keyword, qCriteria, pageSize);
      } else if(UIWebContentSearchForm.TIME_OPTION.equals(radioValue)) {
        Calendar fromDate = uiDocSearchForm.getUIFormDateTimeInput(UIWebContentSearchForm.START_TIME).getCalendar();
        if(uiDocSearchForm.haveEmptyField(uiApp, event, fromDate)) return;
        Calendar endDate = uiDocSearchForm.getUIFormDateTimeInput(UIWebContentSearchForm.END_TIME).getCalendar();
        if(endDate == null) endDate = Calendar.getInstance();
        String dateRangeSelected = uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.TIME_OPTION).getValue();
        if(UIWebContentSearchForm.CREATED_DATE.equals(dateRangeSelected)) {
          pagResult =  uiDocSearchForm.searchDocumentByDate(DATE_RANGE_SELECTED.CREATED, 
              fromDate, endDate, qCriteria, pageSize);
        } else {
          pagResult = uiDocSearchForm.searchDocumentByDate(DATE_RANGE_SELECTED.MODIFIDED, 
              fromDate, endDate, qCriteria, pageSize);
        }
      } else if(UIWebContentSearchForm.DOC_TYPE.equals(radioValue)) {
        String documentType = uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue();
        if(uiDocSearchForm.haveEmptyField(uiApp, event, documentType)) return;
        pagResult = uiDocSearchForm.searchDocumentByType(documentType, qCriteria, pageSize);
      }
      UIWCMSearchResult uiWCSearchResult = uiDocTabSelector.getChild(UIWCMSearchResult.class);
      uiWCSearchResult.updateGrid(pagResult);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
      uiDocTabSelector.setSelectedTab(uiWCSearchResult.getId());
    }
  }

  public static class AddMetadataTypeActionListener extends EventListener<UIDocumentSearchForm> {
    public void execute(Event<UIDocumentSearchForm> event) throws Exception {
      UIDocumentSearchForm uiDocumentSearch = event.getSource();
      UIDocumentTabSelector uiDocumentTabSelector = uiDocumentSearch.getParent();
      uiDocumentSearch.setCheckedRadioId(event.getRequestContext().getRequestParameter(UIWebContentSearchForm.CHECKED_RADIO_ID));
      uiDocumentTabSelector.initMetadataPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentTabSelector);
    }
  }

  public static class AddNodeTypeActionListener extends EventListener<UIDocumentSearchForm> {
    public void execute(Event<UIDocumentSearchForm> event) throws Exception {
      UIDocumentSearchForm uiDocSearchForm = event.getSource();
      UIDocumentTabSelector uiDocTabSelector = uiDocSearchForm.getParent();
      uiDocSearchForm.setCheckedRadioId(event.getRequestContext().
          getRequestParameter(UIWebContentSearchForm.CHECKED_RADIO_ID));
      uiDocTabSelector.initNodeTypePopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
    }
  }
}
