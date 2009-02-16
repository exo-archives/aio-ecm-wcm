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

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.json.JSONObject;
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
  
  public static final String FCK_RESOURCE_BUNDLE_FILE   = "locale.services.fckeditor.FCKConnector".intern();
  
  private ApplicationRegistryService applicationRegistryService;
  private GadgetRegistryService gadgetRegistryService;
  private String internalServerPath;
  
  public GadgetConnector(ExoContainer container, InitParams initParams) {
    applicationRegistryService = (ApplicationRegistryService)container.getComponentInstanceOfType(ApplicationRegistryService.class);
    gadgetRegistryService = (GadgetRegistryService) container.getComponentInstanceOfType(GadgetRegistryService.class) ;
    readServerConfig(initParams);
  }
  
  private void readServerConfig(InitParams initParams) {
    PropertiesParam propertiesParam = initParams.getPropertiesParam("server.config");
    String scheme = propertiesParam.getProperty("scheme");
    String hostName = propertiesParam.getProperty("hostName");
    String port = propertiesParam.getProperty("port");
    StringBuilder builder = new StringBuilder();
    builder.append(scheme).append("://").append(hostName).append(":").append(port);
    internalServerPath = builder.toString();
  }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("currentFolder") String currentFolder, @QueryParam("currentFolder") String language) throws Exception {
    Response response = buildXMLResponse(currentFolder, language);
    if (response == null)
      return Response.Builder.ok().build();
    else
      return response;
  }
  
  public Response buildXMLResponse(String currentFolder, String language) throws Exception {
    List<ApplicationCategory> applicationCategories = getGadgetCategories();
    Element rootElement = createRootElement(currentFolder, applicationCategories, language);
    Document document = rootElement.getOwnerDocument();
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }
  
  private Element createRootElement(String currentFolder, List<ApplicationCategory> applicationCategories, String language) throws Exception {
    Document document = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.newDocument();
    if (applicationCategories.isEmpty()) {
      Locale locale = null;
      if (language == null) {
        locale = Locale.ENGLISH;
      } else {
        locale = new Locale(language);
      }
      ResourceBundle resourceBundle = ResourceBundle.getBundle(FCK_RESOURCE_BUNDLE_FILE, locale);
      Element rootElement = document.createElement("Message");
      document.appendChild(rootElement);
      rootElement.setAttribute("number", "555");
      rootElement.setAttribute("text", resourceBundle.getString("fckeditor.no-gadget"));
      rootElement.setAttribute("type", "Error");
      return rootElement;
    } else {
      Element rootElement = document.createElement("Connector");
      document.appendChild(rootElement);
      rootElement.setAttribute("resourceType", "Gadget");    
      Element currentFolderElement = document.createElement("CurrentFolder");
      if (currentFolder == null || currentFolder.equals("/")){
        currentFolderElement.setAttribute("name", applicationCategories.get(0).getName());
        Element foldersElement = createFolderElement(document, applicationCategories);
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
  }
  
  private Element createFolderElement(Document document, List<ApplicationCategory> applicationCategories) throws Exception {
    Element folders = document.createElement("Folders");
    for (ApplicationCategory applicationCategory : applicationCategories) {
      Element folder = document.createElement("Folder");
      folder.setAttribute("name", applicationCategory.getDisplayName());
      folders.appendChild(folder);  
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
      
      String fullurl = "";
      if (gadget.isLocal()) {
        fullurl = internalServerPath + "/rest/" + gadget.getUrl();
      } else {
        fullurl = gadget.getUrl();
      }
      file.setAttribute("url", fullurl);
      
      String data = "{\"context\":{\"country\":\"US\",\"language\":\"en\"},\"gadgets\":[{\"moduleId\":0,\"url\":\"" + fullurl + "\",\"prefs\":[]}]}";
      URL url = new URL(internalServerPath + "/eXoGadgetServer/gadgets/metadata");
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();
      String strMetadata = IOUtils.toString(conn.getInputStream(), "UTF-8");
      wr.close();
      JSONObject metadata = new JSONObject(strMetadata.toString());
      file.setAttribute("metadata", metadata.toString());
      files.appendChild(file);
    }
    return files;
  }
  
  private List<ApplicationCategory> getGadgetCategories() throws Exception {
    List<ApplicationCategory> applicationCategories = applicationRegistryService.getApplicationCategories();
    List<ApplicationCategory> gadgetCategories = new ArrayList<ApplicationCategory>();
    for (ApplicationCategory applicationCategory : applicationCategories) {
      if (!applicationRegistryService.getApplications(applicationCategory, org.exoplatform.web.application.Application.EXO_GAGGET_TYPE).isEmpty()) {
        gadgetCategories.add(applicationCategory);
      }
    }
    return gadgetCategories;
  }
}