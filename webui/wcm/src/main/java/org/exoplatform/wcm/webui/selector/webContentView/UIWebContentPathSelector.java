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
package org.exoplatform.wcm.webui.selector.webContentView;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.wcm.webui.selector.UISelectPathPanel;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com Jun 23, 2008
 */
@ComponentConfigs({
  @ComponentConfig(
      lifecycle = Lifecycle.class,
      template = "classpath:groovy/wcm/webui/UIWebContentPathSelector.gtmpl",
      events = {
        @EventConfig(listeners = UIWebContentPathSelector.ChangeContentTypeActionListener.class)
      }
  ),
  @ComponentConfig(
      type = UISelectPathPanel.class,
      id = "UIWCMSelectPathPanel",
      template = "classpath:groovy/wcm/webui/UIWCMSelectPathPanel.gtmpl",
      events = @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
  )
})
public class UIWebContentPathSelector extends UIBaseNodeTreeSelector implements UIPopupComponent{
  public static final String WEBCONENT = "WebContent";
  public static final String DMSDOCUMENT = "DMSDocument";
  public static final String MEDIA = "Media";
  public final String SELECT_TYPE_CONTENT = "selectTypeContent";
  public String[] types = new String[]{WEBCONENT, DMSDOCUMENT, MEDIA};
  private String selectedValues = WEBCONENT;
  
  /**
   * Instantiates a new uI web content path selector.
   * 
   * @throws Exception the exception
   */
  private Node currentPortal;
  public String contentType;

  public UIWebContentPathSelector() throws Exception {
    contentType = WEBCONENT;
    addChild(UIWebContentTreeBuilder.class,null, UIWebContentTreeBuilder.class.getName()+hashCode());
    addChild(UISelectPathPanel.class, "UIWCMSelectPathPanel", "UIWCMSelectPathPanel");
  }
  
  public void reRenderChild(String typeContent) throws Exception{
    if(typeContent == null || typeContent.equals(WEBCONENT)){
      this.contentType = WEBCONENT;
    }else if(typeContent.equals(DMSDOCUMENT)){
      this.contentType = DMSDOCUMENT;
    }
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    String[] acceptedNodeTypes = null;
    if(contentType == null || contentType.equals(WEBCONENT)){
      acceptedNodeTypes = new String[]{"exo:webContent"};
    }else if(contentType.equals(DMSDOCUMENT)){
      String repositoryName = ((ManageableRepository)(currentPortal.getSession().getRepository())).getConfiguration().getName();
      List<String> listAcceptedNodeTypes = getApplicationComponent(TemplateService.class).getDocumentTemplates(repositoryName);
      listAcceptedNodeTypes.remove("exo:webContent");
      acceptedNodeTypes = new String[listAcceptedNodeTypes.size()];
      listAcceptedNodeTypes.toArray(acceptedNodeTypes);
    }
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypes);       
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    currentPortal = livePortalManagerService.getLivePortal(currentPortalName,provider);
    
    provider.close();
  }

  @Override
  public void onChange(Node node, Object context) throws Exception {  }

  public void activate() throws Exception {  }

  public void deActivate() throws Exception {  }

  /**
   * @return the currentPortal
   */
  public Node getCurrentPortal() {
    return currentPortal;
  }

  /**
   * @param currentPortal the currentPortal to set
   */
  public void setCurrentPortal(Node currentPortal) {
    this.currentPortal = currentPortal;
  }
  
  public static class ChangeContentTypeActionListener extends EventListener<UIWebContentPathSelector> {
    public void execute(Event<UIWebContentPathSelector> event) throws Exception {
      UIWebContentPathSelector contentPathSelector = event.getSource();
      String type = event.getRequestContext().getRequestParameter(OBJECTID);
      if(type.equals(contentPathSelector.selectedValues)) return;
      contentPathSelector.selectedValues = type;
      UISelectPathPanel selectPathPanel = contentPathSelector.getChild(UISelectPathPanel.class);
      selectPathPanel.setParentNode(null);
      selectPathPanel.updateGrid();
      contentPathSelector.reRenderChild(contentPathSelector.selectedValues);
      contentPathSelector.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(contentPathSelector);
    }
  }
}
