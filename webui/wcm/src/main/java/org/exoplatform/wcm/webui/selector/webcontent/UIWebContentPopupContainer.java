package org.exoplatform.wcm.webui.selector.webcontent;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

// TODO: Auto-generated Javadoc
/**
 * Author : TAN DUNG DANG
 * dzungdev@gmail.com
 * Jan 21, 2009
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UIWebContentPopupContainer extends UIContainer implements UIPopupComponent {

  /** The Constant METADATA_POPUP. */
  final static public String METADATA_POPUP = "MetadataPopup";
  
  /** The Constant NODETYPE_POPUP. */
  final static public String NODETYPE_POPUP = "NodeTypePopup";
  
  /** The Constant CATEGORY_POPUP. */
  final static public String CATEGORY_POPUP = "CategoryPopup";

  /**
   * Instantiates a new uI web content popup container.
   * 
   * @throws Exception the exception
   */
  public UIWebContentPopupContainer() throws Exception {
    addChild(UIWebContentTabSelector.class, null, null);
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    getChild(UIWebContentTabSelector.class).init();
  }

  /**
   * Inits the metadata popup.
   * 
   * @throws Exception the exception
   */
  public void initMetadataPopup() throws Exception {
    UIPopupContainer uiPopupContainer = getAncestorOfType(UIPopupContainer.class);
    if(!uiPopupContainer.getId().equals(METADATA_POPUP)) uiPopupContainer.setId(METADATA_POPUP);
    UIWCMSelectPropertyForm uiWCMSelectPropertyForm = 
      createUIComponent(UIWCMSelectPropertyForm.class, null, null);
    uiWCMSelectPropertyForm.setFieldName(UIWebContentSearchForm.PROPERTY);
    uiPopupContainer.activate(uiWCMSelectPropertyForm,500, 450);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#activate()
   */
  public void activate() throws Exception {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
}
