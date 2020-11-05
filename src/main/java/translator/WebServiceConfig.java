package translator;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

import java.util.List;
import java.util.Properties;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
    @Bean
    public ServletRegistrationBean<HttpServletBean> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "translator")
    public SimpleWsdl11Definition defaultWsdl11Definition() {
        SimpleWsdl11Definition s = new SimpleWsdl11Definition();
        s.setWsdl(new ClassPathResource("schemas/xjc/translator.wsdl"));
        return s;
    }

    // Following methods add configuration for securing the WebService with wss4j
    // References:
    //  - https://docs.spring.io/spring-ws/docs/3.0.11.BUILD-SNAPSHOT/reference/#security-wss4j-security-interceptor
    //  - https://memorynotfound.com/spring-ws-username-password-authentication-wss4j/

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(securityInterceptor());
    }

    /**
     * Adds the Wss4jSecurityInterceptor (interceptor based on Apache's WSS4J).
     * The interceptor is configured with validation actions (server) and securement actions (client).
     *
     * @return
     */
    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
        securityInterceptor.setValidationActions("Timestamp UsernameToken");
        // Validating Timestamps
        securityInterceptor.setTimestampStrict(true);
        securityInterceptor.setValidationTimeToLive(10);
        // Validating Users
        securityInterceptor.setValidationCallbackHandler(securityCallbackHandler());
        return securityInterceptor;
    }

    /**
     * Security Callback Handler that adds the user "root" with password "toor"
     */
    @Bean
    public SimplePasswordValidationCallbackHandler securityCallbackHandler() {
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        Properties users = new Properties();
        users.setProperty("root", "toor");
        callbackHandler.setUsers(users);
        return callbackHandler;
    }
}
