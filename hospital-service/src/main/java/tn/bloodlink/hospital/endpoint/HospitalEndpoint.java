
package tn.bloodlink.hospital.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import tn.bloodlink.hospital.service.BloodRequestService;
import tn.bloodlink.hospital.domain.BloodRequest;

// Classes générées par JAXB
import tn.bloodlink.hospital.ws.RequestBloodUnitsRequest;
import tn.bloodlink.hospital.ws.RequestBloodUnitsResponse;
import tn.bloodlink.hospital.ws.CheckRequestStatusRequest;
import tn.bloodlink.hospital.ws.CheckRequestStatusResponse;
import tn.bloodlink.hospital.ws.RequestStatus;

@Endpoint
public class HospitalEndpoint {

    private static final String NAMESPACE_URI =
            "http://bloodlink.tn/hospital";

    private final BloodRequestService service;

    // Injection du service métier
    public HospitalEndpoint(BloodRequestService service) {
        this.service = service;
    }

    // Opération 1 : créer une demande de sang
    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "RequestBloodUnitsRequest"
    )
    @ResponsePayload
    public RequestBloodUnitsResponse requestBloodUnits(
            @RequestPayload RequestBloodUnitsRequest request) {

        BloodRequest bloodRequest = service.create(
                request.getHospitalName(),
                request.getBloodType(),
                request.getUnits()
        );

        RequestBloodUnitsResponse response =
                new RequestBloodUnitsResponse();

        response.setRequestId(bloodRequest.getId());
        response.setStatus(
                RequestStatus.valueOf(bloodRequest.getStatus().name())
        );
        response.setMessage(
                "Request " + bloodRequest.getStatus().name()
        );

        return response;
    }

    // Opération 2 : consulter le statut d'une demande
    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "CheckRequestStatusRequest"
    )
    @ResponsePayload
    public CheckRequestStatusResponse checkRequestStatus(
            @RequestPayload CheckRequestStatusRequest request) {

        BloodRequest bloodRequest =
                service.get(request.getRequestId());

        CheckRequestStatusResponse response =
                new CheckRequestStatusResponse();

        response.setRequestId(bloodRequest.getId());
        response.setStatus(
                RequestStatus.valueOf(bloodRequest.getStatus().name())
        );
        response.setBloodType(bloodRequest.getBloodType());
        response.setUnits(bloodRequest.getUnits());

        return response;
    }
}
