package se.vgregion.push.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class HttpUtil {

    public static boolean successStatus(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status >= 200 && status <300;
    }
    
    public static String readContent(HttpEntity entity) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entity.writeTo(out);
        
        return out.toString("UTF-8");
    }


}
