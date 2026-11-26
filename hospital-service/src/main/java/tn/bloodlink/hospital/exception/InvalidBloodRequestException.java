
package tn.bloodlink.hospital.exception;

import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;

@SoapFault(faultCode = FaultCode.CLIENT)
public class InvalidBloodRequestException extends RuntimeException {

    public InvalidBloodRequestException(String message) {
        super(message);
    }
}
