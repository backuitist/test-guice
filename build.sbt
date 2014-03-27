name := "test-guice"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
    "com.novocode"              % "junit-interface"           % "0.9"      % "test",
    "junit"                     % "junit"                     % "4.10"     % "test",
    "org.scala-lang" 		% "scala-reflect" 	      % "2.10.1", 		// reflection api
    "commons-io"                % "commons-io"                % "2.4",			// commons shit		
    "org.apache.commons"        % "commons-lang3"             % "3.1",
    "com.google.inject"         % "guice"                     % "3.0",
    "com.google.inject.extensions" % "guice-multibindings"    % "3.0",
    //
    // scala helper -> see CodingWellScalaGuice
    "net.codingwell" %% "scala-guice" % "3.0.2"
)

