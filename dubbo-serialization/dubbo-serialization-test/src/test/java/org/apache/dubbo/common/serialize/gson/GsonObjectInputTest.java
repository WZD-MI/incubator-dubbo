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
package org.apache.dubbo.common.serialize.gson;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GsonObjectInputTest {

    private GsonJsonObjectInput input;

    private ByteArrayOutputStream byteArrayOutputStream;

    @BeforeEach
    public void setUp() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Test
    public void test1() throws IOException, ClassNotFoundException {
        Throwable throwable = new Exception("exception");
        System.out.println(new Gson().toJson(throwable));
        GsonJsonObjectOutput output = new GsonJsonObjectOutput(this.byteArrayOutputStream);
        output.writeObject(throwable);
        this.input= new GsonJsonObjectInput(new ByteArrayInputStream(this.byteArrayOutputStream.toByteArray()));
        Throwable t = this.input.readThrowable();
        System.out.println(t);
    }

}
