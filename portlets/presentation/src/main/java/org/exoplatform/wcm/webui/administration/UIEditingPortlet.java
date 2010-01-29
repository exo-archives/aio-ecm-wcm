package org.exoplatform.wcm.webui.administration;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class,
                 template = "app:/groovy/Editing/UIEditingToolBar.gtmpl",
                 events = {
                   @EventConfig(listeners = UIEditingPortlet.DraftActionListener.class),
                   @EventConfig(listeners = UIEditingPortlet.PublishedActionListener.class) })
public class UIEditingPortlet extends UIPortletApplication {

  public UIEditingPortlet() throws Exception {}
  
 /**
  * The listener interface for receiving turnOnQuickEditAction events.
  * The class that is interested in processing a turnOnQuickEditAction
  * event implements this interface, and the object created
  * with that class is registered with a component using the
  * component's <code>addTurnOnQuickEditActionListener<code> method. When
  * the turnOnQuickEditAction event occurs, that object's appropriate
  * method is invoked.
  * 
  * @see TurnOnQuickEditActionEvent
  */
 public static class DraftActionListener extends EventListener<UIEditingPortlet> {
   
   /* (non-Javadoc)
    * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
    */
   public void execute(Event<UIEditingPortlet> event) throws Exception {
     PortalRequestContext context = Util.getPortalRequestContext();
     context.getRequest().getSession().setAttribute(Utils.TURN_ON_QUICK_EDIT, true);
     Utils.updatePortal((PortletRequestContext) event.getRequestContext());      
   }
 }

 /**
  * The listener interface for receiving turnOffQuickEditAction events.
  * The class that is interested in processing a turnOffQuickEditAction
  * event implements this interface, and the object created
  * with that class is registered with a component using the
  * component's <code>addTurnOffQuickEditActionListener<code> method. When
  * the turnOffQuickEditAction event occurs, that object's appropriate
  * method is invoked.
  * 
  * @see TurnOffQuickEditActionEvent
  */
 public static class PublishedActionListener extends EventListener<UIEditingPortlet> {
   
   /* (non-Javadoc)
    * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
    */
   public void execute(Event<UIEditingPortlet> event) throws Exception {
     PortalRequestContext context = Util.getPortalRequestContext();
     context.getRequest().getSession().setAttribute(Utils.TURN_ON_QUICK_EDIT, false);
     Utils.updatePortal((PortletRequestContext) event.getRequestContext());
   }
 }
}
