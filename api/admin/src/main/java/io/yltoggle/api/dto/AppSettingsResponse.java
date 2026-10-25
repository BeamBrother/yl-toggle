package io.yltoggle.api.dto;

import io.yltoggle.api.base.enums.LoginMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppSettingsResponse {

    public LoginMode loginMode;
    public String serverURI;

}
