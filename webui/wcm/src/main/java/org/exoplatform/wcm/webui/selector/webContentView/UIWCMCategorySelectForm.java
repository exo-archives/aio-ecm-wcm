package org.exoplatform.wcm.webui.selector.webContentView;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Feb 2, 2009  
 */

@ComponentConfigs(
    {
      @ComponentConfig(
          template = "classpath:groovy/wcm/webui/UIWCMCategorySelectForm.gtmpl"
      ),
      @ComponentConfig(
          type = UIBreadcumbs.class, id = "WCMBreadcumbCategoriesOne",
          template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
          events = @EventConfig(listeners = UIWCMCategorySelectForm.SelectPathActionListener.class,phase = Phase.DECODE)
      )
    }
)

public class UIWCMCategorySelectForm extends UIBaseNodeTreeSelector {

  private String[] acceptedNodeTypesInTree = {};  
  private String[] acceptedNodeTypesInPathPanel = {};
  private String[] acceptedMimeTypes = {};

  private String repositoryName = null;
  private String workspaceName = null;
  private String rootTreePath = null;
  private boolean isDisable = false;

  public UIWCMCategorySelectForm() throws Exception {
    addChild(UIBreadcumbs.class, "WCMBreadcumbCategoriesOne", "WCMBreadcumbCategoriesOne");
//  addChild(UIWorkspaceList.class, null, null);
    addChild(UINodeTreeBuilder.class, null, UINodeTreeBuilder.class.getName()+hashCode());
    addChild(UISelectPathPanel.class, null, UISelectPathPanel.class.getName()+hashCode());
  }

  public void init(SessionProvider sessionProvider) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    try {
//    TODO: Should review this method to make sure we have no problem with permission when use system session      
      Node rootNode;
      if (rootTreePath.trim().equals("/")) {
        rootNode = manageableRepository.getSystemSession(workspaceName).getRootNode();
      } else {
        Session session = sessionProvider.getSession(workspaceName, manageableRepository);
        rootNode = (Node)session.getItem(rootTreePath);
      }

//    UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class);
//    uiWorkspaceList.setWorkspaceList(repositoryName);
//    uiWorkspaceList.setIsDisable(workspaceName, isDisable);
      UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);    
      builder.setAcceptedNodeTypes(acceptedNodeTypesInTree);    
      builder.setRootTreeNode(rootNode);

      UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
      selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypesInPathPanel);
      selectPathPanel.setAcceptedMimeTypes(acceptedMimeTypes);
      selectPathPanel.updateGrid();
    } finally {
      sessionProvider.close();
    }        
  }

  public void setRootNodeLocation(String repository, String workspace, String rootPath) throws Exception {
    this.repositoryName = repository;
    this.workspaceName = workspace;
    this.rootTreePath = rootPath;    
  }

  public void setIsDisable(String wsName, boolean isDisable) {
    setWorkspaceName(wsName);
    this.isDisable = isDisable;
  }

  public boolean isDisable() { return isDisable; }

//public void setIsShowSystem(boolean isShowSystem) {
//getChild(UIWorkspaceList.class).setIsShowSystem(isShowSystem);
//}

//public void setShowRootPathSelect(boolean isRendered) {
//UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class);
//uiWorkspaceList.setShowRootPathSelect(isRendered);
//}

  public String[] getAcceptedNodeTypesInTree() {
    return acceptedNodeTypesInTree;
  }

  public void setAcceptedNodeTypesInTree(String[] acceptedNodeTypesInTree) {
    this.acceptedNodeTypesInTree = acceptedNodeTypesInTree;
  }

  public String[] getAcceptedNodeTypesInPathPanel() {
    return acceptedNodeTypesInPathPanel;
  }

  public void setAcceptedNodeTypesInPathPanel(String[] acceptedNodeTypesInPathPanel) {
    this.acceptedNodeTypesInPathPanel = acceptedNodeTypesInPathPanel;
  }  

  public String[] getAcceptedMimeTypes() { return acceptedMimeTypes; }

  public void setAcceptedMimeTypes(String[] acceptedMimeTypes) { this.acceptedMimeTypes = acceptedMimeTypes; } 

  public String getRepositoryName() { return repositoryName; }
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public String getWorkspaceName() { return workspaceName; }

  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }

  public String getRootTreePath() { return rootTreePath; }

  public void setRootTreePath(String rootTreePath) { this.rootTreePath = rootTreePath; 
  }      

  public void onChange(final Node currentNode, Object context) throws Exception {
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setParentNode(currentNode);
    selectPathPanel.updateGrid();

    UIBreadcumbs uiBreadcumbs = getChild(UIBreadcumbs.class);
    List<LocalPath> listLocalPath = new ArrayList<LocalPath>();
    String path = currentNode.getPath().trim();
    if (path.startsWith(rootTreePath)) {
      path = path.substring(rootTreePath.length(), path.length());
    }    
    String[] arrayPath = path.split("/");
    if (arrayPath.length > 0) {
      for (int i = 0; i < arrayPath.length; i++) {
        if (!arrayPath[i].trim().equals("")) {
          UIBreadcumbs.LocalPath localPath1 = new UIBreadcumbs.LocalPath(arrayPath[i].trim(), arrayPath[i].trim());
          listLocalPath.add(localPath1);
        }
      }
    }
    uiBreadcumbs.setPath(listLocalPath);
  }

  private void changeNode(String stringPath, Object context) throws Exception {
    UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);
    builder.changeNode(stringPath, context);
  }

  public void changeGroup(String groupId, Object context) throws Exception {
    String stringPath = rootTreePath;
    if (!rootTreePath.equals("/")) {
      stringPath += "/";    
    }
    UIBreadcumbs uiBreadcumb = getChild(UIBreadcumbs.class);
    if (groupId == null) groupId = "";
    List<LocalPath> listLocalPath = uiBreadcumb.getPath();
    if (listLocalPath == null || listLocalPath.size() == 0) return;
    List<String> listLocalPathString = new ArrayList<String>();
    for (LocalPath localPath : listLocalPath) {
      listLocalPathString.add(localPath.getId().trim());
    }
    if (listLocalPathString.contains(groupId)) {
      int index = listLocalPathString.indexOf(groupId);
      if (index == listLocalPathString.size() - 1) return;
      for (int i = listLocalPathString.size() - 1; i > index; i--) {
        listLocalPathString.remove(i);
        listLocalPath.remove(i);
      }
      uiBreadcumb.setPath(listLocalPath);
      for (int i = 0; i < listLocalPathString.size(); i++) {
        String pathName = listLocalPathString.get(i);
        if (pathName != null || !pathName.equals("")) {
          stringPath += pathName.trim();
          if (i < listLocalPathString.size() - 1) stringPath += "/";
        }
      }
      changeNode(stringPath, context);
    }
  }

  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      UIWCMCategorySelectForm uiCategorySelectForm = uiBreadcumbs.getParent();
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID);
      uiBreadcumbs.setSelectPath(objectId);    
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
      uiCategorySelectForm.changeGroup(selectGroupId, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCategorySelectForm);
    }
  }
}
