package com.github.chrisgleissner.springbatchrest.api.jobdetail;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Quartz Job Details")
@RestController
@RequestMapping(value = "/jobDetails", produces = "application/hal+json")
public class JobDetailController {

    @Autowired
    private JobDetailService jobDetailService;

    @ApiOperation("Get a Quartz job detail by Quartz group and job name")
    @GetMapping("/{quartzGroupName}/{quartzJobName}")
    public JobDetailResource get(@PathVariable String quartzGroupName, @PathVariable String quartzJobName) {
        return new JobDetailResource(jobDetailService.jobDetail(quartzGroupName, quartzJobName));
    }

    @ApiOperation("Get all Quartz job details")
    @GetMapping
    public Resources<JobDetailResource> all(@RequestParam(value = "enabled", required = false) Boolean enabled,
                                            @RequestParam(value = "springBatchJobName", required = false) String springBatchJobName) {
        return new Resources<>(jobDetailService.all(Optional.ofNullable(enabled), Optional.ofNullable(springBatchJobName)).stream()
                .map(JobDetailResource::new).collect(toList()),
                ControllerLinkBuilder.linkTo(methodOn(JobDetailController.class).all(enabled, springBatchJobName)).withSelfRel());
    }
}
