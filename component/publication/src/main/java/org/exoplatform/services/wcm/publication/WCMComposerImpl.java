/**
 * 
 */
package org.exoplatform.services.wcm.publication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.WCMService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.picocontainer.Startable;

/**
 * The Class WCMComposerImpl.
 * 
 * @author benjamin
 */
public class WCMComposerImpl implements WCMComposer, Startable {

	/** The repository service. */
	private RepositoryService repositoryService;

	/** The link manager service. */
	private LinkManager linkManager;
	
	private PublicationService  publicationService;
	
	private TaxonomyService  taxonomyService;
	
	private TemplateService templateService;
	
	private WCMService wcmService;
	
	/** The cache. */
	private ExoCache cache;
	
	private boolean isCached = true;
	
	/** The log. */
	private static Log log = ExoLogger.getLogger(WCMComposerImpl.class);
	
	/** The template filter query */
	private String templatesFilter;

	/**
	 * Instantiates a new WCM composer impl.
	 * 
	 * @param templateService the template service
	 * @param publicationService the publication service
	 * 
	 * @throws Exception the exception
	 */
	public WCMComposerImpl() throws Exception {
		repositoryService = WCMCoreUtils.getService(RepositoryService.class);
		linkManager = WCMCoreUtils.getService(LinkManager.class);
		publicationService = WCMCoreUtils.getService(PublicationService.class);
		templateService = WCMCoreUtils.getService(TemplateService.class);
		wcmService = WCMCoreUtils.getService(WCMService.class);
		cache = WCMCoreUtils.getService(CacheService.class).getCacheInstance("wcm.composer");
//		cache.setLiveTime(60);
	}
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#getContent(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public Node getContent(String repository, String workspace, String nodeIdentifier, HashMap<String, String> filters, SessionProvider sessionProvider) throws Exception {
		String mode = filters.get(FILTER_MODE); 
  		String version = filters.get(FILTER_VERSION);
  		String remoteUser = null;
  		try {
  			remoteUser = Util.getPortalRequestContext().getRemoteUser();
  		} catch (Exception e) {}

		if (repository==null && workspace==null) {
		  String[] params = nodeIdentifier.split("/");
		  repository = params[0];
		  workspace = params[1];
		  nodeIdentifier = nodeIdentifier.substring(repository.length()+workspace.length()+1);
		}
		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(nodeIdentifier, version, remoteUser);
			Node cachedNode = (Node)cache.get(hash);
			if (cachedNode != null) return cachedNode;
		}
		Node node = null;
		try {
		  node = wcmService.getReferencedContent(sessionProvider, repository, workspace, nodeIdentifier);
		} catch (RepositoryException e) {
		  node = getNodeByCategory(repository, repository + "/" + workspace + nodeIdentifier);
		}
		if (version == null || !BASE_VERSION.equals(version)) {
			node = getViewableContent(node, filters);
		}
		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(nodeIdentifier, version, remoteUser);
			cache.remove(hash);
			cache.put(hash, node);
		}
		return node;
	}

	@SuppressWarnings("unchecked")
	public List<Node> getContents(String repository, String workspace, String path, HashMap<String, String> filters, SessionProvider sessionProvider) throws Exception {
		String mode = filters.get(FILTER_MODE);
		String version = filters.get(FILTER_VERSION);
  		String remoteUser = null;
  		try {
  			remoteUser = Util.getPortalRequestContext().getRemoteUser();
  		} catch (Exception e) {}

		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(path, version, remoteUser);
			List<Node> cachedNodes = (List<Node>)cache.get(hash);
			if (cachedNodes != null) return cachedNodes;
		}
		NodeIterator nodeIterator = getViewableContents(repository, workspace, path, filters, sessionProvider);
		List<Node> nodes = new ArrayList<Node>();
		Node node = null, viewNode = null;
		while (nodeIterator.hasNext()) {
			node = nodeIterator.nextNode();
			viewNode = getViewableContent(node, filters);
			if (viewNode != null) {
				nodes.add(viewNode);
			}
		}
		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(path, version, remoteUser);
			cache.remove(hash);
			cache.put(hash, nodes);
		}
		return nodes;    
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#getContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	private NodeIterator getViewableContents(String repository, String workspace, String path, HashMap<String, String> filters, SessionProvider sessionProvider) throws Exception {
		ManageableRepository manageableRepository = repositoryService.getRepository(repository);
		Session session = sessionProvider.getSession(workspace, manageableRepository);
		QueryManager manager = session.getWorkspace().getQueryManager();
		String orderFilter = getOrderSQLFilter(filters);
		String mode = filters.get(FILTER_MODE);
		String recursive = filters.get(FILTER_RECURSIVE);
		String primaryType = filters.get(FILTER_PRIMARY_TYPE);
		if (primaryType == null) {
		  primaryType = "nt:base";
		  Node currentFolder = session.getRootNode().getNode(path.substring(1));
		  if (currentFolder.isNodeType("exo:taxonomy")) {
        primaryType = "exo:taxonomyLink";
      }
		}
		StringBuffer statement = new StringBuffer();

		statement.append("SELECT * FROM " + primaryType + " WHERE (jcr:path LIKE '" + path + "/%'");
		if (recursive==null) {
			statement.append(" AND NOT jcr:path LIKE '" + path + "/%/%')");
		}
		statement.append(" AND (" + getTemlatesSQLFilter(repository) + ") AND (" + "(NOT publication:currentState like '%')");
		if (MODE_LIVE.equals(mode)) {
			statement.append(" OR publication:currentState='" + PublicationDefaultStates.PUBLISHED + "')");
		} else {
			statement.append(" OR (publication:currentState<>'" + PublicationDefaultStates.OBSOLETE + "' AND publication:currentState<>'" + PublicationDefaultStates.ARCHIVED + "'))");
		}
		statement.append(orderFilter);
		Query query = manager.createQuery(statement.toString(), Query.SQL);
		return query.execute().getNodes();
	}

	/**
	 * Gets the node view.
	 *
	 * @param node the node
	 *
	 * @return the node view
	 *
	 * @throws Exception the exception
	 */
	private Node getViewableContent(Node node, HashMap<String, String> filters) throws Exception {
		if (node.isNodeType("exo:taxonomyLink")) node = linkManager.getTarget(node);
		HashMap<String, Object> context = new HashMap<String, Object>();
		String mode = filters.get(FILTER_MODE);
		context.put(WCMComposer.FILTER_MODE, mode);
		String lifecyleName = null;
		try {
			lifecyleName = publicationService.getNodeLifecycleName(node);
		} catch (NotInPublicationLifecycleException e) {
			// Don't log here, this is normal
		}
		if (lifecyleName == null) return node;
		PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecyleName);
		Node viewNode = publicationPlugin.getNodeView(node, context);
		return viewNode;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContent(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public boolean updateContent(String repository, String workspace, String path, HashMap<String, String> filters) throws Exception {
		if (isCached) {
			if (log.isInfoEnabled()) log.info("updateContent : "+path);
			String part = path.substring(0, path.lastIndexOf("/"));
	  		String remoteUser = null;
	  		try {
	  			remoteUser = Util.getPortalRequestContext().getRemoteUser();
	  		} catch (Exception e) {}
			
			/* remove live cache */
			String hash = getHash(path, null);
			cache.remove(hash);
			/* remove base content cache */
			hash = getHash(path, BASE_VERSION);
			cache.remove(hash);
			/* remove parent cache */
			hash = getHash(part, null);
			cache.remove(hash);
			if (remoteUser!=null) {
				/* remove live cache for current user */
				hash = getHash(path, null, remoteUser);
				cache.remove(hash);
				/* remove base content cache for current user */
				hash = getHash(path, BASE_VERSION, remoteUser);
				cache.remove(hash);
				/* remove parent cache for current user */
				hash = getHash(part, null, remoteUser);
				cache.remove(hash);
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public boolean updateContents(String repository, String workspace, String path, HashMap<String, String> filters) throws Exception {
		if (isCached) {
	  		String remoteUser = null;
	  		try {
	  			remoteUser = Util.getPortalRequestContext().getRemoteUser();
	  		} catch (Exception e) {}

	  		if (log.isInfoEnabled()) log.info("updateContents : "+path);
			String hash = getHash(path, null);
			cache.remove(hash);
			hash = getHash(path, BASE_VERSION);
			cache.remove(hash);
			if (remoteUser!=null) {
				hash = getHash(path, null, remoteUser);
				cache.remove(hash);
				hash = getHash(path, BASE_VERSION, remoteUser);
				cache.remove(hash);
			}
		}
		return true;
	}

	/**
	 * We currently support 2 modes :
	 * MODE_LIVE : PUBLISHED state only
	 * MODE_EDIT : PUBLISHED, DRAFT, PENDING, STAGED, APPROVED allowed.
	 * 
	 * @param mode the current mode (MODE_LIVE or MODE_EDIT)
	 * 
	 * @return the allowed states
	 */
	public List<String> getAllowedStates(String mode) {
		List<String> states = new ArrayList<String>();
		if (MODE_LIVE.equals(mode)) {
			states.add(PublicationDefaultStates.PUBLISHED);
		} else if (MODE_EDIT.equals(mode)) {
			states.add(PublicationDefaultStates.PUBLISHED);
			states.add(PublicationDefaultStates.DRAFT);
			states.add(PublicationDefaultStates.PENDING);
			states.add(PublicationDefaultStates.STAGED);
			states.add(PublicationDefaultStates.APPROVED);
		}
		return states;
	}

	/* (non-Javadoc)
	 * @see org.picocontainer.Startable#start()
	 */
	public void start() {}

	/* (non-Javadoc)
	 * @see org.picocontainer.Startable#stop()
	 */
	public void stop() {}

	/**
	 * Gets the order sql filter.
	 * 
	 * @param filters the filters
	 * 
	 * @return the order sql filter
	 */
	private String getOrderSQLFilter(HashMap<String, String> filters) {
		String orderQuery = " ORDER BY ";
		String orderBy = filters.get(FILTER_ORDER_BY);
		String orderType = filters.get(FILTER_ORDER_TYPE);
		if (orderType == null) orderType = "DESC";
		if (orderBy == null) orderBy = "exo:title";
		orderQuery += orderBy + " " + orderType;
		return orderQuery;
	}
	
	/**
	 * Gets all document nodetypes and write a query cause
	 * @param repository the repository's name
	 * @return a part of the query allow search all document node and taxonomy link also. Return null if there is any exception.
	 */
	private String getTemlatesSQLFilter(String repository) {
		if (templatesFilter != null) return templatesFilter;
		else {
			try {
				List<String> documentTypes = templateService.getDocumentTemplates(repository);
				StringBuffer documentTypeClause = new StringBuffer("(");
				for (int i = 0; i < documentTypes.size(); i++) {
					String documentType = documentTypes.get(i);
					documentTypeClause.append("jcr:primaryType = '" + documentType + "'");
					if (i != (documentTypes.size() - 1)) documentTypeClause.append(" OR ");
				}
				templatesFilter = documentTypeClause.toString();
				templatesFilter += "OR jcr:primaryType = 'exo:taxonomyLink')";
				return templatesFilter;
			} catch (Exception e) {
				log.error("Error when perform getTemlatesSQLFilter: ", e);
				return null;
			}
		}
	}
	
	/**
	 * Gets the node by category.
	 * 
	 * @param parameters the parameters
	 * 
	 * @return the node by category
	 * 
	 * @throws Exception the exception
	 */
	private Node getNodeByCategory(String repository, String parameters) throws Exception {
		try {
			if (taxonomyService==null) taxonomyService = WCMCoreUtils.getService(TaxonomyService.class);
			Node taxonomyTree = taxonomyService.getTaxonomyTree(repository, parameters.split("/")[0]);
			Node symlink = taxonomyTree.getNode(parameters.substring(parameters.indexOf("/") + 1));
			return linkManager.getTarget(symlink);
		} catch (Exception e) {
			return null;
		}
	}

	  private String getHash(String path, String version, String remoteUser) throws Exception {
		  String key = path;
		  if (version!=null) key += "::"+version;
		  if (remoteUser!=null) key += ";;"+remoteUser;
		  return MessageDigester.getHash(key);
	  }
	  
	  private String getHash(String path, String version) throws Exception {
		  return getHash(path, version, null);
	  }



}
