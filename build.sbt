
name := "dispatch-interview-coding-test"

version := "0.1"

scalaVersion := "2.13.10"

libraryDependencies += "org.typelevel"                 %% "cats-effect"            % "3.4.1"
libraryDependencies += "org.http4s"                    %% "http4s-core"            % "0.23.16"
libraryDependencies += "org.http4s"                    %% "http4s-dsl"             % "0.23.16"
libraryDependencies += "org.http4s"                    %% "http4s-ember-client"    % "0.23.16"
libraryDependencies += "org.http4s"                    %% "http4s-ember-server"    % "0.23.16"
libraryDependencies += "org.http4s"                    %% "http4s-circe"           % "0.23.16"
libraryDependencies += "io.circe"                      %% "circe-generic"          % "0.14.3"

libraryDependencies += "org.scalatest"                 %% "scalatest"              % "3.2.14"  % Test