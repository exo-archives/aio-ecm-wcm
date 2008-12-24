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
package org.exoplatform.wcm.webui.search;

import java.io.Writer;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequestWrapper;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.SiteSearchService;
import org.exoplatform.services.wcm.search.WCMPaginatedQueryResult;
import org.exoplatform.wcm.webui.paginator.UICustomizeablePaginator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
/**
 * The Class UISearchResult.
 */
@ComponentConfigs( {
    @ComponentConfig(lifecycle = Lifecycle.class),
    @ComponentConfig(type = UICustomizeablePaginator.class, events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class)) })
public class UISearchResult extends UIContainer {

  /** The template path. */
  private String                   templatePath;

  /** The resource resolver. */
  private ResourceResolver         resourceResolver;

  /** The ui paginator. */
  private UICustomizeablePaginator uiPaginator;

  /** The keyword. */
  private String                   keyword;

  /** The result type. */
  private String                   resultType;

  /** The suggetions. */
  private String                   suggetions;

  /** The date formatter. */
  private SimpleDateFormat         dateFormatter    = new SimpleDateFormat(ISO8601.SIMPLE_DATETIME_FORMAT);

  /** The search time. */
  private float                    searchTime;

  /** The Constant PARAMETER_REGX. */
  public final static String       PARAMETER_REGX   = "(portal=.*)&(keyword=.*)";

  /** The Constant RESULT_NOT_FOUND. */
  public final static String       RESULT_NOT_FOUND = "UISearchResult.msg.result-not-found";

  /**
   * Inits the.
   * 
   * @param templatePath the template path
   * @param resourceResolver the resource resolver
   * @throws Exception the exception
   */
  public void init(String templatePath, ResourceResolver resourceResolver) throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    String paginatorTemplatePath = portletPreferences.getValue(UIWCMSearchPortlet.SEARCH_PAGINATOR_TEMPLATE_PATH,
                                                               null);
    this.templatePath = templatePath;
    this.resourceResolver = resourceResolver;
    uiPaginator = addChild(UICustomizeablePaginator.class, null, null);
    uiPaginator.setTemplatePath(paginatorTemplatePath);
    uiPaginator.setResourceResolver(resourceResolver);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui
   * .application.WebuiRequestContext)
   */
  @SuppressWarnings("static-access")
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer writer = context.getWriter();
    PortletRequestContext porletRequestContext = (PortletRequestContext) context;
    ResourceBundle bundle = context.getApplicationResourceBundle();
    PortletPreferences portletPreferences = porletRequestContext.getRequest().getPreferences();
    if (resultType == null || resultType.length() == 0) {
      resultType = bundle.getString("UISearchForm.documentCheckBox.label") + " & "
          + bundle.getString("UISearchForm.pageCheckBox.label");
    }
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext.getRequest();
    String queryString = requestWrapper.getQueryString();
    String message1 = bundle.getString("UISearchResult.msg.your-search");
    String message2 = bundle.getString("UISearchResult.msg.did-not-match");
    String suggestions = bundle.getString("UISearchResult.msg.suggestions");
    String keyword_entered = bundle.getString("UISearchResult.msg.keyword_entered");

    if (queryString != null && queryString.trim().length() != 0
        && queryString.matches(PARAMETER_REGX)) {
      queryString = URLDecoder.decode(queryString, "UTF-8");
      String[] params = queryString.split("&");
      String portalParam = params[0];
      String currentPortal = portalParam.split("=")[1];
      String keywordParam = queryString.substring(portalParam.length() + 1);
      String keyword = keywordParam.substring("keyword=".length());
      if (keyword == null || keyword.trim().length() == 0) { // keyword empty
        writer.write("<div class=\"UIAdvanceSearchResultDefault\">");
        writer.write("<div class=\"ResultHeader\"><div class=\"CaptionSearchType\"><b>"
            + getResultType() + "</b></div><div style=\"clear: left;\"><span></span></div></div>");
        writer.write("<p>" + keyword_entered + "</p>");
        return;
      }
      setKeyword(keyword);
      SiteSearchService siteSearchService = getApplicationComponent(SiteSearchService.class);
      QueryCriteria queryCriteria = new QueryCriteria();
      queryCriteria.setSiteName(currentPortal);
      queryCriteria.setKeyword(keyword);
      queryCriteria.setSearchWebpage(true);
      queryCriteria.setSearchDocument(true);
      SessionProvider provider = SessionProviderFactory.createSessionProvider();
      int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIWCMSearchPortlet.ITEMS_PER_PAGE,
                                                                      null));
      try {
        WCMPaginatedQueryResult paginatedQueryResult = siteSearchService.searchSiteContents(queryCriteria,
                                                                                            provider,
                                                                                            itemsPerPage);
        setSearchTime(paginatedQueryResult.getQueryTimeInSecond());
        setSuggetions(paginatedQueryResult.getSpellSuggestion());
        setPageList(paginatedQueryResult);
      } catch (Exception e) {
        UIApplication uiApp = getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage(UISearchForm.MESSAGE_NOT_SUPPORT_KEYWORD,
                                                null,
                                                ApplicationMessage.WARNING));
      }
    } else if (queryString == null || queryString.trim().length() == 0) {
      writer.write("<div class=\"UIAdvanceSearchResultDefault\">");
      writer.write("<div class=\"ResultHeader\"><div class=\"CaptionSearchType\"><b>"
          + getResultType() + "</b></div><div style=\"clear: left;\"><span></span></div></div>");
      writer.write("<p>" + keyword_entered + "</p>");
      return;
    }
    if (uiPaginator.getTotalItems() == 0) {
      String keyword = getKeyword();
      writer.write("<div class=\"UIAdvanceSearchResultDefault\">");
      writer.write("<div class=\"ResultHeader\"><div class=\"CaptionSearchType\"><b>"
          + getResultType() + "</b></div><div style=\"clear: left;\"><span></span></div></div>");
      if (keyword == null || keyword.trim().length() == 0) {
        // for case "click button search in search form"
        writer.write("<p>" + keyword_entered + "</p>");
      } else { // suggestion-------------------------------------->
        writer.write("<p>");
        writer.write(message1 + " - <b style=\"font-size: 15px; font-style: italic;\">" + keyword
            + "</b> - " + message2 + "&nbsp;" + "<br><br>");
        writer.write(suggestions + "<br>");
        String keySuggestion = getSuggetions();
        if (keySuggestion == null || keySuggestion.equals("null")) {
          String newKeyword = bundle.getString("UISearchResult.msg.try-different-key");
          writer.write("<ul><li>" + newKeyword + "</li></ul>");
        } else {
          setKeyword(keySuggestion);
          writer.write("<ul><li><a class=\"KeySuggestions\" style=\"cursor: pointer;\">"
              + keySuggestion + "</a></li></ul>");
        }
        writer.write("</p>");
      }
      writer.write("</div>");
      return;
    }
    super.processRender(context);
  }

  /**
   * Sets the page list.
   * 
   * @param dataPageList the new page list
   */
  public void setPageList(PageList dataPageList) {
    uiPaginator.setPageList(dataPageList);
  }

  /**
   * Gets the total item.
   * 
   * @return the total item
   */
  public int getTotalItem() {
    return uiPaginator.getPageList().getAvailable();
  }

  /**
   * Gets the items per page.
   * 
   * @return the items per page
   */
  public int getItemsPerPage() {
    return uiPaginator.getPageList().getPageSize();
  }

  /**
   * Gets the current page.
   * 
   * @return the current page
   */
  public int getCurrentPage() {
    return uiPaginator.getCurrentPage();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    return templatePath;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.
   * exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return resourceResolver;
  }

  /**
   * Gets the current page data.
   * 
   * @return the current page data
   * @throws Exception the exception
   */
  public List getCurrentPageData() throws Exception {
    return uiPaginator.getCurrentPageData();
  }

  /**
   * Gets the title.
   * 
   * @param node the node
   * @return the title
   * @throws Exception the exception
   */
  public String getTitle(Node node) throws Exception {
    return node.hasProperty("exo:title") ? node.getProperty("exo:title").getValue().getString()
                                        : node.getName();
  }

  /**
   * Gets the uRL.
   * 
   * @param node the node
   * @return the uRL
   * @throws Exception the exception
   */
  public List<String> getURLs(Node node) throws Exception {
    List<String> urls = new ArrayList<String>();
    if (!node.hasProperty("publication:navigationNodeURIs")) {
      urls.add(getURL(node));
    } else {
      for (Value value : node.getProperty("publication:navigationNodeURIs").getValues()) {
        urls.add(value.getString());
      }
    }
    return urls;
  }

  public String getPublishedNodeURI(String navNodeURI) {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String accessMode = null;
    if ( portalRequestContext.getAccessPath() == PortalRequestContext.PUBLIC_ACCESS) {
      accessMode = "public";
    } else {
      accessMode = "private";
    }
    
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();    
    String baseURI = portletRequestContext.getRequest().getScheme() + "://"
        + portletRequestContext.getRequest().getServerName() + ":"
        + String.format("%s", portletRequestContext.getRequest().getServerPort()) ;
    if (navNodeURI.startsWith(baseURI))
      return navNodeURI;
    return baseURI + portalRequestContext.getRequestContextPath() + "/" + accessMode + navNodeURI;
  }

  public String getURL(Node node) throws Exception {
    String link = null;
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();    
    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    String portalURI = portalRequestContext.getPortalURI();    
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    String repository = portletPreferences.getValue(UIWCMSearchPortlet.REPOSITORY, null);
    String workspace = portletPreferences.getValue(UIWCMSearchPortlet.WORKSPACE, null);
    String baseURI = portletRequestContext.getRequest().getScheme() + "://"
        + portletRequestContext.getRequest().getServerName() + ":"
        + String.format("%s", portletRequestContext.getRequest().getServerPort());
    String parameterizedPageURI = wcmConfigurationService.getParameterizedPageURI();
    link = baseURI + portalURI + parameterizedPageURI.substring(1, parameterizedPageURI.length())
        + "/" + repository + "/" + workspace + node.getPath();
    return link;
  }

  /**
   * Gets the created date.
   * 
   * @param node the node
   * @return the created date
   * @throws Exception the exception
   */
  public String getCreatedDate(Node node) throws Exception {
    if (node.hasProperty("exo:dateCreated")) {
      Calendar calendar = node.getProperty("exo:dateCreated").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;
  }

  /**
   * Checks if is show paginator.
   * 
   * @return true, if is show paginator
   * @throws Exception the exception
   */
  public boolean isShowPaginator() throws Exception {
    PortletPreferences portletPreferences = ((PortletRequestContext) WebuiRequestContext.getCurrentInstance()).getRequest()
                                                                                                              .getPreferences();
    String itemsPerPage = portletPreferences.getValue(UIWCMSearchPortlet.ITEMS_PER_PAGE, null);
    int totalItems = uiPaginator.getTotalItems();
    if (totalItems > Integer.parseInt(itemsPerPage)) {
      return true;
    }
    return false;
  }

  /**
   * Gets the search time.
   * 
   * @return the search time
   */
  public float getSearchTime() {
    return searchTime;
  }

  /**
   * Sets the search time.
   * 
   * @param searchTime the new search time
   */
  public void setSearchTime(float searchTime) {
    this.searchTime = searchTime;
  }

  /**
   * Gets the suggetions.
   * 
   * @return the suggetions
   */
  public String getSuggetions() {
    return suggetions;
  }

  /**
   * Sets the suggetions.
   * 
   * @param suggetions the new suggetions
   */
  public void setSuggetions(String suggetions) {
    this.suggetions = suggetions;
  }

  /**
   * Gets the keyword.
   * 
   * @return the keyword
   */
  public String getKeyword() {
    return this.keyword;
  }

  /**
   * Sets the keyword.
   * 
   * @param keyword the new keyword
   */
  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  /**
   * Gets the result type.
   * 
   * @return the result type
   */
  public String getResultType() {
    return this.resultType;
  }

  /**
   * Sets the result type.
   * 
   * @param resultType the new result type
   */
  public void setResultType(String resultType) {
    this.resultType = resultType;
  }

  public int getNumberOfPage() {
    return uiPaginator.getPageList().getAvailablePage();
  }

}
