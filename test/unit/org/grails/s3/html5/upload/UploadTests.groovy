package org.grails.s3.html5.upload


import grails.test.mixin.*
import org.junit.*

@TestFor(Upload)
class UploadTests {

	void testConstraints() {
		mockForConstraintsTests(Upload, [])
		
		def u = new Upload()
		assert !u.validate()
		assert u.errors['filename'] == 'nullable'
		assert u.errors['filesize'] == 'nullable'
		assert u.errors['lastModified'] == 'nullable'
		assert u.errors['key'] == 'nullable'
		assert u.errors['uploadId'] == 'nullable'
		assert u.errors['chunksUploaded'] == 'nullable'
		
		u = new Upload(filename: '', filesize: 0, lastModified: '', key: '', uploadId: '', chunksUploaded: '')
		assert !u.validate()
		assert u.errors['filename'] == 'blank'
		assert u.errors['lastModified'] == 'blank'
		assert u.errors['key'] == 'blank'
		assert u.errors['uploadId'] == 'blank'

		u = new Upload(filename: 'a', filesize: 0, lastModified: 'a', key: 'a', uploadId: 'a', chunksUploaded: '')
		assert u.validate()
	}
}
