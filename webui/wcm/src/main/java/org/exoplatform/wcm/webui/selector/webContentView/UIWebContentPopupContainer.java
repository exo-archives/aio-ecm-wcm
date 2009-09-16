package org.exoplatform.wcm.webui.selector.webContentView;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Jan 21, 2009  
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UIWebContentPopupContainer extends UIContainer implements UIPopupComponent {

  final static public String METADATA_POPUP = "MetadataPopup";
  final static public String NODETYPE_POPUP = "NodeTypePopup";
  final static public String CATEGORY_POPUP = "CategoryPopup";

  public UIWebContentPopupContainer() throws Exception {
    addChild(UIWebContentTabSelector.class, null, null);
  }

  public void init() throws Exception {
    getChild(UIWebContentTabSelector.class).init();
  }

  public void initMetadataPopup() throws Exception {
    UIPopupContainer uiPopupContainer = getAncestorOfType(UIPopupContainer.class);
    if(!uiPopupContainer.getId().equals(METADATA_POPUP)) uiPopupContainer.setId(METADATA_POPUP);
    UIWCMSelectPropertyForm uiWCMSelectPropertyForm = 
      createUIComponent(UIWCMSelectPropertyForm.class, null, null);
    uiWCMSelectPropertyForm.setFieldName(UIWebContentSearchForm.PROPERTY);
    uiPopupContainer.activate(uiWCMSelectPropertyForm,500, 450);
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
}
