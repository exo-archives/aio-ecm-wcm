package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.wcm.webui.selector.document.UIDocumentSearchForm;
import org.exoplatform.wcm.webui.selector.document.UIDocumentTabSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

// TODO: Auto-generated Javadoc
/**
 * Author : TAN DUNG DANG
 * dzungdev@gmail.com
 * Feb 2, 2009
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIWCMNodeTypeSelectForm.SaveActionListener.class),
      @EventConfig(listeners = UIWCMNodeTypeSelectForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIWCMNodeTypeSelectForm extends UIForm {

  /**
   * Instantiates a new uIWCM node type select form.
   * 
   * @throws Exception the exception
   */
  public UIWCMNodeTypeSelectForm() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.form.UIForm#getLabel(java.util.ResourceBundle, java.lang.String)
   */
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIWCMNodeTypeSelectForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    getChildren().clear();
    UIFormCheckBoxInput<String> uiCheckBox;
    UIPopupWindow uiPopup = getAncestorOfType(UIPopupWindow.class);
    UIWebContentTabSelector uiWCTabSelector = 
      uiPopup.getAncestorOfType(UIWebContentTabSelector.class);
    List<String> nodeTypes = new ArrayList<String>();
    if(uiWCTabSelector != null) {
      nodeTypes = getWebContentNodeTypes();
    } else {
      nodeTypes = getDocumentNodeTypes();
    }
    for(String nodeType : nodeTypes) {
      uiCheckBox = new UIFormCheckBoxInput<String>(nodeType, nodeType, "");
      if(propertiesSelected(nodeType)) uiCheckBox.setChecked(true);
      else uiCheckBox.setChecked(false);
      addUIFormInput(uiCheckBox);
    }
  }

  /**
   * Properties selected.
   * 
   * @param name the name
   * 
   * @return true, if successful
   */
  private boolean propertiesSelected(String name) {
    UIPopupWindow uiPopupWindow = getParent();
    UIWebContentTabSelector uiWCTabSelector = 
      uiPopupWindow.getAncestorOfType(UIWebContentTabSelector.class);
    String typeValues = "";
    if(uiWCTabSelector == null) {
      UIDocumentTabSelector uiDocTabSelector =
        uiPopupWindow.getAncestorOfType(UIDocumentTabSelector.class);
      UIDocumentSearchForm uiDocSearchForm = 
        uiDocTabSelector.getChild(UIDocumentSearchForm.class);
      typeValues = uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue() ;
    } else { 
      UIWebContentSearchForm uiWCSearchForm = 
        uiWCTabSelector.getChild(UIWebContentSearchForm.class);
      typeValues = uiWCSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue() ;
    }
    if(typeValues == null) return false ;
    if(typeValues.indexOf(",") > -1) {
      String[] values = typeValues.split(",") ;
      for(String value : values) {
        if(value.equals(name)) return true ;
      }
    } else if(typeValues.equals(name)) {
      return true ;
    } 
    return false ;
  }

  /**
   * Sets the node types.
   * 
   * @param selectedNodeTypes the selected node types
   * @param uiWCSearchForm the ui wc search form
   */
  private void setNodeTypes(List<String> selectedNodeTypes, UIWebContentSearchForm uiWCSearchForm) {
    String strNodeTypes = null ;
    for(int i = 0 ; i < selectedNodeTypes.size() ; i++) {
      if(strNodeTypes == null) strNodeTypes = selectedNodeTypes.get(i) ;
      else strNodeTypes = strNodeTypes + "," + selectedNodeTypes.get(i) ;
    }
    uiWCSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).setValue(strNodeTypes) ;
  }

  /**
   * Sets the node types.
   * 
   * @param selectedNodeTypes the selected node types
   * @param uiDocSearchForm the ui doc search form
   */
  private void setNodeTypes(List<String> selectedNodeTypes, UIDocumentSearchForm uiDocSearchForm) {
    String strNodeTypes = null ;
    for(int i = 0 ; i < selectedNodeTypes.size() ; i++) {
      if(strNodeTypes == null) strNodeTypes = selectedNodeTypes.get(i) ;
      else strNodeTypes = strNodeTypes + "," + selectedNodeTypes.get(i) ;
    }
    uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).setValue(strNodeTypes) ;
  }

  /**
   * Gets the web content node types.
   * 
   * @return the web content node types
   * 
   * @throws Exception the exception
   */
  private List<String> getWebContentNodeTypes() throws Exception {
    List<String> webContentNodeTypes = new ArrayList<String>();
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    NodeTypeManager nodeTypeManager = repoService.getCurrentRepository().getNodeTypeManager();
    for(NodeTypeIterator nodeTypeIterator = nodeTypeManager.getAllNodeTypes();nodeTypeIterator.hasNext();) {
      NodeType nodeType = nodeTypeIterator.nextNodeType();
      if(nodeType.isNodeType("exo:webContent")) webContentNodeTypes.add(nodeType.getName());
    }
    return webContentNodeTypes;
  }

  /**
   * Gets the document node types.
   * 
   * @return the document node types
   * 
   * @throws Exception the exception
   */
  private List<String> getDocumentNodeTypes() throws Exception {
    List<String> documentNodeTypes = new ArrayList<String>();
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    String repositoryName = repoService.getCurrentRepository().getConfiguration().getName();
    TemplateService tempService = getApplicationComponent(TemplateService.class);
    documentNodeTypes = tempService.getDocumentTemplates(repositoryName);
    NodeTypeManager nodeTypeManager = repoService.getCurrentRepository().getNodeTypeManager();
    List<String> resultNodeTypes = new ArrayList<String>();
    for(String documentNodeType : documentNodeTypes) {
      NodeType nodeType = nodeTypeManager.getNodeType(documentNodeType);
      if(!nodeType.isNodeType("exo:webContent")) resultNodeTypes.add(nodeType.getName());
    }
    return resultNodeTypes;
  }

  /**
   * The listener interface for receiving saveAction events.
   * The class that is interested in processing a saveAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSaveActionListener<code> method. When
   * the saveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveActionEvent
   */
  public static class SaveActionListener extends EventListener<UIWCMNodeTypeSelectForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UIWCMNodeTypeSelectForm> event) throws Exception {
      UIWCMNodeTypeSelectForm uiNTSelectForm = event.getSource();
      UIPopupWindow uiPopup = uiNTSelectForm.getAncestorOfType(UIPopupWindow.class);
      UIWebContentTabSelector uiWCTabSelector = 
        uiPopup.getAncestorOfType(UIWebContentTabSelector.class);
      List<String> selectedNodeTypes = new ArrayList<String>();
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiNTSelectForm.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiPopup.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentSearchForm uiDocSearchForm = 
          uiDocTabSelector.getChild(UIDocumentSearchForm.class);
        String nodeTypesValue = 
          uiDocSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue();
        uiNTSelectForm.makeSelectedNode(nodeTypesValue, selectedNodeTypes, listCheckbox);
        uiNTSelectForm.setNodeTypes(selectedNodeTypes, uiDocSearchForm);
        uiDocTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
        uiDocTabSelector.setSelectedTab(uiDocSearchForm.getId());
      } else {
        UIWebContentSearchForm uiWCSearchForm = 
          uiWCTabSelector.getChild(UIWebContentSearchForm.class);
        String nodeTypesValue = 
          uiWCSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue();
        uiNTSelectForm.makeSelectedNode(nodeTypesValue, selectedNodeTypes, listCheckbox);
        uiNTSelectForm.setNodeTypes(selectedNodeTypes, uiWCSearchForm);
        uiWCTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
        uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
      }
    }
  }

  /**
   * Make selected node.
   * 
   * @param nodeTypesValue the node types value
   * @param selectedNodeTypes the selected node types
   * @param listCheckbox the list checkbox
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void makeSelectedNode(String nodeTypesValue, 
      List<String> selectedNodeTypes, List<UIFormCheckBoxInput> listCheckbox) throws Exception {
    if(nodeTypesValue != null && nodeTypesValue.length() > 0) {
      String[] array = nodeTypesValue.split(",");
      for(int i = 0; i < array.length; i ++) {
        selectedNodeTypes.add(array[i].trim());
      }
    }
    for(int i = 0; i < listCheckbox.size(); i ++) {
      if(listCheckbox.get(i).isChecked()) {
        if(!selectedNodeTypes.contains(listCheckbox.get(i).getName())) {
          selectedNodeTypes.add(listCheckbox.get(i).getName());
        }
      } else if(selectedNodeTypes.contains(listCheckbox.get(i))) {
        selectedNodeTypes.remove(listCheckbox.get(i).getName());
      } else {
        selectedNodeTypes.remove(listCheckbox.get(i).getName());
      }
    }
  } 

  /**
   * The listener interface for receiving cancelAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCancelActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CancelActionEvent
   */
  public static class CancelActionListener extends EventListener<UIWCMNodeTypeSelectForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWCMNodeTypeSelectForm> event) throws Exception {
      UIWCMNodeTypeSelectForm uiNTSelectForm = event.getSource();
      UIPopupWindow uiPopupWindow = uiNTSelectForm.getAncestorOfType(UIPopupWindow.class);
      UIWebContentTabSelector uiWCTabSelector = 
        uiPopupWindow.getAncestorOfType(UIWebContentTabSelector.class);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiPopupWindow.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentSearchForm uiDocSearchForm = 
          uiDocTabSelector.getChild(UIDocumentSearchForm.class);
        uiDocTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
        uiDocTabSelector.setSelectedTab(uiDocSearchForm.getId());
      } else {
        UIWebContentSearchForm uiWCSearchForm = 
          uiWCTabSelector.getChild(UIWebContentSearchForm.class);
        uiWCTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
        uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
      }
    }
  }
}
