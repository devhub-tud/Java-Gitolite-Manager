java-gitolite-manager
=====================

The java-gitolite-manager is a simple and compact Java library which enables developers to manage their gitolite configuration from Java. The aim is to give developers an easy to understand interface to manage repositories, groups and users in gitolite.

In order for java-gitolite-manager to work, you need to have a running installation of gitolite, either on a local or remote machine. Assuming you've installed gitolite on the host 'hostname', using the user 'git', you can use the following line to read the configuration of gitolite

```
ConfigManager manager = ConfigManager.create("git@hostname:gitolite-admin");
```

The ConfigManager is responsible for loading the configuration and applying any changes you make to the configuration. To load the configuration you can do the following:

```
Config config = manager.get();
```

Now let's say we want to create a new repository 'my-first-repo' for a new group called 'devs', whose sole member is a new user called 'newbie'. We can achieve this by doing:

```
Group group = config.createGroup("@devs");
User user = config.createUser("newbie");
group.add(user);

config.createRepository("my-first-repo")
	.setPermission(group, Permission.ALL);

```
But before we're ready to send our new configuration to gitolite, we must first define a public SSH key for the user 'newbie'. We can do that like this:

```
user.setKey("desktop", "ssh-rsa AAAB3Nz...");
```

Now our configuration is complete. We can use the following code to push it to gitolite, which will automatically create our new repository, group and user.

```
manager.apply();
```

Alternatively we can also perform this operation asynchronously by using the following code:

```
Future<Void> future = manager.applyAsync();
```
