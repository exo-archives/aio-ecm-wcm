package org.exoplatform.wcm.webui.wiki;

import javax.servlet.http.HttpServletRequestWrapper;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Mar 4, 2009  
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)

public class UIWikiPortlet extends UIPortletApplication {

  public UIWikiPortlet() throws Exception {
    addChild(UIWikiContentForm.class, null, null);
  }

}
