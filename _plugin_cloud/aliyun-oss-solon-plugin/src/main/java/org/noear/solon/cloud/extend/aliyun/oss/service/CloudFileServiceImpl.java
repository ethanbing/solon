package org.noear.solon.cloud.extend.aliyun.oss.service;

import org.noear.solon.cloud.extend.aliyun.oss.OssProps;
import org.noear.solon.cloud.extend.aliyun.oss.utils.Datetime;
import org.noear.solon.cloud.extend.aliyun.oss.utils.HttpUtils;
import org.noear.solon.cloud.service.CloudFileService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 云端文件服务
 *
 * @author noear
 * @since 1.3
 */
public class CloudFileServiceImpl implements CloudFileService {
    protected final String bucket;
    protected final String accessKey;
    protected final String secretKey;
    protected final String endpoint;

    protected String CHARSET_UTF8 = "utf8";
    protected String ALGORITHM = "HmacSHA1";

    public CloudFileServiceImpl(){
        this.endpoint = OssProps.instance.getFileEndpoint();
        this.bucket = OssProps.instance.getFileBucket();
        this.accessKey = OssProps.instance.getFileAccessKey();
        this.secretKey = OssProps.instance.getFileSecretKey();
    }

    public CloudFileServiceImpl(String endpoint, String bucket, String accessKey, String secretKey) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    public String getString(String key) throws Exception {
        String date = Datetime.Now().toGmtString();

        String objPath = "/" + bucket + key;
        String url = "http://" + bucket + "." + endpoint + "/";

        String Signature = (hmacSha1(buildSignData("GET", date, objPath, null), secretKey));

        String Authorization = "OSS " + accessKey + ":" + Signature;

        Map<String, String> head = new HashMap<String, String>();
        head.put("Date", date);
        head.put("Authorization", Authorization);

        return HttpUtils.http(url + key)
                .header("Date", date)
                .header("Authorization", Authorization)
                .get();
    }

    @Override
    public String putString(String key, String content) throws Exception {
        String date = Datetime.Now().toGmtString();

        String objPath = "/" + bucket + key;
        String url = "http://" + bucket + "." + endpoint + "/";
        String contentType = "text/plain; charset=utf-8";

        String Signature = (hmacSha1(buildSignData("PUT", date, objPath, contentType), secretKey));
        String Authorization = "OSS " + accessKey + ":" + Signature;

        return HttpUtils.http(url + key)
                .header("Date", date)
                .header("Authorization", Authorization)
                .bodyTxt(content, contentType)
                .put();
    }

    @Override
    public String putFile(String key, File file) throws Exception {
        String date = Datetime.Now().toGmtString();

        String objPath = "/" + bucket + key;
        String url = "http://" + bucket + "." + endpoint + "/";
        String contentType = "text/plain; charset=utf-8";

        String Signature = (hmacSha1(buildSignData("PUT", date, objPath, contentType), secretKey));
        String Authorization = "OSS " + accessKey + ":" + Signature;


        return HttpUtils.http(url + key)
                .header("Date", date)
                .header("Authorization", Authorization)
                .bodyRaw(new FileInputStream(file), contentType)
                .put();
    }



    private String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(CHARSET_UTF8));

            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildSignData(String method, String date, String objPath, String contentType) {
        if (contentType == null) {
            return method + "\n\n\n"
                    + date + "\n"
                    + objPath;
        } else {
            return method + "\n\n"
                    + contentType + "\n"
                    + date + "\n"
                    + objPath;
        }
    }
}