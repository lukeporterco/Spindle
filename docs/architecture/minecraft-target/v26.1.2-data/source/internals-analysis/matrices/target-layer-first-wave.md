# Target Layer First Wave

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

| Concept | First-wave status | Include form | Not included |
|---|---|---|---|
| Ticking | include | server tick and level tick events | scheduled block/fluid subsystem |
| Commands | include limited | registration-time/reload-aware | accessor or sync hook semantics |
| Server lifecycle | include limited | post-load, dedicated started, stopping observations | universal ready guarantee |
| World and level | include limited | guarded lookup/enumeration | storage/persistence APIs |
| Registries | include limited | lookup-only | mutation/contribution |
| Resources and datapacks | event-only/document | reload start/complete observation | contribution/listener insertion |
| Networking | optional limited | server phase events | raw packet APIs |
| Data generation and assets | defer | documentation bridge | public datagen API |
