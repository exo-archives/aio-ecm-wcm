package org.exoplatform.wcm.webui.selector.webContentView;

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

  final static public String WEB_CONTENT_METADATA_POPUP = "WebContentMetadataPopup";
  final static public String WEB_CONTENT_NODETYPE_POPUP = "WebContentNodeTypePopup";

  public UIWebContentTabSelector() throws Exception {
    addChild(UIWebContentPathSelector.class, null, null);
    addChild(UIWebContentSearchForm.class,null,null);
    addChild(UIWCMSearchResult.class,null,null);
    setSelectedTab(1);
  }

  public void init() throws Exception {
    getChild(UIWebContentPathSelector.class).init();
    getChild(UIWebContentSearchForm.class).init();
  }

  public void initMetadataPopup() throws Exception {
    UIPopupWindow uiPopupWindow = 
      addChild(UIPopupWindow.class, "UIWebContentSearchPopup", WEB_CONTENT_METADATA_POPUP);
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
    UIPopupWindow uiPopupWindow = 
      addChild(UIPopupWindow.class, "UIWebContentSearchPopup", WEB_CONTENT_NODETYPE_POPUP);
    UIWCMNodeTypeSelectForm uiNTSelectForm = 
      createUIComponent(UIWCMNodeTypeSelectForm.class, null, null);
    uiPopupWindow.setUIComponent(uiNTSelectForm);
    uiNTSelectForm.init();
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWCTabSelector);
      uiWCTabSelector.setSelectedTab(uiWCSearchForm.getId());
    }    
  }
}
