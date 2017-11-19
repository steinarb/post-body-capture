/*
 * Copyright 2017 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.debugutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

@Component(service={Servlet.class}, property={"alias=/post-body-capture"} )
public class PostBodyCaptureServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private LogService logservice;

    @Reference
    public void setLogservice(LogService logservice) {
        this.logservice = logservice;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            File temp = File.createTempFile("postbodycaptureservlet", ".json");
            try(FileOutputStream tempStream = new FileOutputStream(temp)) {
                try(InputStream body = request.getInputStream()) {
                    int c = body.read();
                    while(c != -1) {
                        tempStream.write(c);
                        c = body.read();
                    }
                }
            }

            logservice.log(LogService.LOG_INFO, String.format("Saved body of POST to %s to file: %s", request.getRequestURI(), temp.getAbsolutePath()));
        } catch (Exception e) {
            logservice.log(LogService.LOG_ERROR, "Sonar Collector caught exception ", e);
            response.setStatus(500); // Report internal server error
        }
    }

}
