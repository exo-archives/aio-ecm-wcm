package org.exoplatform.services.wcm.extensions.scheduler.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ChangeStateCronJobImpl implements Job {
    private static final Log log = ExoLogger.getLogger(ChangeStateCronJobImpl.class);

    private static final String START_TIME_PROPERTY = "publication:startPublishedDate".intern();

    private static final String END_TIME_PROPERTY = "publication:endPublishedDate".intern();

    private String fromState = null;

    private String toState = null;

    private String predefinedPath = null;

    private String workspace = null;

    private String repository = null;

    private String contentPath = null;

    public void execute(JobExecutionContext context) throws JobExecutionException {
	Session session = null;
	try {

	    log.info("Start Execute ChangeStateCronJob");
	    if (fromState == null) {

		JobDataMap jdatamap = context.getJobDetail().getJobDataMap();

		fromState = jdatamap.getString("fromState");
		toState = jdatamap.getString("toState");
		predefinedPath = jdatamap.getString("predefinedPath");
		String[] pathTab = predefinedPath.split(":");
		repository = pathTab[0];
		workspace = pathTab[1];
		contentPath = pathTab[2];

	    }
	    log.info("Start Execute ChangeStateCronJob: change the State from " + fromState + " to " + toState);
	    SessionProvider sessionProvider = SessionProvider.createSystemProvider();

	    ExoContainer container = ExoContainerContext.getCurrentContainer();
	    RepositoryService repositoryService_ = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
	    PublicationService publicationService_ = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
	    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
	    session = sessionProvider.getSession(workspace, manageableRepository);
	    QueryManager queryManager = session.getWorkspace().getQueryManager();
	    String property = null;
	    if ("staged".equals(fromState) && "published".equals(toState)) {
		property = START_TIME_PROPERTY;
	    } else if ("published".equals(fromState) && "unpublished".equals(toState)) {
		property = END_TIME_PROPERTY;

	    }
	    if (property != null) {
		Query query = queryManager.createQuery("select * from nt:base where publication:currentState='" + fromState + "' and jcr:path like '"
			+ contentPath + "/%'", Query.SQL);
		QueryResult queryResult = query.execute();
		if (queryResult.getNodes().getSize() > 0) {
		    for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
			Node node_ = iter.nextNode();
			if (node_.hasProperty(property)) {

			    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy - HH:mm");

			    System.out.println(toState + " " + property + " " + format.format(Calendar.getInstance().getTime()) + " "
				    + format.format(node_.getProperty(property).getDate().getTime()) + "  "
				    + Calendar.getInstance().compareTo(node_.getProperty(property).getDate()) );
			    if (Calendar.getInstance().getTime().compareTo(node_.getProperty(property).getDate().getTime()) >= 0) {
				PublicationService publicationService = (PublicationService) ExoContainerContext.getCurrentContainer()
					.getComponentInstanceOfType(PublicationService.class);
				PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(
					AuthoringPublicationConstant.LIFECYCLE_NAME);
				HashMap<String, String> context_=new HashMap<String, String>();
				if("unpublished".equals(toState)) {
				    if(node_.hasProperty("publication:liveRevision")){
					String liveRevisionProperty=node_.getProperty("publication:liveRevision").getString();
					if(!"".equals(liveRevisionProperty)){
					Node liveRevision=session.getNodeByUUID(liveRevisionProperty);
					    if (liveRevision != null) {
						context_.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, liveRevision.getName());
					    }
					}
				    }
				    
				}
				    
				publicationPlugin.changeState(node_, toState, context_);

			    }
			}
		    }
		} else {
		    log.info("There is no contents to change their states from " + fromState + " to " + toState);
		}
	    }
	    log.info("End Execute ChangeStateCronJob");
	} catch (Exception ex) {
	    ex.printStackTrace();
	    log.error("error when change the state of the content" + ex.getMessage());
	} finally {
	    session.logout();
	}
    }
}
