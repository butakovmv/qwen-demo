package miotaxi.aidemo.webapi

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [miotaxi.aidemo.webapi.WebapiMarker::class])
internal class WebApiConfig
