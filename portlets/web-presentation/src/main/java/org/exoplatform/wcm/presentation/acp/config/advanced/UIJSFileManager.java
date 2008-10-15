/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wcm.presentation.acp.config.advanced;

import javax.jcr.Node;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.wcm.presentation.acp.config.UIContentDialogForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 28, 2008  
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIJSFileManager extends UIContainer {

  public UIJSFileManager() throws Exception {
    addChild(UIJSGrid.class, null, null);

  }

  public void initJSPopup() throws Exception {
    UIContentDialogForm uiContentDialogForm = createUIComponent(UIContentDialogForm.class, null, "JSDialogForm");
    String portalName = Util.getUIPortal().getName();
    LivePortalManagerService portalManagerService = uiContentDialogForm.getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node portalNode = portalManagerService.getLivePortal(portalName, sessionProvider);
    WebSchemaConfigService configService = uiContentDialogForm.getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler handler = configService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    Node webContentStorage = handler.getWebContentStorage(portalNode);
    NodeLocation storedLocation = NodeLocation.make(webContentStorage);
    uiContentDialogForm.setStoredLocation(storedLocation);
    uiContentDialogForm.setContentType("exo:jsFile");
    uiContentDialogForm.addNew(true);
    uiContentDialogForm.resetProperties();
    UIPopupWindow uiPopup = getChildById("JSPopup");
    if (uiPopup == null) uiPopup = addChild(UIPopupWindow.class, null, "JSPopup");
    uiPopup.setUIComponent(uiContentDialogForm);
    uiPopup.setWindowSize(300, 400);
    uiPopup.setResizable(true);
    uiPopup.setShow(true);
    uiPopup.setRendered(true);
  }

}
