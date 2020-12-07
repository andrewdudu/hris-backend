package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetFileCommand;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class GetFileCommandImpl implements GetFileCommand {

    @SneakyThrows
    @Override
    public Mono<byte[]> execute(String path) {
        File file = new File(path);

        if (!file.exists()){
            throw new NullPointerException("FILE_NOT_FOUND");
        }

        InputStream in = new FileInputStream(file.getAbsoluteFile());

        return Mono.just(IOUtils.toByteArray(in));
    }

}
