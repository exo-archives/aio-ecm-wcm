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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;

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
      @EventConfig(listeners=UIPublicationPanel.EnrolledActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.DraftActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.AwaitingActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.LiveActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.ObsoleteActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.ChangeVersionActionListener.class),
      @EventConfig(listeners=UIPublicationPanel.CloseActionListener.class)
    } 
)
public class UIPublicationPanel extends UIForm {

  public static final String START_TIME = "startTime".intern();
  public static final String END_TIME = "endTime".intern();
  private List<Version> versions = new ArrayList<Version>();

  public void init(Node node) throws Exception {
    VersionIterator versionIterator = node.getVersionHistory().getAllVersions();
    for (;versionIterator.hasNext();) {
      versions.add(versionIterator.nextVersion());
    }
    addUIFormInput(new UIFormDateTimeInput(START_TIME, START_TIME, null, true));
    addUIFormInput(new UIFormDateTimeInput(END_TIME, END_TIME, null, true));
  }

  public List<Version> getVersions() {
    return versions;
  }

  public void setVersions(List<Version> versions) {
    this.versions = versions;
  }
  
  public static class EnrolledActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> EnrolledActionListener");
    }
  } 
  
  public static class DraftActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> DraftActionListener");
    }
  } 
  
  public static class AwaitingActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> AwaitingActionListener");
    }
  } 
  
  public static class LiveActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> LiveActionListener");
    }
  } 
  
  public static class ObsoleteActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> ObsoleteActionListener");
    }
  } 
  
  public static class ChangeVersionActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      System.out.println("------------------------------------------------> ChangeVersionActionListener");
    }
  } 
  
  public static class CloseActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      UIPopupContainer uiPopupContainer = (UIPopupContainer) publicationPanel.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
    }
  } 
}
