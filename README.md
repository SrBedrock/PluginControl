# PluginControl

## Description

This plugin allows you to control which plugins have to be enabled for the server continues running.

Inspired by [this](https://github.com/PaperMC/Paper/issues/8859#issuecomment-1435905791)
and [this](https://github.com/PaperMC/Paper/pull/8108#issuecomment-1419304955) comment and myself having the same
problem.

## Installation

1. Download the PluginControl plugin from [here](https://github.com/SrBedrock/PluginControl/releases/).
2. Place the downloaded plugin file in the `plugins` folder of your server.
3. Restart the server to load the PluginControl plugin.
4. Use the command `/plugincontrol action <action-type>` to set the action to be taken if any of the listed plugins are
   not enabled. See the [Actions](#actions) section for available actions.
5. Add plugins to the list using the command `/plugincontrol add <plugin-name>`.
6. Enable the PluginControl plugin by running the command `/plugincontrol enable`.
7. Reload the PluginControl plugin configuration and language files with the command `/plugincontrol reload`.

Please note that the `<action-type>` and `<plugin-name>` placeholders should be replaced with the specific action and
plugin names as needed.

**Note: This plugin needs Java 17 to work.**

## Configuration

| Option         | Description                                                                    |
|----------------|--------------------------------------------------------------------------------|
| `enabled`      | Whether the plugin is enabled or not.                                          |
| `action`       | [Action to take if all listed plugins are not enabled.](#actions)              |
| `kick-message` | Message sent to the player when the `disallow-player-login` action is enabled. | 
| `plugins`      | List of plugins to be enabled when the server starts.                          |

### Actions

| Action Type             | Description                                                                           |
|-------------------------|---------------------------------------------------------------------------------------|
| `log-to-console`        | Sends a warning (`log-to-console` inside lang.yaml) in the console.                   |
| `disallow-player-login` | Block player from enter the server with a message (`kick-message` inside config.yml). |
| `shutdown-server`       | Shutdown the server with a warning (`disabling-server` inside lang.yml).              |

### Kick Message

Allows you to customize the message sent to the player when the `disallow-player-login` action is enabled.

Can be customized using `&#<hex>` code or legacy color codes (no MiniMessage support).

## Messages

Change the message formatting using [MiniMessage](https://webui.advntr.dev/)

### Placeholders

| Placeholder      | Usage                                                                                     |
|------------------|-------------------------------------------------------------------------------------------|
| `<prefix>`       | All messages accept this placeholder                                                      |
| `<action>`       | `command.action-type`                                                                     |
| `<actions>`      | `command.action-list`                                                                     |
| `<command>`      | `command.command-not-found`, `command.plugin-add-error` and `command.plugin-remove-error` |
| `<kick-message>` | `command.kick-message` and `command.kick-message-set`                                     |
| `<plugin>`       | `command.plugin-added`, `command.plugin-not-found` and `command.plugin-removed`           |
| `<plugins>`      | `console.disabling-server` and `command.plugin-list`                                      |

### Default

```yaml
prefix: '<dark_gray>[<red>PluginControl<dark_gray>]'
console:
  checking-plugins: '<prefix> <red>Checking plugins...'
  disabling-server: '<prefix> <red>Disabling server because <yellow><plugins> <red>was not found or enabled successfully!'
  finished-checking: '<prefix> <green>Plugins successfully verified!'
  log-to-console: '<prefix> <red>Plugin <yellow><plugins> <red>not found or enabled successfully...'
command:
  action-list: '<prefix> <green>Actions available: <yellow><actions>'
  action-set: '<prefix> <green>Action set to <yellow><action>'
  action-type: '<prefix> <green>Action type: <yellow><action>'
  command-not-found: '<red>Use <yellow>/<command> help <red>to see the available commands'
  kick-message: '<prefix> <green>Kick message: <yellow><kick-message>'
  kick-message-set: '<prefix> <green>Kick message set to <yellow><kick-message>'
  no-permission-error: '<prefix> <red>You do not have permission to use this command'
  plugin-add-error: '<red>Usage: <yellow>/<command> add [plugin-name]'
  plugin-added: '<prefix> <green>Plugin <yellow><plugin> <green>added successfully!'
  plugin-already-added: '<prefix> <red>Plugin already added!'
  plugin-disabled: '<prefix> <red>Deactivating plugin features...'
  plugin-enabled: '<prefix> <green>Activating plugin features...'
  plugin-list: '<prefix> <green>Plugins added: <yellow><plugins>'
  plugin-list-separator: '<gray>, '
  plugin-list-separator-last: '<gray> and '
  plugin-list-empty: '<prefix> <red>No plugins added!'
  plugin-not-found: '<prefix> <red>Plugin <yellow><plugin> <red>not found in the list!'
  plugin-reload: '<prefix> <green>Config and Language reloaded!'
  plugin-remove-error: '<red>Usage: <yellow>/<command> remove <plugin-name>'
  plugin-removed: '<prefix> <green>Plugin <yellow><plugin> <green>removed!'
  plugin-click-remove: '<red>Click to remove the plugin'
  help:
    - '<gradient:aqua:green>==== Plugin Control Help ====</gradient>'
    - '<aqua>/<command> add <green><plugin-name> <yellow>- Add a plugin to the list'
    - '<aqua>/<command> remove <green><plugin-name> <yellow>- Remove a plugin from the list'
    - '<aqua>/<command> action <yellow>- List all actions available'
    - '<aqua>/<command> kick-message <yellow>- Show the kick message'
    - '<aqua>/<command> kick-message <message> <yellow>- Set the kick message'
    - '<aqua>/<command> enable <yellow>- Enable the plugin'
    - '<aqua>/<command> disable <yellow>- Disable the plugin'
    - '<aqua>/<command> toggle <yellow>- Enable or disable the plugin'
    - '<aqua>/<command> list <yellow>- List all plugins added'
    - '<aqua>/<command> reload <yellow>- Reload the config and language'
    - '<aqua>/<command> help <yellow>- Show this help'
    - '<gradient:aqua:green>============================</gradient>'
```

## Commands

Main Command `/plugincontrol` - Aliases: `/pc` and `/pcontrol`

| Command          | Sub Command              | Description                                  |
|------------------|--------------------------|----------------------------------------------|
| `/plugincontrol` | `add <plugin-name>`      | Add a plugin to the list.                    |
| `/plugincontrol` | `remove <plugin-name>`   | Remove a plugin from the list.               |
| `/plugincontrol` | `action`                 | Check the current action.                    |
| `/plugincontrol` | `action <action-type>`   | [Set the action to take.](#actions)          |
| `/plugincontrol` | `kick-message`           | Check the current kick message.              |
| `/plugincontrol` | `kick-message <message>` | Set the kick message.                        |
| `/plugincontrol` | `enable \| on`           | Enable PluginControl.                        |
| `/plugincontrol` | `disable \| off`         | Disable PluginControl.                       |
| `/plugincontrol` | `toggle`                 | Toggle PluginControl on or off.              |
| `/plugincontrol` | `list`                   | List all plugins in the list.                |
| `/plugincontrol` | `reload`                 | Reload the configuration and language files. |
| `/plugincontrol` | `help \| ?`              | Show the list of commands.                   |

Please note that the `<plugin-name>`, `<action-type>` and `<message>` placeholders should be replaced with the specific
plugin name and kick message, respectively, as required.

## Permissions

| Permissions            | Description                                    |
|------------------------|------------------------------------------------|
| `plugincontrol.use`    | Permission to use all [commands](#commands)    |
| `plugincontrol.bypass` | Bypass the `disallow-player-login` in `action` |
