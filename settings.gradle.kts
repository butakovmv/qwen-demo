rootProject.name = "otus"

buildCache {
    local {
        directory = File(rootDir, ".gradle/build-cache")
    }
}

include("operation")
include("postgres")
include("app")
include("web-api")
include("arch-tests")
include("front")
include("ui-test")
