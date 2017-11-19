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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import no.priv.bang.debugutils.mocks.MockLogService;


@SuppressWarnings("unchecked")
public class PostBodyCaptureServletTest {

    @Test
    public void testDoPost() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("http://localhost:8181/post-body-capture");
        when(request.getInputStream()).thenReturn(wrap(getClass().getClassLoader().getResourceAsStream("test.json")));
        HttpServletResponse response = mock(HttpServletResponse.class);

        PostBodyCaptureServlet servlet = new PostBodyCaptureServlet();
        servlet.setLogservice(logservice);
        
        servlet.doPost(request, response);
        assertEquals(1, logservice.getLogmessages().size());
        assertThat(logservice.getLogmessages().get(0), containsString("Saved body of POST to"));
        
        // Clean up from test.  If that fails, that's also a test of sorts
        Path bodypostfile = getFilenameFromLogMessage(logservice.getLogmessages().get(0));
        Files.delete(bodypostfile);
        System.out.println(String.format("Deleted temp file %s", bodypostfile));
    }

    @Test
    public void testDoPostWhenRequestInputStreamThrowsIOException() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletInputStream inputstream = mock(ServletInputStream.class);
        when(inputstream.read()).thenThrow(IOException.class);
        when(request.getInputStream()).thenReturn(inputstream);
        HttpServletResponse response = mock(HttpServletResponse.class);

        PostBodyCaptureServlet servlet = new PostBodyCaptureServlet();
        servlet.setLogservice(logservice);
        
        servlet.doPost(request, response);
        assertEquals(1, logservice.getLogmessages().size());
        assertThat(logservice.getLogmessages().get(0), containsString("[ERROR] Sonar Collector caught exception"));
    }

    private Path getFilenameFromLogMessage(String logmessage) {
        String[] logmessageparts = logmessage.split("to file: ");
        return Paths.get(logmessageparts[logmessageparts.length - 1]);
    }

    private ServletInputStream wrap(InputStream inputStream) {
        return new ServletInputStream() {
    
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
    
            @Override
            public void setReadListener(ReadListener readListener) {
                // TODO Auto-generated method stub
    
            }
    
            @Override
            public boolean isReady() {
                // TODO Auto-generated method stub
                return false;
            }
    
            @Override
            public boolean isFinished() {
                // TODO Auto-generated method stub
                return false;
            }
        };
    }
}
