# ExperienceBankII

[![Build Status](https://travis-ci.org/boy0001/ExperienceBankII.svg?branch=master)](https://travis-ci.org/boy0001/ExperienceBankII) [![codecov.io](https://codecov.io/github/bmhm/ExperienceBankII/coverage.svg?branch=master)](https://codecov.io/github/bmhm/ExperienceBankII?branch=master)

This is a minecraft / spigot / bukkit plugin, which will allow the player to save and
withdraw his XP to a bank, represented by a sign.

## Requirements
* Spigot 1.9.

## Backends
You can choose between yaml, sqlite and mysql. At the moment, only sqlite is being tested and known to work.

## Roadmap
* Re-implement MySQL support with a connection pool. That is not too hard, actually.
* Better language support (German tbd). English as fallback on missing translation.
* YAML storage backend.
* Commands to interact with the plugin (reload etc.).

## Contributing
* If you can translate the message strings you find in lang/english.yml, please create a pull request!
* If you add new code, please include a unit test!
* Database operations should be done in an async thread.

