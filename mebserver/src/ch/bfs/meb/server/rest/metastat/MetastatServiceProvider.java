/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.server.rest.metastat;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.configuration.IMebCommonServerConfiguration;

public class MetastatServiceProvider implements IMetastatServiceProvider {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(MetastatServiceProvider.class);

    private IMebCommonServerConfiguration _configuration;

    public void setConfiguration(IMebCommonServerConfiguration configuration) {
        this._configuration = configuration;
    }

    @Override
    public List<CodeGroup> getCodesFor(String codeGroupId, Long canton) {
        return new ArrayList<CodeGroup>(); // Dummy implementation: is Metastat still used by meb?
    }

    //	@Override
    //	public List<CodeGroup> getCodesFor(String codeGroupId, Long canton) {
    //		List<CodeGroup> codegroups = new ArrayList<CodeGroup>();
    //
    //		// codegroup mapping
    //		String codeList = getCodeListForCodeGroup(codeGroupId);
    //
    //		codeList = addCantonToCodeList(codeList, canton);
    //
    //		// determine current version
    //		List<String> currentCodeListVersions = getCurrentCodeListVersionsForCodeList(codeList);
    //
    //		// get data for each version
    //		for (String currentCodeListVersion : currentCodeListVersions) {
    //			// get xml document as xmlbeans
    //			StructureDocument document = getDocumentForCodeList(codeList, currentCodeListVersion);
    //
    //			codegroups.addAll(getCodegroupFromXml(codeGroupId, document, canton));
    //		}
    //
    //		return codegroups;
    //	}
    //
    //	private String addCantonToCodeList(String codeList, Long canton) {
    //		if (canton != null) {
    //			String cantonString = canton.toString();
    //			return codeList + "_KT" + (cantonString.length() == 2 ? cantonString : "0" + cantonString);
    //		}
    //		return codeList;
    //	}
    //
    //	private String getCodeListForCodeGroup(String codeGroup) {
    //		if (CodegroupUtility.CANTON.equals(codeGroup))
    //			return "CL_HCL_HGDE_CL_CANTONS";
    //		else if (CodegroupUtility.SEX.equals(codeGroup))
    //			return "CL_BINO_SEX";
    //		else if (CodegroupUtility.LANGUAGE.equals(codeGroup))
    //			return "CL_B_LANG";
    //		else if (CodegroupUtility.MUNICIPALITY.equals(codeGroup))
    //			return "CL_HCL_HGDE_CL_MUNICIPALITIES";
    //		else if (CodegroupUtility.COUNTRY.equals(codeGroup))
    //			return "CL_HCL_STAG_COUNTRY";
    //		else if (CodegroupUtility.TEACH_PLAN_STATUS.equals(codeGroup))
    //			return "CL_TEACH_STATUS";
    //		else if (CodegroupUtility.PROF_MATURA.equals(codeGroup))
    //			return "CL_TEACH_MP1";
    //		else if (CodegroupUtility.EDUCATION_TYPE.equals(codeGroup))
    //			return "CL_TEACH_FORM";
    //		else if (CodegroupUtility.SCHOOL_TYPE.equals(codeGroup))
    //			return "CL_SArt_1";
    //		else if (CodegroupUtility.SCHOOL_DEP_TYPE.equals(codeGroup))
    //			return "CL_SAAbt_1";
    //		else if (CodegroupUtility.PERS_CATEGORY.equals(codeGroup))
    //			return "CL_CAT_PERS";
    //		else if (CodegroupUtility.TYPE_CONTRACT.equals(codeGroup))
    //			return "CL_TYPE_CONTRACT";
    //		else if (CodegroupUtility.QUALIFICATION.equals(codeGroup))
    //			return "CL_QUALIF";
    //		// TODO METASTAT: implement with correct metastat name mapping
    //		// else if (CodegroupUtility.BILD_ART.equals(codeGroup))
    //		// return "";
    //		// else if (CodegroupUtility.EXAM_TYPE.equals(codeGroup))
    //		// return "";
    //		// else if (CodegroupUtility.EXAM_RESULT.equals(codeGroup))
    //		// return "";
    //
    //		return null;
    //	}
    //
    //	private List<String> getCurrentCodeListVersionsForCodeList(String codeList) {
    //		URL url = getMetastatURL(codeList);
    //
    //		StructureDocument document = getDocumentFromURL(url);
    //		CodeListsType codeListsType = document.getStructure().getCodeLists();
    //		if (codeListsType == null)
    //			throw new MetastatServiceException(
    //					"Couldn't determine 'CodeLists' in metastat document from URL '" + url.toExternalForm() + "'");
    //
    //		List<String> currentCodeVersionLists = new ArrayList<String>();
    //
    //		Date oldValidFrom = null;
    //		String oldCurrentCodeVersion = null;
    //
    //		CodeListType2[] codeLists = codeListsType.getCodeListArray();
    //		for (CodeListType2 codeListType2 : codeLists) {
    //			// TODO implement correct valid from/to handling
    //			/*
    //			 * implemented draft for first tests => the code list with the most
    //			 * current "validFrom" is used
    //			 */
    //			String id = codeListType2.getId();
    //			if (id != null && id.trim().length() > 0) {
    //				if (codeListType2.isSetValidFrom()) {
    //					Date validFrom = ((XmlCalendar) codeLists[0].getValidFrom()).getTime();
    //					if (oldValidFrom == null || validFrom.after(oldValidFrom)) {
    //						oldValidFrom = validFrom;
    //						oldCurrentCodeVersion = id;
    //					}
    //				} else {
    //					// use the first code list if no valid from is given
    //					oldCurrentCodeVersion = id;
    //					break;
    //				}
    //			}
    //		}
    //
    //		if (oldCurrentCodeVersion != null)
    //			currentCodeVersionLists.add(oldCurrentCodeVersion);
    //
    //		return currentCodeVersionLists;
    //	}
    //
    //	private StructureDocument getDocumentForCodeList(String codeList, String codeListVersion) {
    //		URL url = getMetastatURL(codeList, codeListVersion);
    //		return getDocumentFromURL(url);
    //	}
    //
    //	private List<CodeGroup> getCodegroupFromXml(String codeGroupId, StructureDocument document, Long canton) {
    //		List<CodeGroup> codeGroups = new ArrayList<CodeGroup>();
    //
    //		CodeListsType codeListsType = document.getStructure().getCodeLists();
    //		if (codeListsType == null)
    //			throw new MetastatServiceException(
    //					"Couldn't determine 'CodeListsType' in metastat document '" + document + "'");
    //
    //		CodeListType2[] codeLists = codeListsType.getCodeListArray();
    //		if (codeLists == null || codeLists.length != 1)
    //			throw new MetastatServiceException(
    //					"More or less than one Element 'CodeListType2' found in metastat document '" + document + "'");
    //
    //		if (!codeLists[0].isSetValidFrom())
    //			throw new MetastatServiceException("Attribute 'ValidFrom' is not set for Element '"
    //					+ codeLists[0].toString() + "' in metastat document '" + document + "'");
    //
    //		Date validFrom = ((XmlCalendar) codeLists[0].getValidFrom()).getTime();
    //		Integer validFromYear = getValidYear(validFrom);
    //
    //		Date validTo = null;
    //		Integer validToYear = null;
    //		if (codeLists[0].isSetValidTo()) {
    //			validTo = ((XmlCalendar) codeLists[0].getValidTo()).getTime();
    //			validToYear = getValidYear(validTo);
    //			;
    //		}
    //
    //		for (CodeType codeType : codeLists[0].getCodeArray()) {
    //			String codeValue = codeType.getValue();
    //			if (codeValue.matches("\\d+")) {
    //				Long code = Long.valueOf(codeValue);
    //
    //				CodeGroup cgGerman = createCodeGroup(codeGroupId, code, "de", canton, validFromYear, validToYear);
    //				codeGroups.add(cgGerman);
    //				CodeGroup cgFrench = createCodeGroup(codeGroupId, code, "fr", canton, validFromYear, validToYear);
    //				codeGroups.add(cgFrench);
    //				CodeGroup cgItalian = createCodeGroup(codeGroupId, code, "it", canton, validFromYear, validToYear);
    //				codeGroups.add(cgItalian);
    //
    //				if (codeType.isSetAnnotations()) {
    //					for (AnnotationType annotationType : codeType.getAnnotations().getAnnotationArray()) {
    //						// TODO remove check for "NOTE" as soon as the metastat
    //						// data has been cleaned up
    //						if ("ABBREV".equals(annotationType.getAnnotationType())
    //								|| "NOTE".equals(annotationType.getAnnotationType())) {
    //							for (TextType textType : codeType.getDescriptionArray()) {
    //								String abbr = textType.getStringValue();
    //
    //								if (textType.isSetLang()) {
    //									String lang = textType.getLang();
    //									if ("de".equals(lang)) {
    //										cgGerman.setCodeTextAbbr(abbr);
    //
    //										if (cgFrench.getCodeTextAbbr() == null
    //												|| cgFrench.getCodeTextAbbr().length() == 0)
    //											cgFrench.setCodeTextAbbr(abbr);
    //
    //										if (cgItalian.getCodeTextAbbr() == null
    //												|| cgItalian.getCodeTextAbbr().length() == 0)
    //											cgItalian.setCodeTextAbbr(abbr);
    //									} else if ("fr".equals(lang)) {
    //										cgFrench.setCodeTextAbbr(abbr);
    //									} else if ("it".equals(lang)) {
    //										cgItalian.setCodeTextAbbr(abbr);
    //									}
    //								} else if (codeType.getDescriptionArray().length == 1) {
    //									cgGerman.setCodeTextAbbr(abbr);
    //
    //									cgFrench.setCodeTextAbbr(abbr);
    //
    //									cgItalian.setCodeTextAbbr(abbr);
    //								} else
    //									throw new MetastatServiceException(
    //											"Attribute 'lang' is not set and more than one 'Description' Element exists for Element '"
    //													+ codeType + "' in metastat document '" + document + "'");
    //							}
    //						}
    //					}
    //				}
    //
    //				for (TextType textType : codeType.getDescriptionArray()) {
    //					String description = textType.getStringValue();
    //
    //					if (textType.isSetLang()) {
    //						String lang = textType.getLang();
    //						if ("de".equals(lang)) {
    //							cgGerman.setCodeText(description);
    //
    //							if (cgFrench.getCodeText() == null || cgFrench.getCodeText().length() == 0)
    //								cgFrench.setCodeText(description);
    //
    //							if (cgItalian.getCodeText() == null || cgItalian.getCodeText().length() == 0)
    //								cgItalian.setCodeText(description);
    //						} else if ("fr".equals(lang)) {
    //							cgFrench.setCodeText(description);
    //						} else if ("it".equals(lang)) {
    //							cgItalian.setCodeText(description);
    //						}
    //					} else if (codeType.getDescriptionArray().length == 1) {
    //						cgGerman.setCodeText(description);
    //
    //						cgFrench.setCodeText(description);
    //
    //						cgItalian.setCodeText(description);
    //					} else
    //						throw new MetastatServiceException(
    //								"Attribute 'lang' is not set and more than one 'Description' Element exists for Element '"
    //										+ codeType + "' in metastat document '" + document + "'");
    //				}
    //			} else
    //				throw new MetastatServiceException("Value of attribute 'value' is not a number for Element '" + codeType
    //						+ "' in metastat document '" + document + "'");
    //		}
    //
    //		return codeGroups;
    //	}
    //
    //	private CodeGroup createCodeGroup(String codeGroupId, Long code, String language, Long canton,
    //			Integer validFromYear, Integer validToYear) {
    //		CodeGroup codeGroup = new CodeGroup();
    //		codeGroup.setCodeGroupId(codeGroupId);
    //		codeGroup.setCode(code);
    //		codeGroup.setLanguage(language);
    //		codeGroup.setValidFromYear(validFromYear);
    //		codeGroup.setValidToYear(validToYear);
    //		codeGroup.setCanton(canton);
    //		return codeGroup;
    //	}
    //
    //	/**
    //	 * Determines the year specified by the given date (based on the reference
    //	 * date 15.09.XX)
    //	 * 
    //	 * @param validDate
    //	 * @return year
    //	 */
    //	private Integer getValidYear(Date validDate) {
    //		if (validDate == null)
    //			return null;
    //
    //		Calendar validCal = Calendar.getInstance();
    //		validCal.setTime(validDate);
    //
    //		// check if the given date is after or equals to the 15.08.XX
    //		// if true, increase the year by 1
    //		if (validCal.get(Calendar.MONTH) > 7) {
    //			validCal.add(Calendar.YEAR, 1);
    //		} else if (validCal.get(Calendar.MONTH) == 7) {
    //			if (validCal.get(Calendar.DAY_OF_MONTH) >= 15) {
    //				validCal.add(Calendar.YEAR, 1);
    //			}
    //		}
    //
    //		// // check if the given date is prior to the 15.09.XX
    //		// // if true, decrease the year by 1
    //		// if (validCal.get(Calendar.MONTH) < 8)
    //		// {
    //		// validCal.add(Calendar.YEAR, -1);
    //		// }
    //		// else if (validCal.get(Calendar.MONTH) == 8)
    //		// {
    //		// if (validCal.get(Calendar.DAY_OF_MONTH) < 15)
    //		// {
    //		// validCal.add(Calendar.YEAR, -1);
    //		// }
    //		// }
    //
    //		return validCal.get(Calendar.YEAR);
    //	}
    //
    //	private URL getMetastatURL(String codeList) {
    //		try {
    //			if (_configuration.getSdmxServerURL() == null)
    //				throw new MetastatServiceException("Token '" + IMebCommonServerConfiguration.SDMX_SERVER_URL
    //						+ "' is not set in application.properties");
    //
    //			return new URL(_configuration.getSdmxServerURL().trim() + "/" + codeList);
    //		} catch (MalformedURLException e) {
    //			throw new MetastatServiceException("Address for token '" + IMebCommonServerConfiguration.SDMX_SERVER_URL
    //					+ "' is invalid, please check setting in application.properties");
    //		}
    //	}
    //
    //	private URL getMetastatURL(String codeList, String codeListVersion) {
    //		try {
    //			if (_configuration.getSdmxServerURL() == null)
    //				throw new MetastatServiceException("Token '" + IMebCommonServerConfiguration.SDMX_SERVER_URL
    //						+ "' is not set in application.properties");
    //
    //			return new URL(_configuration.getSdmxServerURL().trim() + "/" + codeList + "/" + codeListVersion);
    //		} catch (MalformedURLException e) {
    //			throw new MetastatServiceException("Address for token '" + IMebCommonServerConfiguration.SDMX_SERVER_URL
    //					+ "' is invalid, please check setting in application.properties");
    //
    //		}
    //	}
    //
    //	private StructureDocument getDocumentFromURL(URL url) {
    //		try {
    //			InputStream inputStream = url.openStream();
    //			return StructureDocument.Factory.parse(inputStream);
    //		} catch (IOException e) {
    //			throw new MetastatServiceException("Unknown IOException", e);
    //		} catch (XmlException e) {
    //			throw new MetastatServiceException("Unknown XmlException", e);
    //		}
    //	}
}
