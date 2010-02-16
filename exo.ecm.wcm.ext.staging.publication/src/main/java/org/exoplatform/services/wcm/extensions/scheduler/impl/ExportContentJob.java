package org.exoplatform.services.wcm.extensions.scheduler.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.extensions.security.SHAMessageDigester;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform MEA Author : haikel.thamri@exoplatform.com
 */
public class ExportContentJob implements Job {
  private static final Log    log                  = ExoLogger.getLogger(ExportContentJob.class);

  private static final String MIX_TARGET_PATH      = "mix:targetPath".intern();

  private static final String MIX_TARGET_WORKSPACE = "mix:targetWorkspace".intern();

  private static final String URL                  = "http://www.w3.org/2001/XMLSchema".intern();

  private static final String START_TIME_PROPERTY  = "publication:startPublishedDate".intern();

  private static String       fromState            = null;

  private static String       toState              = null;

  private static String       localTempDir         = null;

  private static String       targetServerUrl      = null;

  private static String       targetKey            = null;

  private static String       predefinedPath       = null;

  private static String       workspace            = null;

  private static String       repository           = null;

  private static String       contentPath          = null;

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
      ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
      if (manageableRepository == null) {
        if (log.isDebugEnabled())
          log.debug("Repository '" + repository + "' not found., ignoring");
        return;
      }
      PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      session = sessionProvider.getSession(workspace, manageableRepository);
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      boolean isExported = false;
      Query query = queryManager.createQuery("select * from nt:base where publication:currentState='"
                                                 + fromState
                                                 + "' and jcr:path like '"
                                                 + contentPath + "/%'",
                                             Query.SQL);
      File exportFolder = new File(localTempDir);
      if (!exportFolder.exists())
        exportFolder.mkdirs();
      File file = new File(localTempDir + File.separatorChar + "contents.xml");
      ByteArrayOutputStream bos = null;
      List<Node> categorySymLinks = null;
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      FileOutputStream output = new FileOutputStream(file);
      XMLStreamWriter xmlsw = outputFactory.createXMLStreamWriter(output, "UTF-8");
      xmlsw.writeStartDocument("UTF-8", "1.0");
      xmlsw.writeStartElement("xs", "contents", URL);
      xmlsw.writeNamespace("xs", URL);
      QueryResult queryResult = query.execute();
      if (queryResult.getNodes().getSize() > 0) {
        TaxonomyService taxonomyService = (TaxonomyService) container.getComponentInstanceOfType(TaxonomyService.class);
        Date nodeDate = null;
        Date now = null;
        xmlsw.writeStartElement("xs", "published-contents", URL);
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
            session.save();
            HashMap<String, String> context_ = new HashMap<String, String>();
            publicationPlugin.changeState(node_, toState, context_);
            log.info("change the status of the node " + node_.getPath() + " to " + toState);
            bos = new ByteArrayOutputStream();

            NodeLocation nodeLocation = NodeLocation.make(node_);
            StringBuffer contenTargetPath = new StringBuffer();
            contenTargetPath.append(nodeLocation.getRepository());
            contenTargetPath.append(":");
            contenTargetPath.append(nodeLocation.getWorkspace());
            contenTargetPath.append(":");
            contenTargetPath.append(nodeLocation.getPath());

            session.exportSystemView(node_.getPath(), bos, false, false);
            if (!isExported)
              isExported = true;
            xmlsw.writeStartElement("xs", "published-content", URL);
            xmlsw.writeAttribute("targetPath", contenTargetPath.toString());
            xmlsw.writeStartElement("xs", "data", URL);
            xmlsw.writeCData(bos.toString());
            xmlsw.writeEndElement();
            xmlsw.writeStartElement("xs", "links", URL);

            categorySymLinks = taxonomyService.getAllCategories(node_, true);

            for (Node nodeSymlink : categorySymLinks) {

              NodeLocation symlinkLocation = NodeLocation.make(nodeSymlink);
              StringBuffer symlinkTargetPath = new StringBuffer();
              symlinkTargetPath.append(symlinkLocation.getRepository());
              symlinkTargetPath.append(":");
              symlinkTargetPath.append(symlinkLocation.getWorkspace());
              symlinkTargetPath.append(":");
              symlinkTargetPath.append(symlinkLocation.getPath());

              xmlsw.writeStartElement("xs", "link", URL);
              xmlsw.writeStartElement("xs", "type", URL);
              xmlsw.writeCharacters("exo:taxonomyLink");
              xmlsw.writeEndElement();
              xmlsw.writeStartElement("xs", "title", URL);
              xmlsw.writeCharacters(node_.getName());
              xmlsw.writeEndElement();
              xmlsw.writeStartElement("xs", "targetPath", URL);
              xmlsw.writeCharacters(symlinkTargetPath.toString());
              xmlsw.writeEndElement();
              xmlsw.writeEndElement();
            }
            xmlsw.writeEndElement();
            xmlsw.writeEndElement();
          }
        }
        xmlsw.writeEndElement();
      }
      query = queryManager.createQuery("select * from nt:base where publication:currentState='unpublished' and jcr:path like '"
                                           + contentPath + "/%'",
                                       Query.SQL);
      queryResult = query.execute();
      if (queryResult.getNodes().getSize() > 0) {
        xmlsw.writeStartElement("xs", "unpublished-contents", URL);
        for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
          Node node_ = iter.nextNode();

          if (node_.isNodeType("nt:frozenNode"))
            continue;
          NodeLocation nodeLocation = NodeLocation.make(node_);
          StringBuffer contenTargetPath = new StringBuffer();
          contenTargetPath.append(nodeLocation.getRepository());
          contenTargetPath.append(":");
          contenTargetPath.append(nodeLocation.getWorkspace());
          contenTargetPath.append(":");
          contenTargetPath.append(nodeLocation.getPath());

          xmlsw.writeStartElement("xs", "unpublished-content", URL);
          xmlsw.writeAttribute("targetPath", contenTargetPath.toString());
          xmlsw.writeEndElement();
          if (!isExported)
            isExported = true;
        }
        xmlsw.writeEndElement();
      }
      xmlsw.writeEndElement();
      if (bos != null) {
        bos.close();
      }
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
          log.debug("The response of the production server:" + string);
        }
        connection.disconnect();
      }

      log.info("End Execute ExportContentJob");
    } catch (RepositoryException ex) {
      log.debug("Repository 'repository ' not found.");
    } catch (ConnectException ex) {
      log.debug("The front server is down.");
    } 
    catch (Exception ex) {
      log.error("Error when exporting content : " + ex.getMessage(), ex);
    } finally {
      if (session != null)
        session.logout();
    }
  }
}
