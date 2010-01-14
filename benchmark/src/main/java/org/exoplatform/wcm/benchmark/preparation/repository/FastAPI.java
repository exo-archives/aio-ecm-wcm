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
package org.exoplatform.wcm.benchmark.preparation.repository;

import java.util.Calendar;
import javax.jcr.PropertyType;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.query.SearchManager;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Fast access to the JCR.
 */
public class FastAPI {
  
  /** The search index. */
  private SearchManager searchIndex;
  
  /** The chahges log. */
  private PlainChangesLogImpl chahgesLog; 
  
  /** The log. */
  protected Log log = ExoLogger.getLogger("repload.FastAPI");
  
  /** The session. */
  SessionImpl session     = null;
  
  /** The workspace data container. */
  WorkspaceDataContainerBase workspaceDataContainer;
  
  /** The file cleaner. */
  FileCleaner fileCleaner = null;
  
  /** The con. */
  WorkspaceStorageConnection con = null;
  
  /** The date. */
  Calendar date = null;
  
  /**
   * The Class DCPropertyQName.
   */
  protected static class DCPropertyQName {
    
    /** The dc element set. */
    public static InternalQName dcElementSet;
    
    /** The dc title. */
    public static InternalQName dcTitle;
    
    /** The dc creator. */
    public static InternalQName dcCreator;
    
    /** The dc subject. */
    public static InternalQName dcSubject;
  }

  /**
   * Instantiates a new fast api.
   * 
   * @param session the session
   */
  @SuppressWarnings("deprecation")
  public FastAPI(SessionImpl session) {

    try {
      // Cache the Session
      this.session = session;
      
      workspaceDataContainer = (WorkspaceDataContainerBase)
      (session.getContainer().getComponentInstanceOfType(
         WorkspaceDataContainerBase.class));
      
      // Permissions are checked when invoking SessionImpl.getContainer(),
      // but this works as the System Session is used.
      WorkspaceFileCleanerHolder holder = (WorkspaceFileCleanerHolder)
        session.getContainer().
        getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);
      
      // Retrieve the File Cleaner
      fileCleaner = holder.getFileCleaner();
      
      // Retrieve a storage connection
      con = getConnection();
      
      chahgesLog = new PlainChangesLogImpl();
      
      searchIndex = (SearchManager)session.getContainer().getComponentInstanceOfType(SearchManager.class);
      
      // Retrieve the current date
      date = Calendar.getInstance();
      
      // Retrieve a location factory
      LocationFactory locationFactory = session.getLocationFactory();
      
      DCPropertyQName.dcElementSet = locationFactory.
        parseJCRName("dc:elementSet").getInternalName();
      DCPropertyQName.dcTitle = locationFactory.
        parseJCRName("dc:title").getInternalName();
      DCPropertyQName.dcCreator = locationFactory.
        parseJCRName("dc:creator").getInternalName();
      DCPropertyQName.dcSubject =  locationFactory.
        parseJCRName("dc:subject").getInternalName();
    }
    catch(Exception e) {
      log.error("Exception in the constructor", e);
    }
  }
  
  /**
   * Release.
   */
  public void release() {
    try {
      commit();
    }
    catch(Exception ignore) {
      log.error(ignore.getLocalizedMessage(), ignore);
    }
  }
  
  /**
   * Commit.
   */
  public void commit() {
    try {
      this.con.commit();
      this.con = getConnection();
      saveChangesLog(chahgesLog);
    } catch(Exception ignore) {
      log.error(ignore.getLocalizedMessage(), ignore);
    }
  }
  
  /**
   * Save changes log.
   * 
   * @param cLog the c log
   */
  private void saveChangesLog( PlainChangesLog cLog) {
    searchIndex.onSaveItems(chahgesLog); // Indexer work
    chahgesLog = new PlainChangesLogImpl();
  }
  
  /**
   * Gets the connection.
   * 
   * @return the connection
   * 
   * @throws Exception the exception
   */
  private WorkspaceStorageConnection getConnection() throws Exception {
    return workspaceDataContainer.openConnection();
  }
  
  /**
   * Adds the node.
   * 
   * @param name the name
   * @param orderNum the order num
   * @param parentNode the parent node
   * 
   * @return the transient node data
   * 
   * @throws Exception the exception
   */
  public TransientNodeData addNode(
    String name,
    int orderNum,
    NodeImpl parentNode) throws Exception {
    
    TransientNodeData nodeData = createNodeData_nt_folder(name, orderNum, parentNode);
    con.add(nodeData);
    chahgesLog.add(ItemState.createAddedState(nodeData));

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FOLDER));
    con.add(primaryTypeData);
    chahgesLog.add(ItemState.createAddedState(primaryTypeData));

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), IdGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getIdentifier(), false);
    createdData.setValue(new TransientValueData(this.date));
    con.add(createdData);
    chahgesLog.add(ItemState.createAddedState(createdData));

    return nodeData;
  }
  
  /**
   * Creates the node data_nt_folder.
   * 
   * @param name the name
   * @param orderNum the order num
   * @param parentNode the parent node
   * 
   * @return the transient node data
   */
  public TransientNodeData createNodeData_nt_folder(
    String name,
    int orderNum,
    NodeImpl parentNode) {

    InternalQName[] mixinTypeNames = new InternalQName[0];

    InternalQName iQName = new InternalQName(Constants.NS_DEFAULT_URI, name);

    QPath path = QPath.makeChildPath(parentNode.getInternalPath(), iQName);

    AccessControlList acl = new AccessControlList();

    String uuid = IdGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, Constants.NT_FOLDER,
        mixinTypeNames, orderNum, parentNode.getInternalIdentifier(), acl);

    return nodeData;
  }
  
  /**
   * Adds the node.
   * 
   * @param name the name
   * @param orderNum the order num
   * @param parentNode the parent node
   * 
   * @return the transient node data
   * 
   * @throws Exception the exception
   */
  public TransientNodeData addNode(
      String name,
      int orderNum,
      TransientNodeData parentNode) throws Exception {
    TransientNodeData nodeData = createNodeData(new InternalQName(Constants.NS_DEFAULT_URI, name),
        orderNum, parentNode, Constants.NT_FOLDER, null);
    con.add(nodeData);
    chahgesLog.add(ItemState.createAddedState(nodeData));

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FOLDER));
    con.add(primaryTypeData);
    chahgesLog.add(ItemState.createAddedState(primaryTypeData));

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), IdGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getIdentifier(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);
    chahgesLog.add(ItemState.createAddedState(createdData));

    return nodeData;
  }
  
  /**
   * Creates the node data.
   * 
   * @param iQName the i q name
   * @param orderNum the order num
   * @param parentNode the parent node
   * @param primaryType the primary type
   * @param mixinName the mixin name
   * 
   * @return the transient node data
   * 
   * @throws Exception the exception
   */
  public TransientNodeData createNodeData(InternalQName iQName, int orderNum,
      TransientNodeData parentNode, InternalQName primaryType, InternalQName mixinName)
      throws Exception {

    InternalQName[] mixinTypeNames = null;

    if (mixinName == null)
      mixinTypeNames = new InternalQName[0];
    else {
      mixinTypeNames = new InternalQName[1];
      mixinTypeNames[0] = mixinName;
    }

    QPath path = QPath.makeChildPath(parentNode.getQPath(), iQName);

    AccessControlList acl = new AccessControlList();

    String uuid = IdGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, primaryType, mixinTypeNames,
        orderNum, parentNode.getIdentifier(), acl);

    return nodeData;
  }
  
  /**
   * Adds the node_file.
   * 
   * @param name the name
   * @param orderNum the order num
   * @param metadata the metadata
   * @param parentNode the parent node
   * @param fData the f data
   * @param mimeType the mime type
   * @param title the title
   * 
   * @throws Exception the exception
   */
  protected void addNode_file(
    String name,
    int orderNum,
    String metadata,
    TransientNodeData parentNode,
    TransientValueData fData,
                              String mimeType,
                              String title) throws Exception {

    TransientNodeData nodeData = createNodeData(new InternalQName(Constants.NS_DEFAULT_URI, name),
        orderNum, parentNode, Constants.NT_FILE, null);
    con.add(nodeData);
    chahgesLog.add(ItemState.createAddedState(nodeData));

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FILE));
    con.add(primaryTypeData);
    chahgesLog.add(ItemState.createAddedState(primaryTypeData));

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), IdGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getIdentifier(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);
    chahgesLog.add(ItemState.createAddedState(createdData));

    TransientNodeData contentNode = createNodeData(Constants.JCR_CONTENT, 0, nodeData,
        Constants.NT_RESOURCE, DCPropertyQName.dcElementSet);
    con.add(contentNode);
    chahgesLog.add(ItemState.createAddedState(contentNode));

    TransientPropertyData mixinTypeData = new TransientPropertyData(QPath.makeChildPath(contentNode
        .getQPath(), Constants.JCR_MIXINTYPES), IdGenerator.generate(), -1, PropertyType.NAME,
        contentNode.getIdentifier(), true);
    mixinTypeData.setValue(new TransientValueData(DCPropertyQName.dcElementSet));
    con.add(mixinTypeData);
    chahgesLog.add(ItemState.createAddedState(mixinTypeData));
    
    TransientPropertyData primaryTypeContenNode = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1,
        PropertyType.NAME, contentNode.getIdentifier(), false);
    primaryTypeContenNode.setValue(new TransientValueData(Constants.NT_RESOURCE));
    con.add(primaryTypeContenNode);
    chahgesLog.add(ItemState.createAddedState(primaryTypeContenNode));

    TransientPropertyData uuidPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_UUID), IdGenerator.generate(), -1,
        PropertyType.STRING, contentNode.getIdentifier(), false);
    uuidPropertyData.setValue(new TransientValueData(IdGenerator.generate()));
    con.add(uuidPropertyData);
    chahgesLog.add(ItemState.createAddedState(uuidPropertyData));

    TransientPropertyData mimeTypePropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_MIMETYPE), IdGenerator.generate(), -1,
        PropertyType.STRING, contentNode.getIdentifier(), false);
    mimeTypePropertyData.setValue(new TransientValueData(mimeType));
    con.add(mimeTypePropertyData);
    chahgesLog.add(ItemState.createAddedState(mimeTypePropertyData));

    TransientPropertyData lastModifiedPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_LASTMODIFIED), IdGenerator.generate(), -1,
        PropertyType.DATE, contentNode.getIdentifier(), false);
    lastModifiedPropertyData.setValue(new TransientValueData(date));
    con.add(lastModifiedPropertyData);
    chahgesLog.add(ItemState.createAddedState(lastModifiedPropertyData));

    TransientPropertyData dataPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_DATA), IdGenerator.generate(), -1,
        PropertyType.BINARY, contentNode.getIdentifier(), false);
    dataPropertyData.setValue(fData);
    con.add(dataPropertyData);
    chahgesLog.add(ItemState.createAddedState(dataPropertyData));

    addDcElementSet(con, contentNode, metadata, title);
  }

  /**
   * Adds the dc element set.
   * 
   * @param con the con
   * @param nodeData the node data
   * @param value the value
   * @param title the title
   * 
   * @throws Exception the exception
   */
  private void addDcElementSet(WorkspaceStorageConnection con,
                               TransientNodeData nodeData,
                               String value,
                               String title) throws Exception {
    addDCProperty(con, nodeData, DCPropertyQName.dcTitle, title);
    addDCProperty(con, nodeData, DCPropertyQName.dcCreator, value);
    addDCProperty(con, nodeData, DCPropertyQName.dcSubject, value);
  }
  
  /**
   * Adds the dc property.
   * 
   * @param con the con
   * @param dcNode the dc node
   * @param propertyQName the property q name
   * @param propertyContent the property content
   * 
   * @throws Exception the exception
   */
  private void addDCProperty(WorkspaceStorageConnection con, TransientNodeData dcNode,
      InternalQName propertyQName, String propertyContent) throws Exception {

    TransientPropertyData dcPropertyData = new TransientPropertyData(QPath.makeChildPath(dcNode
        .getQPath(), propertyQName), IdGenerator.generate(), -1, PropertyType.STRING, dcNode
        .getIdentifier(), true);
    dcPropertyData.setValue(new TransientValueData(propertyContent));
    con.add(dcPropertyData);
    chahgesLog.add(ItemState.createAddedState(dcPropertyData));
  }
}
