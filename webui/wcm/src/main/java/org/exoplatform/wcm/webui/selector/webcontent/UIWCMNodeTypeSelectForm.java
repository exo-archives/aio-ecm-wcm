package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
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

  public UIWCMNodeTypeSelectForm() throws Exception {
  }

  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIWCMNodeTypeSelectForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }

  @SuppressWarnings("unchecked")
  public void init() throws Exception {
    getChildren().clear();
    UIFormCheckBoxInput<String> uiCheckBox;
    List<String> nodeTypes = getWebContentNodeTypes();
    for(String nodeType : nodeTypes) {
      uiCheckBox = new UIFormCheckBoxInput<String>(nodeType, nodeType, "");
      if(propertiesSelected(nodeType)) uiCheckBox.setChecked(true);
      else uiCheckBox.setChecked(false);
      addUIFormInput(uiCheckBox);
    }
  }

  private boolean propertiesSelected(String name) {
    UIPopupWindow uiPopupWindow = getParent();
    UIWebContentTabSelector uiWCTabSelector = 
      uiPopupWindow.getAncestorOfType(UIWebContentTabSelector.class);
    UIWebContentSearchForm uiWCSearchForm = 
      uiWCTabSelector.getChild(UIWebContentSearchForm.class);
    String typeValues = uiWCSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue() ;
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

  private void setNodeTypes(List<String> selectedNodeTypes, UIWebContentSearchForm uiWCSearchForm) {
    String strNodeTypes = null ;
    for(int i = 0 ; i < selectedNodeTypes.size() ; i++) {
      if(strNodeTypes == null) strNodeTypes = selectedNodeTypes.get(i) ;
      else strNodeTypes = strNodeTypes + "," + selectedNodeTypes.get(i) ;
    }
    uiWCSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).setValue(strNodeTypes) ;
  }

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

  public static class SaveActionListener extends EventListener<UIWCMNodeTypeSelectForm> {
    public void execute(Event<UIWCMNodeTypeSelectForm> event) throws Exception {
      UIWCMNodeTypeSelectForm uiNTSelectForm = event.getSource();
      UIPopupWindow uiPopup = uiNTSelectForm.getAncestorOfType(UIPopupWindow.class);
      UIWebContentTabSelector uiWCTabSelector = uiPopup.getParent();
      UIWebContentSearchForm uiWCSearchForm = 
        uiWCTabSelector.getChild(UIWebContentSearchForm.class);
      List<String> selectedNodeTypes = new ArrayList<String>();
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiNTSelectForm.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      String nodeTypesValue = 
        uiWCSearchForm.getUIStringInput(UIWebContentSearchForm.DOC_TYPE).getValue();
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
      uiNTSelectForm.setNodeTypes(selectedNodeTypes, uiWCSearchForm);
      uiWCTabSelector.removeChild(UIPopupWindow.class);
      uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
    }
  }

  public static class CancelActionListener extends EventListener<UIWCMNodeTypeSelectForm> {
    public void execute(Event<UIWCMNodeTypeSelectForm> event) throws Exception {
      UIWCMNodeTypeSelectForm uiNTSelectForm = event.getSource();
      UIPopupWindow uiPopupWindow = uiNTSelectForm.getAncestorOfType(UIPopupWindow.class);
      UIWebContentTabSelector uiWCTabSelector = 
        uiPopupWindow.getAncestorOfType(UIWebContentTabSelector.class);
      UIWebContentSearchForm uiWCSearchForm = 
        uiWCTabSelector.getChild(UIWebContentSearchForm.class);
      uiWCTabSelector.removeChild(UIPopupWindow.class);
      uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
    }
  }
}
