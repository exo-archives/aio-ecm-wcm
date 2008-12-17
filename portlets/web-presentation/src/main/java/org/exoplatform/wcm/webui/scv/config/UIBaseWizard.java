/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wcm.webui.scv.config;

import javax.portlet.PortletMode;

import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * May 27, 2008
 */
public abstract class UIBaseWizard extends UIWizard {
  
  /** The EX o_ we b_ content. */
  protected final String EXO_WEB_CONTENT = "exo:webContent".intern();
  
  /** The EX o_ j s_ file. */
  protected final String EXO_JS_FILE = "exo:jsFile".intern();
  
  /** The number step. */
  private int numberStep;
  
  /**
   * Instantiates a new uI base wizard.
   * 
   * @throws Exception the exception
   */
  public UIBaseWizard() throws Exception { }
  
  /**
   * Sets the number steps.
   * 
   * @param number the new number steps
   */
  public void setNumberSteps(int number) { numberStep = number; }
  
  /**
   * Gets the number steps.
   * 
   * @return the number steps
   */
  public int getNumberSteps() { return numberStep; }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIWizard#url(java.lang.String)
   */
  public String url(String name) throws Exception {
    if ("Back".equals(name)) return event(name);
    return super.url(name);
  }

  /**
   * Gets the actions by step.
   * 
   * @return the actions by step
   */
  public abstract String[] getActionsByStep();

  /**
   * The listener interface for receiving abortAction events.
   * The class that is interested in processing a abortAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addAbortActionListener<code> method. When
   * the abortAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AbortActionEvent
   */
  public static class AbortActionListener extends EventListener<UIBaseWizard> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIBaseWizard> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }

  /**
   * The listener interface for receiving backAction events.
   * The class that is interested in processing a backAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBackActionListener<code> method. When
   * the backAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BackActionEvent
   */
  public static class BackActionListener extends EventListener<UIQuickCreationWizard> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIWizard uiWizard = event.getSource();
      uiWizard.viewStep(uiWizard.getCurrentStep() - 1);
    }
  }
}
