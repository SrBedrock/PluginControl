# PluginsControl

## Description

This plugin allows you to control which plugins have to be enabled for the server continues running.

## Installation

1. Download the plugin from [here](<toggle|on|off|list>
2. Put the plugin in the `plugins` folder of your server.
3. Restart the server.
4. Add plugins to the list with `/pluginscontrol add <plugin>`.
5. Enable the plugin with `/pluginscontrol enable`.

## Configuration

| Option    | Description                                           |
|-----------|-------------------------------------------------------|
| `enabled` | Whether the plugin is enabled or not.                 |
| `plugins` | List of plugins to be enabled when the server starts. |

## Commands

| Command                        | Sub Command       | Description                      |
|--------------------------------|-------------------|----------------------------------|
| `/pluginscontrol \| plcontrol` | `add <plugin>`    | Add a plugin to the list.        |
| `/pluginscontrol \| plcontrol` | `remove <plugin>` | Remove a plugin from the list.   |
| `/pluginscontrol \| plcontrol` | `enable \| on`    | Enable PluginControl.            |
| `/pluginscontrol \| plcontrol` | `disable \| off`  | Disable PluginControl.           |
| `/pluginscontrol \| plcontrol` | `toggle`          | Enable or disable PluginControl. |
| `/pluginscontrol \| plcontrol` | `list`            | List all plugins in the list.    |

## Permissions

| Permissions          | Description                        |
|----------------------|------------------------------------|
| `pluginscontrol.use` | Permission to use all [[commands]] |