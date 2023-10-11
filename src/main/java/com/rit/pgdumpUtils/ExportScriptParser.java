package com.rit.pgdumpUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ExportScriptParser {

	private final ArrayList<String> seqList = new ArrayList<>();
	private final ArrayList<String> partList = new ArrayList<>();
	private final boolean withPart;
	private final String inFilePath;
	private final String inFile;
	private	final String outFilePath;
	private	final String substitutes;
	private	final String excludes;

	static final String sequences = "sequences";
	static final String tables = "tables";
	static final String foreign_tables = "foreign_tables";
	static final String table_data = "table_data";
	static final String views = "views";
	static final String mviews = "mviews";
	static final String types = "types";
	static final String domains = "domains";
	static final String functions = "functions";
	static final String procedures = "procedures";
	static final String addtables = "addtables";
	static final String addforeigntables = "addforeigntables";

	static final String servers = "servers";
	static final String policies = "policies";
	static final String event_triggers = "event_triggers";

	public ExportScriptParser(String inFilePath, String inFile, String outFilePath, boolean withPart,
							  String substitutes, String excludes) {
		this.inFile = inFile;
		this.inFilePath = inFilePath;
		this.outFilePath = outFilePath;
		this.withPart = withPart;
		this.substitutes = substitutes;
		this.excludes = excludes;
	}

	private String parseWithSubstitutes(String s) {
		if (substitutes == null) return s;
		if (substitutes.trim().equals("")) return s;
		String newS = s;
		for (String subs: substitutes.split(",")) {
			if (subs.split("=").length == 2) {
				String sFrom = subs.split("=")[0];
				String sTo = subs.split("=")[1];
				newS = newS.replace(sFrom, sTo);
			}
		}
		return newS;
	}

    private void saveObj(String path, String objName, String objType, ArrayList<String> obj) {
		try {
			System.out.println("saving object " + objName + " (" + objType + ") to " + outFilePath + path);
			String ls = System.lineSeparator();
			if (objName.trim().equals("")) return;
			File f0 = new File(outFilePath + path);
			if (!f0.exists()) {
				f0.mkdir();
			}
			String objTypeR = objType;
			if (objType.startsWith(addtables)) {
				objTypeR = tables;
			}
			if (objType.startsWith(addforeigntables)) {
				objTypeR = foreign_tables;
			}

			String fileExtension = ".sql";
			/*if (objType.equals(views)) {
				fileExtension = ".vw";
			}
			if (objType.equals(functions)) {
				fileExtension = ".fnc";
			}
			if (objType.equals(procedures)) {
				fileExtension = ".prc";
			}*/

			if (!objTypeR.equals("")) {
				File f1 = new File(outFilePath + path + "/" + objTypeR);
				if (!f1.exists()) {
					f1.mkdir();
				}
				objTypeR = objTypeR + "/";
			}

			String fileName = outFilePath + path + "/" + objTypeR + objName + fileExtension;
			System.out.println("path is " + fileName);

			boolean append = objType.startsWith(addtables) || objType.startsWith(addforeigntables);
			if (objType.equals(functions) || objType.equals(procedures)) {
				int counter = 2;
				File fileTest = new File(fileName);
				while (fileTest.exists()) {
					fileName = outFilePath + path + "/" + objTypeR + objName + "_ovl" + counter + fileExtension;
					fileTest = new File(fileName);
					counter++;
				}
				fileTest.createNewFile();
			}
			FileOutputStream outFile = new FileOutputStream(fileName, append);
			BufferedWriter writer =	new BufferedWriter(new OutputStreamWriter(outFile, StandardCharsets.UTF_8));

			String fullObj = "";
			for (String s : obj)
				fullObj += s + ls;
			fullObj = fullObj.replace(";" + ls + ls + ls + ls + "ALTER",";" + ls + ls + "ALTER");
			fullObj = fullObj.replace(";" + ls + ls + ls + ls + "COMMENT",";" + ls + ls + "COMMENT");
			fullObj = fullObj.replace(";" + ls + ls + ls + "ALTER",";" + ls + ls + "ALTER");
			fullObj = fullObj.replace(";" + ls + ls + ls + "COMMENT",";" + ls + ls + "COMMENT");
			if (fullObj.endsWith(ls)) fullObj = fullObj.substring(0,fullObj.length()-2);
			if ((objType.startsWith(addtables)) || (objType.startsWith(addforeigntables))) {
				writer.write(System.lineSeparator());
			}
			for (String str : fullObj.split(ls)) {
				String updStr = parseWithSubstitutes(str);
				writer.write(updStr + System.lineSeparator());
			}
			writer.close();
		} catch (Exception e) {
			System.out.println(outFilePath + path + "/" +	objName.replace("\"", "")+".sql save failed: " + e.getMessage());
		}
	}

	private String getObjectType(String line) {
		if (line.startsWith("CREATE TABLE ")) {
			return tables;
		}
		if (line.startsWith("CREATE FOREIGN TABLE ")) {
			return foreign_tables;
		}
		if (line.startsWith("CREATE SEQUENCE ")) {
			seqList.add(line);
			return sequences;
		}
		if (line.startsWith("ALTER TABLE ") && line.contains(" ATTACH PARTITION ") && !withPart) {
			partList.add(line);
			return "skip";
		}
		if (line.startsWith("ALTER TABLE ") && line.contains(" OWNER TO ") && !withPart) {
			String[] arrLine = line.split(" ");
			if (arrLine.length > 2) {
				for (String s: partList) {
					if (s.contains(arrLine[2])) {
						return "skip";
					}
				}
			}
		}
		if (line.toUpperCase().matches("GRANT .* ON .* .* TO .*;")) return "skip";
		if (line.toUpperCase().matches("REVOKE .* ON .* .* FROM .*;")) return "skip";
		// sequence via alter table
		if (line.startsWith("ALTER TABLE ")) {
			String[] arrLine = line.split(" ");
			if (arrLine.length > 2 && seqList.contains("CREATE SEQUENCE " + arrLine[2])) {
				return "";
			}
		}
		if (line.startsWith("CREATE INDEX ") || line.startsWith("CREATE UNIQUE INDEX ")) return "index";
		if (line.startsWith("CREATE TRIGGER ")) return "trigger";
		if (line.startsWith("CREATE VIEW ")) return views;
		if (line.startsWith("CREATE MATERIALIZED VIEW ")) return mviews;
		if (line.startsWith("CREATE TYPE ")) return types;
		if (line.startsWith("CREATE DOMAIN ")) return domains;
		if (line.startsWith("CREATE FUNCTION ")) return functions;
		if (line.startsWith("CREATE PROCEDURE ")) return procedures;
		if (line.startsWith("ALTER TABLE ONLY") && (!line.contains(" OWNER TO "))) return addtables + "2";
		if (line.startsWith("ALTER TABLE ") && (!line.contains(" OWNER TO "))) return addtables + "1";
		if (line.startsWith("ALTER FOREIGN TABLE ONLY") && (!line.contains(" OWNER TO "))) return addforeigntables + "2";
		if (line.startsWith("ALTER FOREIGN TABLE ") && (!line.contains(" OWNER TO "))) return addforeigntables + "1";
		if (line.startsWith("CREATE SERVER ")) return servers;
		if (line.startsWith("CREATE POLICY ")) return policies;
		if (line.startsWith("CREATE EVENT TRIGGER ")) return event_triggers;
		if (line.startsWith("COPY ")) return table_data;
		return ""; // this is not new object line
	}

	private String extractObjectPart(String what, String objT, String line) {
		//System.out.println("extractObjectPart " + line);
		int iWordCnt = 0;
		if (
			objT.equals(table_data)
		) iWordCnt = 1;
		if (
			objT.equals(tables) ||
			objT.equals(sequences) ||
			objT.equals(views) ||
			objT.equals(types) ||
			objT.equals(domains) ||
			objT.equals(functions) ||
			objT.equals(procedures) ||
			objT.equals(addtables + "1") ||
			objT.equals(servers) ||
			objT.equals(policies)
		) iWordCnt = 2;

		if (
			objT.equals(addtables + "2") ||
			objT.equals(addforeigntables + "1") ||
			objT.equals(event_triggers) ||
			objT.equals(foreign_tables) ||
			objT.equals(mviews)
		) iWordCnt = 3;

		if (
			objT.equals(addforeigntables + "2")
		) iWordCnt = 4;

		if (iWordCnt > 0) {
			String[] arrLine = line.split(" ");
			if (arrLine.length > iWordCnt) {
				String[] parsedLine = arrLine[iWordCnt].split("\\.");
				if (parsedLine.length > 0) {
					if (what.equals("schema")) {
						if (!arrLine[iWordCnt].contains(".")) return "_default";
						return parsedLine[0];
					}
					if (what.equals("name")) {
						if (!arrLine[iWordCnt].contains(".")) {
							if (line.toUpperCase().startsWith("CREATE SERVER")) return arrLine[iWordCnt];
							if (line.toUpperCase().startsWith("CREATE POLICY")) return arrLine[iWordCnt];
							if (line.toUpperCase().startsWith("CREATE EVENT TRIGGER")) return arrLine[iWordCnt];
							return "";
						}
						String s = parsedLine[1].replace("\"", "");
						if (s.contains("(")) {
							s = s.substring(0, s.indexOf("("));
						}
						return s;
					}
				}
			}
		}
		return "";
	}

	private boolean excludeLine(String s) {
		if (excludes == null) return false;
		if (excludes.trim().equals("")) return false;
		for (String excl: excludes.split(",")) {
			if ("SEQ_COL_OWN".equals(excl)) {
				if (s.toUpperCase().matches("ALTER SEQUENCE .*\\..* OWNED BY .*\\..*\\..*")) return true;
			}
			if ("ALTER_OWN".equals(excl)) {
				//if (s.toUpperCase().matches("ALTER .*")) {
				if (s.toUpperCase().matches("ALTER .* OWNER TO .*")) {
					System.out.println(excl);
					return true;
				}
			}
		}
		return false;
	}

	public void parseExportFile() throws IOException {
		System.out.println("Opening file: " + inFilePath + inFile);
		File fileInp = new File(inFilePath + inFile);

        BufferedReader inputFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileInp), StandardCharsets.UTF_8));
		ArrayList<String> arrObj = new ArrayList<>();
		boolean bObjBegun = false;
		String schema = "";
		String objName = "";
		String objectType = "";
		String begunObjectType = "";
		boolean twoMinus = false;
		for(String line; (line = inputFileReader.readLine()) != null; ) {
			objectType = getObjectType(line);

			if (objectType.equals("skip")) continue;

			if (excludeLine(line)) continue;

			if (objectType.equals("index")) {
				IndexParsedData indexParsedData = parseIndex(line);
				// TODO: подумать, есть ли индексы на FOREIGN TABLE
				saveObj(indexParsedData.getSchema(), indexParsedData.getObjectName(), addtables + "3", indexParsedData.getObjectArray());
				continue;
			}

			if (objectType.equals("trigger")) {
				TriggerParsedData triggerParsedData = parseTrigger(line);
				saveObj(triggerParsedData.getSchema(), triggerParsedData.getObjectName(), addtables + "4", triggerParsedData.getObjectArray());
				continue;
			}

			if (!objectType.equals("")) {
				if (arrObj.size() > 0) {
					if (!begunObjectType.equals("")) {
						saveObj(schema, objName, begunObjectType, arrObj);
					} else {
						saveObj(schema, objName, objectType, arrObj);
					}
				}

				arrObj.clear();
				bObjBegun = false;
				schema = extractObjectPart("schema",objectType, line);
				objName = extractObjectPart("name", objectType, line);
				begunObjectType = "";
			}

        	if (
				objectType.equals(foreign_tables) ||
				objectType.equals(tables) ||
				objectType.equals(sequences) ||
				objectType.equals(views) ||
				objectType.equals(mviews) ||
				objectType.equals(types) ||
				objectType.equals(functions) ||
				objectType.equals(procedures) ||
				objectType.startsWith(addtables) ||
				objectType.startsWith(addforeigntables) ||
				objectType.equals(servers) ||
				objectType.equals(policies) ||
				objectType.equals(event_triggers) ||
				objectType.equals(table_data)
			) {
        		bObjBegun = true;
				begunObjectType = objectType;
			}

			boolean twoMinusAndSpace = line.trim().equals("") && (twoMinus);
        	twoMinus = line.trim().equals("--");
        	if (bObjBegun &&
			    !twoMinusAndSpace &&
			    !line.trim().equals("--") &&
				!line.startsWith("-- TOC") &&
				!line.startsWith("-- Name:") &&
				!line.startsWith("-- Completed on ") &&
				!line.startsWith("-- PostgreSQL database dump complete") &&
				!line.startsWith("-- Dependencies:")
			) {
        		if (line.startsWith("CREATE VIEW")) {
					arrObj.add(line.replace("CREATE VIEW", "create or replace view").replace(" AS", ""));
					arrObj.add("as");
				} else if (line.startsWith("CREATE FUNCTION")) {
					arrObj.add(line
							.replace("CREATE FUNCTION", "create or replace function")
							.replace(" RETURNS ", " returns ")
							.replace(" TABLE(", " table("));
				} else if (line.startsWith("CREATE PROCEDURE")) {
					arrObj.add(line.replace("CREATE PROCEDURE", "create or replace procedure"));
				} else if (line.startsWith("    LANGUAGE plpgsql")) {
					arrObj.add("    language plpgsql");
				} else if (line.equals("    AS $function$")) {
					arrObj.add("as $function$");
				} else if (line.equals("    AS $procedure$")) {
					arrObj.add("as $procedure$");
				} else if (line.equals("    AS $$")) {
					arrObj.add("as $$");
				} else if (line.equals("    AS $_$")) {
					arrObj.add("as $_$");
				} else {
					arrObj.add(line);
				}
			}

        }
		if (!arrObj.isEmpty()) {
			if (!begunObjectType.equals("")) {
				saveObj(schema, objName, begunObjectType, arrObj);
			} else {
				saveObj(schema, objName, objectType, arrObj);
			}
		}
	}

	private IndexParsedData parseIndex(String line) {
		IndexParsedData result = new IndexParsedData();

		String linePart = line.substring(line.indexOf(" ON ") + 4);
		if (linePart.startsWith("ONLY ")) {
			linePart = linePart.substring(5);
		}
		linePart = linePart.substring(0, linePart.indexOf(' '));
		String schema = linePart.substring(0, linePart.indexOf('.'));
		String objectName = linePart.replace(schema + ".", "");

		result.AddObjectLine(line);
		result.setSchema(schema);
		result.setObjectName(objectName);
		return result;
	}

	private TriggerParsedData parseTrigger(String line) {
		TriggerParsedData result = new TriggerParsedData();

		String linePart = line.substring(line.indexOf(" ON ") + 4);

		linePart = linePart.substring(0, linePart.indexOf(' '));
		String schema = linePart.substring(0, linePart.indexOf('.'));
		String objectName = linePart.replace(schema + ".", "");

		result.AddObjectLine(line);
		result.setSchema(schema);
		result.setObjectName(objectName);
		return result;
	}

}
