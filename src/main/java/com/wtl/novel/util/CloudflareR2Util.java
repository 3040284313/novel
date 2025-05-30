package com.wtl.novel.util;

import com.wtl.novel.entity.ChapterImageLink;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudflareR2Util {

    // Cloudflare R2 配置
    private static final String ACCOUNT_ID = "1695ffc38d5*****055757d56";
    private static final String ACCESS_KEY = "c87abd30bdc*****27f421abb0c3";
    private static final String SECRET_KEY = "85c2141304d1d1*****d931d238d6b9323b027b2eb";
    private static final String BUCKET_NAME = "photo";
    private static final String ENDPOINT = String.format("https://%s.r2.cloudflarestorage.com", ACCOUNT_ID);
    private static final Region REGION = Region.of("auto");
    private static final S3Client S3_CLIENT;

    static {
        // 初始化 Cloudflare R2 客户端
        AwsBasicCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        S3_CLIENT = S3Client.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(REGION)
                .build();
    }

//    public static void main(String[] args) throws SQLException {
//        List<ChapterImageLink> imageList = null;
//        // 使用批量更新
//        batchProcessImages(imageList);
//    }
    
    // 下载图片到本地
    public static String downloadImageToLocalStorage(String imageUrl,String localFilePath) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000); // 设置读取超时时间为30秒
            connection.setConnectTimeout(30000); // 设置连接超时时间为30秒
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.err.println("下载图片失败，HTTP 状态码：" + connection.getResponseCode());
                return null;
            }
            Path path = Paths.get(localFilePath);
            Files.createDirectories(path.getParent()); // 创建目录
            // 创建本地临时文件
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(localFilePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return localFilePath;
        } catch (Exception e) {
            System.err.println("下载图片到本地失败：" + imageUrl + "，错误：" + e.getMessage());
            return null;
        }
    }


    // 上传图片到 Cloudflare R2
    public static String uploadImageToCloudflareR2(String localFilePath, String objectKey) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .contentType(getContentType(localFilePath))
                    .build();
            S3_CLIENT.putObject(putObjectRequest, RequestBody.fromFile(new File(localFilePath)));
            return "https://" + BUCKET_NAME + "." + ACCOUNT_ID + ".r2.cloudflarestorage.com/" + objectKey;
        } catch (Exception e) {
            System.err.println("上传图片到 Cloudflare R2 失败：" + objectKey + "，错误：" + e.getMessage());
            return null;
        }
    }

    // 上传图片到 Cloudflare R2
    public static String uploadImageToCloudflareR2(File localFile, String objectKey) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .contentType(getContentType(localFile.getCanonicalPath()))
                    .build();
            S3_CLIENT.putObject(putObjectRequest, RequestBody.fromFile(localFile));
            return "https://" + BUCKET_NAME + "." + ACCOUNT_ID + ".r2.cloudflarestorage.com/" + objectKey;
        } catch (Exception e) {
            System.err.println("上传图片到 Cloudflare R2 失败：" + objectKey + "，错误：" + e.getMessage());
            return null;
        }
    }

    // 根据文件扩展名获取 Content-Type
    public static String getContentType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            default:
                return "application/octet-stream";
        }
    }

    // 替换图片链接
    public static String replaceImageUrl(String contentLink, String oldSrc, String newSrc) {
        // 使用正则表达式匹配 src 属性并替换其值
        return contentLink.replaceAll("src\\s*=\\s*\"" + Pattern.quote(oldSrc) + "\"", "src=\"" + newSrc + "\"");
    }

    public static String getTargetDirectoryBasedOnOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        String targetDirectory;

        if (osName.contains("win")) {
            // Windows 系统
            targetDirectory = "C:\\Users\\30402\\Desktop\\NTR\\333\\"; // 示例路径，根据实际情况修改
        } else {
            // Linux 系统
            targetDirectory = "/home/novel/tmp/"; // 示例路径，根据实际情况修改
        }

        // 创建目标目录（如果不存在）
        File directory = new File(targetDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return targetDirectory;
    }

    public static String getTargetDirectoryBasedOnOS1() {
        String osName = System.getProperty("os.name").toLowerCase();
        String targetDirectory;

        if (osName.contains("win")) {
            // Windows 系统
            targetDirectory = "C:\\Users\\30402\\Desktop\\NTR\\333\\"; // 示例路径，根据实际情况修改
        } else {
            // Linux 系统
            targetDirectory = "/home/novel/file/"; // 示例路径，根据实际情况修改
        }

        // 创建目标目录（如果不存在）
        File directory = new File(targetDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return targetDirectory;
    }

    // 使用 Files.copy 方法保存文件到本地
    public static String saveToFileSystem(MultipartFile multipartFile, String targetDirectory) throws IOException {
        String s = targetDirectory + File.separator + multipartFile.getOriginalFilename();
        File directory = new File(s);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Path targetPath = Path.of(s);
        Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return s;
    }
}