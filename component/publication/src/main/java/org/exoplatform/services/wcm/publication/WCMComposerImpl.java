/**
 * 
 */
package org.exoplatform.services.wcm.publication;

import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.picocontainer.Startable;

/**
 * @author benjamin
 *
 */
public class WCMComposerImpl implements WCMComposer, Startable {

	private TemplateService templateService;
	private ThreadLocalSessionProviderService threadLocalSessionProviderService;
	private RepositoryService repositoryService;
	
	private String templatesFilter = null;
	private String repository;
	
	public WCMComposerImpl(TemplateService templateService) throws Exception {
		this.templateService = templateService;
		this.repository = "repository";
		init();
	}
	
	private void init() throws Exception {
	    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class
        .cast(ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
		
	    repositoryService = RepositoryService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class));
	}
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#getContent(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	@Override
	public Node getContent(String repository, String workspace, String path,
			HashMap<String, String> filters) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#getContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	@Override
	public NodeIterator getContents(String repository, String workspace,
			String path, HashMap<String, String> filters) throws Exception {
		String templatesFilter = getTemlatesSQLFilter();
		SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
	    ManageableRepository manageableRepository = repositoryService.getRepository(repository);    
		Session session = sessionProvider.getSession(workspace, manageableRepository);
	    QueryManager manager = session.getWorkspace().getQueryManager();
	    String orderFilter = getOrderSQLFilter(filters);
	    String mode = filters.get(FILTER_MODE);
	    String statement="";
	    
	    statement = "select * from nt:base where jcr:path like '" + path
	    			+ "/%' AND NOT jcr:path like'" + path + "/%/%'" + " AND( "
	    			+ templatesFilter + ")";
	    if (MODE_LIVE.equals(mode)) {
	      statement += "AND publication:liveRevision IS NOT NULL AND publication:liveRevision <> '' ";
	    } 
	    statement += orderFilter;
	    
	    Query query = manager.createQuery(statement, Query.SQL);
	    return query.execute().getNodes();
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContent(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	@Override
	public boolean updateContent(String repository, String workspace,
			String path, HashMap<String, String> filters) {
		// TODO As we don't use cache management, there's nothing to update here but it will be implemented when we provide content caching
		return false;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	@Override
	public boolean updateContents(String repository, String workspace,
			String path, HashMap<String, String> filters) {
		// TODO As we don't use cache management, there's nothing to update here but it will be implemented when we provide content caching
		return false;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
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
			    return templatesFilter;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	private String getOrderSQLFilter(HashMap<String, String> filters) {
	    String orderQuery = " ORDER BY ";
	    String orderBy = filters.get(FILTER_ORDER_BY);
	    String orderType = filters.get(FILTER_ORDER_TYPE);
	    if (orderType == null) orderType = "DESC";
	    if (orderBy == null) orderBy = "exo:title";
	    orderQuery += orderBy + " " + orderType;
	    
	    return orderQuery;
	}
}
