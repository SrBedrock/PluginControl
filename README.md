# PluginsControl

## Description

This plugin allows you to control which plugins have to be enabled for the server continues running.

Inspired by [this conversation](https://github.com/PaperMC/Paper/pull/8108#issuecomment-1419304955) and myself
having the same problem.

## Installation

1. Download the plugin from [here](https://github.com/SrBedrock/PluginControl/releases/).
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

Main command aliases: `/pc` and `/pcontrol`.

| Command           | Sub Command       | Description                      |
|-------------------|-------------------|----------------------------------|
| `/pluginscontrol` | `add <plugin>`    | Add a plugin to the list.        |
| `/pluginscontrol` | `remove <plugin>` | Remove a plugin from the list.   |
| `/pluginscontrol` | `enable \| on`    | Enable PluginControl.            |
| `/pluginscontrol` | `disable \| off`  | Disable PluginControl.           |
| `/pluginscontrol` | `toggle`          | Enable or disable PluginControl. |
| `/pluginscontrol` | `list`            | List all plugins in the list.    |

## Permissions

| Permissions          | Description                                 |
|----------------------|---------------------------------------------|
| `pluginscontrol.use` | Permission to use all [commands](#commands) |