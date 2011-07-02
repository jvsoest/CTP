/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.ctp.stdstages;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.rsna.ctp.objects.DicomObject;
import org.rsna.ctp.objects.FileObject;
import org.rsna.ctp.pipeline.AbstractPipelineStage;
import org.rsna.ctp.pipeline.Processor;
import org.rsna.ctp.stdstages.anonymizer.AnonymizerStatus;
import org.rsna.ctp.stdstages.anonymizer.IntegerTable;
import org.rsna.ctp.stdstages.anonymizer.LookupTable;
import org.rsna.ctp.stdstages.anonymizer.dicom.DAScript;
import org.rsna.ctp.stdstages.anonymizer.dicom.DICOMAnonymizer;
import org.rsna.util.FileUtil;
import org.w3c.dom.Element;

/**
 * The DicomAnonymizer pipeline stage class.
 */
public class DicomAnonymizer extends AbstractPipelineStage implements Processor, ScriptableDicom {

	static final Logger logger = Logger.getLogger(DicomAnonymizer.class);

	public File scriptFile = null;
	public File lookupTableFile = null;
	public IntegerTable intTable = null;

	/**
	 * Construct the DicomAnonymizer PipelineStage.
	 * @param element the XML element from the configuration file
	 * specifying the configuration of the stage.
	 */
	public DicomAnonymizer(Element element) {
		super(element);
		scriptFile = FileUtil.getFile(element.getAttribute("script"), "examples/example-dicom-anonymizer.script");
		lookupTableFile = FileUtil.getFile(element.getAttribute("lookupTable"), (String)null);
		intTable = new IntegerTable(root);
	}

	/**
	 * Get the script file.
	 */
	public File getScriptFile() {
		return scriptFile;
	}

	/**
	 * Get the lookup table file.
	 */
	public File getLookupTableFile() {
		return lookupTableFile;
	}

	/**
	 * Get the integer table object.
	 */
	public IntegerTable getIntegerTable() {
		return intTable;
	}

	/**
	 * Process a DicomObject, anonymizing it and returning the processed object.
	 * If there is no script file, pass the object unmodified.
	 * If the object is not a DicomObject, pass the object unmodified.
	 * @param fileObject the object to process.
	 * @return the processed FileObject.
	 */
	public FileObject process(FileObject fileObject) {
		lastFileIn = new File(fileObject.getFile().getAbsolutePath());
		lastTimeIn = System.currentTimeMillis();

		if ( (fileObject instanceof DicomObject) && (scriptFile != null) ) {
			File file = fileObject.getFile();
			DAScript dascript = DAScript.getInstance(scriptFile);
			Properties script = dascript.toProperties();
			Properties lookup = LookupTable.getProperties(lookupTableFile);
			AnonymizerStatus status =
						DICOMAnonymizer.anonymize(file, file, script, lookup, intTable, false, false);
			if (status.isOK()) {
				fileObject = FileObject.getInstance(file);
			}
			else if (status.isQUARANTINE()) {
				if (quarantine != null) quarantine.insert(fileObject);
				lastFileOut = null;
				lastTimeOut = System.currentTimeMillis();
				return null;
			}
			else if (status.isSKIP()) ; //keep the input object
		}

		lastFileOut = new File(fileObject.getFile().getAbsolutePath());
		lastTimeOut = System.currentTimeMillis();
		return fileObject;
	}

	/**
	 * Stop the pipeline stage.
	 */
	public void shutdown() {
		intTable.close();
		stop = true;
	}

	/**
	 * Get HTML text displaying the current status of the stage.
	 * @return HTML text displaying the current status of the stage.
	 */
	public String getStatusHTML() {
		return getStatusHTML("");
	}
}