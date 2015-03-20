package com.connectsdk.service;

import android.content.Context;

import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.command.ServiceCommand;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Oleksii Frolov on 3/19/2015.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AirPlayServiceTest {

    private StubAirPlayService service;

    class StubAirPlayService extends AirPlayService {

        private Map<String, String> lastParams;
        private String lastMethod;
        private Object response;

        public StubAirPlayService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) throws IOException {
            super(serviceDescription, serviceConfig);
        }

        public void setResponse(Object response) {
            this.response = response;
        }

        @Override
        public void sendCommand(ServiceCommand<?> serviceCommand) {
            serviceCommand.getResponseListener().onSuccess(response);
        }

        Context getContext() {
            return Robolectric.application;
        }

    }

    @Before
    public void setUp() throws IOException {
        service = new StubAirPlayService(Mockito.mock(ServiceDescription.class), Mockito.mock(ServiceConfig.class));
    }

    @Test
    public void testGetPlayStateFinished() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        service.setResponse(
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +
                "<plist version=\"1.0\">" +
                "<dict>" +
                "</dict>" +
                "</plist>"
        );
        service.getPlayState(new MediaControl.PlayStateListener() {
            @Override
            public void onSuccess(MediaControl.PlayStateStatus object) {
                Assert.assertEquals(MediaControl.PlayStateStatus.Finished, object);
                latch.countDown();
            }

            @Override
            public void onError(ServiceCommandError error) {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    public void testGetPlayStatePlaying() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        service.setResponse(
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +
                "<plist version=\"1.0\">" +
                "<dict>" +
                "<key>rate</key>" +
                "<real>1</real>" +
                "</dict>" +
                "</plist>"
        );
        service.getPlayState(new MediaControl.PlayStateListener() {
            @Override
            public void onSuccess(MediaControl.PlayStateStatus object) {
                Assert.assertEquals(MediaControl.PlayStateStatus.Playing, object);
                latch.countDown();
            }

            @Override
            public void onError(ServiceCommandError error) {
                latch.countDown();
            }
        });
        latch.await();
    }
}
