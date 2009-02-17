package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.search.PaginatedQueryResult;
import org.exoplatform.services.wcm.search.ResultNode;
import org.exoplatform.wcm.webui.selector.document.UIDocumentPathSelector;
import org.exoplatform.wcm.webui.selector.document.UIDocumentTabSelector;
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

  public static final String NAME_FILE = "NameFile".intern();
  public static final String NODE_EXPECT = "NodeExpect".intern();
  public static final String SCORE = "Score".intern();
  public static final String CREATE_DATE = "CreateDate".intern();
  public static final String NODE_PATH = "NodePath".intern();
  public String[] Actions = {"Select", "View"};
  public String[] BEAN_FIELDS = {NAME_FILE, NODE_EXPECT, SCORE, CREATE_DATE};

  private PaginatedQueryResult pagResult;

  public UIWCMSearchResult() throws Exception {
    configure(NODE_PATH, BEAN_FIELDS, Actions);
    getUIPageIterator().setId("UIWCMSearchResultPaginator");
  }

  public void updateGrid(PaginatedQueryResult paginatedResult) throws Exception {
    this.pagResult = paginatedResult;
    List<SearchResultBean> resultBeanList = new ArrayList<SearchResultBean>();
    List<ResultNode> currentPageDate = pagResult.getCurrentPageData();
    for(ResultNode resultNode : currentPageDate) {
      Node node= resultNode.getNode();
      SearchResultBean resultBean = new SearchResultBean();
      resultBean.setCreateDate(getCreateDate(node).getTimeZone().toString());
      resultBean.setNameFile(getTitle(node));
      resultBean.setNodeExpect(resultNode.getExcerpt());
      resultBean.setNodePath(node.getPath());
      resultBean.setScore(resultNode.getScore());
      resultBeanList.add(resultBean);
    }
    ObjectPageList objectPageList = new ObjectPageList(resultBeanList, 10);
    getUIPageIterator().setPageList(objectPageList);
  }


  public String getTitle(Node node) throws Exception {
    return node.hasProperty("exo:title") ? 
        node.getProperty("exo:title").getValue().getString() : node.getName();
  }

  public Calendar getCreateDate(Node node) throws Exception {
    if(node.hasProperty("exo:dateCreated")) {
      Calendar date = node.getProperty("exo:dateCreated").getValue().getDate();
      return date;
    }
    return null;
  }

  private Node getResultNode(String nodePath) throws Exception {
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    ManageableRepository maRepository = repoService.getCurrentRepository();
    String workspace = maRepository.getConfiguration().getDefaultWorkspaceName();
    Session session = 
      SessionProviderFactory.createSessionProvider().getSession(workspace, maRepository);
    Node resultNode = (Node) session.getItem(nodePath);
    return resultNode;
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

  public static class SearchResultBean {
    public String NameFile;
    public String NodeExpect;
    public float Score;
    public String CreateDate;
    public  String NodePath;
    public String getNameFile() {
      return this.NameFile;
    }
    public void setNameFile(String nameFile) {
      this.NameFile = nameFile;
    }
    public String getNodeExpect() {
      return NodeExpect;
    }
    public void setNodeExpect(String nodeExpect) {
      NodeExpect = nodeExpect;
    }
    public float getScore() {
      return Score;
    }
    public void setScore(float score) {
      Score = score;
    }
    public String getCreateDate() {
      return CreateDate;
    }
    public void setCreateDate(String createDate) {
      CreateDate = createDate;
    }
    public String getNodePath() {
      return NodePath;
    }
    public void setNodePath(String nodePath) {
      NodePath = nodePath;
    }
  }
}
