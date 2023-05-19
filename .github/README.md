# Arena

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)  
[![discord-banner](https://img.shields.io/discord/706185253441634317?label=discord&style=for-the-badge&color=7289da)](https://discord.gg/pkFRvqB)

Arena is a demo server made with [Minestom](https://github.com/Minestom/Minestom), as both a showcase for what Minestom is 
capable of and as a way to teach developers how to work with Minestom.

The demo server is hosted at arena.minestom.net, or you can follow below guide to get it up and running yourself.

# Table of contents
- [Install](#install)
- [Goals](#goals)
- [Contributing](#contributing)

# Install
Arena can be installed in multiple ways. You can use a pre-built version or clone and build it yourself.
We recommend cloning since this allows you to experiment with Minestom.

## Cloning
You can also choose to clone this repository with:
```shell
git clone https://github.com/Minestom/Arena.git
```
And then build with:
```shell
./gradlew build
```
You will then find your jar in ./build/libs/ which you can run with:
```shell
java -jar arena-1.0-SNAPSHOT-all.jar
```
Of course, you can also use an IDE which makes this process easier.

## Pre-built
Download the latest release from [here](https://github.com/Minestom/Arena/releases/tag/latest)
and then run it with the following command:
```shell
java -jar arena-1.0-SNAPSHOT-all.jar
```

# Goals
The reason behind making Arena was to help new developers get into Minestom and familiarize themselves with the toolchain.
Besides, it's a good showcase of what Minestom can do.

# Contributing
See [the contributing file](CONTRIBUTING.md)!
