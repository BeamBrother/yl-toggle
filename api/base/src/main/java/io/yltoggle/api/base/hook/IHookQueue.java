package io.yltoggle.api.base.hook;

import io.yltoggle.api.base.model.HookContext;

public interface IHookQueue {

    boolean push(HookContext hookContext);

    HookContext take() throws Exception;

}
