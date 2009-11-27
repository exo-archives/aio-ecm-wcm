/**
 * 
 */
package org.exoplatform.services.wcm.publication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.picocontainer.Startable;

/**
 * The Class WCMComposerImpl.
 * 
 * @author benjamin
 */
public class WCMComposerImpl implements WCMComposer, Startable {

	/** The template service. */
	private TemplateService templateService;

	/** The repository service. */
	private RepositoryService repositoryService;

	/** The publication service. */
	private PublicationService publicationService;

	/** The taxonomy service. */
	private TaxonomyService taxonomyService=null;
	
	/** The cache service. */
	private ExoCache cache;
	
	private boolean isCached = true;
	
	/** The templates filter. */
	private String templatesFilter = null;

	/** The repository. */
	private String repository;

	/** The log. */
	private static Log log = ExoLogger.getLogger(WCMComposerImpl.class);

	/**
	 * Instantiates a new WCM composer impl.
	 * 
	 * @param templateService the template service
	 * @param publicationService the publication service
	 * 
	 * @throws Exception the exception
	 */
	public WCMComposerImpl() throws Exception {
		this.templateService = WCMCoreUtils.getService(TemplateService.class);
		this.publicationService = WCMCoreUtils.getService(PublicationService.class);
		this.repositoryService = WCMCoreUtils.getService(RepositoryService.class);
		this.repository = "repository";
		CacheService cacheService = WCMCoreUtils.getService(CacheService.class);
		cache = cacheService.getCacheInstance("wcm.composer");
//		cache.setLiveTime(60);
	}
	
	private String getHash(String path, String version) throws Exception {
		String key = path;
		if (version!=null) key += "::"+version;
		return MessageDigester.getHash(key);
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#getContent(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public Node getContent(
			String repository,
			String workspace,
			String path,
			HashMap<String, String> filters,
			SessionProvider sessionProvider) throws Exception {

		String mode = filters.get(FILTER_MODE); 
  		String version = filters.get(FILTER_VERSION);

		if (repository==null && workspace==null) {
		    String[] params = path.split("/");
		    repository = params[0];
		    workspace = params[1];
		    path = path.substring(repository.length()+workspace.length()+1);
		}

		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(path, version);
			Node cachedNode = (Node)cache.get(hash);
			if (cachedNode!=null) return cachedNode;
		}
		
		Node node = null;
		try {
			ManageableRepository manageableRepository = repositoryService.getRepository(repository);
			Session session = sessionProvider.getSession(workspace, manageableRepository);
			node = (Node)session.getItem(path);
		} catch (RepositoryException e) {
			node = getNodeByCategory(repository+"/"+workspace+path);
		}
		
  		if (node.isNodeType("exo:taxonomyLink")) {
  			String uuid = node.getProperty("exo:uuid").getString();
  			node = node.getSession().getNodeByUUID(uuid);
  		}
		
  		if (version == null || !BASE_VERSION.equals(version)) {
  			node = getViewableContent(node, filters);
  		}

		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(path, version);
			cache.remove(hash);
			cache.put(hash, node);
		}

		return node;
	}

	public List<Node> getContents(
			String repository,
			String workspace,
			String path,
			HashMap<String, String> filters,
			SessionProvider sessionProvider) throws Exception {

		String mode = filters.get(FILTER_MODE);
		String version = filters.get(FILTER_VERSION);

		if (MODE_LIVE.equals(mode) && isCached) {
			String hash = getHash(path, version);
			List<Node> cachedNodes = (List<Node>)cache.get(hash);
			if (cachedNodes!=null) return cachedNodes;
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
			String hash = getHash(path, version);
			cache.remove(hash);
			cache.put(hash, nodes);
		}


		return nodes;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#getContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	private NodeIterator getViewableContents(
			String repository,
			String workspace,
			String path,
			HashMap<String, String> filters,
			SessionProvider sessionProvider) throws Exception {
		String templatesFilter = getTemlatesSQLFilter();
		ManageableRepository manageableRepository = repositoryService.getRepository(repository);
		Session session = sessionProvider.getSession(workspace, manageableRepository);
		QueryManager manager = session.getWorkspace().getQueryManager();
		String orderFilter = getOrderSQLFilter(filters);
		String mode = filters.get(FILTER_MODE);
		String recursive = filters.get(FILTER_RECURSIVE);
		String primaryType = filters.get(FILTER_PRIMARY_TYPE);
		if (primaryType==null) {
			primaryType = "nt:base";
			Node currentFolder = session.getRootNode().getNode(path.substring(1));
			if (currentFolder.isNodeType("exo:taxonomy")) {
				primaryType = "exo:taxonomyLink";
			}
		}
		StringBuffer statement = new StringBuffer();

		statement.append("select * from "+ primaryType +" where " + "jcr:path like '" + path + "/%'");
		if (recursive==null) {
			statement.append(" AND " + "NOT jcr:path like '" + path + "/%/%'");
		}
		statement.append(" AND " + templatesFilter + " AND " + "NOT publication:currentState like '%'");
		if (MODE_LIVE.equals(mode)) {
			statement.append(" OR publication:currentState = 'published'");
		} else {
			statement.append(" OR publication:currentState <> 'obsolete' AND publication:currentState <> 'archived'");
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
		HashMap<String, Object> context = new HashMap<String, Object>();
		String mode = filters.get(FILTER_MODE);
		context.put(WCMComposer.FILTER_MODE, mode);
		String lifecyleName = null;
		try {
			lifecyleName = publicationService.getNodeLifecycleName(node);
		} catch (NotInPublicationLifecycleException e) {
			// You shouldn't throw popup message, because some exception often rise here.
		}
		if (lifecyleName == null) {
			return node;
		}

		PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecyleName);
		Node viewNode = publicationPlugin.getNodeView(node, context);

		
		
		String state = this.getContentState(node);
		List<String> states = getAllowedStates(mode);

		if (states.contains(state)) {
			return viewNode;
		} else {
			return null;
		}

	}



	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContent(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public boolean updateContent(String repository, String workspace,
			String path, HashMap<String, String> filters) throws Exception {
		
		if (isCached) {
			if (log.isInfoEnabled()) log.info("updateContent : "+path);
			/* remove live cache */
			String hash = getHash(path, null);
			cache.remove(hash);
			/* remove base content cache */
			hash = getHash(path, BASE_VERSION);
			cache.remove(hash);
			/* remove parent cache */
			String part = path.substring(0, path.lastIndexOf("/"));
			hash = getHash(part, null);
			cache.remove(hash);
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public boolean updateContents(String repository, String workspace,
			String path, HashMap<String, String> filters) throws Exception {

		if (isCached) {
			if (log.isInfoEnabled()) log.info("updateContents : "+path);
			String hash = getHash(path, null);
			cache.remove(hash);
			hash = getHash(path, BASE_VERSION);
			cache.remove(hash);
		}
		
		return true;
	}

	/**
	 * We currently support 2 modes :
	 * MODE_LIVE : PUBLISHED state only
	 * MODE_EDIT : PUBLISHED, DRAFT, PENDING, STAGED, APPROVED allowed.
	 * 
	 * @param mode the mode
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
	public void start() {
	}

	/* (non-Javadoc)
	 * @see org.picocontainer.Startable#stop()
	 */
	public void stop() {
	}

	/**
	 * Gets the temlates sql filter.
	 * 
	 * @return the temlates sql filter
	 */
	private String getTemlatesSQLFilter() {
		if (templatesFilter!=null)
			return templatesFilter;
		else {
			try {
				List<String> listDocumentTypes = templateService.getDocumentTemplates(repository);
				StringBuffer documentTypeClause = new StringBuffer();
				for (int i = 0; i < listDocumentTypes.size(); i++) {
					String documentType = listDocumentTypes.get(i);
					documentTypeClause.append("jcr:primaryType = '" + documentType + "'");
					if (i != (listDocumentTypes.size() - 1)) {
						documentTypeClause.append(" OR ");
					}
				}
				templatesFilter = documentTypeClause.toString();
				templatesFilter += "OR jcr:primaryType = 'exo:taxonomyLink'";
				return templatesFilter;
			} catch (Exception e) {
				log.error("Error when perform getTemlatesSQLFilter: ", e.fillInStackTrace());
			}
		}
		return "";
	}

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

	private String getContentState(Node node) throws Exception {
	    String currentState = null;
	    try {
	      if(node.hasProperty("publication:currentState")) {
	        currentState = node.getProperty("publication:currentState").getString();
	      }
	    } catch (Exception e) {
	      //log.info("Error when perform getContentState: " + e.getMessage());
	    }
	    currentState = PublicationDefaultStates.PUBLISHED;
	    return currentState;
	}
	
	//  acme/categories/acme/World/ben
	  /**
	   * Gets the node by category.
	   * 
	   * @param parameters the parameters
	   * 
	   * @return the node by category
	   * 
	   * @throws Exception the exception
	   */
	  private Node getNodeByCategory(String path) throws Exception {
	    if (path == null) return null;
	    String[] params = path.split("/");
	    try {
			if (taxonomyService==null) taxonomyService = WCMCoreUtils.getService(TaxonomyService.class);

	    	Node taxonomyTree = taxonomyService.getTaxonomyTree(this.repository, params[0]);
	      Node symLink = null;
	      path = path.substring(path.indexOf("/") + 1);
	      while(taxonomyTree != null){
	        try{
	          symLink = taxonomyTree.getNode(path);
	          break;
	        }catch(PathNotFoundException exception){
	          taxonomyTree = taxonomyTree.getParent();
	        }
	      }
	      return taxonomyTree.getSession().getNodeByUUID(symLink.getProperty("exo:uuid").getString());
	    } catch (Exception e) {
	      e.printStackTrace();
	      return null;
	    }
	  }


}
