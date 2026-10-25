package io.yltoggle.api.dto;

import io.yltoggle.api.base.enums.OrganizationRoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberCreateRequest {

    @Schema(description = "A system-unique account used to reference the member.")
    @NotNull
    private List<String> accounts;

    @Schema(description = "Mark member registration source.")
    private String source;

    @Schema(description = "Built-in role assigned to new member.")
    @NotNull
    private OrganizationRoleEnum role;

    @Schema(description = "Assign a password for new member to login.")
    @NotBlank
    private String password;

}
