/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.dms.webui.component.UISelectable;
import org.exoplatform.dms.webui.utils.JCRExceptionManager;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.presentation.scp.UIPathChooser.ContentStorePath;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:28:18 PM 
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig(listeners = UICategoriesAddedList.DeleteActionListener.class, confirm="UICategoriesAddedList.msg.confirm-delete")
    }
)
public class UICategoriesAddedList extends UIContainer implements UISelectable{

  private static String[] CATE_BEAN_FIELD = {"path"} ;
  private static String[] ACTION = {"Delete"} ;
  private ContentStorePath storePath_ ;

  public UICategoriesAddedList() throws Exception {
    UIGrid uiGrid = addChild(UIGrid.class, null, "CateAddedList") ;
    uiGrid.getUIPageIterator().setId("CategoriesListIterator");
    uiGrid.configure("path", CATE_BEAN_FIELD, ACTION) ;
  }
  
  public ContentStorePath getStorePath() { return storePath_ ; }
  public void setStorePath(ContentStorePath path) { storePath_ = path ; }
  
  public void updateGrid(List<Node> nodes) throws Exception {
    UIGrid uiGrid = getChild(UIGrid.class) ;   
    if(nodes == null) nodes = new ArrayList<Node>() ;
    ObjectPageList objPageList = new ObjectPageList(nodes, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;
  }
  
  public Node getNode() throws Exception {
    if(storePath_ == null) return null ;
    RepositoryService repoService = getApplicationComponent(RepositoryService.class) ;
    ManageableRepository repo = repoService.getRepository(storePath_.getRepository()) ;
    Session session = SessionProviderFactory.createSystemProvider().getSession(storePath_.getWorkspace(), repo) ;
    return (Node) session.getItem(storePath_.getPath()) ;
  }
  
  public void doSelect(String selectField, String value) throws Exception {
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class) ;
    Node node = getNode() ;
    try {
      categoriesService.addCategory(node, value, storePath_.getRepository()) ;
      node.save() ;
      node.getSession().save() ;
      updateGrid(categoriesService.getCategories(node, storePath_.getRepository())) ;
      setRenderSibbling(UICategoriesAddedList.class) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UICategoriesAddedList> {
    public void execute(Event<UICategoriesAddedList> event) throws Exception {
      UICategoriesAddedList uiAddedList = event.getSource() ;
      Node node = uiAddedList.getNode() ;      
      UICategoryManager uiManager = uiAddedList.getParent() ;
      UIApplication uiApp = uiAddedList.getAncestorOfType(UIApplication.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ContentStorePath storePath = uiAddedList.getStorePath() ;
      CategoriesService categoriesService = 
        uiAddedList.getApplicationComponent(CategoriesService.class) ;
      try {
        categoriesService.removeCategory(node, nodePath, storePath.getRepository()) ;
        uiAddedList.updateGrid(categoriesService.getCategories(node, storePath.getRepository())) ;
      } catch(AccessDeniedException ace) {
        throw new MessageException(new ApplicationMessage("UICategoriesAddedList.msg.access-denied",
                                   null, ApplicationMessage.WARNING)) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
      }
      uiManager.setRenderedChild("UICategoriesAddedList") ;
    }
  }

}
