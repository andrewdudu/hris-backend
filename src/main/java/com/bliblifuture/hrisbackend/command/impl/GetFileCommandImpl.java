package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetFileCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.repository.EventRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class GetFileCommandImpl implements GetFileCommand {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<byte[]> execute(String filename) {
        String path = FileConstant.REQUEST_FILE_PATH + filename;

        File file = new File(path);

        if (!file.exists()){
            throw new NullPointerException("FILE_NOT_FOUND");
        }

        InputStream in = new FileInputStream(file.getAbsoluteFile());

        return Mono.just(IOUtils.toByteArray(in));
    }

}
