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
package org.apache.dubbo.rpc.cluster.support;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UnicastClusterInvoker
 */
public class UnicastClusterInvoker<T> extends AbstractClusterInvoker<T> {

    public UnicastClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        Address address = Address.class.cast(invocation.getObjectAttachment("address"));
        if (!Optional.ofNullable(address).isPresent()) {
            throw new RpcException("Address can not be empty");
        }
        return invokers.stream().filter(it -> {
            URL url = it.getUrl();
            return address.getIp().equals(url.getIp()) && (address.getPort() == url.getPort()) && it.isAvailable();
        }).findAny().orElseThrow(() -> new RpcException("No provider available in " + invokers.stream().map(it-> it.getUrl().getIp()+":"+it.getUrl().getPort()).collect(Collectors.joining(",")))).invoke(invocation);
    }
}
