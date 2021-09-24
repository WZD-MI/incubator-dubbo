/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apache.dubbo.demo.graalvm.consumer;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import org.apace.dubbo.graalvm.demo.DemoService;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Application {

    public static void main(String[] args) {
        runWithBootstrap();
    }

    private static void runWithBootstrap() {
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setInterface(DemoService.class);
        reference.setGeneric("false");

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        ApplicationConfig applicationConfig = new ApplicationConfig("dubbo-demo-api-consumer");
        applicationConfig.setQosEnable(false);

        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setPort(5555);
        protocolConfig.setTransporter("quic");


        bootstrap.application(applicationConfig)
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .reference(reference)
            .start();

        DemoService demoService = bootstrap.getCache().get(reference);
        IntStream.range(0, 100).forEach(i -> {
            try {
                String message = demoService.sayHello("quic");
                System.out.println(message);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });
    }

}
