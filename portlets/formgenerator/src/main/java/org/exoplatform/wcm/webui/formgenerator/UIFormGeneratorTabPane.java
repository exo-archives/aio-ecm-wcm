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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.version.OnParentVersionAction;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
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
    formGeneratorGeneralTab.addUIFormInput(nameFormStringInput);
    List<SelectItemOption<String>> listNodetype = getAllDocumentNodetypes();
    formGeneratorGeneralTab.addUIFormInput(new UIFormSelectBox(UIFormGeneratorConstant.NODETYPE_FORM_SELECTBOX, UIFormGeneratorConstant.NODETYPE_FORM_SELECTBOX, listNodetype));
    formGeneratorGeneralTab.addUIFormInput(new UIFormWYSIWYGInput(UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT, UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT, null));
    formGeneratorGeneralTab.addUIFormInput(new UIFormUploadInput(UIFormGeneratorConstant.ICON_FORM_UPLOAD_INPUT, UIFormGeneratorConstant.ICON_FORM_UPLOAD_INPUT));
    addUIFormInput(formGeneratorGeneralTab);
    
    addChild(UIFormGeneratorDnDTab.class, null, null);
    
    // Active this when working with 1.3 
//    UIFormInputSet formGeneratorOptionsTab = new UIFormInputSet(UIFormGeneratorConstant.FORM_GENERATOR_OPTIONS_TAB);
//    formGeneratorOptionsTab.addUIFormInput(new UIFormCheckBoxInput<String>(UIFormGeneratorConstant.VOTE_FORM_CHECKBOX_INPUT, UIFormGeneratorConstant.VOTE_FORM_CHECKBOX_INPUT, null));
//    formGeneratorOptionsTab.addUIFormInput(new UIFormCheckBoxInput<String>(UIFormGeneratorConstant.COMMENT_FORM_CHECKBOX_INPUT, UIFormGeneratorConstant.COMMENT_FORM_CHECKBOX_INPUT, null));
//    addUIFormInput(formGeneratorOptionsTab);

    setSelectedTab(formGeneratorGeneralTab.getId());
  }
  
  private List<SelectItemOption<String>> getAllDocumentNodetypes() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    String preferenceRepository = UIFormGeneratorUtils.getPreferenceRepository();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(preferenceRepository);
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration.getConfig(preferenceRepository);
    ThreadLocalSessionProviderService threadLocalSessionProviderService = getApplicationComponent(ThreadLocalSessionProviderService.class);
    Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(dmsRepoConfig.getSystemWorkspace(), manageableRepository);
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String templateBasePath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_TEMPLATES_PATH);
    Node templateBaseNode = (Node)session.getItem(templateBasePath);
    NodeIterator templateIter = templateBaseNode.getNodes();
    while(templateIter.hasNext()) {
      Node template = templateIter.nextNode();
      if (template.getProperty(TemplateService.DOCUMENT_TEMPLATE_PROP).getBoolean()) {
        options.add(new SelectItemOption<String>(template.getName()));
      }
    }
    return options;
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
    return "exo:fg_n_" + nodetypeName;
  }
  
  private String getPropertyName(String inputName) {
    return "exo:fg_p_" + inputName;
  }
  
  private void addNodetype(WebuiRequestContext requestContext, String repository, String nodetypeName, String supertypeName, List<UIFormGeneratorInputBean> formBeans) throws Exception {
    NodeTypeValue newNodeType = new NodeTypeValue() ;                             
    newNodeType.setName(nodetypeName) ;
    // TODO: Need update in 1.3
    newNodeType.setPrimaryItemName(null);
    // TODO: Need update in 1.3
    newNodeType.setMixin(false) ;
    // TODO: Need update in 1.3
    newNodeType.setOrderableChild(false) ;

    // TODO: Need update in 1.3, maybe support multi-supertypes
    List<String> supertypes = new ArrayList<String>();
    supertypes.add(supertypeName);
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
      Utils.createPopupMessage(this, requestContext, "UINodeTypeForm.msg.register-failed", ApplicationMessage.WARNING);
    }
  }
  
  private String generateDialogTemplate(List<UIFormGeneratorInputBean> forms) {
    StringBuilder dialogTemplate = new StringBuilder();
    dialogTemplate.append("<div class=\"UIForm FormLayout\">");
    dialogTemplate.append("  <% uiform.begin() %>");
    dialogTemplate.append("    <div class=\"HorizontalLayout\">");
    dialogTemplate.append("      <table class=\"UIFormGrid\">");
    dialogTemplate.append("        <tr>");
    dialogTemplate.append("          <td class=\"FieldLabel\"><%=_ctx.appRes(\"Article.dialog.label.name\")%></td>");
    dialogTemplate.append("          <td class=\"FieldComponent\">");
    dialogTemplate.append("            <%");
    // Active this when working with 1.3 
//    String mixintype = "";
//    if (isVotable && isCommentable)
//      mixintype += ",mix:votable,mix:commentable";
//    else if (!isVotable && isCommentable) 
//      mixintype += ",mix:commentable";
//    else if (!isVotable && isCommentable) 
//      mixintype += ",mix:votable";
//    dialogTemplate.append("              String[] fieldName = [\"jcrPath=/node\", \"mixintype=mix:i18n" + mixintype + "\", \"editable=if-null\", \"validate=empty,name\"] ;");
    dialogTemplate.append("              String[] fieldName = [\"jcrPath=/node\", \"mixintype=mix:i18n\", \"editable=if-null\", \"validate=empty,name\"] ;");
    dialogTemplate.append("              uicomponent.addTextField(\"name\", fieldName) ;");
    dialogTemplate.append("            %>");
    dialogTemplate.append("          </td>");
    dialogTemplate.append("        </tr>");
    for (UIFormGeneratorInputBean form : forms) {
      String inputName = form.getName();
      String inputType = form.getType();
      String inputFieldName = inputName + "FieldName";
      String validate = "";
      String inputField = "";
      if (form.isMandatory())
        validate += ",empty";
      if (UIFormGeneratorConstant.TEXTAREA.equals(inputType)) {
        inputField = "TextAreaField";
      } else if (UIFormGeneratorConstant.WYSIWYG.equals(inputType)) {
        inputField = "WYSIWYGField";
      } else if (UIFormGeneratorConstant.DATE.equals(inputType)) {
        inputField = "CalendarField";
        validate += ",datetime";
      } else if (UIFormGeneratorConstant.SELECT.equals(inputType)) {
        inputField = "SelectBoxField";
      } else {
        inputField = "TextField";
      }
      if (validate.startsWith(","))
        validate = validate.substring(1);
      String propertyName = getPropertyName(inputName);
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
        dialogTemplate.append("           String[] " + inputFieldName + " = [\"jcrPath=/node/" + propertyName + "\", \"defaultValues=" + form.getValue() + "\", \"validate=" + validate + "\", \"options=" + form.getAdvanced() + "\"];");
        dialogTemplate.append("           uicomponent.add" + inputField + "(\"" + inputFieldName + "\", " + inputFieldName + ");");
      }
      dialogTemplate.append("          %>");
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
    viewTemplate.append("   <table>");
    for (UIFormGeneratorInputBean form : forms) {
      viewTemplate.append("   <tr>");
      viewTemplate.append("     <td>");
      viewTemplate.append("       <%= currentNode.getProperty(\"" + getPropertyName(form.getName()) + "\")%>");
      viewTemplate.append("     </td>");
      viewTemplate.append("   </tr>");
    }
    viewTemplate.append("   </table>");
    viewTemplate.append(" </div>");
    return viewTemplate.toString();
  }
  
  public static class SaveActionListener extends EventListener<UIFormGeneratorTabPane> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIFormGeneratorTabPane> event) throws Exception {
      UIFormGeneratorTabPane formGeneratorTabPane = event.getSource();
      String jsonObjectGenerated = event.getRequestContext().getRequestParameter(OBJECTID) ;
      jsonObjectGenerated =  "{" +
                                "\"inputs\":" + 
                                "[" + 
                                 "{" + 
                                   "\"type\": \"Label\"," +
                                   "\"name\": \"label1\"," + 
                                   "\"value\": \"this is the label\"," +
                                   "\"advanced\": \"\"," +
                                   "\"guildline\": \"This is the label\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," + 
                                 "{" + 
                                   "\"type\": \"Input\"," + 
                                   "\"name\": \"input2\"," + 
                                   "\"value\": \"this is the input\"," +
                                   "\"advanced\": \"\"," +
                                   "\"guildline\": \"This is the input\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," + 
                                 "{" + 
                                   "\"type\": \"Select\"," + 
                                   "\"name\": \"select3\"," + 
                                   "\"value\": \"option2\"," +
                                   "\"advanced\": \"option1,option2,option3\"," +
                                   "\"guildline\": \"This is the select\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," + 
                                 "{" + 
                                   "\"type\": \"Checkbox\"," + 
                                   "\"name\": \"checkbox4\"," + 
                                   "\"value\": \"checkbox3\"," +
                                   "\"advanced\": \"checkbox1,checkbox2,checkbox3\"," +
                                   "\"guildline\": \"This is the checkbox\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," + 
                                 "{" + 
                                   "\"type\": \"Radio\"," + 
                                   "\"name\": \"radio5\"," + 
                                   "\"value\": \"radio1\"," +
                                   "\"advanced\": \"radio1,radio2,radio3\"," +
                                   "\"guildline\": \"This is teh radio\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," + 
                                 "{" + 
                                   "\"type\": \"Upload\"," + 
                                   "\"name\": \"upload6\"," + 
                                   "\"value\": \"\"," +
                                   "\"advanced\": \"\"," +
                                   "\"guildline\": \"This is the upload\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," + 
                                 "{" + 
                                   "\"type\": \"Textarea\"," + 
                                   "\"name\": \"textarea7\"," + 
                                   "\"value\": \"this is the textarea\"," +
                                   "\"advanced\": \"row:10,column:20\"," +
                                   "\"guildline\": \"This is the textarea\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," +
                                 "{" + 
                                   "\"type\": \"WYSIWYG\"," + 
                                   "\"name\": \"wysiwyg8\"," + 
                                   "\"value\": \"this is the WYSIWYG\"," +
                                   "\"advanced\": \"toolbar:CompleteWCM,width:100%,height:410px\"," +
                                   "\"guildline\": \"This is the WYSIWYG\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}," +
                                 "{" + 
                                   "\"type\": \"Date\"," + 
                                   "\"name\": \"date9\"," + 
                                   "\"value\": \"\"," +
                                   "\"advanced\": \"format=dd/mm/yyyy,isShowTime=true\"," +
                                   "\"guildline\": \"This is the date\"," + 
                                   "\"size\": 20," + 
                                   "\"mandatory\": true" + 
                                 "}" + 
                               "]" + 
                             "}";
      JsonHandler jsonHandler = new JsonDefaultHandler();
      new JsonParserImpl().parse(new InputStreamReader(new ByteArrayInputStream(jsonObjectGenerated.getBytes())), jsonHandler);
      JsonValue jsonValue = jsonHandler.getJsonObject();
      List<UIFormGeneratorInputBean> forms = ((UIFormGeneratorInputBean)new BeanBuilder().createObject(UIFormGeneratorInputBean.class, jsonValue)).getInputs();

      UIFormStringInput nameFormStringInput = formGeneratorTabPane.getUIStringInput(UIFormGeneratorConstant.NAME_FORM_STRING_INPUT);
      String templateName = nameFormStringInput.getValue();
      String nodetypeName = formGeneratorTabPane.getNodetypeName(templateName);
      
      String preferenceRepository = UIFormGeneratorUtils.getPreferenceRepository();
      
      UIFormSelectBox nodetypeFormSelectBox = formGeneratorTabPane.getUIFormSelectBox(UIFormGeneratorConstant.NODETYPE_FORM_SELECTBOX);
      String supertypeName = nodetypeFormSelectBox.getValue();
      formGeneratorTabPane.addNodetype(event.getRequestContext(), preferenceRepository, nodetypeName, supertypeName, forms);

      // Active this when working with 1.3
//      UIFormCheckBoxInput<String> voteFormCheckBoxInput = formGeneratorTabPane.getUIFormCheckBoxInput(UIFormGeneratorConstant.VOTE_FORM_CHECKBOX_INPUT);
//      UIFormCheckBoxInput<String> commentFormCheckBoxInput = formGeneratorTabPane.getUIFormCheckBoxInput(UIFormGeneratorConstant.COMMENT_FORM_CHECKBOX_INPUT);
//      boolean isVotable = voteFormCheckBoxInput.isChecked();
//      boolean isCommentable = commentFormCheckBoxInput.isChecked();
      
      // Active this when working with 1.3
//      String newGTMPLTemplate = formGeneratorTabPane.generateDialogTemplate(forms, isVotable, isCommentable);
      String newGTMPLTemplate = formGeneratorTabPane.generateDialogTemplate(forms);
      String newViewTemplate = formGeneratorTabPane.generateViewTemplate(forms);
      TemplateService templateService = formGeneratorTabPane.getApplicationComponent(TemplateService.class) ;
      templateService.addTemplate(true, nodetypeName, templateName, true, templateName, new String[] {"*"}, newGTMPLTemplate, preferenceRepository) ;
      templateService.addTemplate(false, nodetypeName, templateName, true, templateName, new String[] {"*"}, newViewTemplate, preferenceRepository) ;
    }
  }
  
  public static class ResetActionListener extends EventListener<UIFormGeneratorTabPane> {
    public void execute(Event<UIFormGeneratorTabPane> event) throws Exception {
      
    }
  }
  
}
