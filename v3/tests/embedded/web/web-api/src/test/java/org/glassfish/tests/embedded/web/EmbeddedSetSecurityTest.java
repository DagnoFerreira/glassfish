/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.tests.embedded.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests Context#setSecurity
 * 
 * @author Amy Roh
 */
public class EmbeddedSetSecurityTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;                
    static String contextRoot = "security";

    @BeforeClass
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedSetSecurity Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        root = new File("/Users/Amy/tests/security");
        config.setDocRootDir(root);
        config.setListings(true);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }
    
    @Test
    public void test() throws Exception {

        try {

        Context context = embedded.createContext(root);
        embedded.addContext(context, contextRoot);

        FormLoginConfig form = new FormLoginConfig("/login.html", "/error.html");

        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setAuthMethod(AuthMethod.FORM);
        loginConfig.setRealmName("default");
        loginConfig.setFormLoginConfig(form);

        WebResourceCollection webResource = new WebResourceCollection();
        webResource.setName("ServletTest");
        Set<String> urlPatterns = new HashSet<String>();
        urlPatterns.add("/*");
        webResource.setUrlPatterns(urlPatterns);
        Set<String> httpMethods = new HashSet<String>();
        httpMethods.add("GET");
        httpMethods.add("POST");
        webResource.setHttpMethods(httpMethods);
        // This should throw Exception if uncommented
        //webResource.setHttpMethodOmissions(httpMethods);

        SecurityConstraint securityConstraint = new SecurityConstraint();
        Set<WebResourceCollection> webResources = new HashSet<WebResourceCollection>();
        webResources.add(webResource);
        securityConstraint.setWebResourceCollection(webResources);
        securityConstraint.setAuthConstraint("administrator");
        //securityConstraint.setUserDataConstraint(TransportGuarantee.NONE);

        SecurityConfig securityConfig = new SecurityConfig();
        securityConfig.setLoginConfig(loginConfig);
        Set<SecurityConstraint> securityConstraints = new HashSet<SecurityConstraint>();
        securityConstraints.add(securityConstraint);
        securityConfig.setSecurityConstraints(securityConstraints);

        context.setSecurityConfig(securityConfig);

        //    Thread.sleep(100000000);
          /*
        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/ServletTest");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();   */

        embedded.removeContext(context);

        } catch (Exception ex) {
            ex.printStackTrace();
            //ignore for now
        }

    } 

    @AfterClass
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }
    
}
