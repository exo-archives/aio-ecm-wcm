import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectItemCategory;

List categories = new ArrayList(); 

SelectItemCategory appPageConfigs = new SelectItemCategory("WCM Page Configs") ;
categories.add(appPageConfigs) ;
appPageConfigs.addSelectItemOption(new SelectItemOption("SingleContentViewer", "SingleContentViewer", "SingleContentViewer")) ;
appPageConfigs.addSelectItemOption(new SelectItemOption("ContentListViewer", "ContentListViewer", "ContentListViewer")) ;
appPageConfigs.addSelectItemOption(new SelectItemOption("Search", "Search", "Search")) ;
appPageConfigs.addSelectItemOption(new SelectItemOption("Sitemap", "Sitemap", "Sitemap")) ;

SelectItemCategory columnPageConfigs = new SelectItemCategory("Column Page Configs") ;
categories.add(columnPageConfigs);  
columnPageConfigs.addSelectItemOption(new SelectItemOption("TwoColumnsLayout", "TwoColumnsLayout", "TwoColumnsLayout"));
columnPageConfigs.addSelectItemOption(new SelectItemOption("ThreeColumnsLayout", "ThreeColumnsLayout", "ThreeColumnsLayout"));

SelectItemCategory rowPageConfigs = new SelectItemCategory("Row Page Configs") ;
categories.add(rowPageConfigs); 
rowPageConfigs.addSelectItemOption(new SelectItemOption("TwoRowsLayout", "TwoRowsLayout", "TwoRowsLayout"));
rowPageConfigs.addSelectItemOption(new SelectItemOption("ThreeRowsLayout", "ThreeRowsLayout", "ThreeRowsLayout"));

SelectItemCategory tabsPageConfigs = new SelectItemCategory("Tabs Page Configs") ;
categories.add(tabsPageConfigs) ;
tabsPageConfigs.addSelectItemOption(new SelectItemOption("TwoTabsLayout", "TwoTabsLayout", "TwoTabsLayout")) ;
tabsPageConfigs.addSelectItemOption(new SelectItemOption("ThreeTabsLayout", "ThreeTabsLayout", "ThreeTabsLayout")) ;

SelectItemCategory mixPageConfigs = new SelectItemCategory("Mix Page Configs") ;
categories.add(mixPageConfigs); 
mixPageConfigs.addSelectItemOption(new SelectItemOption("TwoColumnsOneRowLayout", "TwoColumnsOneRowLayout", "TwoColumnsOneRowLayout"));
mixPageConfigs.addSelectItemOption(new SelectItemOption("OneRowTwoColumnsLayout", "OneRowTwoColumnsLayout", "OneRowTwoColumnsLayout"));
mixPageConfigs.addSelectItemOption(new SelectItemOption("ThreeRowsTwoColumnsLayout", "ThreeRowsTwoColumnsLayout", "ThreeRowsTwoColumnsLayout"));
return categories;