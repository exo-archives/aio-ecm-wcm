/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.formgenerator;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.version.OnParentVersionAction;

import org.exoplatform.ecm.webui.form.validator.ECMNameValidator;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormHiddenInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
import org.exoplatform.ws.frameworks.json.JsonHandler;
import org.exoplatform.ws.frameworks.json.impl.BeanBuilder;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 22, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/FormGeneratorPortlet/UIFormGeneratorTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIFormGeneratorTabPane.SaveActionListener.class),
      @EventConfig(listeners = UIFormGeneratorTabPane.ResetActionListener.class, phase = Phase.DECODE)
    }
)
public class UIFormGeneratorTabPane extends UIFormTabPane {

  public UIFormGeneratorTabPane() throws Exception {
    super(UIFormGeneratorConstant.FORM_GENERATOR_TABPANE);
    
    UIFormInputSet formGeneratorGeneralTab = new UIFormInputSet(UIFormGeneratorConstant.FORM_GENERATOR_GENERAL_TAB);
    UIFormStringInput nameFormStringInput = new UIFormStringInput(UIFormGeneratorConstant.NAME_FORM_STRING_INPUT, UIFormGeneratorConstant.NAME_FORM_STRING_INPUT, null); 
    nameFormStringInput.addValidator(MandatoryValidator.class);
    nameFormStringInput.addValidator(ECMNameValidator.class);
    formGeneratorGeneralTab.addUIFormInput(nameFormStringInput);
    formGeneratorGeneralTab.addUIFormInput(new UIFormHiddenInput(UIFormGeneratorConstant.JSON_OBJECT_FORM_GENERATOR, UIFormGeneratorConstant.JSON_OBJECT_FORM_GENERATOR, null));
    formGeneratorGeneralTab.addUIFormInput(new UIFormWYSIWYGInput(UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT, UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT, null));
    formGeneratorGeneralTab.addUIFormInput(new UIFormUploadInput(UIFormGeneratorConstant.ICON_FORM_UPLOAD_INPUT, UIFormGeneratorConstant.ICON_FORM_UPLOAD_INPUT));
    addUIFormInput(formGeneratorGeneralTab);
    
    addChild(UIFormGeneratorDnDTab.class, null, null);
    
    setSelectedTab(formGeneratorGeneralTab.getId());
  }
  
  private int getNumberRequireType(String formType, int size) {
    if (UIFormGeneratorConstant.UPLOAD.equals(formType))
      return PropertyType.BINARY;
    else if (UIFormGeneratorConstant.DATE.equals(formType))
      return PropertyType.DATE;
    else if (size == 1)
      return PropertyType.BOOLEAN;
    else
      return PropertyType.STRING;
  }
  
  private String getNodetypeName(String nodetypeName) {
    return "exo:fg_n_" + Utils.cleanString(nodetypeName);
  }
  
  private String getPropertyName(String inputName) {
    return "exo:fg_p_" + Utils.cleanString(inputName);
  }
  
  private void addNodetype(WebuiRequestContext requestContext, String repository, String nodetypeName, List<UIFormGeneratorInputBean> formBeans) throws Exception {
    NodeTypeValue newNodeType = new NodeTypeValue() ;                             
    newNodeType.setName(nodetypeName) ;
    // TODO: Need update in 1.3
    newNodeType.setPrimaryItemName(null);
    // TODO: Need update in 1.3
    newNodeType.setMixin(false) ;
    // TODO: Need update in 1.3
    newNodeType.setOrderableChild(false) ;
    // TODO: Need update in 1.3
    List<String> supertypes = new ArrayList<String>();
    supertypes.add("nt:base");
    newNodeType.setDeclaredSupertypeNames(supertypes) ;      

    List<PropertyDefinitionValue> properties = new ArrayList<PropertyDefinitionValue>();
    for (UIFormGeneratorInputBean form : formBeans) {
      PropertyDefinitionValue property = new PropertyDefinitionValue() ;
      property.setName(getPropertyName(form.getName())) ;          
      property.setRequiredType(getNumberRequireType(form.getType(), formBeans.size())) ;
      // TODO: Need update in 1.3
      property.setMultiple(false) ;    
      property.setMandatory(form.isMandatory()) ;
      // TODO: Need update in 1.3
      property.setAutoCreate(false) ;
      // TODO: Need update in 1.3
      property.setReadOnly(false) ;
      // TODO: Need update in 1.3
      property.setOnVersion(OnParentVersionAction.COPY) ; 
      property.setValueConstraints(null) ;
      properties.add(property) ;
    }
    newNodeType.setDeclaredPropertyDefinitionValues(properties) ;
    
    // TODO: Need update in 1.3
    newNodeType.setDeclaredChildNodeDefinitionValues(new ArrayList<NodeDefinitionValue>()) ;
    try {
      ExtendedNodeTypeManager extendedNodeTypeManager = getApplicationComponent(RepositoryService.class).getRepository(repository).getNodeTypeManager(); 
      extendedNodeTypeManager.registerNodeType(newNodeType, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    } catch (Exception e) {
      Utils.createPopupMessage(this, "UIFormGeneratorTabPane.msg.register-failed", null, ApplicationMessage.WARNING);
    }
  }
  
  private String generateDialogTemplate(List<UIFormGeneratorInputBean> forms) throws Exception {
    StringBuilder dialogTemplate = new StringBuilder();
    dialogTemplate.append("<%\n");
    dialogTemplate.append(" import java.util.Calendar;\n");
    dialogTemplate.append(" import java.text.SimpleDateFormat;\n");

    dialogTemplate.append(" private String getTimestampName() {\n");
    dialogTemplate.append(" 	Calendar now = Calendar.getInstance();\n");
    dialogTemplate.append(" 	SimpleDateFormat formatter = new SimpleDateFormat(\"yyyy.MM.dd '-' hh'h'mm'm'ss\");\n");
    dialogTemplate.append(" 	return formatter.format(now.getTime());\n");
    dialogTemplate.append(" }\n");
    dialogTemplate.append(" String timestampName = getTimestampName();\n");
    dialogTemplate.append(" %>\n");

    
    dialogTemplate.append("<div class=\"UIForm FormLayout\">");
    dialogTemplate.append("  <% uiform.begin() %>");
    dialogTemplate.append("    <div class=\"HorizontalLayout\">");
    dialogTemplate.append("      <table class=\"UIFormGrid\">");
    
//    UIFormGeneratorInputBean nameForm = forms.get(0);
    dialogTemplate.append("        <tr>");
    dialogTemplate.append("          <td class=\"FieldLabel\"><%=_ctx.appRes(\"FormGenerator.dialog.label." + "Date" + "\")%></td>");
    dialogTemplate.append("          <td class=\"FieldComponent\">");
    dialogTemplate.append("            $timestampName <div style=\"display:none;\"><%");
    dialogTemplate.append("              String[] fieldName = [\"jcrPath=/node\", \"mixintype=mix:i18n\", \"editable=if-null\", \"validate=empty,name\", timestampName] ;");
    dialogTemplate.append("              uicomponent.addTextField(\"name\", fieldName) ;");
    dialogTemplate.append("            %></div>");
    dialogTemplate.append("          </td>");
    dialogTemplate.append("        </tr>");
    for (int i = 0; i < forms.size(); i++) {
      UIFormGeneratorInputBean form = forms.get(i);
      String inputName = form.getName();
      String inputType = form.getType();
      String inputFieldName = Utils.cleanString(inputName) + "FieldName";
      String validate = "validate=";
      String inputField = "";
      String guideLine = form.getGuildLine();
      if (guideLine == null) guideLine = "";
      if (form.isMandatory())
        validate += "empty,";
      if (UIFormGeneratorConstant.TEXTAREA.equals(inputType)) {
        inputField = "TextAreaField";
      } else if (UIFormGeneratorConstant.WYSIWYG.equals(inputType)) {
        inputField = "WYSIWYGField";
      } else if (UIFormGeneratorConstant.DATE.equals(inputType)) {
        inputField = "CalendarField";
        validate += "datetime,";
      } else if (UIFormGeneratorConstant.SELECT.equals(inputType)) {
        inputField = "SelectBoxField";
      } else {
        inputField = "TextField";
      }
      if (validate.endsWith(",")) validate = validate.substring(0, validate.length() - 1);
      if (validate.endsWith("=")) validate = "";
      String propertyName = getPropertyName(inputName);
      if (UIFormGeneratorConstant.LABEL.equals(inputType)) {
    	  dialogTemplate.append("      <tr>");
    	  dialogTemplate.append("        <td class=\"FieldLabel\" colspan=\"2\">"+form.getValue()+"</td>");
    	  dialogTemplate.append("        </td>");
    	  dialogTemplate.append("      </tr>");
    	  
      } else {
    	  dialogTemplate.append("      <tr>");
    	  dialogTemplate.append("        <td class=\"FieldLabel\"><%=_ctx.appRes(\"FormGenerator.dialog.label." + inputName + "\")%></td>");
    	  dialogTemplate.append("        <td class=\"FieldComponent\">");
    	  dialogTemplate.append("          <%");
    	  if (UIFormGeneratorConstant.UPLOAD.equals(inputType)) {
    		  dialogTemplate.append("           if(uicomponent.isEditing()) {");
    		  dialogTemplate.append("             def curNode = uicomponent.getNode() ;");
    		  dialogTemplate.append("             if(curNode.hasNode(\"" + propertyName + "\")) {");
    		  dialogTemplate.append("               def imageNode = curNode.getNode(\"" + propertyName + "\") ;");
    		  dialogTemplate.append("               if(imageNode.getProperty(\"jcr:data\").getStream().available() > 0) {");
    		  dialogTemplate.append("                 def imgSrc = uicomponent.getImage(curNode, \"" + propertyName + "\");");
    		  dialogTemplate.append("                 def actionLink = uicomponent.event(\"RemoveData\", \"/" + propertyName + "\");");
    		  dialogTemplate.append("                 %>");
    		  dialogTemplate.append("                   <div>");
    		  dialogTemplate.append("                     <image src=\"$imgSrc\" width=\"100px\" height=\"80px\"/>");
    		  dialogTemplate.append("                     <a href=\"$actionLink\">");
    		  dialogTemplate.append("                       <img src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" alt=\"\" class=\"ActionIcon Remove16x16Icon\"/>");
    		  dialogTemplate.append("                     </a>");
    		  dialogTemplate.append("                   </div>");
    		  dialogTemplate.append("                 <%");
    		  dialogTemplate.append("               } else {");
    		  dialogTemplate.append("                 String[] fieldImage = [\"jcrPath=/node/" + propertyName + "/jcr:data\"] ;");
    		  dialogTemplate.append("                 uicomponent.addUploadField(\"image\", fieldImage) ;");
    		  dialogTemplate.append("               }");
    		  dialogTemplate.append("             }");
    		  dialogTemplate.append("           } else if(uicomponent.dataRemoved()) {");
    		  dialogTemplate.append("             String[] fieldImage = [\"jcrPath=/node/" + propertyName + "/jcr:data\"] ;");
    		  dialogTemplate.append("             uicomponent.addUploadField(\"image\", fieldImage) ;");
    		  dialogTemplate.append("           } else {");
    		  dialogTemplate.append("             String[] fieldImage = [\"jcrPath=/node/" + propertyName + "/jcr:data\"] ;");
    		  dialogTemplate.append("             uicomponent.addUploadField(\"image\", fieldImage) ;");
    		  dialogTemplate.append("           }");
    	  } else {
    		  dialogTemplate.append("           String[] " + inputFieldName + " = [\"jcrPath=/node/" + propertyName + "\", \"defaultValues=" + form.getValue() + "\", \"" + validate + "\", \"options=" + form.getAdvanced() + "\"];");
    		  dialogTemplate.append("           uicomponent.add" + inputField + "(\"" + inputFieldName + "\", " + inputFieldName + ");");
    	  }
    	  dialogTemplate.append("          %>");
    	  dialogTemplate.append("        </td>");
    	  dialogTemplate.append("      </tr>");
    	  
      }
      
      dialogTemplate.append("      <tr>");
      dialogTemplate.append("        <td>&nbsp;</td>");
      dialogTemplate.append("        <td>");
      dialogTemplate.append("          <div class=\"GuideLine\">" + guideLine + "</div>");
      dialogTemplate.append("        </td>");
      dialogTemplate.append("      </tr>");
    }
    dialogTemplate.append("      </table>");
    dialogTemplate.append("      <div class=\"UIAction\">");
    dialogTemplate.append("        <table class=\"ActionContainer\">");
    dialogTemplate.append("          <tr>");
    dialogTemplate.append("            <td>");
    dialogTemplate.append("              <%");
    dialogTemplate.append("                for(action in uicomponent.getActions()) {");
    dialogTemplate.append("                  String actionLabel = _ctx.appRes(uicomponent.getName() + \".action.\" + action);");
    dialogTemplate.append("                  String link = uicomponent.event(action);");
    dialogTemplate.append("                  %>");
    dialogTemplate.append("                    <div onclick=\"$link\" class=\"ActionButton LightBlueStyle\">");
    dialogTemplate.append("                      <div class=\"ButtonLeft\">");
    dialogTemplate.append("                        <div class=\"ButtonRight\">");
    dialogTemplate.append("                          <div class=\"ButtonMiddle\">");
    dialogTemplate.append("                            <a href=\"javascript:void(0);\">$actionLabel</a>");
    dialogTemplate.append("                          </div>");
    dialogTemplate.append("                        </div>");
    dialogTemplate.append("                      </div>");
    dialogTemplate.append("                    </div>");
    dialogTemplate.append("                  <%");
    dialogTemplate.append("                }");
    dialogTemplate.append("              %>");
    dialogTemplate.append("            </td>");
    dialogTemplate.append("          </tr>");
    dialogTemplate.append("        </table>");
    dialogTemplate.append("      </div>");
    dialogTemplate.append("    </div>");
    dialogTemplate.append("  <% uiform.end() %>");
    dialogTemplate.append("</div>");
    return dialogTemplate.toString();
  }
  
  private String generateViewTemplate(List<UIFormGeneratorInputBean> forms) {
    StringBuilder viewTemplate = new StringBuilder();
    viewTemplate.append(" <% def currentNode = uicomponent.getNode() ; %>");
    viewTemplate.append(" <div>");
    viewTemplate.append("   <table width='90%' border='1px'>");
    viewTemplate.append("     <tr>");
    viewTemplate.append("       <th>Name</th>");
    viewTemplate.append("       <th>Value</th>");
    viewTemplate.append("     </tr>");
    for (UIFormGeneratorInputBean form : forms) {
      String propertyName = getPropertyName(form.getName());
      viewTemplate.append("   <tr>");
      viewTemplate.append("     <%");
      viewTemplate.append("       if (currentNode.hasProperty(\"" + propertyName + "\")) {");
      viewTemplate.append("         %>");
      viewTemplate.append("           <td><%= currentNode.getProperty(\"" + propertyName + "\").getName() %></td>");
      viewTemplate.append("           <td><%= currentNode.getProperty(\"" + propertyName + "\").getString() %></td>");
      viewTemplate.append("         <%");
      viewTemplate.append("       }");
      viewTemplate.append("     %>");
      viewTemplate.append("   </tr>");
    }
    viewTemplate.append("   </table>");
    viewTemplate.append(" </div>");
    return viewTemplate.toString();
  }
  
  public static class SaveActionListener extends EventListener<UIFormGeneratorTabPane> {
    public void execute(Event<UIFormGeneratorTabPane> event) throws Exception {
      UIFormGeneratorTabPane formGeneratorTabPane = event.getSource();
      UIFormInputSet formGeneratorGeneralTab = formGeneratorTabPane.getChildById(UIFormGeneratorConstant.FORM_GENERATOR_GENERAL_TAB);
      UIFormHiddenInput hiddenInputJSonObject = formGeneratorGeneralTab.getChildById(UIFormGeneratorConstant.JSON_OBJECT_FORM_GENERATOR);
      String jsonObjectGenerated = hiddenInputJSonObject.getValue();
      
      System.out.println("\n\n\n================================================\n\n\n");
      System.out.println(jsonObjectGenerated);	
      System.out.println("\n\n\n================================================\n\n\n");
      
      JsonHandler jsonHandler = new JsonDefaultHandler();
      new JsonParserImpl().parse(new InputStreamReader(new ByteArrayInputStream(jsonObjectGenerated.getBytes())), jsonHandler);
      JsonValue jsonValue = jsonHandler.getJsonObject();
      List<UIFormGeneratorInputBean> forms = ((UIFormGeneratorInputBean)new BeanBuilder().createObject(UIFormGeneratorInputBean.class, jsonValue)).getInputs();

      UIFormStringInput nameFormStringInput = formGeneratorTabPane.getUIStringInput(UIFormGeneratorConstant.NAME_FORM_STRING_INPUT);
      String templateName = nameFormStringInput.getValue();
      String nodetypeName = formGeneratorTabPane.getNodetypeName(templateName);
      
      String preferenceRepository = UIFormGeneratorUtils.getPreferenceRepository();
      
      formGeneratorTabPane.addNodetype(event.getRequestContext(), preferenceRepository, nodetypeName, forms);
      String newGTMPLTemplate = formGeneratorTabPane.generateDialogTemplate(forms);
      String newViewTemplate = formGeneratorTabPane.generateViewTemplate(forms);
      
      TemplateService templateService = Utils.getService(formGeneratorTabPane, TemplateService.class);
      templateService.addTemplate(true, nodetypeName, templateName, true, templateName, new String[] {"*"}, newGTMPLTemplate, preferenceRepository) ;
      templateService.addTemplate(false, nodetypeName, templateName, true, templateName, new String[] {"*"}, newViewTemplate, preferenceRepository) ;
      
      Utils.createPopupMessage(formGeneratorTabPane, "UIFormGeneratorTabPane.msg.AddNewsSuccessful", new Object[]{templateName}, ApplicationMessage.INFO);
      
      nameFormStringInput.setValue("");
      ((UIFormWYSIWYGInput)formGeneratorGeneralTab.getChildById(UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT)).setValue("");
      event.getRequestContext().addUIComponentToUpdateByAjax(formGeneratorTabPane);
    }
  }
  
  public static class ResetActionListener extends EventListener<UIFormGeneratorTabPane> {
    public void execute(Event<UIFormGeneratorTabPane> event) throws Exception {
      UIFormGeneratorTabPane formGeneratorTabPane = event.getSource();
      UIFormInputSet formGeneratorGeneralTab = formGeneratorTabPane.getChildById(UIFormGeneratorConstant.FORM_GENERATOR_GENERAL_TAB);
      formGeneratorGeneralTab.getUIStringInput(UIFormGeneratorConstant.NAME_FORM_STRING_INPUT).setValue("");
      ((UIFormWYSIWYGInput)formGeneratorGeneralTab.getChildById(UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT)).setValue("");
      event.getRequestContext().addUIComponentToUpdateByAjax(formGeneratorTabPane);
    }
  }
  
}