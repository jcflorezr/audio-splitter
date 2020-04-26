rootProject.name = "transcriber"

include(":core")
include(":audio-splitter:domain")
project(":audio-splitter:domain").name = "audio-splitter-domain"
include(":audio-splitter:adapters")
project(":audio-splitter:adapters").name = "audio-splitter-adapters"
include(":audio-splitter:application")
project(":audio-splitter:application").name = "audio-splitter-application"
include(":audio-transcriber:domain")
project(":audio-transcriber:domain").name = "audio-transcriber-domain"
include(":audio-transcriber:adapters")
project(":audio-transcriber:adapters").name = "audio-transcriber-adapters"
include(":audio-transcriber:application")
project(":audio-transcriber:application").name = "audio-transcriber-application"