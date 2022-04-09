/* 
     Author: Michael Gatzonis - 9/11/2020 
     live
*/
package org.opendelos.control.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.i18n.OptionServices;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.common.Select2GenGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OptionsApi {

	private final OptionServices optionServices;
	private final MultilingualServices multilingualServices;

	@Autowired
	public OptionsApi(OptionServices optionServices, MultilingualServices multilingualServices) {
		this.optionServices = optionServices;
		this.multilingualServices = multilingualServices;
	}

	@RequestMapping(value= "/api/v1/s2/categories.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getCategories(Locale locale) {

		LinkedHashMap<String, List<String>> lhm = optionServices.getSortedCategories(locale);
		try {
			String s2categories = ApiUtils.FormatResultsForSelect2(lhm);
			return new ResponseEntity<>(s2categories, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v1/s2/areas.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getEventAreas(Locale locale) {

		String[] event_areas = optionServices.getEventAreas(locale);
		List<Select2GenChild> groupChildren = new ArrayList<>();
		for (String area: event_areas) {
			Select2GenChild select2GenChild = new Select2GenChild();
			String area_name = multilingualServices.getValue(area, null, locale);
			select2GenChild.setId(area);
			select2GenChild.setText(area_name);
			groupChildren.add(select2GenChild );
		}
		try {
			String s2areas = ApiUtils.FormatResultsForSelect2(groupChildren);
			return new ResponseEntity<>(s2areas, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
	@RequestMapping(value= "/api/v1/s2/eventarea.web/{area}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getEventTypesByArea(@PathVariable String area, Locale locale) {

		String[] event_types = optionServices.getEventTypesByArea(locale,area);
		List<Select2GenChild> groupChildren = new ArrayList<>();
		for (String type: event_types) {
			Select2GenChild select2GenChild = new Select2GenChild();
			String type_name = multilingualServices.getValue(type, null, locale);
			select2GenChild.setId(type);
			select2GenChild.setText(type_name);
			groupChildren.add(select2GenChild );
		}
		try {
			String s2types = ApiUtils.FormatResultsForSelect2(groupChildren);
			return new ResponseEntity<>(s2types, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v1/s2/categoriesAlt.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getSortedCategoriesAlt(Locale locale) {

		String mainCategoriesKeys = multilingualServices.getValue("Categories.keys",null,locale);
		String[] mainCategoriesKeysArray = mainCategoriesKeys.split(",");

		List<Select2GenGroup> select2GenGroupList = new ArrayList<>();
		for (String mainCategoryCode : mainCategoriesKeysArray) {
			String mainCategoryName = multilingualServices.getValue(mainCategoryCode, null, locale);
			//set group properties
			Select2GenGroup select2GenGroup = new Select2GenGroup();
			select2GenGroup.setId(mainCategoryCode);
			select2GenGroup.setText(mainCategoryName);
			//set children properties
			String subCategoryCodes = multilingualServices.getValue("category." + mainCategoryCode + ".sub", null, locale);
			String[] subCategoriesCodesArray = subCategoryCodes.split(",");
			List<Select2GenChild> groupChildren = new ArrayList<>();
			for (String subCategoryCode : subCategoriesCodesArray) {
				Select2GenChild select2GenChild = new Select2GenChild();
				String subCategoryName = multilingualServices.getValue(subCategoryCode, null, locale);
				select2GenChild.setId(subCategoryCode);
				select2GenChild.setText(subCategoryName);
				groupChildren.add(select2GenChild);
			}
			select2GenGroup.setChildren(groupChildren);
			select2GenGroupList.add(select2GenGroup);
		}
		try {
			select2GenGroupList.sort(new TitleSorter());
			String s2categories = ApiUtils.FormatResultsForSelect2(select2GenGroupList);
			return new ResponseEntity<>(s2categories, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	public class TitleSorter implements Comparator<Select2GenGroup>
	{
		@Override
		public int compare(Select2GenGroup o1, Select2GenGroup o2) {
			return o1.getText().compareToIgnoreCase(o2.getText());
		}
	}

}
