package com.example.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    // Implement gRPC service methods here
    @Override
    public void createBillingAccount(BillingRequest billingRequest , StreamObserver<BillingResponse> responseObserver){

        log.info("createBillingAccount request recieved {}", billingRequest.toString());

//        buissnes logic - e.g save to databse , perform calculations etc

        BillingResponse response = BillingResponse.newBuilder()
                .setBillingAccountId("12345")
                .setStatus("ACTIVE")
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
