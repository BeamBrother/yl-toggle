package io.yltoggle.api.base.hook;

import io.yltoggle.api.base.model.HookContext;

public interface IHookRule {

    boolean isHid(HookContext hookContext);

}
