# JitStatic Client

This is a Java client for the [JitStatic API](https://github.com/hheg/jitstatic)

### API

There are two APIs since you have different rights to each endpoint. The POST API is a different login so it will have a specfic class holding that information.

The client is backed by Apache Http Client and you can use configure it through its and JitStatic Client's fluent API.

#### To create a key
```java
 JitStaticClient client = JitStaticClientBuilder.create().setAppContext("/app/")
 	.setHost("localhost").setPort(80).setUser("user").setPassword("pass").setScheme("http")
 	.setHttpClientBuilder(clientBuilderMock).build();
       
Entity entity = client.createKey(data, new CommitData("master", "key", "message", "user", "mail"),new MetaData(users, "application/test"), entityFactory);
```

#### To get a key
```java
JitStaticClient client = JitStaticClientBuilder.create().setAppContext("/app/")
	.setHost("localhost").setPort(80).setUser("user").setPassword("pass").setScheme("http")
	.setHttpClientBuilder(clientBuilderMock).build();
	
Enity entity = client.getKey("key", entityFactory);
```

#### To modify a key
```java
JitStaticClient client = JitStaticClientBuilder.create().setAppContext("/app/")
	.setHost("localhost").setPort(80).setUser("user").setPassword("pass").setScheme("http")
	.setHttpClientBuilder(clientBuilderMock).build();

Enity entity = client.getKey("key", entityFactory);
String version = entity.getTag();
String contentType = entity.getContentType();
Entity modifiedEntity = client.modifyKey(data, new CommitData("master", "key", "message", "user", "mail"), version, contentType, entityFactory);
```

#### To modify a metadatakey
```java
 JitStaticClient client = JitStaticClientBuilder.create().setAppContext("/app/")
 	.setHost("localhost").setPort(80).setUser("user").setPassword("pass").setScheme("http")
 	.setHttpClientBuilder(clientBuilderMock).build();
 	
Entity entity = client.getMetaKey("key", null, entityFactory);
User u = new User("user", "pass");
Set<User> users = new HashSet<>();
users.add(u);
String newVersion = client.modifyMetaKey("key", null, entity.getTag(), new ModifyUserKeyData(new MetaData(users, "application/test2"), "msg", "mail", "info"));
```
