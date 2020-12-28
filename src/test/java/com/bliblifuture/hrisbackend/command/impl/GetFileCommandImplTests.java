package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetFileCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
public class GetFileCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetFileCommand getFileCommand(){
            return new GetFileCommandImpl();
        }
    }

    @Autowired
    private GetFileCommand getFileCommand;

    @Test
    public void test_execute() throws IOException {
        String request = FileConstant.BASE_STORAGE_PATH + "\\dummy\\image1.webp";

        byte[] fileByte = Files.readAllBytes(
                new File(request).toPath());

        getFileCommand.execute(request)
                .subscribe(response -> {
                    for (int i = 0; i < fileByte.length; i++) {
                        Assert.assertEquals(fileByte[i], response[i]);
                    }
                });
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_NPE() {
        String request = FileConstant.BASE_STORAGE_PATH + "\\dummy\\image10.webp";

        getFileCommand.execute(request).subscribe();
    }
}
