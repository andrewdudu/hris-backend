package com.bliblifuture.hrisbackend.constant;

public class FileConstant {

    public final static String BASE_STORAGE_PATH = System.getProperty("user.dir") + "\\file";

    public final static String IMAGE_ATTENDANCE_PATH = FileConstant.BASE_STORAGE_PATH + "\\attendance-image\\";

    public final static String IMAGE_ATTENDANCE_BASE_URL = "/api/attendances/image/";

    public final static String REQUEST_FILE_PATH = FileConstant.BASE_STORAGE_PATH + "\\request-file\\";

    public final static String REQUEST_FILE_BASE_URL = "/api/request/file/pdf/";

    public final static String REQUEST_IMAGE_BASE_URL = "/api/request/file/image/";

}
