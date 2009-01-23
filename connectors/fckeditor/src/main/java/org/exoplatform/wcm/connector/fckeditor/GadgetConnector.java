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
package org.exoplatform.wcm.connector.fckeditor;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Jan 21, 2009  
 */

@URITemplate("/wcmGadget/")
public class GadgetConnector implements ResourceContainer {
  
  private ApplicationRegistryService applicationRegistryService;
  private GadgetRegistryService gadgetRegistryService;
  
  public GadgetConnector(ExoContainer container) {
    applicationRegistryService = (ApplicationRegistryService)container.getComponentInstanceOfType(ApplicationRegistryService.class);
    gadgetRegistryService = (GadgetRegistryService) container.getComponentInstanceOfType(GadgetRegistryService.class) ;
  }
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("currentFolder") String currentFolder) throws Exception {
    Response response = buildXMLResponse(currentFolder);
    if (response == null)
      return Response.Builder.ok().build();
    else
      return response;
  }
  
  public Response buildXMLResponse(String currentFolder) throws Exception {
    Element rootElement = createRootElement(currentFolder);
    Document document = rootElement.getOwnerDocument();
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }
  
  private Element createRootElement(String currentFolder) throws Exception {
    Document document = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.newDocument();
    Element rootElement = document.createElement("Connector");
    document.appendChild(rootElement);
    rootElement.setAttribute("resourceType", "Gadget");    
    Element currentFolderElement = document.createElement("CurrentFolder");
    if (currentFolder == null || currentFolder.equals("/")){
      currentFolderElement.setAttribute("name", applicationRegistryService.getApplicationCategories().get(0).getName());
      Element foldersElement = createFolderElement(document);
      rootElement.appendChild(foldersElement);
    } else {
      ApplicationCategory applicationCategory = applicationRegistryService.getApplicationCategory(currentFolder.substring(1, currentFolder.length() - 1));
      currentFolderElement.setAttribute("name", applicationCategory.getDisplayName());
      Element filesElement = createFileElement(document, applicationCategory);
      rootElement.appendChild(filesElement);
    }
    rootElement.appendChild(currentFolderElement);
    return rootElement;
  }
  
  private Element createFolderElement(Document document) throws Exception {
    Element folders = document.createElement("Folders");
    List<ApplicationCategory> listApplicationCategory = applicationRegistryService.getApplicationCategories(); 
    for (ApplicationCategory applicationCategory : listApplicationCategory) {
      if (!applicationRegistryService.getApplications(applicationCategory, org.exoplatform.web.application.Application.EXO_GAGGET_TYPE).isEmpty()) {
        Element folder = document.createElement("Folder");
        folder.setAttribute("name", applicationCategory.getDisplayName());
        folders.appendChild(folder);  
      }
    }
    return folders;
  }
  
  private Element createFileElement(Document document, ApplicationCategory applicationCategory) throws Exception {
    Element files = document.createElement("Files");
    List<Application> listApplication = applicationRegistryService.getApplications(applicationCategory, org.exoplatform.web.application.Application.EXO_GAGGET_TYPE);
    for (Application application : listApplication) {
      Gadget gadget = gadgetRegistryService.getGadget(application.getApplicationName());
      Element file = document.createElement("File");
      file.setAttribute("name", gadget.getName());
      file.setAttribute("fileType", "nt_unstructured");
      file.setAttribute("size", "0");
      file.setAttribute("thumbnail", gadget.getThumbnail());
      file.setAttribute("description", gadget.getDescription());
      file.setAttribute("url", gadget.getUrl());
      file.setAttribute("local", String.valueOf(gadget.isLocal()));
      files.appendChild(file);
    }
    return files;
  }
}