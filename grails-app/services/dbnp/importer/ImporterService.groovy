/**
 * Importer service
 *
 * The importer service handles the import of tabular, comma delimited and Excel format
 * based files.
 *
 * @package	importer
 * @author	t.w.abma@umcutrecht.nl
 * @since	20100126
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.importer
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter

import dbnp.studycapturing.TemplateFieldType
import dbnp.studycapturing.Template
import dbnp.studycapturing.SamplingEvent
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Event
import dbnp.studycapturing.Sample

import dbnp.data.Term

class ImporterService {

    boolean transactional = true

    /**
    * @param is input stream representing the (workbook) resource
    * @return high level representation of the workbook
    */
    HSSFWorkbook getWorkbook(InputStream is) {
	POIFSFileSystem	fs = new POIFSFileSystem(is)
	HSSFWorkbook	wb = new HSSFWorkbook(fs);
	return wb;
    }

    /**
     * @param wb high level representation of the workbook
     * @param sheetindex sheet to use within the workbook
     * @return header representation as a MappingColumn hashmap
     */
    def getHeader(HSSFWorkbook wb, int sheetindex, int headerrow, int datamatrix_start, theEntity=null){

	def sheet = wb.getSheetAt(sheetindex)	
	def sheetrow = sheet.getRow(datamatrix_start)
	//def header = []
	def header = [:]
        def df = new DataFormatter()
	def property = new String()

	//for (HSSFCell c: sheet.getRow(datamatrix_start)) {

	(0..sheetrow.getLastCellNum() -1 ).each { columnindex ->

	    //def index	=   c.getColumnIndex()
	    def datamatrix_celltype = sheet.getRow(datamatrix_start).getCell(columnindex, org.apache.poi.ss.usermodel.Row.CREATE_NULL_AS_BLANK).getCellType()
	    def datamatrix_celldata = df.formatCellValue(sheet.getRow(datamatrix_start).getCell(columnindex))
	    def datamatrix_cell	    = sheet.getRow(datamatrix_start).getCell(columnindex)
	    println "frn is "+sheet.getFirstRowNum()
		def headercell = sheet.getRow(headerrow-1+sheet.getFirstRowNum()).getCell(columnindex)
	    def tft = TemplateFieldType.STRING //default templatefield type

            // Check for every celltype, currently redundant code, but possibly this will be
	    // a piece of custom code for every cell type like specific formatting	    
	        
	    switch (datamatrix_celltype) {
                    case HSSFCell.CELL_TYPE_STRING:
			    //parse cell value as double
			    def doubleBoolean = true
			    def fieldtype = TemplateFieldType.STRING

			    // is this string perhaps a double?
			    try {
				formatValue(datamatrix_celldata, TemplateFieldType.DOUBLE)
			    } catch (NumberFormatException nfe) { doubleBoolean = false }
			    finally {
				if (doubleBoolean) fieldtype = TemplateFieldType.DOUBLE
			    }

			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:fieldtype,
									    index:columnindex,
									    entity:theEntity,
									    property:property);

			    break
                    case HSSFCell.CELL_TYPE_NUMERIC:
			    def fieldtype = TemplateFieldType.INTEGER
			    def doubleBoolean = true
			    def integerBoolean = true

			    // is this cell really an integer?
			    try {
				Integer.valueOf(datamatrix_celldata)
			    } catch (NumberFormatException nfe) { integerBoolean = false }
			    finally {
				if (integerBoolean) fieldtype = TemplateFieldType.INTEGER
			    }

			    // it's not an integer, perhaps a double?
			    if (!integerBoolean)
				try {
				    formatValue(datamatrix_celldata, TemplateFieldType.DOUBLE)
				} catch (NumberFormatException nfe) { doubleBoolean = false }
				finally {
				    if (doubleBoolean) fieldtype = TemplateFieldType.DOUBLE
				}

			    if (HSSFDateUtil.isCellDateFormatted(datamatrix_cell)) fieldtype = TemplateFieldType.DATE

			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:fieldtype,
									    index:columnindex,
									    entity:theEntity,
									    property:property);
			    break
		    case HSSFCell.CELL_TYPE_BLANK:
			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:columnindex,
									    entity:theEntity,
									    property:property);
			    break
                    default:
			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:columnindex,
									    entity:theEntity,
									    property:property);
			    break
            } // end of switch
	} // end of cell loop
        return header
    }

    /**
     * This method is meant to return a matrix of the rows and columns
     * used in the preview
     *
     * @param wb workbook object
     * @param sheetindex sheet index used
     * @param rows amount of rows returned
     * @return two dimensional array (matrix) of HSSFCell objects
     */

    HSSFCell[][] getDatamatrix(HSSFWorkbook wb, header, int sheetindex, int datamatrix_start, int count) {
	def sheet = wb.getSheetAt(sheetindex)
	def rows  = []
	def df = new DataFormatter()	

	// walk through all rows
	(count <= sheet.getLastRowNum()) ?
	((datamatrix_start+sheet.getFirstRowNum())..count).each { rowindex ->
	    def row = []

	    // walk through every cell
	    /*for (HSSFCell c: sheet.getRow(rowindex)) {
		row.add(c)
		println c.getColumnIndex() + "=" +c
	    }*/
	    
	    (0..header.size()-1).each { columnindex ->
		def c = sheet.getRow(rowindex).getCell(columnindex, org.apache.poi.ss.usermodel.Row.CREATE_NULL_AS_BLANK)		
		//row.add(df.formatCellValue(c))
		row.add(c)
		//if (c.getCellType() == c.CELL_TYPE_STRING) println "STR"+c.getStringCellValue()
		//if (c.getCellType() == c.CELL_TYPE_NUMERIC) println "INT" +c.getNumericCellValue()
	    }
		//row.add(df.formatCellValue(c))
	    rows.add(row)
	} : 0

	return rows
    }

    /**
    * This method will move a file to a new location.
    *
    * @param file File object to move
    * @param folderpath folder to move the file to
    * @param filename (new) filename to give
    * @return if file has been moved succesful, the new path and filename will be returned, otherwise an empty string will be returned
    */
    def moveFile(File file, String folderpath, String filename) {
        try {
		def rnd = ""; //System.currentTimeMillis()
		file.transferTo(new File(folderpath, rnd+filename))
		return folderpath + filename
	    } catch(Exception exception) {
		log.error "File move error, ${exception}"
		return ""
		}
    }

    /**
    * @return random numeric value
    */
    def random = {
	    return System.currentTimeMillis() + Runtime.runtime.freeMemory()
	}

    /**
    * Method to read data from a workbook and to import data into a two dimensional
    * array
    *
    * @param template_id template identifier to use fields from
    * @param wb POI horrible spreadsheet formatted workbook object
    * @param mcmap linked hashmap (preserved order) of MappingColumns
    * @param sheetindex sheet to use when using multiple sheets
    * @param rowindex first row to start with reading the actual data (NOT the header)
    * @return two dimensional array containing records (with entities)
    * 
    * @see dbnp.importer.MappingColumn
    */
    def importdata(template_id, HSSFWorkbook wb, int sheetindex, int rowindex, mcmap) {
	def sheet = wb.getSheetAt(sheetindex)
	def table = []
	
	// walk through all rows and fill the table with records
	(rowindex..sheet.getLastRowNum()).each { i ->
	    table.add(createRecord(template_id, sheet.getRow(i), mcmap))
	}
	return table	
    }
   
    /**
     * Method to store a matrix containing the entities in a record like structure. Every row in the table
     * contains one or more entity objects (which contain fields with values). So actually a row represents
     * a record with fields from one or more different entities.
     *
     * @param study entity Study
     * @param datamatrix two dimensional array containing entities with values read from Excel file     *
     */    
    def saveDatamatrix(Study study, datamatrix) {
	def validatedSuccesfully = 0
	study.refresh()
	
	// go through the data matrix, read every record and validate the entity and try to persist it
	datamatrix.each { record ->
	    record.each { entity ->
			switch (entity.getClass()) {
			case Study	 :  print "Persisting Study `" + entity + "`: "
						if (persistEntity(entity)) validatedSuccesfully++
						break
			case Subject	 :  print "Persisting Subject `" + entity + "`: "
						study.addToSubjects(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break
			case Event	 :  print "Persisting Event `" + entity + "`: "
						study.addToEvents(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break
			case Sample	 :  print "Persisting Sample `" + entity +"`: "
						study.addToSamples(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break
			case SamplingEvent: print "Persisting SamplingEvent `" + entity + "`: "
						study.addToSamplingEvents(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break;
			default		 :  println "Skipping persisting of `" + entity.getclass() +"`"
						break
			} // end switch
	    } // end record
	} // end datamatrix
	return validatedSuccesfully
    }

    /**
     * Method to persist entities into the database
     * Checks whether entity already exists (based on identifier column 'name')
     * 
     * @param entity entity object like Study, Subject, Protocol et cetera
     * 
     */
    boolean persistEntity(entity) {
	    println "persisting ${entity}"
	    // if not validated
		if (entity.validate()) {
			if (entity.save()) { //.merge?
				return true
			}
			else { // if save was unsuccesful
				entity.errors.allErrors.each {
					println it
				}
				return false
			}
		}
	    else { // if not validated
		    entity.errors.each {
			    println it
		    }
			return false
	    }
	 }

	/**
	 * This method creates a record (array) containing entities with values
	 *
	 * @param template_id template identifier
	 * @param excelrow POI based Excel row containing the cells
	 * @param mcmap map containing MappingColumn objects
	 */
	def createRecord(template_id, HSSFRow excelrow, mcmap) {
		def df = new DataFormatter()
		def template = Template.get(template_id)
		def record = []

		// Initialize all possible entities with the chosen template
		def study = new Study(template: template)
		def subject = new Subject(template: template)
		def samplingEvent = new SamplingEvent(template: template)
		def event = new Event(template: template)
		def sample = new Sample(template: template)

		// Go through the Excel row cell by cell
		for (HSSFCell cell: excelrow) {
			// get the MappingColumn information of the current cell
			def mc = mcmap[cell.getColumnIndex()]
			def value

			// Check if column must be imported
			if (!mc.dontimport) {
				try {
					value = formatValue(df.formatCellValue(cell), mc.templatefieldtype)
				} catch (NumberFormatException nfe) {
					value = ""
				}

				// which entity does the current cell (field) belong to?
				switch (mc.entity) {
					case Study: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(study)
						study.setFieldValue(mc.property, value)
						break
					case Subject: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(subject)
						subject.setFieldValue(mc.property, value)
						break
					case SamplingEvent: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(samplingEvent)
						samplingEvent.setFieldValue(mc.property, value)
						break
					case Event: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(event)
						event.setFieldValue(mc.property, value)
						break
					case Sample: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(sample)
						sample.setFieldValue(mc.property, value)
						break
					case Object:   // don't import
						break
				} // end switch
			} // end
		} // end for

	return record
    }

    /**
    * Method to parse a value conform a specific type
    * @param value string containing the value
    * @return object corresponding to the TemplateFieldType
    */
    def formatValue(String value, TemplateFieldType type) throws NumberFormatException {
	    switch (type) {
		case TemplateFieldType.STRING	    :   return value.trim()
		case TemplateFieldType.TEXT	    :   return value.trim()
		case TemplateFieldType.INTEGER	    :   return (int) Double.valueOf(value)
		case TemplateFieldType.FLOAT	    :   return Float.valueOf(value.replace(",","."));
		case TemplateFieldType.DOUBLE	    :   return Double.valueOf(value.replace(",","."));
		case TemplateFieldType.STRINGLIST   :   return value.trim()
		case TemplateFieldType.ONTOLOGYTERM :   return value.trim()
		case TemplateFieldType.DATE	    :   return value
		default				    :   return value
	    }
    }

}
