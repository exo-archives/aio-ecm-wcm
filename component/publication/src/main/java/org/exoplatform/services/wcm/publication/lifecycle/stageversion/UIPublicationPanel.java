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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.ArrayUtils;
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

  private Node currentRevision;
  private List<Node> viewedRevisions = new ArrayList<Node>(3);  
  private Node currentNode;

  public UIPublicationPanel() throws Exception {}

  public void init(Node node) throws Exception {
    this.currentNode = node;    
    this.currentRevision = node;
    this.viewedRevisions = getLatestRevisions(3,node);
  }

  public List<Node> getLatestRevisions(int limit, Node node) throws Exception {
    List<Node> allversions = getAllRevisions(node);
    List<Node> latestVersions = new ArrayList<Node>();
    if(allversions.size() > limit) {
      latestVersions = allversions.subList(0, limit);
    } else {
      latestVersions = allversions;
    }
    return latestVersions;
  }

  public List<Node> getAllRevisions(Node node) throws Exception {
    List<Node> allversions = new ArrayList<Node>();
    VersionIterator iterator = node.getVersionHistory().getAllVersions();
    for(;iterator.hasNext();) {
      Version version = iterator.nextVersion();
      if (version.getName().equals("jcr:rootVersion")) continue;
      allversions.add(version);      
    }    
    //current node is a revision
    allversions.add(node);
    Collections.reverse(allversions);
    return allversions;
  }

  public Node getRevisionByUUID(String revisionUUID) throws Exception {
    Session session = currentNode.getSession();
    return session.getNodeByUUID(revisionUUID);
  }

  public List<Node> getRevisions() {
    return viewedRevisions;    
  }

  public void setRevisions(List<Node> revisions) {
    this.viewedRevisions = revisions;
  }

  public Node getCurrentRevision() { return currentRevision; }
  public void setCurrentRevision(Node revision) { this.currentRevision = revision; }
  public Node getCurrentNode() { return currentNode; }

  public String getRevisionState(Node revision) throws Exception{
    if(revision instanceof Version) {
      Value referenceValue = revision.getSession().getValueFactory().createValue(revision);
      Value liveRevision = getLiveRevision();
      if(referenceValue.equals(liveRevision))
        return Constant.LIVE;
      Value[] obsoleteRevisions = getObsoleteRevisions();
      if(ArrayUtils.contains(obsoleteRevisions,referenceValue))
        return Constant.OBSOLETE;
    }
    return revision.getProperty(Constant.CURRENT_STATE).getString();    
  }

  private Value getLiveRevision() {
    try {
      return currentNode.getProperty(Constant.LIVE_REVISION_PROP).getValue();
    } catch (Exception e) {
      return null;
    }
  }

  private Value[] getObsoleteRevisions() {
    try {
      return currentNode.getProperty(Constant.OBSOLETE_REVISIONS_PROP).getValues();
    } catch (Exception e) {
      return new Value[] {} ;
    }
  }

  public String getRevisionAuthor(Node revision) {
    return "Root";
  }

  public String getRevisionCreatedDate(Node revision) throws Exception {
    UIPublicationContainer container = getAncestorOfType(UIPublicationContainer.class);
    DateFormat dateFormater = container.getDateTimeFormater();
    Calendar calendar = null;
    if(revision instanceof Version) {
      calendar= ((Version)revision).getCreated();
    }else {
      calendar = revision.getProperty("exo:dateCreated").getDate();
    }
    return dateFormater.format(calendar.getTime());
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
      Node currentRevision = publicationPanel.getCurrentRevision();
      if(currentRevision != null) {
        context.put(Constant.CURRENT_REVISION_NAME,currentRevision.getName()); 
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
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(Constant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();      
      Node currentRevision = publicationPanel.getCurrentRevision();
      if(currentRevision != null) {
        context.put(Constant.CURRENT_REVISION_NAME,currentRevision.getName()); 
      }
      try {
        publicationPlugin.changeState(currentNode,Constant.LIVE,context); 
        publicationPanel.updateHistoryLog();
      } catch (Exception e) {
        e.printStackTrace();
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationPanel.setRevisions(publicationPanel.getLatestRevisions(3, currentNode));
      event.getRequestContext().addUIComponentToUpdateByAjax(publicationContainer);
    }
  } 

  public static class ObsoleteActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();     
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(Constant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if(currentRevision != null) {
        context.put(Constant.CURRENT_REVISION_NAME,currentRevision.getName()); 
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
      Node revision = publicationPanel.getRevisionByUUID(versionUUID);
      publicationPanel.setCurrentRevision(revision);
    }
  } 

  public static class PreviewVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      UIVersionViewer versionViewer = publicationContainer.createUIComponent(UIVersionViewer.class, null, "UIVersionViewer"); 
      String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
      Node revision = publicationPanel.getRevisionByUUID(versionUUID);      
      Node frozenNode = revision;
      if(revision instanceof Version) {
        frozenNode = revision.getNode("jcr:frozenNode") ; 
      }        
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
//    UIPublicationPanel publicationPanel = event.getSource();

//    Node currentNode = publicationPanel.getCurrentNode();
//    if(currentNode.isLocked()) {
//    String lockToken = LockUtil.getLockToken(currentNode);
//    currentNode.getSession().addLockToken(lockToken) ;
//    }

//    String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
//    Version version = publicationPanel.getVersionByUUID(versionUUID);
//    publicationPanel.getCurrentNode().restore(version, true);

//    if(!currentNode.isCheckedOut()) currentNode.checkout() ;
//    currentNode.getSession().save() ;
    }
  }

  public static class SeeAllVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      publicationPanel.setRevisions(publicationPanel.getAllRevisions(publicationPanel.getCurrentNode()));
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
