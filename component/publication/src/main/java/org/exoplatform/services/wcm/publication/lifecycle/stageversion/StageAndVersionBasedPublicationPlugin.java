/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 2, 2009  
 */
public class StageAndVersionBasedPublicationPlugin extends WebpagePublicationPlugin{
  
  public void addMixin(Node node) throws Exception {
    node.addMixin(Constant.PUBLICATION_LIFECYCLE_TYPE);
    if(!node.isNodeType(Constant.MIX_VERSIONABLE)) {
      node.addMixin(Constant.MIX_VERSIONABLE);
    }            
  }

  public boolean canAddMixin(Node node) throws Exception {
    return node.canAddMixin(Constant.PUBLICATION_LIFECYCLE_TYPE);   
  }    
  
  public void changeState(Node node, String newState, HashMap<String, String> context) throws IncorrectStateUpdateLifecycleException,                                                                               Exception {
    node.setProperty(Constant.CURRENT_STATE,newState);
    VersionLog versionLog = null;
    if(Constant.ENROLLED.endsWith(newState)) {
      versionLog = new VersionLog(node.getUUID(),newState,node.getSession().getUserID(),GregorianCalendar.getInstance(),Constant.ENROLLED);
    }
    node.setProperty(Constant.HISTORY,versionLog.toStringValues());  
    if(!node.isNew()) 
      node.save();
  }

  public String getLocalizedAndSubstituteMessage(Locale arg0, String arg1, String[] arg2) throws Exception {
    return null;
  }

  public Node getNodeView(Node arg0, Map<String, Object> arg1) throws Exception {
    return null;
  }

  public String[] getPossibleStates() {    
    return new String[] { Constant.ENROLLED, Constant.DRAFT, Constant.AWAITING, Constant.LIVE, Constant.OBSOLETE};
  }

  public byte[] getStateImage(Node arg0, Locale arg1) throws IOException,
                                                     FileNotFoundException,
                                                     Exception {
    return null;
  }

  public UIForm getStateUI(Node node, UIComponent component) throws Exception {   
    UIPublicationContainer publicationContainer = component.createUIComponent(UIPublicationContainer.class, null, null);
    publicationContainer.initContainer(node);
    return publicationContainer;
  }

  public String getUserInfo(Node arg0, Locale arg1) throws Exception {
    return null;
  }
  
  public void publishContentToPage(Node content, Page page) throws Exception {
  }

  public void suspendPublishedContentFromPage(Node content, Page page) throws Exception {
  }

  public void updateLifecycleOnChangeNavigation(PageNavigation navigation) throws Exception {
  }

  public void updateLifecycleOnRemovePage(Page page) throws Exception {
  }

  public void updateLifecyleOnChangePage(Page page) throws Exception {
  }

  public void updateLifecyleOnCreateNavigation(PageNavigation navigation) throws Exception {
  }

  public void updateLifecyleOnCreatePage(Page page) throws Exception {
  }

  public void updateLifecyleOnRemoveNavigation(PageNavigation navigation) throws Exception {
  }
}
