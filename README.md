# TerminatorPlus [v3.0-BETA]

TerminatorPlus is a Spigot plugin that allows the creation of server-side player bots. Unlike many NPC plugins that already exist, this project has an emphasis on making the bots as human-like as possible.

### Download

Releases are currently available on our Discord server, which can be found [here](https://discord.gg/horsenuggets).

### Machine Learning

TerminatorPlus currently utilizes classic population-based reinforcement learning for bot PVP training. Q-learning is a work in progress, along with variable A* pathfinding.

### API Support

The jar artifact can be used as a dependency for your own plugins, however stronger support will come in the future. Below is an exmaple of a simple bot creation method provided.

```java
Location loc = player.getLocation();
Bot bot = Bot.createBot(loc, "Dream");
```

### Version Support

This plugin requires [Spigot 1.16.5](https://www.spigotmc.org/wiki/buildtools/#1-16-5). NMS 1.17 kind of screwed up a lot of the EntityPlayer variable names (obfuscation yay!!!) so I don't really wanna have to deal with that yet.

### Future Updates

This project is in a very early stage, and we have many more ideas to tackle.
- [ ] Individual agents assigned per bot
- [ ] A GUI to view currently loaded bots and cool data with them
- [ ] AI data saved to the plugin data folder, able to be loaded into bots
- [ ] Saving config data in memory

## License

This project is licensed under [Eclipse Public License](https://github.com/batchprogrammer314/player-ai/blob/master/LICENSE).
