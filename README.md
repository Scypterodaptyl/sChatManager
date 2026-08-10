# sChatManager

A Bukkit/Spigot/Paper plugin that turns off chat completely and blocks blacklisted commands.
No permissions, no bypasses — the block applies to every player equally.

One jar covers every server version from **1.8 to 26.2**.

## Features

- Disable player chat server-wide with a single config flag.
- Block commands by blacklist, or block all commands at once.
- Fully customizable response messages, including multi-line ones.
- `&` color codes everywhere, plus `&#RRGGBB` hex colors on 1.16+.
- No permission nodes and no bypass logic of any kind.

## Installation

1. Drop `sChatManager-1.0.0.jar` into the server's `plugins` folder.
2. Start the server once to generate `plugins/sChatManager/config.yml`.
3. Edit the config and run `/schatmanager reload`.

## Configuration

```yaml
chat:
  enabled: false
  message: "&cЧат на сервере отключён."

commands:
  block-all: false
  blacklist:
    - "msg"
    - "tell"
    - "w"
    - "r"
    - "me"
    - "say"
  message: "&cЭта команда заблокирована."

reload-message: "&aКонфигурация sChatManager перезагружена."
```


### Messages

Any message may be a single string or a list of strings, in which case each entry is sent as
its own line:

```yaml
chat:
  message:
    - "&cЧат отключён."
    - "&7Обратитесь к администрации."
```

Setting a message to `""` sends nothing at all — the action is still blocked, just silently.

### Blacklist matching

Entries are matched against the first word of the command, case-insensitively. A leading `/`
and a namespace prefix are stripped from both sides, so `tell`, `/tell` and `minecraft:tell`
are all the same entry.

An entry containing spaces is matched as a prefix, which lets you block a single subcommand:

```yaml
blacklist:
  - "gamemode creative"
```

This blocks `/gamemode creative Steve` but leaves `/gamemode survival` working.

## Commands

| Command | Description |
| --- | --- |
| `/schatmanager reload` | Reloads `config.yml`. Aliases: `/scm`, `/schat`. |

The reload command is available to the console and to operators. This is an `isOp()` check,
not a permission node — the plugin registers no permissions at all.

`/schatmanager` is the only command exempt from `block-all`, otherwise there would be no way
to turn blocking off from in-game.

## Building

```
gradle build
```

Requires JDK 21 to build, since JDK 25 can no longer target Java 8. The `verifyModernApi`
task additionally recompiles the sources against `paper-api 26.2` and needs a JDK 25
installed for its toolchain; it runs as part of `check`.


## License

See [LICENSE](LICENSE).
