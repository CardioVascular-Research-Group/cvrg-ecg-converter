package edu.jhu.icm.enums;

/*
Copyright 2015 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author Chris Jurado
*/
public enum DataFileFormat {

//	RDT, 
//	HL7, 
//	WFDB, 
	WFDB_16, 
	WFDB_61, 
	WFDB_212, 
	GEMUSE, 
//	RAW_XY_CONST_SAMPLE, 
//	RAW_XY_VAR_SAMPLE, 
	PHILIPS103, 
	PHILIPS104, 
//	SCHILLER, 
	MUSEXML,
	GE_MAGELLAN,
	WFDB,
	RDT, 
	HOLTER12,
	HOLTER3, 
//	GE_MUSE,
	HL7,
	XY_FILE,
//	PHILIPS_103,
//	PHILIPS_104("Philips 1.04", EnumFileExtension.XML),
//	MUSE_XML("Muse", EnumFileExtension.XML),
	SCHILLER;
}