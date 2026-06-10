package miotaxi.aidemo

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks

@Configuration
internal class MdcConfig {
    @PostConstruct
    fun enableMdcPropagation() {
        Hooks.enableAutomaticContextPropagation()
    }
}
