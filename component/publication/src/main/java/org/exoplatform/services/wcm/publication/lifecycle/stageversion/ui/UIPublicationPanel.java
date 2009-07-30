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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionData;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong_phan@exoplatform.com
 * Mar 2, 2009
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/ui/UIPublicationPanel.gtmpl",
    events = {
      @EventConfig(listeners=UIPublicationPanel.DraftActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.LiveActionListener.class),
      @EventConfig(name= "obsolete", listeners= UIPublicationPanel.ObsoleteActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.ChangeVersionActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.PreviewVersionActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.RestoreVersionActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.SeeAllVersionActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.CloseActionListener.class)
    } 
)

public class UIPublicationPanel extends UIForm {

  /** The current node. */
  private Node currentNode;
  
  /** The current revision. */
  private Node currentRevision;  
  
  /** The revisions data map. */
  private Map<String,VersionData> revisionsDataMap = new HashMap<String,VersionData>();
  
  /** The viewed revisions. */
  private List<Node> viewedRevisions = new ArrayList<Node>(3);
  
  /**
   * Instantiates a new uI publication panel.
   * 
   * @throws Exception the exception
   */
  public UIPublicationPanel() throws Exception {}
  
  /**
   * Inits the.
   * 
   * @param node the node
   * 
   * @throws Exception the exception
   */
  public void init(Node node) throws Exception {
    this.currentNode = node;    
    this.currentRevision = node;
    this.viewedRevisions = getLatestRevisions(3,node);    
    this.revisionsDataMap = getRevisionData(node);
    //In somecases as copy a a node, we will lost all version of the node
    //So we will clean all publication data
    cleanPublicationData(node);
  }
  
  /**
   * Clean publication data.
   * 
   * @param node the node
   * 
   * @throws Exception the exception
   */
  private void cleanPublicationData(Node node) throws Exception {
    if(viewedRevisions.size() == 1 && revisionsDataMap.size()>1) {
      node.setProperty(StageAndVersionPublicationConstant.REVISION_DATA_PROP,new Value[] {});
      node.setProperty(StageAndVersionPublicationConstant.HISTORY,new Value[] {});
      node.setProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP,"");      
      node.save();
      this.revisionsDataMap = getRevisionData(node);
    }
  }
  
  /**
   * Gets the all revisions.
   * 
   * @param node the node
   * 
   * @return the all revisions
   * 
   * @throws Exception the exception
   */
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

  /**
   * Gets the current node.
   * 
   * @return the current node
   */
  public Node getCurrentNode() { return currentNode; }

  /**
   * Gets the current revision.
   * 
   * @return the current revision
   */
  public Node getCurrentRevision() { return currentRevision; }

  /**
   * Gets the revision author.
   * 
   * @param revision the revision
   * 
   * @return the revision author
   * 
   * @throws Exception the exception
   */
  public String getRevisionAuthor(Node revision) throws Exception{
    VersionData revisionData = revisionsDataMap.get(revision.getUUID());
    if(revisionData!= null)
      return revisionData.getAuthor();
    if(revision.getUUID().equalsIgnoreCase(currentNode.getUUID())) {
      return currentNode.getProperty("exo:owner").getString();
    }
    return null;
  }

  /**
   * Gets the revision by uuid.
   * 
   * @param revisionUUID the revision uuid
   * 
   * @return the revision by uuid
   * 
   * @throws Exception the exception
   */
  public Node getRevisionByUUID(String revisionUUID) throws Exception {
    Session session = currentNode.getSession();
    return session.getNodeByUUID(revisionUUID);
  }

  /**
   * Gets the revision created date.
   * 
   * @param revision the revision
   * 
   * @return the revision created date
   * 
   * @throws Exception the exception
   */
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

  /**
   * Gets the revisions.
   * 
   * @return the revisions
   */
  public List<Node> getRevisions() {
    return viewedRevisions;    
  }
  
  /**
   * Gets the revision state.
   * 
   * @param revision the revision
   * 
   * @return the revision state
   * 
   * @throws Exception the exception
   */
  public String getRevisionState(Node revision) throws Exception{
    VersionData revisionData = revisionsDataMap.get(revision.getUUID());
    if(revisionData!= null)
      return revisionData.getState();
    if(revision.getUUID().equalsIgnoreCase(currentNode.getUUID())) {
      return currentNode.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE).getString();
    }
    return null;
  }    

  /**
   * Sets the current revision.
   * 
   * @param revision the new current revision
   */
  public void setCurrentRevision(Node revision) { this.currentRevision = revision; }

  /**
   * Sets the revisions.
   * 
   * @param revisions the new revisions
   */
  public void setRevisions(List<Node> revisions) {
    this.viewedRevisions = revisions;
  }

  /**
   * Update panel.
   * 
   * @throws Exception the exception
   */
  public void updatePanel() throws Exception{     
    UIPublicationContainer publicationContainer = getAncestorOfType(UIPublicationContainer.class);
    UIPublicationHistory publicationHistory = publicationContainer.getChild(UIPublicationHistory.class);
    publicationHistory.updateGrid();
    this.revisionsDataMap = getRevisionData(currentNode);
    this.viewedRevisions = getLatestRevisions(3,currentNode);
  }

  /**
   * Gets the latest revisions.
   * 
   * @param limit the limit
   * @param node the node
   * 
   * @return the latest revisions
   * 
   * @throws Exception the exception
   */
  private List<Node> getLatestRevisions(int limit, Node node) throws Exception {
    List<Node> allversions = getAllRevisions(node);
    List<Node> latestVersions = new ArrayList<Node>();
    if(allversions.size() > limit) {
      latestVersions = allversions.subList(0, limit);
    } else {
      latestVersions = allversions;
    }
    return latestVersions;
  }

  /**
   * Gets the revision data.
   * 
   * @param node the node
   * 
   * @return the revision data
   * 
   * @throws Exception the exception
   */
  private Map<String, VersionData> getRevisionData(Node node) throws Exception{
    Map<String,VersionData> map = new HashMap<String,VersionData>();    
    try {
      for(Value v: node.getProperty(StageAndVersionPublicationConstant.REVISION_DATA_PROP).getValues()) {
        VersionData versionData = VersionData.toVersionData(v.getString());        
        map.put(versionData.getUUID(),versionData);;
      }
    } catch (Exception e) {
      return map;
    }
    return map;
  }   
  
  /**
   * The listener interface for receiving changeVersionAction events.
   * The class that is interested in processing a changeVersionAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addChangeVersionActionListener<code> method. When
   * the changeVersionAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChangeVersionActionEvent
   */
  public static class ChangeVersionActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
      Node revision = publicationPanel.getRevisionByUUID(versionUUID);
      publicationPanel.setCurrentRevision(revision);
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
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
  public static class CloseActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      UIPopupContainer uiPopupContainer = (UIPopupContainer) publicationPanel.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  } 

  /**
   * The listener interface for receiving draftAction events.
   * The class that is interested in processing a draftAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addDraftActionListener<code> method. When
   * the draftAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DraftActionEvent
   */
  public static class DraftActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if(currentRevision != null) {
        context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME,currentRevision.getName()); 
      }      
      try {
        publicationPlugin.changeState(currentNode,StageAndVersionPublicationConstant.DRAFT_STATE,context);
        publicationPanel.updatePanel();
      } catch (Exception e) {
        e.printStackTrace();
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  } 

  /**
   * The listener interface for receiving liveAction events.
   * The class that is interested in processing a liveAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addLiveActionListener<code> method. When
   * the liveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see LiveActionEvent
   */
  public static class LiveActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();      
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();      
      Node currentRevision = publicationPanel.getCurrentRevision();
      if(currentRevision != null) {
        context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME,currentRevision.getName()); 
      }
      try {
        publicationPlugin.changeState(currentNode,StageAndVersionPublicationConstant.LIVE_STATE,context); 
        publicationPanel.updatePanel();
      } catch (Exception e) {        
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);      
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  } 

  /**
   * The listener interface for receiving obsoleteAction events.
   * The class that is interested in processing a obsoleteAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addObsoleteActionListener<code> method. When
   * the obsoleteAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ObsoleteActionEvent
   */
  public static class ObsoleteActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();     
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
      HashMap<String,String> context = new HashMap<String,String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if(currentRevision != null) {
        context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME,currentRevision.getName()); 
      }
      try {
        publicationPlugin.changeState(currentNode,StageAndVersionPublicationConstant.OBSOLETE_STATE,context); 
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }  
  
  /**
   * The listener interface for receiving previewVersionAction events.
   * The class that is interested in processing a previewVersionAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addPreviewVersionActionListener<code> method. When
   * the previewVersionAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see PreviewVersionActionEvent
   */
  public static class PreviewVersionActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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
        uiApp.addMessage(new ApplicationMessage("UIPublicationPanel.msg.have-no-view-template", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if (publicationContainer.getChildById("UIVersionViewer") == null) publicationContainer.addChild(versionViewer);
      else publicationContainer.replaceChild("UIVersionViewer", versionViewer);
      publicationContainer.setActiveTab(versionViewer, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving restoreVersionAction events.
   * The class that is interested in processing a restoreVersionAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addRestoreVersionActionListener<code> method. When
   * the restoreVersionAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see RestoreVersionActionEvent
   */
  public static class RestoreVersionActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {      
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();        
      String versionUUID = event.getRequestContext().getRequestParameter(OBJECTID);
      Version version = (Version)publicationPanel.getRevisionByUUID(versionUUID);
      try {
        currentNode.restore(version,true);
        if(!currentNode.isCheckedOut())
          currentNode.checkout();
        PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
        PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
        HashMap<String,String> context = new HashMap<String,String>();
        publicationPlugin.changeState(currentNode,StageAndVersionPublicationConstant.ENROLLED_STATE,context);
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp,e);
      }        
      
      UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIPublicationPanel.msg.restore-complete", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  } 

  /**
   * The listener interface for receiving seeAllVersionAction events.
   * The class that is interested in processing a seeAllVersionAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSeeAllVersionActionListener<code> method. When
   * the seeAllVersionAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SeeAllVersionActionEvent
   */
  public static class SeeAllVersionActionListener extends EventListener<UIPublicationPanel> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      publicationPanel.setRevisions(publicationPanel.getAllRevisions(publicationPanel.getCurrentNode()));
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  } 
}
