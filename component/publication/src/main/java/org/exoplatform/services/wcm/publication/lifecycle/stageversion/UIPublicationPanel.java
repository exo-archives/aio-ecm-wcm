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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 2, 2009  
 */
@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/UIPublicationPanel.gtmpl",
     events = {
       @EventConfig(listeners=UIPublicationPanel.DraftActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.LiveActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.ObsoleteActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.ChangeVersionActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.PreviewVersionActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.RestoreVersionActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.SeeAllVersionActionListener.class),
       @EventConfig(listeners=UIPublicationPanel.CloseActionListener.class)
     } 
 )

public class UIPublicationPanel extends UIForm {

  public static final String START_TIME = "startTime".intern();
  public static final String END_TIME = "endTime".intern();

  private Version currentVersion;
  private List<Version> viewedVersions = new ArrayList<Version>(3);  
  private Node currentNode;

  public UIPublicationPanel() throws Exception {}

  public void init(Node node) throws Exception {
    this.currentNode = node;
    List<Version> allversions = getAllVersion(node);
    if (!allversions.isEmpty()) {
      this.currentVersion = allversions.get(0);
      if(allversions.size()>3) {
        viewedVersions = allversions.subList(0, 3);
      } else {
        viewedVersions = allversions;
      }
    }
  }

  private List<Version> getAllVersion(Node node) throws Exception {
    List<Version> allversions = new ArrayList<Version>();
    VersionIterator iterator = node.getVersionHistory().getAllVersions();
    for(;iterator.hasNext();) {
      Version version = iterator.nextVersion();
      if (version.getName().equals("jcr:rootVersion")) continue;
      allversions.add(version);      
    }    
    Collections.reverse(allversions);
    return allversions;
  }
  
  private Version getVersionByUUID(String versionUUID) throws Exception {
    Session session = currentNode.getSession();
    return (Version) session.getNodeByUUID(versionUUID);
  }
  
  public List<Version> getVersions() {
    return viewedVersions;    
  }

  public void setVersions(List<Version> versions) {
    this.viewedVersions = versions;
  }

  public Version getCurrentVerion() { return currentVersion; }
  public void setCurrentVersion(Version version) { this.currentVersion = version; }
  public Node getCurrentNode() { return currentNode; }
 
  public String getVersionState(Version version) {
    try {            
      Node frozenNode = version.getNode("jcr:frozenNode");
       return frozenNode.getProperty(Constant.REVISION_STATE).getString();      
    } catch (Exception e) {      
      return null;
    }
  }
  
  public String getVersionAuthor(Version version) {
    return "Root";
  }
  
  private void updateHistoryLog() throws Exception {
    UIPublicationContainer publicationContainer = getAncestorOfType(UIPublicationContainer.class);
    UIPublicationHistory publicationHistory = publicationContainer.getChild(UIPublicationHistory.class);
    publicationHistory.updateGrid();
  }
  
  public static class DraftActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();      
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(Constant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();
      Version version = publicationPanel.getCurrentVerion();
      if(version != null) {
        context.put(Constant.CURRENT_VERSION_NAME,version.getName()); 
      }      
      try {
        publicationPlugin.changeState(currentNode,Constant.DRAFT,context);
        publicationPanel.updateHistoryLog();
      } catch (Exception e) {
        e.printStackTrace();
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }
    }
  } 

  public static class LiveActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Version version = publicationPanel.getCurrentVerion();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(Constant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();      
      if(version != null) {
        context.put(Constant.CURRENT_VERSION_NAME,version.getName()); 
      }
      try {
        publicationPlugin.changeState(currentNode,Constant.LIVE,context); 
        publicationPanel.updateHistoryLog();
      } catch (Exception e) {
        e.printStackTrace();
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }      
    }
  } 

  public static class ObsoleteActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Version version = publicationPanel.getCurrentVerion();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(Constant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();
      if(version != null) {
        context.put(Constant.CURRENT_VERSION_NAME,version.getName()); 
      }
      try {
        publicationPlugin.changeState(currentNode,Constant.OBSOLETE,context); 
        publicationPanel.updateHistoryLog();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }
    }
  } 

  public static class ChangeVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
      Version version = publicationPanel.getVersionByUUID(versionUUID);
      publicationPanel.setCurrentVersion(version);
    }
  } 

  public static class PreviewVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      UIVersionViewer versionViewer = publicationContainer.createUIComponent(UIVersionViewer.class, null, "UIVersionViewer"); 
      String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
      Version version = publicationPanel.getVersionByUUID(versionUUID);
      Node frozenNode = version.getNode("jcr:frozenNode") ;
      versionViewer.setOriginalNode(publicationPanel.getCurrentNode());
      versionViewer.setNode(frozenNode);
      if(versionViewer.getTemplate() == null || versionViewer.getTemplate().trim().length() == 0) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIVersionInfo.msg.have-no-view-template", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if (publicationContainer.getChildById("UIVersionViewer") == null) publicationContainer.addChild(versionViewer);
      else publicationContainer.replaceChild("UIVersionViewer", versionViewer);
      for (UIComponent component : publicationContainer.getChildren()) {
        component.setRendered(false);
      }
      versionViewer.setRendered(true);
      publicationContainer.setSelectedTab("UIVersionViewer");
      event.getRequestContext().addUIComponentToUpdateByAjax(publicationContainer);
    }
  }
  
  public static class RestoreVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> RestoreVersionActionListener");
//      UIPublicationPanel publicationPanel = event.getSource();
//      
//      Node currentNode = publicationPanel.getCurrentNode();
//      if(currentNode.isLocked()) {
//        String lockToken = LockUtil.getLockToken(currentNode);
//        currentNode.getSession().addLockToken(lockToken) ;
//      }
//      
//      String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
//      Version version = publicationPanel.getVersionByUUID(versionUUID);
//      publicationPanel.getCurrentNode().restore(version, true);
//      
//      if(!currentNode.isCheckedOut()) currentNode.checkout() ;
//      currentNode.getSession().save() ;
    }
  }
  
  public static class SeeAllVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      publicationPanel.setVersions(publicationPanel.getAllVersion(publicationPanel.getCurrentNode()));
      event.getRequestContext().addUIComponentToUpdateByAjax(publicationPanel);
    }
  } 
  
  public static class CloseActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      UIPopupContainer uiPopupContainer = (UIPopupContainer) publicationPanel.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  } 
}
