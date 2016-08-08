package cloud.tracing.demo;

import cloud.context.client.ClientContext;
import cloud.tracing.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dummy RestController
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

@RestController
public class DemoRestController {
    private static Logger log = LoggerFactory.getLogger(DemoRestController.class);

    private final AtomicLong counter = new AtomicLong();
    @Autowired
    RestTemplate restTemplate;
    @Value("${demo.rest.baseurl}")
    String restBaseUri;

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping("/")
    public String index() {
        StringBuilder sb = new StringBuilder();
        sb.append("<H2>cloud-tracing guidelines and sample app</H2>")
                .append("<H3>(Last updated after on 05/27/2016 at 1:20pm on master)</H3>")
                .append("<br/><b>1)  Use a REST client tool (e.g. Postman)</b>")
                .append("<br/><br/><b>2)  Add header params to see in Trace Context e.g. -</b> <br/>X-Context-Session-Id=02263c3b-b249-4d48-8b6d-eb7009120dd0<br/>X-Context-Request-Id=8ac3b305-9bf5-48c4-bb49-79c050451553")
                .append("<br/><br/><b>3)  Add to header so see sample Client Context -</b><br>X-Context-Token=")
                .append("eyJraWQiOiJhcGlLZXkiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ0b2tlbi1zZXJ2aWNlIiwiYXVkIjoiYXBpLXNlcnZpY2VzIiwianRpIjoiRVNHV204QnlQWlpJT2hQUjIwMDNHUSIsImlhdCI6MTQ2MzA4OTgzMiwic3ViIjoiYXJnaGFuaWwiLCJhZ2VudElkIjoiYXJnaGFuaWwiLCJzZXNzaW9uSWQiOiJzZXNzaW9uIiwiYWNxdWlzaXRpb25DaGFubmVsIjoiY2hhbm5lbCIsImNmdCI6ImFwcCIsImNyZWF0ZURhdGUiOiIyMDE2LTA1LTEyVDIxOjUwOjMyLjk3OFoiLCJmaW5nZXJwcmludCI6ImJmcCIsInNvdXJjZUlwIjoiYWRkcmVzcyJ9.KqaxYBi6yt2u4wA-GOE_8BN48OimYMwn5sN7RATlQp2AUoFMhOkB9VOH7CDOxV2tyVrATU1OCfSIenGx5VvCBptdMPzfToLtFUk7ITdwAqeI4IeerxGVFaw26wESYTufUr_PuXRxU15S3AbGpCLWh4Wr1BSmgTxuoR1ku9wNxzRuM8ZrpFigbiHgAfn9eZgoy4LjMIWWzE0Gq4DnywlfZBCVi2jBqTCFRlJxccLWDKJxnUKQ1epEniLizDWxbwdfpIGCqYyiXBOKa0ozSLl6eOLBN87Vii9Qy4Feots0AifbcIHEmFf-2p_eE1VwGnPoE39_wiax4ad5L4B2Iye2iQ")
                .append("<br/><br/><b>4)  Invoke service from REST client tool -</b> <a href='/service1?value=100'>" + restBaseUri + "/service1?value=100</a>")
                .append("<br/><br/><b>5)  Check logs for Trace Context (in MDC) and Client Context. Done.</b> ");

        return sb.toString();
    }

    /**
     * This method in turn calls 2 more RESTful services in a sequence using @restTemplate
     *
     * @param value a dummy @{@link String}
     * @return instance of @{@link ValueObject}
     * @throws MalformedURLException as exception
     */
    @RequestMapping("/service1")
    public ValueObject method1(@RequestParam(value = "value", defaultValue = "empty") String value) throws MalformedURLException {
        log.info(" <--- entering service1 ---> ");
        for (ClientContext.Keys key : ClientContext.Keys.values())
            log.debug("Client context: {} - {}", key, ClientContext.getTrace(key));
        TraceContext.addTrace(TraceContext.Keys.ORDER_ID, "123456789");
        log.info("Order Id: {}", TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        final String template = "Returning from service1 - %s";
        final String uri = restBaseUri + "/service2";
        log.info(uri);
        ValueObject vo = new ValueObject(counter.incrementAndGet(),
                String.format(template, value));
        ResponseEntity<ValueObject> responseEntity = restTemplate.postForEntity(uri, vo, ValueObject.class);
        ValueObject vo2 = responseEntity.getBody();
        return vo2;
    }

    @RequestMapping(value = "/service2", method = RequestMethod.POST)
    public ValueObject method2(@RequestBody ValueObject vo) {
        log.info(" <--- entering service2 ---> ");
        for (ClientContext.Keys key : ClientContext.Keys.values())
            log.info("Client context: {} - {}", key, ClientContext.getTrace(key));
        log.info("Order Id: {}", TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        final String template = "Returning from service2 - %s";
        final String uri = restBaseUri + "/service3";
        ValueObject vo2 = new ValueObject(counter.incrementAndGet(),
                String.format(template, vo.getContent()));
        ResponseEntity<ValueObject> responseEntity = restTemplate.postForEntity(uri, vo2, ValueObject.class);
        ValueObject vo3 = responseEntity.getBody();
        return vo3;
    }

    @RequestMapping(value = "/service3", method = RequestMethod.POST)
    public ValueObject method3(@RequestBody ValueObject vo) {
        log.info(" <--- entering service3 ---> ");
        final String template = "Returning from service3 - %s";
        return new ValueObject(counter.incrementAndGet(),
                String.format(template, vo.getContent()));
    }

    @RequestMapping("/service4")
    public ValueObject method4(@RequestParam(value = "value", defaultValue = "empty") String value) {
        log.info(" <--- entering service1 ---> ");
        for (ClientContext.Keys key : ClientContext.Keys.values())
            log.debug("Client context: {} - {}", key, ClientContext.getTrace(key));
        TraceContext.addTrace(TraceContext.Keys.ORDER_ID, "123456789");
        log.info("Order Id: {}", TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        final String template = "Returning from service4 - %s";
        ValueObject vo = new ValueObject(counter.incrementAndGet(),
                String.format(template, value));
        return vo;
    }

}

