package org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.wcm.extensions.publication.impl.PublicationManagerImpl;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.ui.UIPublicationContainer;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionData;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionLog;
import org.exoplatform.services.wcm.publication.listener.navigation.NavigationEventListenerDelegate;
import org.exoplatform.services.wcm.publication.listener.page.PageEventListenerDelegate;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/** 
 * 
 * @author nedved
 *
 */

public class AuthoringPublicationPlugin extends StageAndVersionPublicationPlugin {

    /** The page event listener delegate. */
    private PageEventListenerDelegate pageEventListenerDelegate;

    /** The navigation event listener delegate. */
    private NavigationEventListenerDelegate navigationEventListenerDelegate;

    /**
     * Instantiates a new stage and version publication plugin.
     */
    public AuthoringPublicationPlugin() {
	pageEventListenerDelegate = new PageEventListenerDelegate(AuthoringPublicationConstant.LIFECYCLE_NAME, ExoContainerContext
		.getCurrentContainer());
	navigationEventListenerDelegate = new NavigationEventListenerDelegate(AuthoringPublicationConstant.LIFECYCLE_NAME, ExoContainerContext
		.getCurrentContainer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.exoplatform.services.ecm.publication.PublicationPlugin#changeState
     * (javax.jcr.Node, java.lang.String, java.util.HashMap)
     */
    public void changeState(Node node, String newState, HashMap<String, String> context) throws IncorrectStateUpdateLifecycleException, Exception {

	if (PublicationDefaultStates.PENDING.equals(newState)) {
	    String versionName = context.get(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME);
	    String logItemName = versionName;
	    String userId = node.getSession().getUserID();
	    if (node.getName().equals(versionName) || versionName == null) {
		logItemName = node.getName();
	    }
	    Map<String, VersionData> revisionsMap = getRevisionData(node);
	    VersionLog versionLog = null;

	    node.setProperty(StageAndVersionPublicationConstant.CURRENT_STATE, newState);
	    versionLog = new VersionLog(logItemName, newState, node.getSession().getUserID(), GregorianCalendar.getInstance(),
		    StageAndVersionPublicationConstant.PUBLICATION_LOG_DRAFT);
	    addLog(node, versionLog);
	    VersionData versionData = revisionsMap.get(node.getUUID());
	    if (versionData != null) {
		versionData.setAuthor(userId);
		versionData.setState(newState);
	    } else {
		versionData = new VersionData(node.getUUID(), newState, userId);
	    }
	    revisionsMap.put(node.getUUID(), versionData);
	    addRevisionData(node, revisionsMap.values());

	} else {
	    super.changeState(node, newState, context);
	}

	if (!node.isNew())
	    node.save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.exoplatform.services.ecm.publication.PublicationPlugin#getPossibleStates
     * ()
     */
    public String[] getPossibleStates() {
	ExoContainer container = ExoContainerContext.getCurrentContainer();
	container.getComponentInstanceOfType(PublicationManagerImpl.class);
	return new String[] { PublicationDefaultStates.ENROLLED, PublicationDefaultStates.DRAFT, PublicationDefaultStates.PENDING,
		PublicationDefaultStates.PUBLISHED, PublicationDefaultStates.OBSOLETE };
    }

    public String getLifecycleType() {
	return AuthoringPublicationConstant.PUBLICATION_LIFECYCLE_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.exoplatform.services.ecm.publication.PublicationPlugin#getStateUI
     * (javax.jcr.Node, org.exoplatform.webui.core.UIComponent)
     */
    public UIForm getStateUI(Node node, UIComponent component) throws Exception {
	UIPublicationContainer publicationContainer = component.createUIComponent(UIPublicationContainer.class, null, null);
	publicationContainer.initContainer(node);
	return publicationContainer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.exoplatform.services.ecm.publication.PublicationPlugin#addMixin(javax
     * .jcr.Node)
     */
    public void addMixin(Node node) throws Exception {
	node.addMixin(AuthoringPublicationConstant.PUBLICATION_LIFECYCLE_TYPE);
	if (!node.isNodeType(AuthoringPublicationConstant.MIX_VERSIONABLE)) {
	    node.addMixin(AuthoringPublicationConstant.MIX_VERSIONABLE);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.exoplatform.services.ecm.publication.PublicationPlugin#canAddMixin
     * (javax.jcr.Node)
     */
    public boolean canAddMixin(Node node) throws Exception {
	return node.canAddMixin(AuthoringPublicationConstant.PUBLICATION_LIFECYCLE_TYPE);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.exoplatform.services.wcm.publication.WebpagePublicationPlugin#
     * updateLifecycleOnChangeNavigation
     * (org.exoplatform.portal.config.model.PageNavigation, java.lang.String)
     */
    public void updateLifecycleOnChangeNavigation(PageNavigation pageNavigation, String remoteUser) throws Exception {
	navigationEventListenerDelegate.updateLifecycleOnChangeNavigation(pageNavigation, remoteUser, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.exoplatform.services.wcm.publication.WebpagePublicationPlugin#
     * updateLifecycleOnRemovePage(org.exoplatform.portal.config.model.Page,
     * java.lang.String)
     */
    public void updateLifecycleOnRemovePage(Page page, String remoteUser) throws Exception {
	pageEventListenerDelegate.updateLifecycleOnRemovePage(page, remoteUser, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.exoplatform.services.wcm.publication.WebpagePublicationPlugin#
     * updateLifecyleOnChangePage(org.exoplatform.portal.config.model.Page,
     * java.lang.String)
     */
    public void updateLifecyleOnChangePage(Page page, String remoteUser) throws Exception {
	pageEventListenerDelegate.updateLifecyleOnChangePage(page, remoteUser, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.exoplatform.services.wcm.publication.WebpagePublicationPlugin#
     * updateLifecyleOnCreateNavigation
     * (org.exoplatform.portal.config.model.PageNavigation)
     */
    public void updateLifecyleOnCreateNavigation(PageNavigation pageNavigation) throws Exception {
	navigationEventListenerDelegate.updateLifecyleOnCreateNavigation(pageNavigation);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.exoplatform.services.wcm.publication.WebpagePublicationPlugin#
     * updateLifecyleOnCreatePage(org.exoplatform.portal.config.model.Page,
     * java.lang.String)
     */
    public void updateLifecyleOnCreatePage(Page page, String remoteUser) throws Exception {
	pageEventListenerDelegate.updateLifecyleOnCreatePage(page, remoteUser, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.exoplatform.services.wcm.publication.WebpagePublicationPlugin#
     * updateLifecyleOnRemoveNavigation
     * (org.exoplatform.portal.config.model.PageNavigation)
     */
    public void updateLifecyleOnRemoveNavigation(PageNavigation pageNavigation) throws Exception {
	navigationEventListenerDelegate.updateLifecyleOnRemoveNavigation(pageNavigation);
    }

    /**
     * Adds the log.
     * 
     * @param node
     *            the node
     * @param versionLog
     *            the version log
     * 
     * @throws Exception
     *             the exception
     */
    private void addLog(Node node, VersionLog versionLog) throws Exception {
	Value[] values = node.getProperty(AuthoringPublicationConstant.HISTORY).getValues();
	ValueFactory valueFactory = node.getSession().getValueFactory();
	List<Value> list = new ArrayList<Value>(Arrays.asList(values));
	list.add(valueFactory.createValue(versionLog.toString()));
	node.setProperty(AuthoringPublicationConstant.HISTORY, list.toArray(new Value[] {}));
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
