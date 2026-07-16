# D0cCtor's Lives

D0cCtor's Lives is a Minecraft NeoForge mod that adds a daily life system for survival servers.

## Features

- Daily life system.
- Configurable maximum lives.
- Life Fragment item to restore lives.
- Limbo system for players with zero lives.
- Daily reset system.
- Personal HUD for remaining lives.
- Global death messages.
- Respawn effects when a player loses a life.
- Admin commands for managing lives and limbo positions.
- Config file support.

## Minecraft Version

```txt
Minecraft: 1.21.1
NeoForge: 21.1.121
```

## Mod Info

```txt
Mod ID: d0cctors_lives
Display Name: D0cCtor's Lives
Version: 1.0.0
```

## Main Item

```txt
d0cctors_lives:fragmento_del_ciclo
```

## Player Commands

```txt
/lives
```

## Admin Commands

```txt
/vidas reset <player>
/vidas ver <player>
/vidas set <player> <amount>
/vidas limbo <player>
/vidas liberar <player>
/vidas setlimbo
/vidas settown
/vidas setdeathspawn
/vidas tp limbo
/vidas tp town
/vidas tp deathspawn
/vidas config
```

## Config

The config file is generated at:

```txt
config/d0cctors_lives-common.toml
```

## Build

```powershell
gradle clean build
```

The compiled `.jar` will be generated in:

```txt
build/libs/
```

## Notes

This mod is designed for a custom private server environment.
