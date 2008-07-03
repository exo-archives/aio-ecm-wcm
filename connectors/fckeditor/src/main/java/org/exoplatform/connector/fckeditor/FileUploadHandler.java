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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

  public Document upload(String uploadId, InputStream data, String contentType, double contentLength) throws Exception {
    uploadService.createUploadResource(uploadId, data, contentType, contentLength);        
    Document doc = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc = builder.newDocument();
    Element rootElement = doc.createElement("FileUpload");
    rootElement.setAttribute("uploadID", uploadId);    
    doc.appendChild(rootElement);
    return doc;
  }

  public Document refreshProgress(String uploadId) throws Exception {    
    double percent = 100;
    UploadResource resource = uploadService.getUploadResource(uploadId);
    if (resource.getStatus() == UploadResource.UPLOADING_STATUS) {
      percent = (resource.getUploadedSize() * 100) / resource.getEstimatedSize();      
    }    
    Document doc = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc = builder.newDocument();
    Element rootElement = doc.createElement("FileUpload");
    rootElement.setAttribute("uploadID", uploadId);
    rootElement.setAttribute("uploadFile", resource.getFileName());
    rootElement.setAttribute("percent", Double.toString(percent));
    doc.appendChild(rootElement);
    return doc;
  }

  public Document abort(String uploadId) throws Exception { 
    uploadService.removeUpload(uploadId);
    return null;
  }

  public Document delete(String uploadId) throws Exception { 
    uploadService.removeUpload(uploadId);
    return null;
  }

  public void saveAsNTFile(String uploadId, Node parent) throws Exception { 
    /*UploadResource resource = uploadService.getUploadResource(uploadId) ;    
    resource.getStoreLocation()
    InputStream inputStream = new BufferedInputStream(new FileInputStream(resource.getStoreLocation()));*/    
  }
}
