# Spindle Loader CLI

`spindle-loader-cli` contains the executable entrypoint and application wiring for Spindle Loader.

It owns `LoaderMain`, CLI parsing, resolved launch arguments, and provider-selection wiring. It depends on `spindle-loader-core` and can wire in target modules such as `target-minecraft` without making those targets part of the stable loader API.
