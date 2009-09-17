package org.exoplatform.wcm.webui.selector.document;

import org.exoplatform.wcm.webui.selector.webcontent.UIWCMNodeTypeSelectForm;
import org.exoplatform.wcm.webui.selector.webcontent.UIWCMSearchResult;
import org.exoplatform.wcm.webui.selector.webcontent.UIWCMSelectPropertyForm;
import org.exoplatform.wcm.webui.selector.webcontent.UIWebContentSearchForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : TAN DUNG DANG
 * dzungdev@gmail.com
 * Feb 14, 2009
 */
@ComponentConfigs ({
  @ComponentConfig(
      template = "system:/groovy/webui/core/UITabPane_New.gtmpl"
  ),
  @ComponentConfig(
      type = UIPopupWindow.class,
      id = "UIDocumentSearchPopup",
      template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
      events = {
        @EventConfig(listeners = UIDocumentTabSelector.CloseActionListener.class, name = "ClosePopup")
      }
  )
})
public class UIDocumentTabSelector extends UITabPane {

  /** The Constant DOCUMENT_METADATA_POPUP. */
  final static public String DOCUMENT_METADATA_POPUP = "DocumentMetadataPopup";
  
  /** The Constant DOCUMENT_NODETYPE_POPUP. */
  final static public String DOCUMENT_NODETYPE_POPUP = "DocumentNodeTypePopup";

  /**
   * Instantiates a new uI document tab selector.
   * 
   * @throws Exception the exception
   */
  public UIDocumentTabSelector() throws Exception {
    addChild(UIDocumentPathSelector.class, null, null);
    addChild(UIDocumentSearchForm.class, null, null);
    addChild(UIWCMSearchResult.class, null, null);
    setSelectedTab(1);
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    getChild(UIDocumentPathSelector.class).init();
    getChild(UIDocumentSearchForm.class).init();
  }

  /**
   * Inits the metadata popup.
   * 
   * @throws Exception the exception
   */
  public void initMetadataPopup() throws Exception {
    UIPopupWindow uiPopupWindow = 
      addChild(UIPopupWindow.class, "UIDocumentSearchPopup", DOCUMENT_METADATA_POPUP);
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

  /**
   * Inits the node type popup.
   * 
   * @throws Exception the exception
   */
  public void initNodeTypePopup() throws Exception {
    UIPopupWindow uiPopupWindow = 
      addChild(UIPopupWindow.class, "UIDocumentSearchPopup", DOCUMENT_NODETYPE_POPUP);
    UIWCMNodeTypeSelectForm uiNTSelectForm = 
      createUIComponent(UIWCMNodeTypeSelectForm.class, null, null);
    uiPopupWindow.setUIComponent(uiNTSelectForm);
    uiNTSelectForm.init();
    uiPopupWindow.setWindowSize(500, 450);
    uiPopupWindow.setResizable(true);
    uiPopupWindow.setShow(true);
    this.setSelectedTab(uiPopupWindow.getId());
  }

  /**
   * The listener interface for receiving closeAction events.
   * The class that is interested in processing a closeAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the closeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  public static class CloseActionListener extends EventListener<UIPopupWindow> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIDocumentTabSelector uiDocTabSelector = 
        event.getSource().getAncestorOfType(UIDocumentTabSelector.class);
      UIDocumentSearchForm uiDocSearchForm = 
        uiDocTabSelector.getChild(UIDocumentSearchForm.class);
      uiDocTabSelector.removeChild(UIPopupWindow.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocTabSelector);
      uiDocTabSelector.setSelectedTab(uiDocSearchForm.getId());
    }
  }
}
