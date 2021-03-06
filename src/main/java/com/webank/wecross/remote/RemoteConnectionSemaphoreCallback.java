package com.webank.wecross.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.network.NetworkCallback;
import com.webank.wecross.network.NetworkResponse;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.StubQueryStatus;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RemoteConnectionSemaphoreCallback extends NetworkCallback {
    private Logger logger = LoggerFactory.getLogger(RemoteConnectionSemaphoreCallback.class);

    public transient Semaphore semaphore = new Semaphore(1, true);
    private Response responseData;

    public RemoteConnectionSemaphoreCallback() {
        super.setTypeReference(new TypeReference<NetworkResponse<Response>>() {});
        try {
            semaphore.acquire(1);

        } catch (Exception e) {
            logger.warn("Thread exception", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onResponse(int status, String message, NetworkResponse msg) {
        responseData = (Response) msg.getData();
        semaphore.release();
    }

    public Response getResponseData() {
        try {
            semaphore.acquire(1);
        } catch (Exception e) {
            Thread.interrupted();
            logger.warn("send error", e);
            Response response = new Response();
            response.setErrorCode(StubQueryStatus.REMOTE_QUERY_FAILED);
            response.setErrorMessage("acquire exception: " + e.getMessage());
            return response;
        }
        semaphore.release();
        return responseData;
    }
}
