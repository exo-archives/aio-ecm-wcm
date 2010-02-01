package org.exoplatform.services.wcm.extensions.scheduler.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.extensions.security.SHAMessageDigester;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionData;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform MEA Author : haikel.thamri@exoplatform.com
 */
public class ExportContentJob implements Job {
    private static final Log log = ExoLogger.getLogger(ExportContentJob.class);

    private static final String MIX_TARGET_PATH = "mix:targetPath";

    private static final String MIX_TARGET_WORKSPACE = "mix:targetWorkspace";

    private static final String URL = "http://www.w3.org/2001/XMLSchema";

    private static final String START_TIME_PROPERTY = "publication:startPublishedDate".intern();

    private static String fromState = null;

    private static String toState = null;

    private static String localTempDir = null;

    private static String targetServerUrl = null;

    private static String targetKey = null;

    private static String predefinedPath = null;

    private static String workspace = null;

    private static String repository = null;

    private static String contentPath = null;

    public void execute(JobExecutionContext context) throws JobExecutionException {
	// TODO Auto-generated method stub
	Session session = null;
	try {

	    log.info("Start Execute ExportContentJob");
	    if (fromState == null) {

		JobDataMap jdatamap = context.getJobDetail().getJobDataMap();

		fromState = jdatamap.getString("fromState");
		toState = jdatamap.getString("toState");
		localTempDir = jdatamap.getString("localTempDir");
		targetServerUrl = jdatamap.getString("targetServerUrl");
		targetKey = jdatamap.getString("targetKey");
		predefinedPath = jdatamap.getString("predefinedPath");
		String[] pathTab = predefinedPath.split(":");
		repository = pathTab[0];
		workspace = pathTab[1];
		contentPath = pathTab[2];

		log.debug("Init parameters first time :");
		log.debug("\tFromState = " + fromState);
		log.debug("\tToState = " + toState);
		log.debug("\tLocalTempDir = " + localTempDir);
		log.debug("\tTargetServerUrl = " + targetServerUrl);

	    }
	    SessionProvider sessionProvider = SessionProvider.createSystemProvider();

	    ExoContainer container = ExoContainerContext.getCurrentContainer();
	    RepositoryService repositoryService_ = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
	    PublicationService publicationService_ = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
	    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
	    session = sessionProvider.getSession(workspace, manageableRepository);
	    QueryManager queryManager = session.getWorkspace().getQueryManager();
	    boolean isExported = false;
	    Query query = queryManager.createQuery("select * from nt:base where publication:currentState='" + fromState + "' and jcr:path like '"
		    + contentPath + "/%'", Query.SQL);
	    QueryResult queryResult = query.execute();
	    if (queryResult.getNodes().getSize() > 0) {

		File exportFolder = new File(localTempDir);
		if (!exportFolder.exists())
		    exportFolder.mkdirs();
		File file = new File(localTempDir + File.separatorChar + "contents.xml");
		ByteArrayOutputStream bos = null;
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		FileOutputStream output = new FileOutputStream(file);
		XMLStreamWriter xmlsw = outputFactory.createXMLStreamWriter(output, "UTF-8");
		xmlsw.writeStartDocument("UTF-8", "1.0");
		xmlsw.writeStartElement("xs", "contents", URL);
		xmlsw.writeNamespace("xs", URL);
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
		ValueFactory valueFactory = session.getValueFactory();
		Date nodeDate = null;
		Date now = null;
		for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
		    Node node_ = iter.nextNode();
		    if (node_.hasProperty(START_TIME_PROPERTY)) {
			now = Calendar.getInstance().getTime();
			nodeDate = node_.getProperty(START_TIME_PROPERTY).getDate().getTime();
		    }
		    if (nodeDate == null || now.compareTo(nodeDate) >= 0) {
			if (node_.canAddMixin(MIX_TARGET_PATH))
			    node_.addMixin(MIX_TARGET_PATH);
			node_.setProperty(MIX_TARGET_PATH, node_.getPath());

			if (node_.canAddMixin(MIX_TARGET_WORKSPACE))
			    node_.addMixin(MIX_TARGET_WORKSPACE);
			node_.setProperty(MIX_TARGET_WORKSPACE, workspace);
			node_.setProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP, valueFactory.createValue(""));
			Map<String, VersionData> revisionsMap = getRevisionData(node_);
			node_.setProperty(StageAndVersionPublicationConstant.CURRENT_STATE, PublicationDefaultStates.PUBLISHED);

			VersionData versionData = revisionsMap.get(node_.getUUID());
			if (versionData != null) {
			    versionData.setAuthor(node_.getSession().getUserID());
			    versionData.setState(PublicationDefaultStates.PUBLISHED);
			} else {
			    versionData = new VersionData(node_.getUUID(), PublicationDefaultStates.PUBLISHED, node_.getSession().getUserID());
			}
			revisionsMap.put(node_.getUUID(), versionData);
			addRevisionData(node_, revisionsMap.values());
			session.save();
			bos = new ByteArrayOutputStream();
			session.exportSystemView(node_.getPath(), bos, false, false);
			if (!isExported)
			    isExported = true;
			xmlsw.writeStartElement("xs", "content", URL);
			xmlsw.writeCData(bos.toString());
			xmlsw.writeEndElement();
		    }
		}

		if (bos != null) {
		    bos.close();
		}
		xmlsw.writeEndElement();
		xmlsw.flush();
		output.close();
		xmlsw.close();
		if (isExported) {
		    // connect
		    URI uri = new URI(targetServerUrl + "/rest/copyfile/copy/");
		    URL url = uri.toURL();
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		    // initialize the connection
		    connection.setDoOutput(true);
		    connection.setDoInput(true);
		    connection.setRequestMethod("POST");
		    connection.setUseCaches(false);
		    connection.setRequestProperty("Content-type", "text/plain");
		    connection.setRequestProperty("Connection", "Keep-Alive");

		    OutputStream out = connection.getOutputStream();
		    BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
		    char[] buf = new char[1024];
		    int numRead = 0;
		    Date date = new Date();
		    Timestamp time = new Timestamp(date.getTime());
		    String[] tab = targetKey.split(":");
		    String superpassword = tab[1];
		    String hashCode = SHAMessageDigester.getHash(time.toString() + ":" + superpassword);
		    StringBuffer param = new StringBuffer();
		    param.append("timestamp=" + time.toString() + "&&hashcode=" + hashCode + "&&contentsfile=");
		    while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			param.append(readData);
		    }
		    reader.close();
		    out.write(param.toString().getBytes());
		    out.flush();
		    connection.connect();
		    BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    out.close();
		    String string = null;
		    while ((string = inStream.readLine()) != null) {
			log.info("Server: " + string);
		    }

		    connection.disconnect();
		}
	    } else {
		log.info("there is no content to be exported");
	    }

	    log.info("End Execute ExportContentJob");
	} catch (Exception ex) {
	    ex.printStackTrace();
	    log.error("error when exporting content" + ex.getMessage());
	} finally {
	    session.logout();
	}

    }

    /**
     * Adds the revision data.
     * 
     * @param node
     *            the node
     * @param list
     *            the list
     * 
     * @throws Exception
     *             the exception
     */
    private void addRevisionData(Node node, Collection<VersionData> list) throws Exception {
	List<Value> valueList = new ArrayList<Value>();
	ValueFactory factory = node.getSession().getValueFactory();
	for (VersionData versionData : list) {
	    valueList.add(factory.createValue(versionData.toStringValue()));
	}
	node.setProperty(AuthoringPublicationConstant.REVISION_DATA_PROP, valueList.toArray(new Value[] {}));
    }

    /**
     * Gets the revision data.
     * 
     * @param node
     *            the node
     * 
     * @return the revision data
     * 
     * @throws Exception
     *             the exception
     */
    private Map<String, VersionData> getRevisionData(Node node) throws Exception {
	Map<String, VersionData> map = new HashMap<String, VersionData>();
	try {
	    for (Value v : node.getProperty(AuthoringPublicationConstant.REVISION_DATA_PROP).getValues()) {
		VersionData versionData = VersionData.toVersionData(v.getString());
		map.put(versionData.getUUID(), versionData);
	    }
	} catch (Exception e) {
	    return map;
	}
	return map;
    }

}
