package io.yltoggle.api.hook;

import io.yltoggle.api.base.hook.IHookRule;
import io.yltoggle.api.base.hook.IHookRuleBuilder;
import org.springframework.stereotype.Component;

@Component
public class DefaultHookRuleBuilder implements IHookRuleBuilder {

    @Override
    public IHookRule build(Long settingsId) {
        return new DefaultHookRule();
    }

}
