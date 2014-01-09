package org.grails.s3.html5.upload

class Upload {

	String filename
	
	Long filesize
	
	String lastModified
	
	String key
	
	String uploadId
	
	String chunksUploaded
	
	Date dateCreated
	
	Date lastUpdated
	
	static constraints = {
		filename nullable: false, blank: false
		filesize nullable: false
		lastModified nullable: false, blank: false
		key nullable: false, blank: false
		uploadId nullable: false, blank: false
		chunksUploaded nullable: false, blank: true
	}
	
	static mapping = {
		filename type: 'text'
		chunksUploaded type: 'text'
		key column: 'file_key'
	}
}
