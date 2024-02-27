
name := "dispatch-interview-coding-test"

version := "0.1"

scalaVersion := "3.3.1"

scalacOptions ++= Seq("-feature", "-language:_", "-deprecation", "-unchecked", "-Wvalue-discard")

libraryDependencies += "org.http4s"                    %% "http4s-core"            % "0.23.22"
libraryDependencies += "org.http4s"                    %% "http4s-dsl"             % "0.23.22"
libraryDependencies += "org.http4s"                    %% "http4s-ember-client"    % "0.23.22"
libraryDependencies += "org.http4s"                    %% "http4s-ember-server"    % "0.23.22"
libraryDependencies += "org.http4s"                    %% "http4s-circe"           % "0.23.22"
libraryDependencies += "io.circe"                      %% "circe-generic"          % "0.14.6"

libraryDependencies += "org.scalatest"                 %% "scalatest"              % "3.2.15"  % Test