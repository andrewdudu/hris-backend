package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static List<String> saveFiles(LeaveRequestData data, String employeeId, long currentDateTime) {
        List<String> filesPath = new ArrayList<>();
        for (int i = 0; i < data.getFiles().size(); i++) {

            // Format file is "extension;base64data"
            String[] fileData = data.getFiles().get(i).split(";");
            if (fileData[0] == null || fileData[1] == null){
                String errorsMessage = "files=FILES_INVALID";
                throw new RuntimeException(errorsMessage);
            }
            String extension = "." + fileData[0];
            String base64 = fileData[1];

            String filename = data.getType() + "-" + employeeId + "-" + (i+1) +"-" + currentDateTime + extension;
            String uploadPath = FileConstant.REQUEST_FILE_PATH + filename;

            Path path = Paths.get(uploadPath);
            byte[] imageByte;
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                imageByte = decoder.decodeBuffer(base64);
                Files.write(path, imageByte);
            } catch (IOException e) {
                String errorsMessage = "files=FILES_ERROR";
                throw new RuntimeException(errorsMessage);
            }

            if (extension.equals(".webp")){
                filesPath.add(FileConstant.REQUEST_IMAGE_BASE_URL + filename);
            }
            else {
                filesPath.add(FileConstant.REQUEST_FILE_BASE_URL + filename);
            }
        }
        return filesPath;
    }

}
