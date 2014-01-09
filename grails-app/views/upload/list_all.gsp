<!DOCTYPE html>
	<head>
		<title>Uploads</title>
	</head>
	<body>
		<table>
			<thead>
				<tr>
					<th>Id</th>
					<th>Key</th>
					<th>Filename</th>
					<th>Filesize</th>
					<th>Chunks</th>
					<th>Status</th>
					<th>Created At</th>
					<th>Updated At</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${uploads}" var="upload">
					<tr>
						<td>${upload.id}</td>
						<td>${upload.key}</td>
						<td>${upload.filename}</td>
						<td>${upload.humanReadableFilesize}</td>
						<td>${upload.numChunksLoaded}/${upload.numChunks}</td>
						<td>${upload.status}</td>
						<td>${upload.dateCreated}</td>
						<td>${upload.lastUpdated}</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</body>
</html>