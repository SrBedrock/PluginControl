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
| `groups`       | List of groups to be enabled when the server starts.                           |

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

### Default lang.yml

```yaml
prefix: '<dark_gray>[<red>PluginControl<dark_gray>]'
console:
   checking-plugins: '<prefix> <red>Checking plugins...'
   disabling-server: '<prefix> <red>Disabling server because <yellow><plugins> <red>was not found or enabled!'
   finished-checking: '<prefix> <green>Plugins verified!'
   log-to-console: '<prefix> <red>Plugin <yellow><plugins> <red>not found or enabled...'
   log-to-console-group: '<yellow>from the group <red><group>'
command:
   action-list: '<prefix> <green>Actions available: <yellow><actions>'
   action-set: '<prefix> <green>Action set to <yellow><action>'
   action-type: '<prefix> <green>Action type: <yellow><action>'
   command-not-found: '<red>Usage: <yellow>/<command> help <red>to see the available commands'
   kick-message: '<prefix> <green>Kick message: <yellow><kick-message>'
   kick-message-set: '<prefix> <green>Kick message set to <yellow><kick-message>'
   plugin-add-error: '<red>Usage: <yellow>/<command> add [plugin-name]'
   plugin-added: '<prefix> <green>Plugin <yellow><plugin> <green>added!'
   plugin-added-all: '<prefix> <green>All plugins added!'
   plugin-already-added: '<prefix> <red>Plugin already added!'
   plugin-disabled: '<prefix> <red>Deactivating plugin features...'
   plugin-enabled: '<prefix> <green>Activating plugin features...'
   plugin-list: '<prefix> <green>Plugins added: <yellow><plugins>'
   plugin-list-separator: '<gray>, '
   plugin-list-separator-last: '<gray> and '
   plugin-list-empty: '<prefix> <red>No plugins added!'
   plugin-not-found: '<prefix> <red>Plugin <yellow><plugin> <red>not found in the list!'
   plugin-reload: '<prefix> <green>Config and Language reloaded!'
   plugin-remove-error: '<red>Usage: <yellow>/<command> remove <plugin>'
   plugin-removed: '<prefix> <green>Plugin <yellow><plugin> <green>removed!'
   plugin-removed-all: '<prefix> <green>All plugins removed!'
   plugin-click-remove: '<red>Click to remove the plugin'
   group-create-error: '<red>Usage: <yellow>/<command> group create <group>'
   group-created: '<prefix> <green>Group <yellow><group> <green>added!'
   group-already-exist: '<prefix> <red>Group already added!'
   group-remove-error: '<red>Usage: <yellow>/<command> group delete <group>'
   group-removed: '<prefix> <green>Group <yellow><group> <green>removed!'
   group-not-found: '<prefix> <red>Group <yellow><group> <red>not found!'
   group-list-empty: '<prefix> <red>No groups added!'
   group-list: '<prefix> <green>Groups added: <yellow><groups>'
   group-list-error: '<red>Usage: <yellow>/<command> group listplugins <group>'
   plugin-added-to-group: '<prefix> <green>Plugin <yellow><plugin> <green>added to group <yellow><group>'
   plugin-add-to-group-error: '<red>Usage: <yellow>/<command> group add <group> <plugin>'
   plugin-removed-from-group: '<prefix> <green>Plugin <yellow><plugin> <green>removed from group <yellow><group>'
   plugin-removed-from-group-error: '<red>Usage: <yellow>/<command> group remove <group> <plugin>'
   plugin-not-in-group: '<prefix> <red>Plugin <yellow><plugin> <red>not in group <yellow><group>'
   group-plugin-list-error: '<red>Usage: <yellow>/<command> group list <group>'
   group-has-no-plugins: '<prefix> <red>Group <yellow><group> <red>has no plugins!'
   group-plugin-list: '<prefix> <green>Plugins in group <yellow><group> <green>: <yellow><plugins>'
   group-list-name: '<yellow>Group <group>'
   group-click-remove-plugin: '<red>Click to remove the plugin from the group'
   group-click-info: '<yellow>Click to see the group info'
   group-help:
      - '<gradient:aqua:green>==== Plugin Control Group Help ====</gradient>'
      - '<aqua>/<command> group create <green><group> <yellow>- Create a group'
      - '<aqua>/<command> group delete <green><group> <yellow>- Remove a group'
      - '<aqua>/<command> group list <yellow>- List all groups'
      - '<aqua>/<command> group add <green><group> <plugin> <yellow>- Add a plugin into a group'
      - '<aqua>/<command> group remove <green><group> <plugin> <yellow>- Remove a plugin from a group'
      - '<aqua>/<command> group list <green><group> <yellow>- List all plugins in a group'
      - '<aqua>/<command> group help <yellow>- Show the group help'
      - '<gradient:aqua:green>================================</gradient>'
   help:
      - '<gradient:aqua:green>==== Plugin Control Help ====</gradient>'
      - '<aqua>/<command> add <green><plugin> <yellow>- Add a plugin to the list'
      - '<aqua>/<command> remove <green><plugin> <yellow>- Remove a plugin from the list'
      - '<aqua>/<command> action <yellow>- List all actions available'
      - '<aqua>/<command> action <green><action-type> <yellow>- Set an action type'
      - '<aqua>/<command> group create <green><group> <yellow>- Create a group'
      - '<aqua>/<command> group delete <green><group> <yellow>- Remove a group'
      - '<aqua>/<command> group list <yellow>- List all groups'
      - '<aqua>/<command> group add <green><group> <plugin> <yellow>- Add a plugin into a group'
      - '<aqua>/<command> group remove <green><group> <plugin> <yellow>- Remove a plugin from a group'
      - '<aqua>/<command> group list <green><group> <yellow>- List all plugins in a group'
      - '<aqua>/<command> group help <yellow>- Show the group help'
      - '<aqua>/<command> kick-message <yellow>- Show the kick message'
      - '<aqua>/<command> kick-message <green><message> <yellow>- Set the kick message'
      - '<aqua>/<command> enable <yellow>- Enable the plugin'
      - '<aqua>/<command> disable <yellow>- Disable the plugin'
      - '<aqua>/<command> toggle <yellow>- Enable or disable the plugin'
      - '<aqua>/<command> list <yellow>- List all plugins added'
      - '<aqua>/<command> reload <yellow>- Reload the config and language'
      - '<aqua>/<command> help <yellow>- Show this help'
      - '<gradient:aqua:green>=========================</gradient>'
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
