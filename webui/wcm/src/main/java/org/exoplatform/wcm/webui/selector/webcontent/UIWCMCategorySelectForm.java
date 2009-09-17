package org.exoplatform.wcm.webui.selector.webcontent;

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
 * dzungdev@gmail.com
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

  /** The accepted node types in tree. */
  private String[] acceptedNodeTypesInTree = {};  
  
  /** The accepted node types in path panel. */
  private String[] acceptedNodeTypesInPathPanel = {};
  
  /** The accepted mime types. */
  private String[] acceptedMimeTypes = {};

  /** The repository name. */
  private String repositoryName = null;
  
  /** The workspace name. */
  private String workspaceName = null;
  
  /** The root tree path. */
  private String rootTreePath = null;
  
  /** The is disable. */
  private boolean isDisable = false;

  /**
   * Instantiates a new uIWCM category select form.
   * 
   * @throws Exception the exception
   */
  public UIWCMCategorySelectForm() throws Exception {
    addChild(UIBreadcumbs.class, "WCMBreadcumbCategoriesOne", "WCMBreadcumbCategoriesOne");
//  addChild(UIWorkspaceList.class, null, null);
    addChild(UINodeTreeBuilder.class, null, UINodeTreeBuilder.class.getName()+hashCode());
    addChild(UISelectPathPanel.class, null, UISelectPathPanel.class.getName()+hashCode());
  }

  /**
   * Inits the.
   * 
   * @param sessionProvider the session provider
   * 
   * @throws Exception the exception
   */
  public void init(SessionProvider sessionProvider) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    Node rootNode;
    if (rootTreePath.trim().equals("/")) {
      rootNode = manageableRepository.getSystemSession(workspaceName).getRootNode();
    } else {
      Session session = sessionProvider.getSession(workspaceName, manageableRepository);
      rootNode = (Node)session.getItem(rootTreePath);
    }
    UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);    
    builder.setAcceptedNodeTypes(acceptedNodeTypesInTree);    
    builder.setRootTreeNode(rootNode);

    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypesInPathPanel);
    selectPathPanel.setAcceptedMimeTypes(acceptedMimeTypes);
    selectPathPanel.updateGrid();
  }

  /**
   * Sets the root node location.
   * 
   * @param repository the repository
   * @param workspace the workspace
   * @param rootPath the root path
   * 
   * @throws Exception the exception
   */
  public void setRootNodeLocation(String repository, String workspace, String rootPath) throws Exception {
    this.repositoryName = repository;
    this.workspaceName = workspace;
    this.rootTreePath = rootPath;    
  }

  /**
   * Sets the is disable.
   * 
   * @param wsName the ws name
   * @param isDisable the is disable
   */
  public void setIsDisable(String wsName, boolean isDisable) {
    setWorkspaceName(wsName);
    this.isDisable = isDisable;
  }

  /**
   * Checks if is disable.
   * 
   * @return true, if is disable
   */
  public boolean isDisable() { return isDisable; }

  /**
   * Gets the accepted node types in tree.
   * 
   * @return the accepted node types in tree
   */
  public String[] getAcceptedNodeTypesInTree() {
    return acceptedNodeTypesInTree;
  }

  /**
   * Sets the accepted node types in tree.
   * 
   * @param acceptedNodeTypesInTree the new accepted node types in tree
   */
  public void setAcceptedNodeTypesInTree(String[] acceptedNodeTypesInTree) {
    this.acceptedNodeTypesInTree = acceptedNodeTypesInTree;
  }

  /**
   * Gets the accepted node types in path panel.
   * 
   * @return the accepted node types in path panel
   */
  public String[] getAcceptedNodeTypesInPathPanel() {
    return acceptedNodeTypesInPathPanel;
  }

  /**
   * Sets the accepted node types in path panel.
   * 
   * @param acceptedNodeTypesInPathPanel the new accepted node types in path panel
   */
  public void setAcceptedNodeTypesInPathPanel(String[] acceptedNodeTypesInPathPanel) {
    this.acceptedNodeTypesInPathPanel = acceptedNodeTypesInPathPanel;
  }  

  /**
   * Gets the accepted mime types.
   * 
   * @return the accepted mime types
   */
  public String[] getAcceptedMimeTypes() { return acceptedMimeTypes; }

  /**
   * Sets the accepted mime types.
   * 
   * @param acceptedMimeTypes the new accepted mime types
   */
  public void setAcceptedMimeTypes(String[] acceptedMimeTypes) { this.acceptedMimeTypes = acceptedMimeTypes; } 

  /**
   * Gets the repository name.
   * 
   * @return the repository name
   */
  public String getRepositoryName() { return repositoryName; }
  
  /**
   * Sets the repository name.
   * 
   * @param repositoryName the new repository name
   */
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  /**
   * Gets the workspace name.
   * 
   * @return the workspace name
   */
  public String getWorkspaceName() { return workspaceName; }

  /**
   * Sets the workspace name.
   * 
   * @param workspaceName the new workspace name
   */
  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }

  /**
   * Gets the root tree path.
   * 
   * @return the root tree path
   */
  public String getRootTreePath() { return rootTreePath; }

  /**
   * Sets the root tree path.
   * 
   * @param rootTreePath the new root tree path
   */
  public void setRootTreePath(String rootTreePath) { this.rootTreePath = rootTreePath; 
  }      

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector#onChange(javax.jcr.Node, java.lang.Object)
   */
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

  /**
   * Change node.
   * 
   * @param stringPath the string path
   * @param context the context
   * 
   * @throws Exception the exception
   */
  private void changeNode(String stringPath, Object context) throws Exception {
    UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);
    builder.changeNode(stringPath, context);
  }

  /**
   * Change group.
   * 
   * @param groupId the group id
   * @param context the context
   * 
   * @throws Exception the exception
   */
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
        if (pathName != null && !pathName.equals("")) {
          stringPath += pathName.trim();
          if (i < listLocalPathString.size() - 1) stringPath += "/";
        }
      }
      changeNode(stringPath, context);
    }
  }

  /**
   * The listener interface for receiving selectPathAction events.
   * The class that is interested in processing a selectPathAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectPathActionListener<code> method. When
   * the selectPathAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectPathActionEvent
   */
  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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
