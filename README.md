# TGCore
Core for TrustGames.net

###### Configs:
- Player activity menu materials

###### Features:
- Chat Decoration manager
- GUI Manager with Pagination
- ItemBuilder with Skulls support
- Skin Manager with fetching
- Command Cooldown manager
- Cooldown manager
- File creation manager
- LuckPerms manager
- Player Data saving (kills/deaths/playtime/...)
- Player display name
- Gamerules
- Tablist Teams
- Color conversion Util
- Component conversion Util
- Placeholder conversion Util (MiniPlaceholders)

###### Commands:
- /activity id <id> 
- /activity player <name>

###### How to get Toolkit:
TG-Toolkit is self-hosted on a server. To be able to reach that server you need to set the server up in your maven settings.xml. Insert the following lines in the server section

**_settings.xml_**
```
<servers>
    <server>
      <id>trustgames-repo</id>
      <username>{username}</username>
      <password>{secret}</password>
    </server>
  </servers>
```
