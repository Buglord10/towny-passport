# TownyPassport

TownyPassport is a Paper/Spigot plugin that adds roleplay-style identity documents for Towny servers.

## Integrations

- **Vault** for charging issuance fees.
- **Towny** for validating towns/nations and enforcing border entry rules.

## What it does

- Nations and towns can issue **passports** and **visas**.
- Players can apply for passports/visas and officials can approve applications.
- Viewing documents opens a **written book** with identity info:
  - Optional portrait integration in books via PlaceholderAPI-compatible portrait placeholders using free plugin setups (for example SkinsRestorer placeholders or free glyph packs).
  - Name
  - Age (provided during application/issuance)
  - Sex
  - Issuing authority
  - Issue/expiry dates
  - Notes
- Border enforcement: players can only enter a town if they hold:
  - A valid passport issued by that **town**, or
  - A valid passport issued by that town's **nation**.
- Entry scope rules:
  - A **nation passport** grants access to all towns in that nation.
  - A **town passport** grants access only to that one town (not other towns in the nation).

## Commands

### Passport
- `/passport apply <town|nation> <authorityName> <age> <sex> [notes]`
- `/passport issue <player> <town|nation> <authorityName> <age> <sex> [notes]`
- `/passport applications <town|nation> <authorityName>`
- `/passport approve <applicationId>`
- `/passport view [player] [index]`

### Visa
- `/visa apply <town|nation> <authorityName> <age> <sex> [notes]`
- `/visa issue <player> <town|nation> <authorityName> <age> <sex> [notes]`
- `/visa approve <applicationId>`

## Permissions

- `townypassport.admin`
- `townypassport.issue.town.*`
- `townypassport.issue.nation.*`
- or per-authority dynamic nodes:
  - `townypassport.issue.town.<townname>`
  - `townypassport.issue.nation.<nationname>`

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
