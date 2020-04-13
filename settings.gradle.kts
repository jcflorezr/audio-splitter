rootProject.name = "transcriber"

include(":audio-splitter:domain", ":audio-splitter:adapters", ":audio-splitter:application")
include(":audio-transcriber:domain", ":audio-transcriber:adapters", ":audio-transcriber:application")
include(":core")
