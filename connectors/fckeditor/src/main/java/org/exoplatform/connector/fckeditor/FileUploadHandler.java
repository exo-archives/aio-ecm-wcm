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

import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
public class FileUploadHandler {
  public final static String UPLOAD_ACTION = "upload".intern();
  public final static String PROGRESS_ACTION = "progress".intern();
  public final static String ABORT_ACTION = "abort".intern();
  public final static String DELETE_ACTION = "delete".intern();
  public final static String SAVE_ACTION = "save".intern();
  
  private UploadService uploadService;
  public FileUploadHandler(UploadService uploadService) {
    this.uploadService = uploadService;
  }
  
  public void upload(String uploadId, String fileName, 
      InputStream data, double contentLength, String mimetype) throws Exception {
    //uploadService.createUploadResource(uploadId,fileName,data,contentLength,mimetype);    
  }
  
  public void refreshProgress(String uploadId) throws Exception { }
  
  public void abort(String uploadId) throws Exception {}
  
  public void delete(String uploadId) throws Exception {}
  
  public void saveAsNTFile(String uploadId,Node parent) { 
    //UploadResource resource = uploadService.getUploadResource(uploadId) ;    
  }
}
