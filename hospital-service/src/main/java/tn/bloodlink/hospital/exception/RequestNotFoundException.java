
package tn.bloodlink.hospital.exception;

import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;

@SoapFault(faultCode = FaultCode.CLIENT)
public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(long id) {
        super("Request " + id + " not found");
    }
}
