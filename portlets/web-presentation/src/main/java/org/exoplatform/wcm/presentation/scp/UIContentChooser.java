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
package org.exoplatform.wcm.presentation.scp;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.portlet.PortletPreferences;

import org.exoplatform.dms.model.ContentStorePath;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 24, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIContentChooser.SaveActionListener.class),
      @EventConfig(listeners = UIContentChooser.BackActionListener.class),
      @EventConfig(listeners = UIDocumentChooser.BrowseActionListener.class)
    }
)
public class UIContentChooser extends UIDocumentChooser {
  
  public UIContentChooser() throws Exception {
    super(UIDocumentChooser.SELECT_DOCUMENT_MODE) ;
    setActions(new String [] {"Save", "Back"}) ;
  }

  public static class SaveActionListener extends EventListener<UIContentChooser> {    
    public void execute(Event<UIContentChooser> event) throws Exception {
      UIContentChooser uiForm = event.getSource() ; 
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      ContentStorePath storePath = new ContentStorePath() ;
      uiForm.invokeSetBindingBean(storePath) ;
      ManageableRepository repo = uiForm.getApplicationComponent(RepositoryService.class).getRepository(storePath.getRepository()) ;
      Session session = SessionProviderFactory.createSystemProvider().getSession(storePath.getWorkspace(), repo) ;      
      Node node = ((Node) session.getItem(storePath.getPath())) ;
      if(!node.isNodeType("mix:referenceable")) {
        node.addMixin("mix:referenceable") ;
        session.save();
      }
      String nodeUUID = node.getUUID() ;
      PortletPreferences prefs = context.getRequest().getPreferences() ;            
      prefs.setValue(UISimplePresentationPortlet.REPOSITORY, storePath.getRepository()) ;
      prefs.setValue(UISimplePresentationPortlet.WORKSPACE, storePath.getWorkspace()) ;
      prefs.setValue(UISimplePresentationPortlet.UUID, nodeUUID) ;
      prefs.store() ;      
      //TODO should use other way to set the application info      
      try{        
        PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance();      
        String instanceId =  portletRequestContext.getApplication().getApplicationId() + "/" + portletRequestContext.getWindowId();
        Value value = session.getValueFactory().createValue(instanceId) ;        
        if(!node.isNodeType("exo:applicationLinkable")) {
          node.addMixin("exo:applicationLinkable");
          node.setProperty("exo:linkedApplications",new Value[]{value}) ;
        }else {
          List<Value> list = new ArrayList<Value>() ;
          list.add(value) ;
          for(Value v: node.getProperty("exo:linkedApplications").getValues()) {
            if(value.getString().equalsIgnoreCase(v.getString())) continue ;
            list.add(v);
          }
          node.setProperty("exo:linkedApplications",list.toArray(new Value[list.size()])) ;         
        }
        session.save();
      }catch (Exception e) {
        e.printStackTrace();
      }      

    }

  }

  public static class BackActionListener extends EventListener<UIContentChooser> {

    public void execute(Event<UIContentChooser> event) throws Exception {
      UIContentChooser uiChooser = event.getSource() ;
      UIPortletConfig uiConfig = uiChooser.getAncestorOfType(UIPortletConfig.class) ;
      uiConfig.getChildren().clear() ;
      uiConfig.addChild(uiConfig.getBackComponent()) ;
    }

  }

}
