/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.bfs.meb.server.commons.util.ValidationUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;

/**
 * Persistence Object for the intervention data table
 * 
 * @author $Author: dzw $
 * @version $Revision: 542 $
 */
@Entity
@Table(name = "SBA_CANTONINTERVENTIONS")
@GenericGenerator(name = "cantoninterventionseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SBASEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaCantonIntervention extends CantonIntervention {
    private Blob plausireportDe;
    private Blob plausireportFr;
    private Blob plausireportIt;

    /** Default constructor */
    public SbaCantonIntervention() {}

    /** Copy from dto */
    public SbaCantonIntervention(CantonIntervention dtoIntervention) {
        setInterventionId(dtoIntervention.getInterventionId());
        setCantonId(dtoIntervention.getCantonId());
        setType(dtoIntervention.getType());
        setIntervention_user(dtoIntervention.getIntervention_user());
        setIntervention_date(dtoIntervention.getIntervention_date());
        setText(dtoIntervention.getText());
    }

    @Transient
    public void setPlausireport_de_zipped(byte[] plausireportDe) throws IOException {
        setPlausireport_de(zip(plausireportDe, "PlausiReport.xlsx"));
    }

    @Transient
    public void setPlausireport_fr_zipped(byte[] plausireportFr) throws IOException {
        setPlausireport_fr(zip(plausireportFr, "PlausiReport.xlsx"));
    }

    @Transient
    public void setPlausireport_it_zipped(byte[] plausireportIt) throws IOException {
        setPlausireport_it(zip(plausireportIt, "PlausiReport.xlsx"));
    }

    /**
     * @return the plausireport_de
     */
    @Column
    public byte[] getPlausireport_de() {
        return extractBytesFromBlob(plausireportDe);
    }

    public void setPlausireport_de(byte[] plausireport_de) {
        this.plausireportDe = toSerialBlob(plausireport_de);
    }

    /**
     * @param plausireportDe
     *            the plausireport_de to set
     */
    protected void setPlausireport_de(Blob plausireportDe) {
        plausireportDe = plausireportDe;
    }

    /**
     * @return the plausireportFr
     */
    public byte[] getPlausireport_fr() {
        return extractBytesFromBlob(plausireportFr);
    }

    public void setPlausireport_fr(byte[] plausireport_fr) {
        this.plausireportFr = toSerialBlob(plausireport_fr);
    }


    /**
     * @param plausireportFr
     *            the plausireportFr to set
     */
    protected void setPlausireport_fr(Blob plausireportFr) {
        plausireportFr = plausireportFr;
    }

    /**
     * @return the plausireportIt
     */
    public byte[] getPlausireport_it() {
        return extractBytesFromBlob(plausireportIt);
    }
    public void setPlausireport_it(byte[] plausireport_it) {
this.plausireportIt = toSerialBlob(plausireport_it);
    }
    /**
     * @param plausireportIt
     *            the plausireportIt to set
     */
    protected void setPlausireport_it(Blob plausireportIt) {
        plausireportIt = plausireportIt;
    }


    public byte[] convertXlsToXlsx(InputStream xlsInputStream) throws IOException {
        try (HSSFWorkbook hssfWorkbook = new HSSFWorkbook(xlsInputStream);
             XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
             ByteArrayOutputStream xlsxOutputStream = new ByteArrayOutputStream()) {

            // Copier chaque feuille du fichier XLS vers le fichier XLSX
            for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); i++) {
                Sheet oldSheet = hssfWorkbook.getSheetAt(i);
                Sheet newSheet = xssfWorkbook.createSheet(oldSheet.getSheetName());
                copySheet(oldSheet, newSheet);
            }

            // Écrire le fichier XLSX en mémoire
            xssfWorkbook.write(xlsxOutputStream);
            return xlsxOutputStream.toByteArray();
        }
    }

    private void copySheet(Sheet oldSheet, Sheet newSheet) {
        for (int rowIndex = 0; rowIndex <= oldSheet.getLastRowNum(); rowIndex++) {
            Row oldRow = oldSheet.getRow(rowIndex);
            if (oldRow != null) {
                Row newRow = newSheet.createRow(rowIndex);
                copyRow(oldRow, newRow);
            }
        }
    }

    private void copyRow(Row oldRow, Row newRow) {
        for (int cellIndex = 0; cellIndex < oldRow.getLastCellNum(); cellIndex++) {
            Cell oldCell = oldRow.getCell(cellIndex);
            if (oldCell != null) {
                Cell newCell = newRow.createCell(cellIndex, oldCell.getCellType());
                copyCell(oldCell, newCell);
            }
        }
    }

    private void copyCell(Cell oldCell, Cell newCell) {
        CellType cellType = CellType.forInt(oldCell.getCellType()); // Pour Apache POI 4.x et supérieur

        switch (cellType) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case BLANK:
            default:
                newCell.setCellValue("");
                break;
        }
    }
    /**
     * Méthode utilitaire pour extraire un byte[] depuis un BLOB avec conversion .xls -> .xlsx si nécessaire.
     */
    private byte[] extractBytesFromBlob(Blob blob) {
        try {
            if (blob != null) {
                byte[] fileData = blob.getBytes(1L, (int) blob.length());

                if (ValidationUtils.isXlsFormat(fileData)) {
                    return convertXlsToXlsx(new ByteArrayInputStream(fileData));
                }

                return fileData;
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Unable to process blob file", e);
        }

        return null;
    }

    /**
     * Convertit un tableau de bytes en SerialBlob ou retourne null si null.
     */
    private javax.sql.rowset.serial.SerialBlob toSerialBlob(byte[] data) {
        if (data == null) return null;
        try {
            return new javax.sql.rowset.serial.SerialBlob(data);
        } catch (SQLException e) {
            throw new IllegalStateException("Error converting byte array to SerialBlobfile", e);
        }
    }
}
