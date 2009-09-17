package org.exoplatform.wcm.webui.selector.webContentView;

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
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.NotInWCMPublicationException;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
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
 * dzungdev@gmail.com
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

  /** The Constant TITLE. */
  public static final String TITLE = "title".intern();
  
  /** The Constant NODE_EXPECT. */
  public static final String NODE_EXPECT = "excerpt".intern();
  
  /** The Constant SCORE. */
  public static final String SCORE = "score".intern();
  
  /** The Constant CREATE_DATE. */
  public static final String CREATE_DATE = "CreateDate".intern();
  
  /** The Constant PUBLICATION_STATE. */
  public static final String PUBLICATION_STATE = "publicationstate".intern();
  
  /** The Constant NODE_PATH. */
  public static final String NODE_PATH = "path".intern();
  
  /** The Actions. */
  public String[] Actions = {"Select", "View"};
  
  /** The BEA n_ fields. */
  public String[] BEAN_FIELDS = {TITLE, SCORE, PUBLICATION_STATE};


  /**
   * Instantiates a new uIWCM search result.
   * 
   * @throws Exception the exception
   */
  public UIWCMSearchResult() throws Exception {
    configure(NODE_PATH, BEAN_FIELDS, Actions);
    getUIPageIterator().setId("UIWCMSearchResultPaginator");
  }

  /**
   * Gets the date format.
   * 
   * @return the date format
   */
  public DateFormat getDateFormat() {
    Locale locale = new Locale(Util.getUIPortal().getLocale());
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    return dateFormat;
  }

  /**
   * Update grid.
   * 
   * @param paginatedResult the paginated result
   * 
   * @throws Exception the exception
   */
  public void updateGrid(PaginatedQueryResult paginatedResult) throws Exception {           
    getUIPageIterator().setPageList(paginatedResult); 
  }


  /**
   * Gets the title node.
   * 
   * @param node the node
   * 
   * @return the title node
   * 
   * @throws Exception the exception
   */
  public String getTitleNode(Node node) throws Exception {
    return node.hasProperty("exo:title") ? 
                                          node.getProperty("exo:title").getValue().getString() : node.getName();
  }

  /**
   * Gets the creates the date.
   * 
   * @param node the node
   * 
   * @return the creates the date
   * 
   * @throws Exception the exception
   */
  public Date getCreateDate(Node node) throws Exception {
    if(node.hasProperty("exo:dateCreated")) {
      Calendar cal = node.getProperty("exo:dateCreated").getValue().getDate();
      return cal.getTime();
    }
    return null;
  }

  /**
   * Gets the result node.
   * 
   * @param nodePath the node path
   * 
   * @return the result node
   * 
   * @throws Exception the exception
   */
  private Node getResultNode(String nodePath) throws Exception {
    Session session = getSession();
    Node resultNode = (Node) session.getItem(nodePath);
    return resultNode;
  }

  /**
   * Gets the expect.
   * 
   * @param expect the expect
   * 
   * @return the expect
   */
  public String getExpect(String expect) {
    expect = expect.replaceAll("<[^>]*/?>", "");
    return expect;
  }

  /**
   * Gets the current state.
   * 
   * @param node the node
   * 
   * @return the current state
   * 
   * @throws Exception the exception
   */
  public String getCurrentState(Node node) throws Exception {
    PublicationService pubService = getApplicationComponent(PublicationService.class);
    return pubService.getCurrentState(node);
  }

  /**
   * Gets the session.
   * 
   * @return the session
   * 
   * @throws Exception the exception
   */
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

  /**
   * The listener interface for receiving selectAction events.
   * The class that is interested in processing a selectAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectActionListener<code> method. When
   * the selectAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectActionEvent
   */
  public static class SelectActionListener extends EventListener<UIWCMSearchResult> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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
        RepositoryService repositoryService = uiWCSearchResult.getApplicationComponent(RepositoryService.class);
        String repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
        WCMConfigurationService configurationService = uiWCSearchResult.getApplicationComponent(WCMConfigurationService.class);
        NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repoName);

        ManageableRepository manageableRepository = repositoryService.getRepository(nodeLocation.getRepository());
        Session session = uiWCSearchResult.getApplicationComponent(ThreadLocalSessionProviderService.class).getSessionProvider(null)
        .getSession(nodeLocation.getWorkspace(), manageableRepository);
        Node webContent = (Node) session.getItem(nodePath);
        NodeIdentifier nodeIdentifier = NodeIdentifier.make(webContent);
        PortletRequestContext pContext = (PortletRequestContext) event.getRequestContext();
        PortletPreferences prefs = pContext.getRequest().getPreferences();
        prefs.setValue("repository", nodeIdentifier.getRepository());
        prefs.setValue("workspace", nodeIdentifier.getWorkspace());
        prefs.setValue("nodeIdentifier", nodeIdentifier.getUUID());
        prefs.store();

        String remoteUser = Util.getPortalRequestContext().getRemoteUser();
        String currentSite = Util.getPortalRequestContext().getPortalOwner();

        WCMPublicationService wcmPublicationService = uiWCSearchResult.getApplicationComponent(WCMPublicationService.class);

        try {
          wcmPublicationService.isEnrolledInWCMLifecycle(webContent);
        } catch (NotInWCMPublicationException e){
          wcmPublicationService.unsubcribeLifecycle(webContent);
          wcmPublicationService.enrollNodeInLifecycle(webContent, currentSite, remoteUser);          
        }
      }
    }
  }

  /**
   * The listener interface for receiving viewAction events.
   * The class that is interested in processing a viewAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addViewActionListener<code> method. When
   * the viewAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ViewActionEvent
   */
  public static class ViewActionListener extends EventListener<UIWCMSearchResult> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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
