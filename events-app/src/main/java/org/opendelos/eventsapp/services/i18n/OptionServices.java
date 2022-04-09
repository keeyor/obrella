/* 
     Author: Michael Gatzonis - 1/16/2019 
     OpenDelosDAC
*/
package org.opendelos.eventsapp.services.i18n;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class OptionServices  {

    private final MultilingualServices multilingualServices;

    public OptionServices(MultilingualServices multilingualServices) {
        this.multilingualServices = multilingualServices;
    }

    public LinkedHashMap<String, List<String>> getSortedCategories(Locale locale) {

        LinkedHashMap<String, List<String>> allCategories = new LinkedHashMap<>();
        String mainCategoriesKeys = multilingualServices.getValue("Categories.keys",null,locale);
        String[] mainCategoriesKeysArray = mainCategoriesKeys.split(",");

        for (String mainCategoryCode : mainCategoriesKeysArray) {
            String mainCategoryName = multilingualServices.getValue(mainCategoryCode, null, locale);
            String subCategoryCodes = multilingualServices.getValue("category." + mainCategoryCode + ".sub", null, locale);
            String[] subCategoriesCodesArray = subCategoryCodes.split(",");
            
            List<String> sub_categories = new ArrayList<>();
            for (String subCategoryCode : subCategoriesCodesArray) {
                String subCategoryName = multilingualServices.getValue(subCategoryCode, null, locale);
                sub_categories.add(subCategoryCode + ":" + subCategoryName);
            }
            allCategories.put(mainCategoryCode + ":" + mainCategoryName,sub_categories);
        }
        return allCategories;
    }

    public String[] getLanguages(Locale locale) {
        String languageKeys = multilingualServices.getValue("Language.keys",null,locale);
        return languageKeys.split(",");
    }
    public String[] getLicenses(Locale locale) {
        String licenseKeys = multilingualServices.getValue("License.keys",null,locale);
        return licenseKeys.split(",");
    }
    public String[] getEventTypes(Locale locale) {
        String eventTypesKeys = multilingualServices.getValue("EventType.keys",null,locale);
        return eventTypesKeys.split(",");
    }

    public String[] getSubAreasOfAreaByKey(String area_key,Locale locale) {

        String subAreaChildrenKeys = multilingualServices.getValue("Event.subarea." + area_key + ".keys",null,locale);
        return subAreaChildrenKeys.split(",");
    }

    public LinkedHashMap<String, List<String>> getSortedThematics(Locale locale) {

        LinkedHashMap<String, List<String>> allCategories = new LinkedHashMap<>();
        String mainCategoriesKeys = multilingualServices.getValue("Event.tu.keys",null,locale);
        String[] mainCategoriesKeysArray = mainCategoriesKeys.split(",");

        for (String mainCategoryCode : mainCategoriesKeysArray) {
            String mainCategoryName = multilingualServices.getValue(mainCategoryCode, null, locale);
            String subCategoryCodes = multilingualServices.getValue("Event.tus." + mainCategoryCode + ".keys", null, locale);
            String[] subCategoriesCodesArray = subCategoryCodes.split(",");

            List<String> sub_categories = new ArrayList<>();
            for (String subCategoryCode : subCategoriesCodesArray) {
                String subCategoryName = multilingualServices.getValue(subCategoryCode, null, locale);
                sub_categories.add(subCategoryCode + ":" + subCategoryName);
            }
            allCategories.put(mainCategoryCode + ":" + mainCategoryName,sub_categories);
        }
        return allCategories;
    }

}
