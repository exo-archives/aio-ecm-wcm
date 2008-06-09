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
package org.exoplatform.connector.fckeditor;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 9, 2008  
 */
public class ImagesXMLOutputBuilder extends FCKConnectorXMLOutputBuilder{

  public ImagesXMLOutputBuilder(ExoContainer container) {
    super(container);
  }

  public Document buildFilesXMLOutput(String repository, String workspace, String currentFolder)
  throws Exception {
    return null;
  }

  public Document buildFoldersAndFilesXMLOutput(String repository, String workspace,
      String currentFolder) throws Exception {
    return null;
  }

  protected String createFileLink(Node node) throws Exception {

    return null;
  }

  protected String getFileType(Node node) throws Exception {

    return null;
  }  

}
