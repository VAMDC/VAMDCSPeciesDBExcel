package com.vamdc.speciesDB;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.excel.writer.ExcelCreator;
import com.excel.writer.ExcelFileWriter;

public class SpeciesDBToExcelConverter {

	public SpeciesDBToExcelConverter(String serviceUrl) {
		super();
		this.serviceUrl = serviceUrl;
	}

	private List<String> columnsNames;
	private Map<String, String> columnsTypes;
	private Map<String, List<String>> columnsDataContent;

	private ExcelCreator excelCreator;

	private String serviceUrl;

	private void buildInternalObjectModel() {
		this.columnsNames = new ArrayList<String>();
		columnsNames.add("Node identifier"); //0
		columnsNames.add("InChIKey"); //1
		columnsNames.add("name"); //2
		columnsNames.add("massNumber"); //3
		columnsNames.add("did"); //4
		columnsNames.add("stoichiometricFormula"); //5
		columnsNames.add("speciesType"); //6
		columnsNames.add("charge"); //7
		columnsNames.add("InChI"); //8
		columnsNames.add("formula"); //9

		this.columnsTypes = new HashMap<String, String>();
		columnsTypes.put(columnsNames.get(0), "String");
		columnsTypes.put(columnsNames.get(1), "String");
		columnsTypes.put(columnsNames.get(2), "String");
		columnsTypes.put(columnsNames.get(3), "number");
		columnsTypes.put(columnsNames.get(4), "String");
		columnsTypes.put(columnsNames.get(5), "String");
		columnsTypes.put(columnsNames.get(6), "String");
		columnsTypes.put(columnsNames.get(7), "number");
		columnsTypes.put(columnsNames.get(8), "String");
		columnsTypes.put(columnsNames.get(9), "String");

		this.columnsDataContent = new HashMap<String, List<String>>();
		for (int i = 0; i < columnsNames.size(); i++) {
			List<String> currentList = new ArrayList<String>();
			columnsDataContent.put(columnsNames.get(i), currentList);
		}
	}

	private void getInformationFromService() throws IOException {
		URL url = new URL(serviceUrl);
		try (InputStream is = url.openStream();
				JsonReader rdr = Json.createReader(is)) {

			JsonObject obj = rdr.readObject();
			for (Entry<String, JsonValue> e : obj.entrySet()) {
				String nodeId = e.getKey();
				JsonArray array = (JsonArray) e.getValue();
				for (int i = 0; i < array.size(); i++) {
					columnsDataContent.get(columnsNames.get(0)).add(nodeId);

					for (int k = 1; k < columnsNames.size(); k++) {
						String fieldName = columnsNames.get(k);
						String fieldValue;
						try {
							String fieldType = columnsTypes.get(fieldName);
							if(fieldType.equalsIgnoreCase("number")){
								fieldValue = array.getJsonObject(i)
										.getJsonNumber(fieldName).toString();
							}else{
								fieldValue = array.getJsonObject(i)
										.getJsonString(fieldName).toString();
								fieldValue = fieldValue.replace("\"", "");
							}
						} catch (ClassCastException castException) {
							fieldValue = "";
						}
						columnsDataContent.get(fieldName).add(fieldValue);
					}
				}
			}
		}
	}

	private void convertIntoExcel() {
		excelCreator = new ExcelCreator("speciesList", columnsNames,
				columnsTypes, columnsDataContent, false);
	}
	
	
	private void performConversion() throws IOException{
		this.buildInternalObjectModel();
		this.getInformationFromService();
		this.convertIntoExcel();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
		Date date = new Date();
		String currentDate = dateFormat.format(date);
		new ExcelFileWriter(excelCreator, "VAMDC_SpeciesDBContent_on"+currentDate+".xls").writeFile();;
	}

	public static void main(String[] args) throws IOException {
		String url = args[0];
		SpeciesDBToExcelConverter converter = new SpeciesDBToExcelConverter(url);
		converter.performConversion();
	}

}
