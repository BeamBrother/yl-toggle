package io.yltoggle.api.server;

import io.yltoggle.api.dto.SdkKeyResponse;
import io.yltoggle.api.dto.ServerResponse;

public interface ServerDataSource {

    SdkKeyResponse queryAllSdkKeys() throws Exception;

    ServerResponse queryServerTogglesByServerSdkKey(String serverSdkKey) throws Exception;

}
