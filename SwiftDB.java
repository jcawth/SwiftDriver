import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.ByteArrayEntity;

//import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.FileInputStream;
import org.apache.http.entity.ContentType;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.io.InputStreamReader;
import java.util.Properties;

public class Client{
    public static String AUTHTOKEN;
    public static String STORAGEURL; 
    
    public static String readBuffer(BufferedReader rd){
        try {
            StringBuffer buffyTheVampire = new StringBuffer();
            String line;
            while((line = rd.readLine()) != null){
                buffyTheVampire.append(line);
            }
            return buffyTheVampire.toString();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }
    public static BufferedReader executeRequest(CloseableHttpClient client,
                                              HttpRequestBase request){
        try {
            CloseableHttpResponse response = client.execute(request);
            System.out.println("Response: \n" + response.getStatusLine());
            HttpEntity entity = response.getEntity();
            if(entity != null){
                BufferedReader rd = new BufferedReader(
                                new InputStreamReader(
                                entity.getContent()));
                return rd;
            }  
            return null;            
        } catch (Exception e) {
            System.out.println("Executing the Request caught the following error:");
            System.out.println(e);
            return null;
        }
    
    }
    public static void responseHandler(BufferedReader response){
        if(response != null){
            String status = readBuffer(response);
            System.out.println(status);
        }
    }
    /*
     * Grab Authentication settings from a config file....
     */
    public static void grabAuthSettings(){
        Properties prop = new Properties();
        InputStream input; 
        try {
            
            input = new FileInputStream("credentials.properties");
        
            prop.load(input);
            STORAGEURL = prop.getProperty("STORAGEURL");
            AUTHTOKEN = prop.getProperty("AUTHTOKEN");
            /*System.out.println(STORAGEURL);
            System.out.println(AUTHTOKEN);*/
        } catch (IOException except){
            except.printStackTrace();    
        } 
    }
    
    public static void addAuthHeader(HttpRequestBase request){
        request.addHeader("X-Auth-Token", AUTHTOKEN);
    }
    /*
     * Use Put Method
     * append container name
     * Print Response
     */
    public static  createContainer(String url, String containerName){
        HttpPut putRequest = new HttpPut(url + "/" + containerName +
                                        "?format=json");
        return putRequest;     
    }
    
    public static void deleteContainer(String url, String containerName){
        HttpDelete delRequest = new HttpDelete(url + "/" + containerName +
                                                "?format=json");
        return delRequest;
    }
    public static void listObjects(String url, String containerName){
        HttpGet getRequest = new HttpGet(url + "/" + containerName +
                                         "?format=json");
        return getRequest;   
    }
    public static HttpRequestBase listContainers(String url){
        HttpGet request = new HttpGet(url + "?format=json");
        return request;
    }
    public static void readObject(String url, String container, String objectName){
        HttpGet getRequest = new HttpGet(url + "/" + container +
                                      "/" + objectName + "?format=json");
        return getRequest;
    }
    public static HttpRequestBase putObject(String url, String container, 
                                 String objectName, byte [] file){
        HttpPut request = new HttpPut(url + "/" + container +
                                      "/" + objectName + "?format=json");
        request.setEntity(new ByteArrayEntity(file, ContentType.create("SwiftSamples")));

        //request.setEntity(new FileEntity(file, ContentType.create("SwiftSamples")));
        /*MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file, ContentType.create("SwiftSamples"));
        mpEntity.addPart("SwiftExamplefile", cbFile);
        request.setEntity(mpEntity);*/

        return request;
    }
    public static HttpRequestBase delObject(String url, String container, String objectName){
        HttpDelete delRequest = new HttpDelete(url + "/" + container +
                                      "/" + objectName + "?format=json");
        return delRequest;
    }
    public static void sendRequest(CloseableHttpClient client,
                                              HttpRequestBase request){
        addAuthHeader(request);
        BufferedReader response = executeRequest(client, request);
        responseHandler(response);
    }
    public static void readEntity(CloseableHttpClient client, HttpRequestBase request){
        addAuthHeader(request);
        try {
            CloseableHttpResponse response = client.execute(request);
            System.out.println("Response: \n" + response.getStatusLine());
            HttpEntity entity = response.getEntity();
            if(entity != null){
                byte [] file = EntityUtils.toByteArray(entity);
            }  
            return null;            
        } catch (Exception e) {
            System.out.println("Executing the Request caught the following error:");
            System.out.println(e);
        }
    }
    public static void main(String[] args){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        grabAuthSettings();
        HttpRequestBase listAllConts = listContainers(STORAGEURL);
        HttpRequestBase createContCommand = createContainer(STORAGEURL, "SuccessfulContainer");
        byte [] file = new byte[1024];
        HttpRequestBase putCommand = putObject(STORAGEURL, "SuccesfulContainer", "testObject", file);
        HttpRequestBase examineCont = listObjects(STORAGEURL, "SuccessfulContainer");
        HttpRequestBase delContCommand = deleteContainer(httpclient, STORAGEURL, "SuccessfulContainer");
        
        
        sendRequest(httpclient, listAllConts);
        sendRequest(httpclient, createContCommand);
        sendRequest(httpclient, listAllConts);
        sendRequest(httpclient, putCommand);
        sendRequest(httpclient, examineCont);
    }
}
