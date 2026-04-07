package miotaxi.aidemo.webapi.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["miotaxi.aidemo.model", "miotaxi.aidemo.usecase.impl", "miotaxi.aidemo.webapi"])
internal class WebApiConfig
