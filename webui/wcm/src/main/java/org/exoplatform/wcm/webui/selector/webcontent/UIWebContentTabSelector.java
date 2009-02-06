package org.exoplatform.wcm.webui.selector.webcontent;

import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Jan 20, 2009  
 */

@ComponentConfigs ({
  @ComponentConfig(
      template = "system:/groovy/webui/core/UITabPane_New.gtmpl"
  ),
  @ComponentConfig(
      type = UIPopupWindow.class,
      id = "UIWebContentSearchPopup",
      template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
      events = {
        @EventConfig(listeners = UIWebContentTabSelector.CloseActionListener.class, name = "ClosePopup")
      }
  )
})

public class UIWebContentTabSelector extends UITabPane {

  final static public String METADATA_POPUP = "MetadataPopup";
  final static public String NODETYPE_POPUP = "NodeTypePopup";
  final static public String CATEGORY_POPUP = "CategoryPopup";

  public UIWebContentTabSelector() throws Exception {
    addChild(UIWebContentPathSelector.class, null, null);
    addChild(UIWebContentSearchForm.class,null,null);
    addChild(UISelectPathPanel.class,null,null);
    setSelectedTab(1);
  }

  public void init() throws Exception {
    getChild(UIWebContentPathSelector.class).init();
    getChild(UIWebContentSearchForm.class).init();
  }

  public void initMetadataPopup() throws Exception {
    UIPopupWindow uiPopupWindow = addChild(UIPopupWindow.class, "UIWebContentSearchPopup", METADATA_POPUP);
    UIWCMSelectPropertyForm uiSelectProperty = 
      createUIComponent(UIWCMSelectPropertyForm.class, null, null);
    uiSelectProperty.setFieldName(UIWebContentSearchForm.PROPERTY);
    uiSelectProperty.init();
    uiPopupWindow.setUIComponent(uiSelectProperty);
    uiPopupWindow.setWindowSize(500, 450);
    uiPopupWindow.setResizable(true);
    uiPopupWindow.setShow(true);
    this.setSelectedTab(uiPopupWindow.getId());
  }

  public void initNodeTypePopup() throws Exception {
    UIPopupWindow uiPopupWindow = addChild(UIPopupWindow.class, "UIWebContentSearchPopup", NODETYPE_POPUP);
    UIWCMNodeTypeSelectForm uiNTSelectForm = 
      createUIComponent(UIWCMNodeTypeSelectForm.class, null, null);
    uiPopupWindow.setUIComponent(uiNTSelectForm);
    uiNTSelectForm.init();
    uiPopupWindow.setWindowSize(500, 450);
    uiPopupWindow.setResizable(true);
    uiPopupWindow.setShow(true);
    this.setSelectedTab(uiPopupWindow.getId());
  }

  public void initCategoryPopup() throws Exception {
    UIPopupWindow uiPopupWindow = 
      addChild(UIPopupWindow.class, "UIWebContentSearchPopup", NODETYPE_POPUP);
    UIWCMCategorySelectForm uiCategorySelect = 
      createUIComponent(UIWCMCategorySelectForm.class, null, null);
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    ManageableRepository maRepository = repoService.getCurrentRepository();
    String workspaceName = 
      maRepository.getConfiguration().getSystemWorkspaceName();
    uiCategorySelect.setIsDisable(workspaceName, true);
    String repository = maRepository.getConfiguration().getName();
    NodeHierarchyCreator nodeHierCreator = getApplicationComponent(NodeHierarchyCreator.class);
    uiCategorySelect.setRootNodeLocation(repository, workspaceName, 
        nodeHierCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH));
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    uiCategorySelect.init(sessionProvider);
    UIWebContentSearchForm uiWCSearchForm = getChild(UIWebContentSearchForm.class);
    uiCategorySelect.setSourceComponent(uiWCSearchForm, new String[] {UIWebContentSearchForm.CATEGORY_TYPE});
    uiPopupWindow.setUIComponent(uiCategorySelect);
    uiPopupWindow.setWindowSize(500, 450);
    uiPopupWindow.setResizable(true);
    uiPopupWindow.setShow(true);
    this.setSelectedTab(uiPopupWindow.getId());
  }

  public static class CloseActionListener extends EventListener<UIPopupWindow> {
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIWebContentTabSelector uiWCTabSelector = 
        event.getSource().getAncestorOfType(UIWebContentTabSelector.class);
      UIWebContentSearchForm uiWCSearchForm = 
        uiWCTabSelector.getChild(UIWebContentSearchForm.class);
      uiWCTabSelector.removeChild(UIPopupWindow.class);
      uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
    }    
  }
}
