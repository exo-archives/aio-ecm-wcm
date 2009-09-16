package org.exoplatform.wcm.webui.selector.webContentView;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.wcm.webui.selector.document.UIDocumentSearchForm;
import org.exoplatform.wcm.webui.selector.document.UIDocumentTabSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Jan 21, 2009  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UIWCMSelectPropertyForm.CancelActionListener.class),
      @EventConfig(listeners = UIWCMSelectPropertyForm.AddActionListener.class),
      @EventConfig(listeners = UIWCMSelectPropertyForm.ChangeMetadataTypeActionListener.class)
    }    
)
public class UIWCMSelectPropertyForm extends UIForm{

  final static public String METADATA_TYPE = "metadataType" ;
  final static public String PROPERTY_SELECT = "property_select" ;

  private String fieldName = null ;

  private List<SelectItemOption<String>> properties = new ArrayList<SelectItemOption<String>>() ;

  public UIWCMSelectPropertyForm() throws Exception {
    setActions(new String[] {"Add", "Cancel"}) ;
  }

  public String getLabel(ResourceBundle res, String id)  {
    try {
      return super.getLabel(res, id) ;
    } catch (Exception ex) {
      return id ;
    }
  }

  public void init() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);

    UIFormSelectBox uiSelect = new UIFormSelectBox(METADATA_TYPE, METADATA_TYPE, options);
    uiSelect.setOnChange("ChangeMetadataType");
    addUIFormInput(uiSelect);
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manRepository = repoService.getCurrentRepository();
    //String workspaceName = manRepository.getConfiguration().getSystemWorkspaceName();
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspaceName = dmsConfiguration.getConfig(manRepository.getConfiguration().getName()).getSystemWorkspace();
    Session session = sessionProvider.getSession(workspaceName, manRepository);
    String metadataPath = nodeHierarchyCreator.getJcrPath(BasePath.METADATA_PATH);
    Node homeNode = (Node) session.getItem(metadataPath);
    NodeIterator nodeIter = homeNode.getNodes();
    Node meta = nodeIter.nextNode();
    renderProperties(meta.getName());
    options.add(new SelectItemOption<String>(meta.getName(), meta.getName()));
    while(nodeIter.hasNext()) {
      meta = nodeIter.nextNode();
      options.add(new SelectItemOption<String>(meta.getName(), meta.getName()));
    }
    addUIFormInput(new UIFormRadioBoxInput(PROPERTY_SELECT, null, properties).
        setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN));
    session.logout();
    sessionProvider.close();
  }

  public void setFieldName(String fieldName) { this.fieldName = fieldName ; }

  public void renderProperties(String metadata) throws Exception {
    properties.clear() ;
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manRepository = repoService.getCurrentRepository();
    String workspaceName = manRepository.getConfiguration().getSystemWorkspaceName();
    Session session = sessionProvider.getSession(workspaceName, manRepository);
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeType nt = ntManager.getNodeType(metadata);
    PropertyDefinition[] propertieDefs = nt.getPropertyDefinitions();
    for(PropertyDefinition property : propertieDefs) {
      String name = property.getName();
      if(!name.equals("exo:internalUse")) {
        this.properties.add(new SelectItemOption<String>(name,name));
      }
    }
    session.logout();
  }

  static  public class CancelActionListener extends EventListener<UIWCMSelectPropertyForm> {
    public void execute(Event<UIWCMSelectPropertyForm> event) throws Exception {
      UIWCMSelectPropertyForm uiForm = event.getSource();
      UIPopupWindow uiPopupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
      UIWebContentTabSelector uiWCTabSelector = 
        uiPopupWindow.getAncestorOfType(UIWebContentTabSelector.class);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiPopupWindow.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentSearchForm uiDocSearchForm = 
          uiDocTabSelector.findFirstComponentOfType(UIDocumentSearchForm.class);
        uiDocTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
        uiDocTabSelector.setSelectedTab(uiDocSearchForm.getId());
      } else {
        uiWCTabSelector.removeChild(UIPopupWindow.class);
        UIWebContentSearchForm uiWCSearchForm = 
          uiWCTabSelector.getChild(UIWebContentSearchForm.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
        uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
      }
    }
  }

  static  public class AddActionListener extends EventListener<UIWCMSelectPropertyForm> {
    public void execute(Event<UIWCMSelectPropertyForm> event) throws Exception {
      UIWCMSelectPropertyForm uiForm = event.getSource();
      String property = uiForm.<UIFormRadioBoxInput>getUIInput(PROPERTY_SELECT).getValue();
      UIPopupWindow uiPopupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
      UIWebContentTabSelector uiWCTabSelector = 
        uiPopupWindow.getAncestorOfType(UIWebContentTabSelector.class);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiPopupWindow.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentSearchForm uiDocSearchForm = 
          uiDocTabSelector.findFirstComponentOfType(UIDocumentSearchForm.class);
        uiDocSearchForm.getUIStringInput(uiForm.getFieldName()).setValue(property);
        uiDocTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
        uiDocTabSelector.setSelectedTab(uiDocSearchForm.getId());
      } else {
        UIWebContentSearchForm uiWCSearchForm =
          uiWCTabSelector.findFirstComponentOfType(UIWebContentSearchForm.class);
        uiWCSearchForm.getUIStringInput(uiForm.getFieldName()).setValue(property);
        uiWCTabSelector.removeChild(UIPopupWindow.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
        uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
      }
    }
  }

  static  public class ChangeMetadataTypeActionListener extends EventListener<UIWCMSelectPropertyForm> {
    public void execute(Event<UIWCMSelectPropertyForm> event) throws Exception {
      UIWCMSelectPropertyForm uiForm = event.getSource();
      uiForm.renderProperties(uiForm.getUIFormSelectBox(METADATA_TYPE).getValue());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  public String getFieldName() {
    return fieldName;
  }
}
