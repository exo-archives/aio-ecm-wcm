/*
 * Copyright (C) 2003-2007 eXo Platform SEA.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.connector.BaseConnector;
import org.exoplatform.wcm.connector.handler.FCKFileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SEA
 * Author : Do Dang Thang
 * thang.do@exoplatform.com
 * Sep 7, 2009
 */
@URITemplate("/wcmDriver/")
public class DriverConnector extends BaseConnector implements ResourceContainer {
  
  /** The Constant FILE_TYPE_WEBCONTENT. */
  public static final String FILE_TYPE_WEBCONTENT                        = "Web Contents"; 
  
  /** The Constant FILE_TYPE_DMSDOC. */
  public static final String FILE_TYPE_DMSDOC                        = "DMS Documents"; 
  
  /** The Constant FILE_TYPE_MEDIAS. */
  public static final String FILE_TYPE_MEDIAS                       = "Medias"; 
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(DriverConnector.class);

  /** The limit. */
  private int limit;
  
  /**
   * Instantiates a new driver connector.
   * 
   * @param container the container
   */
  public DriverConnector(ExoContainer container, InitParams params) {
    super(container);
    limit = Integer.parseInt(params.getValueParam("upload.limit.size").getValue());
  }
	
  /**
   * Gets the drivers.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userId the user id
   * 
   * @return the drivers
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getDrivers/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getDrivers(
  		@QueryParam("repositoryName") String repositoryName,
  		@QueryParam("workspaceName") String workspaceName,
  		@QueryParam("userId") String userId)
  		throws Exception {
  	List<DriveData> listDriver = getDriversByUserId(repositoryName, userId);
  	
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    
    Element rootElement = document.createElement("Connector");
    document.appendChild(rootElement);

    rootElement.appendChild(appendDrivers(document, generalDrivers(listDriver), "General Drives"));
    rootElement.appendChild(appendDrivers(document, groupDrivers(listDriver, userId), "Group Drives"));
    rootElement.appendChild(appendDrivers(document, personalDrivers(listDriver), "Personal Drives"));
    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Gets the folders and files.
   * 
   * @param driverName the driver name
   * @param currentFolder the current folder
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param filterBy the filter by
   * @param userId the user id
   * 
   * @return the folders and files
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(
  		@QueryParam("driverName") String driverName,
  		@QueryParam("currentFolder") String currentFolder,
  		@QueryParam("repositoryName") String repositoryName,
  		@QueryParam("workspaceName") String workspaceName,
  		@QueryParam("filterBy") String filterBy,
  		@QueryParam("userId") String userId)
  		throws Exception {
    try {
      SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer()
      	.getComponentInstanceOfType(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = sessionProvider.getSession(workspaceName, manageableRepository);
      ManageDriveService manageDriveService = (ManageDriveService)ExoContainerContext.getCurrentContainer()
      	.getComponentInstanceOfType(ManageDriveService.class);
      
      Node node = (Node)session.getItem(
      		manageDriveService.getDriveByName(driverName, repositoryName).getHomePath()
          + ((currentFolder != null && currentFolder.length() != 0) ? "/" : "")
          + currentFolder);
      
      return buildXMLResponseForChildren(node, null, repositoryName, filterBy);

    } catch (Exception e) {
      log.error("Error when perform getFoldersAndFiles: ", e.fillInStackTrace());
    }    
    return Response.Builder.ok().build();
  }
	

  /**
   * Upload file.
   * 
   * @param inputStream the input stream
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param uploadId the upload id
   * @param language the language
   * @param contentType the content type
   * @param contentLength the content length
   * @param currentPortal the current portal
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/uploadFile/upload/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)
  public Response uploadFile(InputStream inputStream, @QueryParam("repositoryName")
      String repositoryName, @QueryParam("workspaceName")
      String workspaceName, @QueryParam("currentFolder")
      String currentFolder, @QueryParam("currentPortal")
      String currentPortal,@QueryParam("jcrPath")
      String jcrPath, @QueryParam("uploadId")
      String uploadId, @QueryParam("language")
      String language, @HeaderParam("content-type")
      String contentType, @HeaderParam("content-length")
      String contentLength) throws Exception {
    return createUploadFileResponse(inputStream, repositoryName, workspaceName, currentFolder,
        currentPortal, jcrPath, uploadId, language, contentType, contentLength, limit);
  }

  /**
   * Process upload.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param action the action
   * @param language the language
   * @param fileName the file name
   * @param uploadId the upload id
   * @param currentPortal the current portal
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/uploadFile/control/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response processUpload(@QueryParam("repositoryName")
      String repositoryName, @QueryParam("workspaceName")
      String workspaceName, @QueryParam("currentFolder")
      String currentFolder, @QueryParam("currentPortal")
      String currentPortal, @QueryParam("jcrPath")
      String jcrPath, @QueryParam("action")
      String action, @QueryParam("language")
      String language, @QueryParam("fileName")
      String fileName, @QueryParam("uploadId")
      String uploadId) throws Exception {
    try {
      return createProcessUploadResponse(repositoryName, workspaceName, currentFolder,currentPortal ,jcrPath,
          action, language, fileName, uploadId);  
    } catch (Exception e) {
      log.error("Error when perform processUpload: ", e.fillInStackTrace());
    }
    return Response.Builder.ok().build();
  }
  
  /**
   * Gets the drivers by user id.
   * 
   * @param repoName the repo name
   * @param userId the user id
   * 
   * @return the drivers by user id
   * 
   * @throws Exception the exception
   */
  private List<DriveData> getDriversByUserId(String repoName, String userId) throws Exception {    
    ManageDriveService driveService = (ManageDriveService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ManageDriveService.class);      
    List<DriveData> driveList = new ArrayList<DriveData>();    
    List<String> userRoles = getMemberships(userId);    
    List<DriveData> allDrives = driveService.getAllDrives(repoName);
    Set<DriveData> temp = new HashSet<DriveData>();
    if (userId != null) {
      // We will improve ManageDrive service to allow getAllDriveByUser
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        boolean flag = false;
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("${userId}")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (flag)
            continue;
          for (String rolse : userRoles) {
            if (driveData.hasPermission(allPermission, rolse)) {
              temp.add(driveData);
              break;
            }
          }
        }
      }
    } else {
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            break;
          }
        }
      }
    }
    
    for(Iterator<DriveData> iterator = temp.iterator();iterator.hasNext();) {
      driveList.add(iterator.next());
    }
    Collections.sort(driveList);
    return driveList; 
  }
  
	/**
	 * Append drivers.
	 * 
	 * @param document the document
	 * @param driversList the drivers list
	 * @param groupName the group name
	 * 
	 * @return the element
	 */
	private Element appendDrivers(Document document, List<DriveData> driversList, String groupName) {
	  Element folders = document.createElement("Folders");
	  folders.setAttribute("name", groupName);
    for (DriveData driver : driversList) {
      Element folder = document.createElement("Folder");
      folder.setAttribute("name", driver.getName());
      folder.setAttribute("driverPath", driver.getHomePath());
      folders.appendChild(folder);  
    }
	  return folders;
  }
  
  /**
   * Personal drivers.
   * 
   * @param driveList the drive list
   * 
   * @return the list< drive data>
   */
  private List<DriveData> personalDrivers(List<DriveData> driveList) {
    List<DriveData> personalDrivers = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(userPath + "/${userId}/")) {
        personalDrivers.add(drive);
      }
    }
    Collections.sort(personalDrivers);
    return personalDrivers;
  }
  
  /**
   * Group drivers.
   * 
   * @param driverList the driver list
   * @param userId the user id
   * 
   * @return the list< drive data>
   * 
   * @throws Exception the exception
   */
  private List<DriveData> groupDrivers(List<DriveData> driverList, String userId) throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    List<DriveData> groupDrivers = new ArrayList<DriveData>();
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    List<String> groups = getGroups(userId);
    for(DriveData drive : driverList) {
      if(drive.getHomePath().startsWith(groupPath)) {
        for(String group : groups) {
          if(drive.getHomePath().equals(groupPath + group)) {
            groupDrivers.add(drive);
            break;
          }
        }
      } 
    }
    Collections.sort(groupDrivers);
    return groupDrivers;
  }
  
  /**
   * General drivers.
   * 
   * @param driverList the driver list
   * 
   * @return the list< drive data>
   * 
   * @throws Exception the exception
   */
  private List<DriveData> generalDrivers(List<DriveData> driverList) throws Exception {
    List<DriveData> generalDrivers = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    for(DriveData drive : driverList) {
      if((!drive.getHomePath().startsWith(userPath) && !drive.getHomePath().startsWith(groupPath)) 
          || drive.getHomePath().equals(userPath)) {
        generalDrivers.add(drive);
      }
    }
    return generalDrivers;
  }

  /**
   * Gets the memberships.
   * 
   * @param userId the user id
   * 
   * @return the memberships
   * 
   * @throws Exception the exception
   */
  private static List<String> getMemberships(String userId) throws Exception {
  	OrganizationService oservice = (OrganizationService)
  		ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    List<String> userMemberships = new ArrayList<String> ();
    userMemberships.add(userId);
    Collection<?> memberships = oservice.getMembershipHandler().findMembershipsByUser(userId);
    if(memberships == null || memberships.size() < 0) return userMemberships;
    Object[] objects = memberships.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Membership membership = (Membership)objects[i];
      String role = membership.getMembershipType() + ":" + membership.getGroupId();
      userMemberships.add(role);     
    }
    return userMemberships;
  }

  /**
   * Gets the groups.
   * 
   * @param userId the user id
   * 
   * @return the groups
   * 
   * @throws Exception the exception
   */
  private static List<String> getGroups(String userId) throws Exception {
    OrganizationService oservice = (OrganizationService) 
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    List<String> groupList = new ArrayList<String> ();
    Collection<?> groups = oservice.getGroupHandler().findGroupsOfUser(userId);
    Object[] objects = groups.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Group group = (Group)objects[i];
      String groupPath = null;
      if(group.getParentId() == null || group.getParentId().length() == 0) groupPath = "/" + group.getGroupName(); 
      else groupPath = group.getParentId() + "/" + group.getGroupName(); 
      groupList.add(groupPath);
    }
    return groupList;
  }

  /**
   * Builds the xml response for children.
   * 
   * @param node the node
   * @param command the command
   * @param repositoryName the repository name
   * @param filterBy the filter by
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  private Response buildXMLResponseForChildren(Node node, String command, String repositoryName, String filterBy) throws Exception {
    Element rootElement = FCKUtils.createRootElement(command, node, folderHandler.getFolderType(node));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    
    for (NodeIterator iterator = node.getNodes(); iterator.hasNext();) {
      Node child = iterator.nextNode();
      
      if (child.isNodeType(FCKUtils.EXO_HIDDENABLE))
        continue;

      if (!NodetypeConstant.EXO_WEBCONTENT.equals(child.getPrimaryNodeType().getName())
      		&& child.getPrimaryNodeType().isNodeType(FCKUtils.NT_UNSTRUCTURED)
      		|| child.getPrimaryNodeType().isNodeType(FCKUtils.NT_FOLDER)) {
        Element folder = folderHandler.createFolderElement(document, child, child.getPrimaryNodeType().getName());
        folders.appendChild(folder);
      } else {
      	String fileType = null;
      	if (child.getPrimaryNodeType().isNodeType(NodetypeConstant.EXO_WEBCONTENT) && FILE_TYPE_WEBCONTENT.equals(filterBy)) {
      		fileType = FILE_TYPE_WEBCONTENT;
      	} else if (isDMSDocument(child, repositoryName)&& FILE_TYPE_DMSDOC.equals(filterBy)) {
      		fileType = FILE_TYPE_DMSDOC;
      	} else if (FILE_TYPE_MEDIAS.equals(filterBy)){
      		fileType = FILE_TYPE_MEDIAS;
      	} 
      	
      	if (fileType != null) {
      		Element file = FCKFileHandler.createFileElement(document, child, fileType);
      		files.appendChild(file);
      	}
      }
    }
    
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return getResponse(document);
  }
  
  /**
   * Checks if is dMS document.
   * 
   * @param node the node
   * @param repositoryName the repository name
   * 
   * @return true, if is dMS document
   * 
   * @throws Exception the exception
   */
  private boolean isDMSDocument(Node node, String repositoryName) throws Exception {
  	TemplateService templateService = (TemplateService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(TemplateService.class);
  	List<String> dmsDocumentList = templateService.getDocumentTemplates(repositoryName);
  	dmsDocumentList.remove(NodetypeConstant.EXO_WEBCONTENT);
  	for (String documentType : dmsDocumentList) {
	    if (node.getPrimaryNodeType().isNodeType(documentType)) {
	    	return true;
	    }
    }
  	return false;
  }
  
	/* (non-Javadoc)
	 * @see org.exoplatform.wcm.connector.BaseConnector#getContentStorageType()
	 */
	@Override
	protected String getContentStorageType() throws Exception {
    return null;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.wcm.connector.BaseConnector#getRootContentStorage(javax.jcr.Node)
	 */
	@Override
	protected Node getRootContentStorage(Node node) throws Exception {
    try {
      PortalFolderSchemaHandler folderSchemaHandler = webSchemaConfigService
      .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      return folderSchemaHandler.getImagesFolder(node);
    } catch (Exception e) {
      WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService
      .getWebSchemaHandlerByType(WebContentSchemaHandler.class);
      return webContentSchemaHandler.getImagesFolders(node);
    }
	}
}
