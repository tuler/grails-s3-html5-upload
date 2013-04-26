package org.grails.s3.html5.upload

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64
import java.text.SimpleDateFormat
import groovy.json.JsonOutput

class UploadController {

	def grailsApplication
	
	private static String sign(String secret, String data) {
		byte[] dataBytes = data.getBytes('UTF-8')
		byte[] secretBytes = secret.getBytes('UTF-8')
		SecretKeySpec signingKey = new SecretKeySpec(secretBytes, 'HmacSHA1');
		Mac mac = Mac.getInstance('HmacSHA1');
		mac.init(signingKey);
		byte[] signature = mac.doFinal(dataBytes);
		return Base64.encodeBase64String(signature).minus('\n').minus('\r');
	}
	
	private http_date() {
		def df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
		df.setTimeZone(TimeZone.getTimeZone('GMT'))
		df.format(new Date())
	}
	
	private String init(String key, String date) {
		def secretKey = grailsApplication.config.grails.plugin.s3.html5.upload.secretKey ?: grailsApplication.config.grails.plugin.awssdk.secretKey
		def acl = "public-read"
		def bucket = grailsApplication.config.grails.plugin.s3.html5.upload.bucket
		return sign(secretKey, "POST\n\n\n\nx-amz-date:${date}\n/${bucket}/${key}?uploads")
	}
	
	private String chunk(String key, String uploadId, String chunk, String date) {
		def secretKey = grailsApplication.config.grails.plugin.s3.html5.upload.secretKey ?: grailsApplication.config.grails.plugin.awssdk.secretKey
		def mimeType = grailsApplication.config.grails.plugin.s3.html5.upload.mimeType
		def bucket = grailsApplication.config.grails.plugin.s3.html5.upload.bucket
		return sign(secretKey, "PUT\n\n${mimeType}\n\nx-amz-date:${date}\n/${bucket}/${key}?partNumber=${chunk}&uploadId=${uploadId}")
	}
	
	private String list(String key, String uploadId, String date) {
		def secretKey = grailsApplication.config.grails.plugin.s3.html5.upload.secretKey ?: grailsApplication.config.grails.plugin.awssdk.secretKey
		def bucket = grailsApplication.config.grails.plugin.s3.html5.upload.bucket
		return sign(secretKey, "GET\n\n\n\nx-amz-date:${date}\n/${bucket}/${key}?uploadId=${uploadId}")
	}
	
	private String end(String key, String uploadId, String date) {
		def secretKey = grailsApplication.config.grails.plugin.s3.html5.upload.secretKey ?: grailsApplication.config.grails.plugin.awssdk.secretKey
		def mimeType = grailsApplication.config.grails.plugin.s3.html5.upload.mimeType
		def bucket = grailsApplication.config.grails.plugin.s3.html5.upload.bucket
		return sign(secretKey, "POST\n\n${mimeType}\n\nx-amz-date:${date}\n/${bucket}/${key}?uploadId=${uploadId}")
	}
	
	private String delete(String key, String uploadId, String date) {
		def secretKey = grailsApplication.config.grails.plugin.s3.html5.upload.secretKey ?: grailsApplication.config.grails.plugin.awssdk.secretKey
		def bucket = grailsApplication.config.grails.plugin.s3.html5.upload.bucket
		return sign(secretKey, "DELETE\n\n\n\nx-amz-date:${date}\n/${bucket}/${key}?uploadId=${uploadId}")
	}
	
	def chunk_loaded() {
		String key = params.key
		String uploadId = params.upload_id
		String filename = params.filename
		long filesize = params.long('filesize')
		String lastModified = params.last_modified
		String chunk = params.chunk
		
		if (filesize > grailsApplication.config.grails.plugin.s3.html5.upload.chunkSize) {
			def upload = Upload.createCriteria().get {
				eq('filename', filename)
				eq('filesize', filesize)
				eq('lastModified', lastModified)
			}
			
			if (!upload) {
				// upload not found, create a brand new one
				upload = new Upload(
					filename: filename, 
					filesize: filesize, 
					lastModified: lastModified, 
					chunksUploaded: "", 
					key: key, 
					uploadId: uploadId
				)
			}
			
			def chunks = upload.chunksUploaded.split(',') as Set
			chunks.add(chunk)
			upload.chunksUploaded = chunks.join(',')
			upload.save(failOnError: true)
			
			render ''
		}
	}
	
	def get_all_signatures() {
		def date = http_date()
		def list_signature = list(params.key, params.upload_id, date)
		def end_signature = end(params.key, params.upload_id, date)
		def delete_signature = delete(params.key, params.upload_id, date)
		
		int numChunks = params.int('num_chunks')
		def chunk_signatures = (1..numChunks).collect { [chunk(params.key, params.upload_id, "${it}", date), date]}
		
		render JsonOutput.toJson([
			list_signature: [list_signature, date], 
			end_signature: [end_signature, date], 
			chunk_signatures: chunk_signatures
		])
	}
	
	def get_init_signature() {
		String filename = params.filename
		long filesize = params.long('filesize')
		String lastModified = params.last_modified
		String key = params.key
		String uploadId = params.upload_id
		String chunk = params.chunk
		def date = http_date()
		
		def upload = Upload.createCriteria().get {
			eq('filename', filename)
			eq('filesize', filesize)
			eq('lastModified', lastModified)
		}
		
		if (upload && params.force) {
			// if force and exists, delete it
			upload.delete()
			upload = null
		}
		
		if (upload) {
			def signature = init(upload.key, date)
			render JsonOutput.toJson([
				signature: signature, 
				date: date, 
				key: upload.key, 
				upload_id: upload.uploadId, 
				chunks: upload.chunksUploaded.split(',')
			])
		} else {
			def signature = init(key, date)
			render JsonOutput.toJson([signature: signature, date: date])
		}
	}
	
	def get_chunk_signature() {
		def date = http_date()
		def signature = chunk(params.key, params.upload_id, params.chunk, date)
		render JsonOutput.toJson([signature: signature, date: date])
	}
	
	def get_list_signature() {
		def date = http_date()
		def signature = list(params.key, params.upload_id, date)
		render JsonOutput.toJson([signature: signature, date: date])
	}
	
	def get_end_signature() {
		def date = http_date()
		def signature = end(params.key, params.upload_id, date)
		render JsonOutput.toJson([signature: signature, date: date])
	}
	
	def get_delete_signature() {
		def date = http_date()
		def signature = delete(params.key, params.upload_id, date)
		render JsonOutput.toJson([signature: signature, date: date])
	}
}
