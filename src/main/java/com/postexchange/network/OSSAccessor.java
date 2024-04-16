package com.postexchange.network;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.setting.Setting;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.util.Date;

public class OSSAccessor {

    public static final Setting oss_creds = new Setting("oss.setting");

    public static OSS getOssClient()
    {
        String endpoint = oss_creds.get("endpoint");
        String accessKeyId = oss_creds.get("id");
        String accessKeySecret = oss_creds.get("secret");

        //Init oss instance
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public static void uploadStream(String path, InputStream stream)
    {
        OSS ossClient = getOssClient();
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject(oss_creds.get("bucket"), path, stream);
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    public static void uploadString(String path, String content)
    {
        uploadStream(path, new ByteArrayInputStream(content.getBytes()));
    }

    public static void logError(Throwable t)
    {
        String error = ExceptionUtil.stacktraceToString(t);
        uploadString("debug/"+ DateUtil.today()+"/err-"+DateUtil.format(new Date(), "HH:mm:ss")+"-"+error.length()+".txt", error);
    }




}
