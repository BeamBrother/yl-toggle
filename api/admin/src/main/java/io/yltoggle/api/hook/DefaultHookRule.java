package io.yltoggle.api.hook;

import io.yltoggle.api.base.hook.IHookRule;
import io.yltoggle.api.base.model.HookContext;

public class DefaultHookRule implements IHookRule {

    @Override
    public boolean isHid(HookContext hookContext) {
        return true;
    }

}
