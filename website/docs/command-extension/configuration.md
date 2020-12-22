---
title: Configuration
---

In order to use the extension, an entry must be added in the `config.json`. Here is a valid example:
```json
{
    "command": {
        "prefix": "!",
        "pagination_controls": {
            "next_emoji": {
                "unicode": "▶"
            },
            "previous_emoji": {
                "unicode": "◀"
            },
            "close_emoji": {
                "id": 12345678912345678,
                "name": "cross",
                "animated": true
            }
        },
        "menu_timeout_seconds": 1200
    }
}
```

JSON structure for `command`:

| Field | Type | Description | Required? |
|-------|------|-------------|-----------|
| prefix | string | the prefix to use for commands | Yes |
| pagination_controls | object | the emojis to use for pagination in [interactive menus](interactive-menus.md) | No, defaults to Unicode emojis ◀, ▶ and 🚫 |
| menu_timeout_seconds | integer | the number of seconds for interactive menus to close automatically if user is inactive | No, defaults to `600` (10 minutes) |

JSON structure for `pagination_controls`:

| Field | Type | Description | Required? |
|-------|------|-------------|-----------|
| next_emoji | object | the reaction emoji to use for going to next page in paginated interactive menus | No, defaults to Unicode emoji ▶ |
| previous_emoji | object | the reaction emoji to use for going to previous page in paginated interactive menus | No, defaults to Unicode emoji ◀ |
| close_emoji | object | the reaction emoji to use for closing interactive menus | No, defaults to Unicode emoji 🚫 |

JSON structure for `next_emoji`, `previous_emoji` and `close_emoji`:

| Field | Type | Description | Required? |
|-------|------|-------------|-----------|
| id | integer | the emoji ID | Only if it's a **custom emoji** |
| name | string | the emoji name | Only if it's a **custom emoji** |
| animated | boolean | whether the emoji is animated | No, defaults to `false` |
| unicode | string | the Unicode character for the emoji | Only if it's a **Unicode emoji** |
