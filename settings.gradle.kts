rootProject.name = "smart_factory_platform"

include(":common:db")
include(":common:apc")
include(":common:define")

include(":collector")
include(":emulator")
include(":portal")
include(":processor")
include(":summarizer")
include(":failover")
include(":apc-processor")
include(":apc-modeling")

include(":gateway")

include(":extensions:scheduler")

include(":extensions:server:proxy-status-gateway")
include(":extensions:server:koreawaida-roboshot")
include(":extensions:server:misillan-alarm")