package org.exoplatform.wcm.webui.selector.webcontent;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.wcm.webui.selector.document.UIDocumentPathSelector;
import org.exoplatform.wcm.webui.selector.document.UIDocumentTabSelector;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Feb 10, 2009  
 */

@ComponentConfig(
    template = "classpath:groovy/wcm/webui/UIWCMSearchResult.gtmpl",
    events = {
        @EventConfig(listeners = UIWCMSearchResult.SelectActionListener.class),
        @EventConfig(listeners = UIWCMSearchResult.ViewActionListener.class)
    }
)

public class UIWCMSearchResult extends UIGrid {

  public static final String TITLE = "title".intern();
  public static final String NODE_EXPECT = "excerpt".intern();
  public static final String SCORE = "score".intern();
  public static final String CREATE_DATE = "CreateDate".intern();
  public static final String NODE_PATH = "path".intern();
  public String[] Actions = {"Select", "View"};
  public String[] BEAN_FIELDS = {TITLE, SCORE};
  
  
  public UIWCMSearchResult() throws Exception {
    configure(NODE_PATH, BEAN_FIELDS, Actions);
    getUIPageIterator().setId("UIWCMSearchResultPaginator");
  }

  public DateFormat getDateFormat() {
    Locale locale = new Locale(Util.getUIPortal().getLocale());
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    return dateFormat;
  }

  public void updateGrid(PaginatedQueryResult paginatedResult) throws Exception {           
    getUIPageIterator().setPageList(paginatedResult); 
  }


  public String getTitleNode(Node node) throws Exception {
    return node.hasProperty("exo:title") ? 
        node.getProperty("exo:title").getValue().getString() : node.getName();
  }

  public Date getCreateDate(Node node) throws Exception {
    if(node.hasProperty("exo:dateCreated")) {
      Calendar cal = node.getProperty("exo:dateCreated").getValue().getDate();
      return cal.getTime();
    }
    return null;
  }

  private Node getResultNode(String nodePath) throws Exception {
    Session session = getSession();
    Node resultNode = (Node) session.getItem(nodePath);
    return resultNode;
  }

  public String getExpect(String expect) {
    expect = expect.replaceAll("<[^>]*/?>", "");
    return expect;
  }

  public Session getSession() throws Exception {
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    ManageableRepository maRepository = repoService.getCurrentRepository();
    String repository = maRepository.getConfiguration().getName();
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = pContext.getRequest().getPreferences();
    String workspace = prefs.getValue("workspace", null);
    if(workspace == null) {
      WCMConfigurationService wcmConfService = 
        getApplicationComponent(WCMConfigurationService.class);
      NodeLocation nodeLocation = wcmConfService.getLivePortalsLocation(repository);
      workspace = nodeLocation.getWorkspace();
    }
    Session session = 
      SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    return session;
  }

  public static class SelectActionListener extends EventListener<UIWCMSearchResult> {
    public void execute(Event<UIWCMSearchResult> event) throws Exception {
      UIWCMSearchResult uiWCSearchResult = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = 
        uiWCSearchResult.getAncestorOfType(UIWebContentTabSelector.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiWCSearchResult.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentPathSelector uiDocPathSelector =
          uiDocTabSelector.getChild(UIDocumentPathSelector.class);
        String returnField = ((UIBaseNodeTreeSelector)uiDocPathSelector).getReturnFieldName();
        ((UISelectable)((UIBaseNodeTreeSelector)uiDocPathSelector).getSourceComponent()).doSelect(returnField, nodePath);
        event.getRequestContext().addUIComponentToUpdateByAjax(
            ((UIBaseNodeTreeSelector)uiDocPathSelector).getSourceComponent().getParent());
      } else {
        UIWebContentPathSelector uiWCPathSelector = 
          uiWCTabSelector.getChild(UIWebContentPathSelector.class);
        String returnField = ((UIBaseNodeTreeSelector)uiWCPathSelector).getReturnFieldName();
        ((UISelectable)((UIBaseNodeTreeSelector)uiWCPathSelector).getSourceComponent()).doSelect(returnField, nodePath);
        event.getRequestContext().addUIComponentToUpdateByAjax(
            ((UIBaseNodeTreeSelector)uiWCPathSelector).getSourceComponent().getParent());
      }
    }
  }

  public static class ViewActionListener extends EventListener<UIWCMSearchResult> {
    public void execute(Event<UIWCMSearchResult> event) throws Exception {
      UIWCMSearchResult uiWCSearchResult = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = 
        uiWCSearchResult.getAncestorOfType(UIWebContentTabSelector.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiWCSearchResult.getAncestorOfType(UIDocumentTabSelector.class);
        Node resultNode = uiWCSearchResult.getResultNode(nodePath);
        UIResultView uiResultView = uiDocTabSelector.getChild(UIResultView.class);
        if(uiResultView == null) {
          uiResultView = uiDocTabSelector.addChild(UIResultView.class, null, null);
        }
        uiResultView.init(resultNode, false);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
        uiDocTabSelector.setSelectedTab(uiResultView.getId());
      } else {
        Node resultNode = uiWCSearchResult.getResultNode(nodePath);
        UIResultView uiResultView = uiWCTabSelector.getChild(UIResultView.class);
        if(uiResultView == null) {
          uiResultView = uiWCTabSelector.addChild(UIResultView.class, null, null);
        }
        uiResultView.init(resultNode, false);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
        uiWCTabSelector.setSelectedTab(uiResultView.getId());
      }
    }
  }  
}
