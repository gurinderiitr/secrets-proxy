/**
 * *****************************************************************************
 *
 * <p>Copyright 2017 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.proxy.gateway;

import java.util.Collections;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.*;

/**
 * A transparent proxy servlet, which just forward http requests to the configured <b>proxyTo</b>
 * server after stripping down the {@link #X_AUTH_HEADER} token header. If you want to implement
 * more sophisticated proxy routing logic, can do the same in {@link
 * #rewriteTarget(HttpServletRequest)} method.
 *
 * @author Suresh G
 * @see <a href="https://en.wikipedia.org/wiki/Proxy_server#Transparent_proxy">Transparent Proxy</a>
 */
public class ProxyServlet extends org.eclipse.jetty.proxy.ProxyServlet.Transparent {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Client request authorization header. This needs to be removed from the proxy request before
   * forwarding to target <b>proxyTo</b> server.
   */
  private String X_AUTH_HEADER;

  /** Trust all proxyTo connections?. */
  private boolean trustAllCerts;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    X_AUTH_HEADER = getInitParameter(InitParams.xAuthHeader.name());
    trustAllCerts = Boolean.parseBoolean(getInitParameter(InitParams.trustAll.name()));
    log.info(
        "Initializing the Http Proxy with params: "
            + Collections.list(config.getInitParameterNames()));
  }

  /**
   * Async http client used to connect to <b>proxyTo</b> server. TLS config is usually done here.
   *
   * @return {@link HttpClient}
   */
  @Override
  protected HttpClient newHttpClient() {
    SslContextFactory sslFactory = new SslContextFactory();
    sslFactory.setTrustAll(trustAllCerts);
    return new HttpClient(sslFactory);
  }

  /**
   * Customize the headers of forwarding proxy requests. Make sure to remove all the token headers
   * from the proxy request.
   */
  @Override
  protected void addProxyHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
    super.addProxyHeaders(clientRequest, proxyRequest);
    proxyRequest.getHeaders().remove(X_AUTH_HEADER);
  }

  @Override
  protected String rewriteTarget(HttpServletRequest request) {
    return super.rewriteTarget(request);
  }

  /** Default servlet init params. */
  public enum InitParams {
    proxyTo,
    prefix,
    viaHost,
    trustAll,
    xAuthHeader
  }
}
