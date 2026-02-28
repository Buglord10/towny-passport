# TownyPassport

TownyPassport is a Paper/Spigot plugin that adds roleplay-style identity documents for Towny servers.

## Integrations

- **Vault** for charging issuance fees.
- **Towny** for validating towns/nations and enforcing border entry rules.
- Optional: **PlaceholderAPI** for in-book portrait placeholders.

## What it does

- Nations and towns can issue **passports** and **visas**.
- Players can apply for passports/visas and officials can approve applications.
- Town owners (mayors) can automatically receive an initial town passport on join/enable.
- Viewing documents opens a **written book** with identity info:
  - Optional portrait placeholders using free plugin setups.
  - Name, age, sex, authority, issue/expiry, notes.
- Border enforcement: players can only enter a town if they hold:
  - A valid passport issued by that **town**, or
  - A valid passport issued by that town's **nation**.
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
- `/townypassport renew <documentId> <days>` (admin)
- `/townypassport revoke <documentId>` (admin)

Legacy aliases still work if not taken by another plugin: `/passport`, `/tpassport`.

### Visa
Use the namespaced command to avoid conflicts:
- `/townyvisa apply <town|nation> <authorityName> <age> <sex> [notes]`
- `/townyvisa issue <player> <town|nation> <authorityName> <age> <sex> [notes]`
- `/townyvisa approve <applicationId>`

Legacy aliases still work if free: `/visa`, `/tvisa`.

## Permissions

- `townypassport.admin`
- `townypassport.issue.town.*`
- `townypassport.issue.nation.*`
- or per-authority dynamic nodes:
  - `townypassport.issue.town.<townname>`
  - `townypassport.issue.nation.<nationname>`

## Config highlights

```yml
fees:
  charge-on-approval: false

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
