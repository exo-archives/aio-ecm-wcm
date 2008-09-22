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
package org.exoplatform.services.wcm.core;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com May 28, 2008
 */
public abstract class BaseWebSchemaHandler extends BaseComponentPlugin implements WebSchemaHandler {

  /** The EX o_ ownable. */
  protected final String EXO_OWNABLE = "exo:owneable".intern();
  
  /** The N t_ folder. */
  protected final String NT_FOLDER = "nt:folder".intern();
  
  /** The N t_ unstructured. */
  protected final String NT_UNSTRUCTURED = "nt:unstructured".intern();
  
  /** The N t_ file. */
  protected final String NT_FILE = "nt:file".intern() ; 

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.WebSchemaHandler#matchHandler(javax.jcr.Node)
   */
  public boolean matchHandler(Node node) throws Exception {
    String handlerNodeType = getHandlerNodeType();    
    if (node.getPrimaryNodeType().isNodeType(handlerNodeType) 
        && node.getParent().isNodeType(getParentNodeType())) {
      return true;
    }
    return false;    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.WebSchemaHandler#onCreateNode(javax.jcr.Node)
   */
  public abstract void onCreateNode(Node node) throws Exception;
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.WebSchemaHandler#onModifyNode(javax.jcr.Node)
   */
  public void onModifyNode(Node node) throws Exception { }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.WebSchemaHandler#onRemoveNode(javax.jcr.Node)
   */
  public void onRemoveNode(Node node) throws Exception { }
  
  /**
   * Gets the handler node type.
   * 
   * @return the handler node type
   */
  protected abstract String getHandlerNodeType() ;
  
  /**
   * Gets the parent node type.
   * 
   * @return the parent node type
   */
  protected abstract String getParentNodeType();

  /**
   * Adds the mixin.
   * 
   * @param node the node
   * @param mixin the mixin
   * @throws Exception the exception
   */
  protected void addMixin(Node node, String mixin) throws Exception {
    if (!node.isNodeType(mixin)) node.addMixin(mixin);
  }
  
  protected <T> T getService(Class<T> clazz) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  } 
}
