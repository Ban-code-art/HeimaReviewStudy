package com.itheima.springbootadmin_client.actuator;

import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "pay",defaultAccess = Access.READ_ONLY)
public class PayEndpoint {
}
