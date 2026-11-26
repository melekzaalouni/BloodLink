
package tn.bloodlink.hospital.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.server.EndpointInterceptor;

import java.util.List;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    // Bean 1 : configuration du servlet SOAP
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet>
    messageDispatcherServlet(ApplicationContext applicationContext) {

        MessageDispatcherServlet servlet =
                new MessageDispatcherServlet();

        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);

        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    // Bean 2 : chargement du fichier XSD
    @Bean
    public XsdSchema hospitalSchema() {
        return new SimpleXsdSchema(
                new ClassPathResource("xsd/hospital.xsd")
        );
    }

    // Bean 3 : génération automatique du WSDL
    @Bean(name = "hospital")
    public DefaultWsdl11Definition defaultWsdl11Definition(
            XsdSchema hospitalSchema) {

        DefaultWsdl11Definition definition =
                new DefaultWsdl11Definition();

        definition.setPortTypeName("HospitalPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace(
                "http://bloodlink.tn/hospital"
        );
        definition.setSchema(hospitalSchema);

        return definition;
    }

    // Validation des requêtes et des réponses SOAP
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {

        PayloadValidatingInterceptor interceptor =
                new PayloadValidatingInterceptor();

        interceptor.setXsdSchema(hospitalSchema());
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);

        interceptors.add(interceptor);
    }
}
