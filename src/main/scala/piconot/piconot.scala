import picolib.maze._
import picolib.semantics._
import picolib.display._

import piconot.external.PiconotParser

// This is mostly a copy and paste from the sample solution
// without the nice error message part
@main
def main(args: String*) = {

  if (args.length != 2) {
    println(usage)
    sys.exit()
  }

  // parse the maze file
  val mazeFileName = args(0)
  val maze = Maze(getFileLines(mazeFileName))

  // parse the program file
  val programFilename = args(1)
  val program = PiconotParser(getFileContents(programFilename))

  // process the results of parsing
  program match {
    // Error handling: syntax errors
    case e: PiconotParser.NoSuccess => println(e.toString)

    // If parsing succeeded, create the bot and run it
    case PiconotParser.Success(t, _) => {
      object bot extends Picobot(maze, program.get) with TextDisplay
      bot.run()
    }
  }

}

/** A string that describes how to use the program * */
def usage = "usage: sbt run <maze-file> <rules-file>"

/** Given a filename, get a list of the lines in the file */
def getFileLines(filename: String): List[String] =
  try {
    io.Source.fromFile(filename).getLines().toList
  } catch { // Error handling: non-existent file
    case e: java.io.FileNotFoundException => {
      println(e.getMessage()); sys.exit(1)
    }
  }

/** Given a filename, get the contents of the file */
def getFileContents(filename: String): String =
  try {
    io.Source.fromFile(filename).mkString
  } catch { // Error handling: non-existent file
    case e: java.io.FileNotFoundException => {
      println(e.getMessage()); sys.exit(1)
    }
  }
