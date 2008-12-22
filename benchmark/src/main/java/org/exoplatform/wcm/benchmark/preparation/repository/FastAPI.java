package org.exoplatform.wcm.benchmark.preparation.repository;


import java.util.Calendar;

import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
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

/**
 * Fast access to the JCR 
 */
public class FastAPI {
  
  private SearchManager searchIndex;
  
  private PlainChangesLogImpl chahgesLog; 
  
  protected Log log = ExoLogger.getLogger("repload.FastAPI");
  
  SessionImpl session     = null;
  WorkspaceDataContainerBase workspaceDataContainer;
  FileCleaner fileCleaner = null;
  WorkspaceStorageConnection con = null;
  Calendar date = null;
  
  protected static class DCPropertyQName {
    public static InternalQName dcElementSet;
    public static InternalQName dcTitle;
    public static InternalQName dcCreator;
    public static InternalQName dcSubject;
  }

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
  
  public void release() {
    try {
      commit();
      //this.session.save();
      //this.session.logout();
    }
    catch(Exception ignore) {
      log.error(ignore.getLocalizedMessage(), ignore);
    }
  }
  
  public void commit() {
    try {
      this.con.commit();
      this.con = getConnection();
      saveChangesLog(chahgesLog);
    } catch(Exception ignore) {
      log.error(ignore.getLocalizedMessage(), ignore);
    }
  }
  
  private void saveChangesLog( PlainChangesLog cLog) {
    searchIndex.onSaveItems(chahgesLog); // Indexer work
    chahgesLog = new PlainChangesLogImpl();
  }
  
  private WorkspaceStorageConnection getConnection() throws Exception {
    return workspaceDataContainer.openConnection();
  }
  
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

  private void addDcElementSet(WorkspaceStorageConnection con,
                               TransientNodeData nodeData,
                               String value,
                               String title) throws Exception {
    addDCProperty(con, nodeData, DCPropertyQName.dcTitle, title);
    addDCProperty(con, nodeData, DCPropertyQName.dcCreator, value);
    addDCProperty(con, nodeData, DCPropertyQName.dcSubject, value);
  }
  
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
