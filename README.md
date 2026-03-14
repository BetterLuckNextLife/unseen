# Unseen

Unseen is a **dead-simple** Paper plugin that makes invisibility actually work on SMP servers.

Normally, when players drink an invisibility potion, they still show up in the tab list and their join/leave messages reveal them.
Unseen fixes that by hiding invisible players properly.

Works on Paper / Purpur (or any other fork) 1.21+

---

# Features

When a player has the invisibility potion effect:

* The `Player joined/left the game` message about them is suppressed
* The player is removed from the tab list

Admins can still see everyone normally.

---

# Permissions

| Permission         | Description                                     | Default |
| ------------------ | ----------------------------------------------- | ------- |
| `unseen.seehidden` | Allows seeing invisible players in the tab list | OP      |
| `unseen.reload`    | Allows using the `/unseen reload` command       | OP      |

---

# Commands

| Command          | Description                |
| ---------------- | -------------------------- |
| `/unseen reload` | Refreshes the plugin state |

This command simply reapplies the invisibility logic to all online players.

---

# Setup

1. Go to Releases
2. Download the desired `.jar`
3. Put the file in your server's `/plugins` folder
4. Start the server

Done.

---

License

[MIT](./LICENSE) License
