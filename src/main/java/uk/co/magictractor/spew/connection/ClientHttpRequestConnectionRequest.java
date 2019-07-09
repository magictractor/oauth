/**
 * Copyright 2015-2019 Ken Dobson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.magictractor.spew.connection;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.http.client.ClientHttpRequest;

public class ClientHttpRequestConnectionRequest implements ConnectionRequest {

    private final ClientHttpRequest clientHttpRequest;

    public ClientHttpRequestConnectionRequest(ClientHttpRequest clientHttpRequest) {
        this.clientHttpRequest = clientHttpRequest;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return clientHttpRequest.getBody();
    }

    // this could be the default?
    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        setHeader("Content-Length", Long.toString(contentLength));
    }

    @Override
    public void setHeader(String headerName, String headerValue) {
        clientHttpRequest.getHeaders().add(headerName, headerValue);
    }

    // this could be the default?
    @Override
    public void setDoOutput(boolean b) {
        // Do nothing.
    }

}
