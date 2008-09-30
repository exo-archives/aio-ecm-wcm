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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 30, 2008  
 */
public class WCMPublicationPlugin extends WebpagePublicationPlugin {
  
  public void addMixin(Node node) throws Exception {    
  }

  @Override
  public boolean canAddMixin(Node node) throws Exception {    
    return false;
  }

  @Override
  public void changeState(Node node, String newState, HashMap<String, String> context)
      throws IncorrectStateUpdateLifecycleException, Exception {    
  }

  @Override
  public String[] getPossibleStates() {  
    return null;
  }

  @Override
  public byte[] getStateImage(Node node, Locale locale) throws IOException, FileNotFoundException,
      Exception {
    return null;
  }

  @Override
  public UIForm getStateUI(Node node, UIComponent component) throws Exception {
    
    return null;
  }

  @Override
  public String getUserInfo(Node node, Locale locale) throws Exception {
    return null;
  }

  @Override
  public void publishContentToPage(Node content, Page page) throws Exception {    
  }

  @Override
  public void suspendPublishedContentFromPage(Node content, Page page) throws Exception {
    
  }
  
  @SuppressWarnings("unused")
  public Node getNodeView(Node node, Map<String, Object> context) throws Exception {
    return node;
  }
}
