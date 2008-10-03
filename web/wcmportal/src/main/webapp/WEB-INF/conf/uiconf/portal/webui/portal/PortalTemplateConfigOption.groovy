import java.util.List;
import java.util.ArrayList;
import org.exoplatform.portal.webui.portal.PortalTemplateConfigOption ;
import org.exoplatform.webui.core.model.SelectItemCategory;

List options = new ArrayList();

  SelectItemCategory guest = new SelectItemCategory("ClassicPortal");
  guest.addSelectItemOption(
      new PortalTemplateConfigOption("", "classic", "Classic Portal", "ClassicPortal").addGroup("/platform/administrators")
  );
  guest.addSelectItemOption(
      new PortalTemplateConfigOption("", "acme", "ACME Site", "ACMESite").addGroup("/platform/administrators")
  );
  options.add(guest);
  
return options ;
