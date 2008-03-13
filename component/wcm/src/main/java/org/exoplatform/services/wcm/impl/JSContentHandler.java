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

package org.exoplatform.services.wcm.impl;

import javax.jcr.Node;

import org.exoplatform.services.wcm.BaseWebContentHandler;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 10, 2008  
 */
public class JSContentHandler extends BaseWebContentHandler {

  protected String getFileType() { return "nt:file"; }

  protected String getFolderPathExpression() { return null; }

  protected String getFolderType() { return "exo:jsFolder"; }

  public String handle(Node file) throws Exception {
    if(!file.isNodeType("exo:jsFile")) {
      file.addMixin("exo:jsFile") ;      
    }
    return file.getPath() ;
  }

}
