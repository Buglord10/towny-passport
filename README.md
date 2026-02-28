# TownyPassport

TownyPassport is a Paper/Spigot plugin that adds roleplay-style identity documents for Towny servers.

## Integrations

- **Vault** for charging issuance fees (optional; plugin can run in no-fee mode if no provider is present).
- **Towny** for validating towns/nations and enforcing border entry rules.
- Optional: **PlaceholderAPI** for in-book portrait placeholders.

## What it does

- Nations and towns can issue **passports** and **visas**.
- Players can apply for passports/visas and officials can approve applications.
- On approval, configured fee can be charged to the applicant and paid to the town/nation owner, with a confirmation message showing the amount.
- Only one pending application per player is allowed at a time.
- Town owners (mayors) can automatically receive an initial town passport on join/enable.
- Viewing documents opens a **written book** with identity info:
  - Optional portrait placeholders using free plugin setups.
  - Name, age, sex, authority, issue/expiry, notes.
- Border enforcement: players can only enter a town if they hold:
  - A valid passport issued by that **town**, or
  - A valid passport issued by that town's **nation**.
- Spawn restrictions: respawning inside a town is denied unless the player has valid town/nation passport access for that town.
- Entry scope rules:
  - A **nation passport** grants access to all towns in that nation.
  - A **town passport** grants access only to that one town.

## Commands

### Passport
Use the namespaced command to avoid command conflicts with other plugins:
- `/townypassport apply <town|nation> <authorityName> <age> <sex> [notes]`
- `/townypassport issue <player> <town|nation> <authorityName> <age> <sex> [notes]`
- `/townypassport applications <town|nation> <authorityName>`
- `/townypassport approve <applicationId>`
- `/townypassport view [player] [index]`
- `/townypassport list [player]`
- `/townypassport search <documentId>`
- `/townypassport renew <documentId> <days>`
- `/townypassport revoke <documentId>`
- `/townypassport settings <town|nation> <authorityName> <show|passport-fee|visa-fee|passport-days|visa-days> [value]`

Legacy aliases still work if not taken by another plugin: `/passport`, `/tpassport`.

### Visa
Use the namespaced command to avoid conflicts:
- `/townyvisa apply <town|nation> <authorityName> <age> <sex> [notes]`
- `/townyvisa issue <player> <town|nation> <authorityName> <age> <sex> [notes]`
- `/townyvisa approve <applicationId>`

Legacy aliases still work if free: `/visa`, `/tvisa`.

## Permissions

Authority management is owner-based:
- Town passport actions/settings require being that town's mayor/owner.
- Nation passport actions/settings require being that nation's leader/owner.

## Config highlights

```yml
fees:
  charge-on-approval: true

starter-passport-town-owner:
  enabled: true
```

## Build

```bash
mvn package
```

### Portrait inside the passport book (plugin integration)
Vanilla Minecraft books cannot embed bitmap images directly. To show a "picture" in-book, TownyPassport supports a configurable text/glyph line rendered at the top of the page.

Recommended setup:
1. Install **PlaceholderAPI**.
2. Use a free portrait-capable setup (for example SkinsRestorer + PlaceholderAPI placeholders, optionally with a free glyph resource-pack).
3. Configure `config.yml`:

```yml
book-portrait:
  enabled: true
  template: "%your_portrait_placeholder%"
```

If PlaceholderAPI is not installed, the template is used literally (with `{player}` replacement only).


## Troubleshooting Vault detection
If TownyPassport starts before your economy provider registers with Vault, it now listens for Bukkit service registration and automatically picks the provider up when it appears.

Make sure one supported economy plugin (EssentialsX Economy, CMI, etc.) is installed alongside Vault.


## Economy behavior
- If a Vault economy provider is present, configured fees are charged.
- If no provider is found and `economy.required: false` (default), plugin still runs and treats fees as no-cost.
- Set `economy.required: true` to force-disable startup without an economy provider.


## Spawn restrictions
Configure spawn checks in `config.yml`:

```yml
spawn-restrictions:
  enabled: true
  fallback-to-world-spawn: true
```

If enabled, respawns in Towny towns require a valid passport/visa for that town or its nation.
Towny spawn commands (`/t spawn`, `/town spawn`, `/n spawn`, `/nation spawn`) are also blocked when the player lacks access documents.


## Authority-controlled pricing and validity
Town and nation owners (or permitted staff) can set per-authority fees and validity periods using the settings command.
These settings override global defaults for that authority.
