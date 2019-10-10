
// Dependencies
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.3")

// Code Formatter
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

// Linter and code smells
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

// Assembly
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

// Git helper
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("com.dwijnand" % "sbt-travisci" % "1.1.3")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

// Releases

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.5")

addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")