# TGCore
Core for TrustGames.net

###### Configs:
- Player activity menu materials

###### Features:
- Chat Decoration manager
- GUI Manager with Pagination and PlayerInventory
- ItemBuilder with Skulls support (converts default italic text)
- Command Cooldown manager
- Cooldown manager
- LuckPerms manager
- Player Data saving (kills/deaths/playtime/...)
- Player display name
- Gamerules
- Hunger disable
- Tablist Teams
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
