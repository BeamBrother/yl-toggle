package io.yltoggle.api.base.hook;

import io.yltoggle.api.base.model.CallbackResult;
import io.yltoggle.api.base.model.HookContext;

public interface ICallback {

    CallbackResult callback(HookContext hookContext, String url, String secretKey);

}
