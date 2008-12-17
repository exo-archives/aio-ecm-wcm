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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.jcr.Node;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Romain Dénarié
 *          romain.denarie@exoplatform.com
 * 29 mai 08  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPublicationStatus.gtmpl",
    events = {
      @EventConfig(listeners = UIPublicationComponentStatus.CloseActionListener.class)
    }
)

public class UIPublicationComponentStatus extends UIForm {

  private Node node_;

  public UIPublicationComponentStatus() throws Exception {
  }

  public UIPublicationComponentStatus(Node node) throws Exception {
    this.node_=node;
  }

  public Node getNode() {
    return this.node_;
  }

  public void setNode(Node node) {
    this.node_=node;
  }

  public String getNodeName() {
    try {
      return node_.getName();
    } catch (Exception e) {
      return "Error in getNodeName";
    }
  }

  public String getLifeCycleName () {
    try {
      PublicationService service = getApplicationComponent(PublicationService.class) ;
      return service.getNodeLifecycleName(node_);
    } catch (Exception e) {
      return "Error in getLifeCycleName";
    }
  }

  public String getStateName () {
    try {
      PublicationService service = getApplicationComponent(PublicationService.class) ;
      return service.getCurrentState(node_);
    } catch (Exception e) {
      return "Error in getStateName";
    }
  }

  public String getLinkStateImage (Locale locale) {
    try {
      DownloadService dS = getApplicationComponent(DownloadService.class);
      PublicationService service = getApplicationComponent(PublicationService.class) ;

      byte[] bytes=service.getStateImage(node_,locale);
      InputStream iS = new ByteArrayInputStream(bytes);    
      String id = dS.addDownloadResource(new InputStreamDownloadResource(iS, "image/gif"));
      return dS.getDownloadLink(id);
    } catch (Exception e) {
      return "Error in getStateImage";
    }
  }

  public static class CloseActionListener extends EventListener<UIPublicationComponentStatus> {
    public void execute(Event<UIPublicationComponentStatus> event) throws Exception {
      UIPublicationComponentStatus publicationComponentStatus = event.getSource();
      UIPublishingPanel publishingPanel = publicationComponentStatus.getAncestorOfType(UIPublishingPanel.class);
      UIPopupWindow popupAction = publishingPanel.getAncestorOfType(UIPopupWindow.class) ;
      popupAction.setShow(false);
      popupAction.setRendered(false);
    }
  }

}
