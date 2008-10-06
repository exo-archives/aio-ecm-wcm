import java.util.List;
import java.util.ArrayList;
import org.exoplatform.portal.webui.portal.PortalTemplateConfigOption ;
import org.exoplatform.webui.core.model.SelectItemCategory;

List options = new ArrayList();

  SelectItemCategory classic = new SelectItemCategory("ClassicSite");
  classic.addSelectItemOption(
      new PortalTemplateConfigOption("", "classic", "Classic Site", "ClassicSite").addGroup("/platform/administrators")
  );
  
  SelectItemCategory acme = new SelectItemCategory("ACMESite");
  acme.addSelectItemOption(
      new PortalTemplateConfigOption("ACME Site", "acme", "ACME Site", "ACMESite").addGroup("/platform/administrators")
  );
  options.add(classic);
  options.add(acme);
  
return options ;
