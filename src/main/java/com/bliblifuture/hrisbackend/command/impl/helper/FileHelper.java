package com.bliblifuture.hrisbackend.command.impl.helper;

import com.blibli.oss.command.exception.CommandValidationException;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.util.DateUtil;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileHelper {

    public static String getExtension(String base64){
        String[] fileData = base64.split(",");
        String[] dataURI = fileData[0].split("/");
        String[] typeBase64 = dataURI[1].split(";");

        return "." + typeBase64[0];
    }

    public static List<String> saveFiles(LeaveRequestData data, String employeeId) {
        DateUtil dateUtil = new DateUtil();
        List<String> filesPath = new ArrayList<>();
        for (String base64 : data.getFiles()) {
            String filename = data.getType() + employeeId + "_" + dateUtil.getNewDate().getTime() + getExtension(base64);
            String uploadPath = FileConstant.REQUEST_FILE_PATH + filename;

            Path path = Paths.get(uploadPath);
            byte[] imageByte;
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                imageByte = decoder.decodeBuffer(base64);
                Files.write(path, imageByte);
            } catch (IOException e) {
                throw new CommandValidationException(Collections.singleton("INVALID_FORMAT"));
            }
            filesPath.add(FileConstant.REQUEST_FILE_BASE_URL + filename);
        }
        return filesPath;
    }

}
