package com.fortinet.forticontainer

class HttpUploadFile {
    private static final String LINE_FEED = "\r\n";
    private static final String charset="UTF-8";

    private HttpURLConnection httpConn;
    private OutputStream outputStream;
    private PrintWriter writer;
    private String boundary;

    /**
     * This constructor initializes a new HTTP POST request with content type is set to multipart/form-data
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public HttpUploadFile( requestURL, ctrolToken,imageName) throws IOException {

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("x-controller-token",ctrolToken)
        httpConn.setRequestProperty("imageName",imageName)
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

    /**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append( "Content-Disposition: form-data;name=\"" + fieldName + "\";filename=\"" + fileName + "\"").append(LINE_FEED);
        writer.append( "Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append(" Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        while (bytesRead != -1) {
            outputStream.write(buffer, 0, bytesRead);
            bytesRead = inputStream.read(buffer);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public List<String> finish() throws IOException {
        List<String> response = new ArrayList<String>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader( httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return response;
    }

    public boolean upload(file){
        try {
//          addFormField("file", "{\"name\":\"tmp_image.tar\"}");
            addFilePart("file", file);
            List<String> response = finish();
             for (String line : response) {
                 if("ok".equalsIgnoreCase(line)){
                     return true;
                 }
             }
            return true;
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
            return false;
        }
        return false;
    }



    public static void main(String[] args) {
//        String requestURL = "http://172.30.154.23:10030/api/v1/jenkins/image/9908022949449732";
        String requestURL = "http://127.0.0.1:8000/api/v1/jenkins/image/9959610875383812";

        try {
            HttpUploadFile multipart = new HttpUploadFile(requestURL,"52677600474AFBAB4BD30EEE9D7B6D28","image");
//            multipart.addFormField("file", "{\"name\":\"tmp_image.tar\"}");
            def result = multipart.upload("/tmp/tmp_image.tar");
            System.out.println(result);
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }
}
