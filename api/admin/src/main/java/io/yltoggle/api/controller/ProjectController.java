package io.yltoggle.api.controller;

import io.yltoggle.api.base.doc.CreateApiResponse;
import io.yltoggle.api.base.doc.DefaultApiResponses;
import io.yltoggle.api.base.doc.DeleteApiResponse;
import io.yltoggle.api.base.doc.GetApiResponse;
import io.yltoggle.api.base.doc.PatchApiResponse;
import io.yltoggle.api.base.doc.ProjectKeyParameter;
import io.yltoggle.api.base.hook.Action;
import io.yltoggle.api.base.hook.Resource;
import io.yltoggle.api.base.hook.Hook;
import io.yltoggle.api.dto.ApprovalSettingsResponse;
import io.yltoggle.api.dto.PreferenceCreateRequest;
import io.yltoggle.api.dto.ProjectCreateRequest;
import io.yltoggle.api.dto.ProjectQueryRequest;
import io.yltoggle.api.dto.ProjectResponse;
import io.yltoggle.api.dto.ProjectUpdateRequest;
import io.yltoggle.api.base.enums.ResponseCodeEnum;
import io.yltoggle.api.base.enums.ValidateTypeEnum;
import io.yltoggle.api.base.model.BaseResponse;
import io.yltoggle.api.service.ProjectService;
import io.yltoggle.api.validate.ResourceExistsValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/projects")
@DefaultApiResponses
@ProjectKeyParameter
@ResourceExistsValidate
@Tag(name = "Projects", description = "The projects API allows you to list, create, modify, " +
        "query and delete project programmatically. <br/> You can also query and update approval settings.")
public class ProjectController {

    private ProjectService projectService;

    @PostMapping
    @CreateApiResponse
    @Operation(summary = "Create project", description = "Create a new project.")
    @Hook(resource = Resource.PROJECT, action = Action.CREATE)
    public ProjectResponse create(@RequestBody @Validated ProjectCreateRequest createRequest) {
        return projectService.create(createRequest);
    }

    @PatchMapping("/{projectKey}")
    @PatchApiResponse
    @Operation(summary = "Update project", description = "Update a project.")
    @Hook(resource = Resource.PROJECT, action = Action.UPDATE)
    public ProjectResponse update(@PathVariable("projectKey") String projectKey,
                                  @RequestBody @Validated ProjectUpdateRequest updateRequest) {
        return projectService.update(projectKey, updateRequest);
    }

    @DeleteMapping("/{projectKey}")
    @DeleteApiResponse
    @Operation(summary = "Delete project", description = "Delete a project.")
    @Hook(resource = Resource.PROJECT, action = Action.DELETE)
    @PreAuthorize("hasAnyAuthority('OWNER')")
    public ProjectResponse delete(@PathVariable("projectKey") String projectKey) {
        return projectService.delete(projectKey);
    }

    @GetMapping
    @GetApiResponse
    @Operation(summary = "List projects", description = "Fetch a list of all projects.")
    public Object list(ProjectQueryRequest queryRequest) {
        return projectService.list(queryRequest);
    }

    @GetMapping("/{projectKey}")
    @GetApiResponse
    @Operation(summary = "Query project", description = "Get a single project by key.")
    public ProjectResponse query(
            @PathVariable("projectKey") String projectKey) {
        return projectService.queryByKey(projectKey);
    }

    @GetMapping("/exists")
    @GetApiResponse
    @Operation(summary = "Check project exist", description = "Check project exist.")
    public BaseResponse exists(
            @RequestParam @Schema(description = "The type needs to be checked.") ValidateTypeEnum type,
            @RequestParam @Schema(description = "The attribute value to be checked.") String value) {
        projectService.validateExists(type, value);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }

    @PatchMapping("/{projectKey}/approvalSettings")
    @PatchApiResponse
    @Operation(summary = "Update approval settings", description = "Update approval settings for project.")
    @Hook(resource = Resource.PROJECT, action = Action.UPDATE_APPROVAL_SETTINGS)
    public List<ApprovalSettingsResponse> updateApprovalSettings(@PathVariable("projectKey") String projectKey,
                                                                 @RequestBody @Validated
                                                                 PreferenceCreateRequest createRequest) {
        return projectService.updateApprovalSettings(projectKey, createRequest);
    }

    @GetMapping("/{projectKey}/approvalSettings")
    @GetApiResponse
    @Operation(summary = "Get approval settings", description = "Get approval settings for project.")
    public List<ApprovalSettingsResponse> approvalSettingsList(@PathVariable("projectKey") String projectKey) {
        return projectService.approvalSettingsList(projectKey);
    }

}
