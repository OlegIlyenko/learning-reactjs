resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)


addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

//addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")
addSbtPlugin("default" % "sbt-coffeescript-reactjs" % "1.0.1-SNAPSHOT")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.4.0")
addSbtPlugin("net.litola" % "play-sass" % "0.4.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.8")