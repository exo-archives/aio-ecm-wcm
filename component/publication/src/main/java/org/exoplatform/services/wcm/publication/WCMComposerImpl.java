/**
 * 
 */
package org.exoplatform.services.wcm.publication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
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
    /** The wcm publication service. */
    private WCMPublicationService wcmPublicationService = null;
    /** The templates filter. */
    private String templatesFilter = null;
    /** The repository. */
    private String repository;
    /** The log. */
    private static Log log = ExoLogger.getLogger(WCMComposerImpl.class);

    private HashMap<String, Object> cache = new HashMap<String, Object>(100);

    /**
     * Instantiates a new wCM composer impl.
     *
     * @param templateService the template service
     * @param publicationService the publication service
     *
     * @throws Exception the exception
     */
    public WCMComposerImpl(TemplateService templateService, PublicationService publicationService) throws Exception {
        this.templateService = templateService;
        this.repository = "repository";
        this.publicationService = publicationService;
        init();
    }

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
    private void init() throws Exception {
        repositoryService = RepositoryService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class));
    }

    /**
     * Gets the wCM publication service.
     *
     * @return the wCM publication service
     */
    private WCMPublicationService getWCMPublicationService() {
        if (wcmPublicationService == null) {
            wcmPublicationService = WCMPublicationService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WCMPublicationService.class));
        }
        return wcmPublicationService;
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

        String hash = MessageDigester.getHash(path);
        Node cachedNode = (Node)cache.get(hash);
        if (cachedNode!=null) return cachedNode;

        ManageableRepository manageableRepository = repositoryService.getRepository(repository);
        Session session = sessionProvider.getSession(workspace, manageableRepository);
        Node node = (Node) session.getItem(path);
        node = getViewableContent(node, filters);

        cache.remove(hash);
        cache.put(hash, node);

        return node;
        /*
        String lifecycleName = publicationService.getNodeLifecycleName(node);
        PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecycleName);
        Node nodeView = publicationPlugin.getNodeView(node, new HashMap<String, Object>());
        String state = getWCMPublicationService().getContentState(node);
        String mode = filters.get(FILTER_MODE);
        List<String> states = getAllowedStates(mode);

        if (states.contains(state)) {
            return nodeView;
        } else {
            return null;
        }
        */
    }

    /* (non-Javadoc)
     * @see org.exoplatform.services.wcm.publication.WCMComposer#getContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
     */
    public NodeIterator getContents(
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
        if (primaryType==null) primaryType = "nt:base";
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

    public List<Node> getViewableContents(
            String repository,
            String workspace,
            String path,
            HashMap<String, String> filters,
            SessionProvider sessionProvider) throws Exception {

        String hash = MessageDigester.getHash(path);
        List<Node> cachedNodes = (List<Node>)cache.get(hash);
        if (cachedNodes!=null) return cachedNodes;


        NodeIterator nodeIterator = getContents(repository, workspace, path, filters, sessionProvider);
        List<Node> nodes = new ArrayList<Node>();
        Node node = null, viewNode = null;
        while (nodeIterator.hasNext()) {
            node = nodeIterator.nextNode();
            viewNode = getViewableContent(node, filters);
            if (viewNode != null) {
                nodes.add(viewNode);
            }
        }

        cache.remove(hash);
        cache.put(hash, nodes);


        return nodes;
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


        String state = getWCMPublicationService().getContentState(node);
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
            String path, HashMap<String, String> filters) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.exoplatform.services.wcm.publication.WCMComposer#updateContents(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
     */
    public boolean updateContents(String repository, String workspace,
            String path, HashMap<String, String> filters) {
        return false;
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
        if (templatesFilter != null) {
            return templatesFilter;
        } else {
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
        if (orderType == null) {
            orderType = "DESC";
        }
        if (orderBy == null) {
            orderBy = "exo:title";
        }
        orderQuery += orderBy + " " + orderType;

        return orderQuery;
    }

}
